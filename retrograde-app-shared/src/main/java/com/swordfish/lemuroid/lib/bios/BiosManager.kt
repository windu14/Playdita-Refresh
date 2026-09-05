package com.swordfish.lemuroid.lib.bios

import com.swordfish.lemuroid.common.files.safeDelete
import com.swordfish.lemuroid.common.kotlin.associateByNotNull
import com.swordfish.lemuroid.common.kotlin.writeToFile
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.storage.StorageFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.InputStream

class BiosManager(private val directoriesManager: DirectoriesManager) {
    private val crcLookup = SUPPORTED_BIOS.associateByNotNull { it.externalCRC32 }
    private val nameLookup = SUPPORTED_BIOS.associateByNotNull { it.externalName }

    fun getMissingBiosFiles(
        coreConfig: SystemCoreConfig,
        game: Game,
    ): List<String> {
        val regionalBiosFiles = coreConfig.regionalBIOSFiles

        val gameLabels =
            Regex("\\([A-Za-z]+\\)")
                .findAll(game.title)
                .map { it.value.drop(1).dropLast(1) }
                .filter { it.isNotBlank() }
                .toSet()

        Timber.d("Found game labels: $gameLabels")

        val requiredRegionalFiles =
            gameLabels.intersect(regionalBiosFiles.keys)
                .ifEmpty { regionalBiosFiles.keys }
                .mapNotNull { regionalBiosFiles[it] }

        Timber.d("Required regional files for game: $requiredRegionalFiles")

        return (coreConfig.requiredBIOSFiles + requiredRegionalFiles)
            .filter { !File(directoriesManager.getSystemDirectory(), it).exists() }
    }

    fun deleteBiosBefore(timestampMs: Long) {
        Timber.i("Pruning old bios files")
        SUPPORTED_BIOS
            .map { File(directoriesManager.getSystemDirectory(), it.libretroFileName) }
            .filter { it.lastModified() < normalizeTimestamp(timestampMs) }
            .forEach {
                Timber.d("Pruning old bios file: ${it.path}")
                it.safeDelete()
            }
    }

    @Deprecated("Use the suspend variant")
    fun getBiosInfo(): BiosInfo {
        syncBiosFromAssets()
        val bios =
            SUPPORTED_BIOS.groupBy {
                val file = File(directoriesManager.getSystemDirectory(), it.libretroFileName)
                file.exists() && file.length() > 0L
            }.withDefault { listOf() }

        return BiosInfo(bios.getValue(true), bios.getValue(false))
    }

    fun syncBiosFromAssets() {
        val systemDir = directoriesManager.getSystemDirectory()
        systemDir.mkdirs()

        val candidates = listOf(
            "gba_bios.bin" to "gba_bios.bin",
            "bios/gba_bios.bin" to "gba_bios.bin",
            "GBA_BIOS.BIN" to "gba_bios.bin",
            "bios/GBA_BIOS.BIN" to "gba_bios.bin",
            "gba.bin" to "gba_bios.bin",
            "bios/gba.bin" to "gba_bios.bin",
            "gbabios.bin" to "gba_bios.bin",
            "bios/gbabios.bin" to "gba_bios.bin",
        )

        for ((assetPath, targetName) in candidates) {
            val targetFile = File(systemDir, targetName)
            if (!targetFile.exists() || targetFile.length() == 0L) {
                runCatching {
                    directoriesManager.appContext.assets.open(assetPath).use { input ->
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (targetFile.exists() && targetFile.length() > 0L) {
                        Timber.i("Copied BIOS from asset $assetPath to ${targetFile.path}")
                    }
                }
            }
        }
    }

    suspend fun getBiosInfoAsync(): BiosInfo =
        withContext(Dispatchers.IO) {
            getBiosInfo()
        }

    fun tryAddBiosAfter(
        storageFile: StorageFile,
        inputStream: InputStream,
        timestampMs: Long,
    ): Boolean {
        val bios = findByCRC(storageFile) ?: findByName(storageFile) ?: return false

        Timber.i("Importing bios file: $bios")

        val biosFile = File(directoriesManager.getSystemDirectory(), bios.libretroFileName)
        if (biosFile.exists() && biosFile.setLastModified(normalizeTimestamp(timestampMs))) {
            Timber.d("Bios file already present. Updated last modification date.")
        } else {
            Timber.d("Bios file not available. Copying new file.")
            inputStream.writeToFile(biosFile)
        }
        return true
    }

    private fun findByCRC(storageFile: StorageFile): Bios? {
        val crc = storageFile.crc ?: return null
        return crcLookup[crc] ?: crcLookup[crc.uppercase()] ?: crcLookup[crc.lowercase()]
    }

    private fun findByName(storageFile: StorageFile): Bios? {
        val direct = nameLookup[storageFile.name] ?: nameLookup[storageFile.name.lowercase()]
        if (direct != null) return direct

        val lower = storageFile.name.lowercase()
        if (lower in listOf("gba_bios.bin", "gbabios.bin", "gba.bios", "bios_gba.bin", "normatt_bios.bin", "gba_bios.rom")) {
            return SUPPORTED_BIOS.firstOrNull { it.systemID == SystemID.GBA }
        }
        return null
    }

    private fun normalizeTimestamp(timestamp: Long) = (timestamp / 1000) * 1000

    data class BiosInfo(val detected: List<Bios>, val notDetected: List<Bios>)

    companion object {
        private val SUPPORTED_BIOS =
            listOf(
                Bios(
                    "gba_bios.bin",
                    "A860E8C0B6D573D191E4EC7DB1B1E4F6",
                    "Game Boy Advance BIOS",
                    SystemID.GBA,
                    "81977335",
                    "gba_bios.bin",
                ),
            )
    }
}
