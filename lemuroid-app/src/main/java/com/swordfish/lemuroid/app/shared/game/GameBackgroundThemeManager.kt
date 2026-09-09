package com.swordfish.lemuroid.app.shared.game

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

enum class BackgroundThemeMode {
    FULLSCREEN,
    SPLIT,
    NONE,
}

enum class BackgroundSlot {
    FULLSCREEN,
    TOP,
    BOTTOM,
}

object GameBackgroundThemeManager {
    private const val PREFS_NAME = "game_background_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_TIMESTAMP = "background_timestamp"

    private const val FILE_FULLSCREEN = "custom_bg_fullscreen.img"
    private const val FILE_TOP = "custom_bg_top.img"
    private const val FILE_BOTTOM = "custom_bg_bottom.img"
    private const val FILE_LEGACY = "custom_game_background.img"

    private val _themeModeFlow = MutableStateFlow(BackgroundThemeMode.NONE)
    val themeModeFlow: StateFlow<BackgroundThemeMode> = _themeModeFlow.asStateFlow()

    private val _backgroundUpdateFlow = MutableStateFlow(0L)
    val backgroundUpdateFlow: StateFlow<Long> = _backgroundUpdateFlow.asStateFlow()

    private val _hasCustomBackgroundFlow = MutableStateFlow(false)
    val hasCustomBackgroundFlow: StateFlow<Boolean> = _hasCustomBackgroundFlow.asStateFlow()

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // Migrate legacy background file if present
            val legacyFile = File(context.filesDir, FILE_LEGACY)
            val fullscreenFile = File(context.filesDir, FILE_FULLSCREEN)
            if (legacyFile.exists() && legacyFile.length() > 0 && (!fullscreenFile.exists() || fullscreenFile.length() == 0L)) {
                legacyFile.copyTo(fullscreenFile, overwrite = true)
            }

            val hasFullscreen = hasBackground(context, BackgroundSlot.FULLSCREEN)
            val hasTop = hasBackground(context, BackgroundSlot.TOP)
            val hasBottom = hasBackground(context, BackgroundSlot.BOTTOM)

            val rawMode = prefs.getString(KEY_THEME_MODE, null)
            val resolvedMode = when {
                rawMode != null -> try {
                    BackgroundThemeMode.valueOf(rawMode)
                } catch (e: Exception) {
                    BackgroundThemeMode.FULLSCREEN
                }
                hasTop || hasBottom -> BackgroundThemeMode.SPLIT
                hasFullscreen -> BackgroundThemeMode.FULLSCREEN
                else -> BackgroundThemeMode.NONE
            }

