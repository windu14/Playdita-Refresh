package com.swordfish.lemuroid.app.shared.cheat

import android.content.Context
import android.content.SharedPreferences
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.LibretroDroid
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.UUID

class CheatManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lemuroid_gba_cheats", Context.MODE_PRIVATE)

    private fun getStorageKey(game: Game): String {
        return "cheats_${game.id}_${game.fileName.hashCode()}"
    }

    fun getCheats(game: Game): List<GbaCheat> {
        val jsonString = prefs.getString(getStorageKey(game), null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<GbaCheat>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    GbaCheat(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.getString("title"),
                        code = obj.getString("code"),
                        enabled = obj.optBoolean("enabled", true),
                    ),
                )
            }
            list
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse cheats for game: ${game.title}")
            emptyList()
        }
    }

    fun saveCheats(game: Game, cheats: List<GbaCheat>) {
        val jsonArray = JSONArray()
        cheats.forEach { cheat ->
            val obj = JSONObject().apply {
                put("id", cheat.id)
                put("title", cheat.title)
                put("code", cheat.code)
                put("enabled", cheat.enabled)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(getStorageKey(game), jsonArray.toString()).apply()
    }

    fun addCheat(game: Game, title: String, code: String): GbaCheat {
        val current = getCheats(game).toMutableList()
        val newCheat = GbaCheat(title = title, code = code, enabled = true)
        current.add(newCheat)
        saveCheats(game, current)
        return newCheat
    }

    fun updateCheat(game: Game, updated: GbaCheat) {
        val current = getCheats(game).toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            current[index] = updated
            saveCheats(game, current)
        }
    }

    fun toggleCheat(game: Game, cheatId: String, isEnabled: Boolean) {
        val current = getCheats(game).toMutableList()
        val index = current.indexOfFirst { it.id == cheatId }
        if (index != -1) {
            current[index] = current[index].copy(enabled = isEnabled)
            saveCheats(game, current)
        }
    }

    fun deleteCheat(game: Game, cheatId: String) {
        val current = getCheats(game).toMutableList()
        current.removeAll { it.id == cheatId }
        saveCheats(game, current)
    }

    /**
     * Applies all stored cheats to the running GLRetroView emulator core.
     * Safely executes on the emulation GLThread via queueEvent to ensure
     * synchronization with libretro core lifecycle and frame execution.
     */
    fun applyCheatsToEmulator(game: Game, retroView: GLRetroView?) {
        if (retroView == null) {
            Timber.w("Cannot apply cheats: retroView is null")
            return
        }

        val cheats = getCheats(game)
        val enabledCheats = cheats.filter { it.enabled }
        Timber.i("Applying cheats to emulator for game '${game.title}': total=${cheats.size}, enabled=${enabledCheats.size}")

        // Critical: Native calls to resetCheat and setCheat MUST run on GLSurfaceView's GLThread.
        // Running on the main UI thread causes race conditions or crashes with native core.
        // In addition, mGBA's retro_cheat_set ignores the 'enabled' parameter, so:
        // 1. We must call LibretroDroid.resetCheat() to clear any previous cheat list.
        // 2. We only send cheats where enabled == true.
        retroView.queueEvent {
            try {
                Timber.d("Resetting cheats on emulation thread...")
                LibretroDroid.resetCheat()

                var slotIndex = 0
                enabledCheats.forEach { cheat ->
                    val lines = cheat.normalizedLines
                    if (lines.isNotEmpty()) {
                        // mGBA supports multiline cheats joined with '+'
                        val joinedCode = lines.joinToString("+")
                        Timber.d("Setting cheat slot #$slotIndex [${cheat.title}]: $joinedCode")
                        LibretroDroid.setCheat(slotIndex++, true, joinedCode)
                    }
                }
                Timber.i("Successfully applied $slotIndex cheat(s) to emulator core")
            } catch (t: Throwable) {
                Timber.e(t, "Error applying cheats on emulation thread")
            }
        }
    }
}