            _themeModeFlow.value = resolvedMode
            _hasCustomBackgroundFlow.value = hasFullscreen || hasTop || hasBottom
            _backgroundUpdateFlow.value = prefs.getLong(KEY_TIMESTAMP, System.currentTimeMillis())
            initialized = true
        }
    }

    private fun getFileName(slot: BackgroundSlot): String {
        return when (slot) {
            BackgroundSlot.FULLSCREEN -> FILE_FULLSCREEN
            BackgroundSlot.TOP -> FILE_TOP
            BackgroundSlot.BOTTOM -> FILE_BOTTOM
        }
    }

    fun getBackgroundFile(context: Context, slot: BackgroundSlot): File? {
        val fileName = getFileName(slot)
        val file = File(context.filesDir, fileName)
        if (file.exists() && file.length() > 0) return file

        // Fallback for legacy fullscreen file
        if (slot == BackgroundSlot.FULLSCREEN) {
            val legacy = File(context.filesDir, FILE_LEGACY)
            if (legacy.exists() && legacy.length() > 0) return legacy
        }
        return null
    }

    fun getBackgroundFile(context: Context): File? = getBackgroundFile(context, BackgroundSlot.FULLSCREEN)

    fun hasBackground(context: Context, slot: BackgroundSlot): Boolean {
        return getBackgroundFile(context, slot) != null
    }

    fun hasCustomBackground(context: Context): Boolean {
        init(context)
        return _hasCustomBackgroundFlow.value
    }

    fun getThemeMode(context: Context): BackgroundThemeMode {
        init(context)
        return _themeModeFlow.value
    }

    fun setThemeMode(context: Context, mode: BackgroundThemeMode) {
        init(context)
        val newTimestamp = System.currentTimeMillis()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.name)
            .putLong(KEY_TIMESTAMP, newTimestamp)
            .apply()

        _themeModeFlow.value = mode
        _hasCustomBackgroundFlow.value = hasBackground(context, BackgroundSlot.FULLSCREEN) ||
            hasBackground(context, BackgroundSlot.TOP) ||
            hasBackground(context, BackgroundSlot.BOTTOM)
        _backgroundUpdateFlow.value = newTimestamp
    }

    suspend fun saveBackground(
        context: Context,
        uri: Uri,
        slot: BackgroundSlot = BackgroundSlot.FULLSCREEN,
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(context.cacheDir, "temp_bg_${slot.name}_${System.currentTimeMillis()}")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext false

            // Verify it is a valid decodable image
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(tempFile.absolutePath, options)
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                tempFile.delete()
                Timber.w("Selected file is not a valid decodable image")
                return@withContext false
            }

            // Copy to internal files directory
            val destFile = File(context.filesDir, getFileName(slot))
            tempFile.copyTo(destFile, overwrite = true)
            tempFile.delete()

            val newTimestamp = System.currentTimeMillis()
            val targetMode = if (slot == BackgroundSlot.FULLSCREEN) {
                BackgroundThemeMode.FULLSCREEN
            } else {
                BackgroundThemeMode.SPLIT
            }

            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME_MODE, targetMode.name)
                .putLong(KEY_TIMESTAMP, newTimestamp)
                .apply()

            _themeModeFlow.value = targetMode
            _hasCustomBackgroundFlow.value = true
            _backgroundUpdateFlow.value = newTimestamp
            true
        } catch (e: Exception) {
            Timber.e(e, "Error saving custom background theme for slot $slot")
            false
        }
    }

    suspend fun resetBackground(
        context: Context,
        slot: BackgroundSlot,
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, getFileName(slot))
            if (file.exists()) {
                file.delete()
            }
            if (slot == BackgroundSlot.FULLSCREEN) {
                val legacy = File(context.filesDir, FILE_LEGACY)
                if (legacy.exists()) legacy.delete()
            }

            val hasFullscreen = hasBackground(context, BackgroundSlot.FULLSCREEN)
            val hasTop = hasBackground(context, BackgroundSlot.TOP)
            val hasBottom = hasBackground(context, BackgroundSlot.BOTTOM)

            val newMode = when (_themeModeFlow.value) {
                BackgroundThemeMode.FULLSCREEN -> if (hasFullscreen) BackgroundThemeMode.FULLSCREEN else BackgroundThemeMode.NONE
                BackgroundThemeMode.SPLIT -> if (hasTop || hasBottom) BackgroundThemeMode.SPLIT else BackgroundThemeMode.NONE
                BackgroundThemeMode.NONE -> BackgroundThemeMode.NONE
            }

            val newTimestamp = System.currentTimeMillis()
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME_MODE, newMode.name)
                .putLong(KEY_TIMESTAMP, newTimestamp)
                .apply()

            _themeModeFlow.value = newMode
            _hasCustomBackgroundFlow.value = hasFullscreen || hasTop || hasBottom
            _backgroundUpdateFlow.value = newTimestamp
            true
        } catch (e: Exception) {
            Timber.e(e, "Error resetting custom background theme for slot $slot")
            false
        }
    }

    suspend fun resetAll(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            listOf(FILE_FULLSCREEN, FILE_TOP, FILE_BOTTOM, FILE_LEGACY).forEach { fileName ->
                val file = File(context.filesDir, fileName)
                if (file.exists()) file.delete()
            }

            val newTimestamp = System.currentTimeMillis()
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME_MODE, BackgroundThemeMode.NONE.name)
                .putLong(KEY_TIMESTAMP, newTimestamp)
                .apply()

            _themeModeFlow.value = BackgroundThemeMode.NONE
            _hasCustomBackgroundFlow.value = false
            _backgroundUpdateFlow.value = newTimestamp
            true
        } catch (e: Exception) {
            Timber.e(e, "Error resetting all custom background themes")
            false
        }
    }
}
