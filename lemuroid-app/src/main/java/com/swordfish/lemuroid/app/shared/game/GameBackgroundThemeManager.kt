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

object GameBackgroundThemeManager {
    private const val FILE_NAME = "custom_game_background.img"
    private const val PREFS_NAME = "game_background_theme_prefs"
    private const val KEY_TIMESTAMP = "background_timestamp"
    private const val KEY_HAS_CUSTOM = "has_custom_background"

    private val _backgroundUpdateFlow = MutableStateFlow(0L)
    val backgroundUpdateFlow: StateFlow<Long> = _backgroundUpdateFlow.asStateFlow()

    private val _hasCustomBackgroundFlow = MutableStateFlow(false)
    val hasCustomBackgroundFlow: StateFlow<Boolean> = _hasCustomBackgroundFlow.asStateFlow()

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val file = getBackgroundFile(context)
            val hasCustom = prefs.getBoolean(KEY_HAS_CUSTOM, false) && file != null
            val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
            _hasCustomBackgroundFlow.value = hasCustom
            _backgroundUpdateFlow.value = if (hasCustom) timestamp else 0L
            initialized = true
        }
    }

    fun getBackgroundFile(context: Context): File? {
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists() && file.length() > 0) file else null
    }

    fun hasCustomBackground(context: Context): Boolean {
        init(context)
        return _hasCustomBackgroundFlow.value
    }

    suspend fun saveBackground(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(context.cacheDir, "temp_bg_${System.currentTimeMillis()}")
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
            val destFile = File(context.filesDir, FILE_NAME)
            tempFile.copyTo(destFile, overwrite = true)
            tempFile.delete()

            val newTimestamp = System.currentTimeMillis()
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_HAS_CUSTOM, true)
                .putLong(KEY_TIMESTAMP, newTimestamp)
                .apply()

            _hasCustomBackgroundFlow.value = true
            _backgroundUpdateFlow.value = newTimestamp
            true
        } catch (e: Exception) {
            Timber.e(e, "Error saving custom background theme")
            false
        }
    }

    suspend fun resetBackground(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
            val newTimestamp = System.currentTimeMillis()
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_HAS_CUSTOM, false)
                .putLong(KEY_TIMESTAMP, newTimestamp)
                .apply()

            _hasCustomBackgroundFlow.value = false
            _backgroundUpdateFlow.value = newTimestamp
            true
        } catch (e: Exception) {
            Timber.e(e, "Error resetting custom background theme")
            false
        }
    }
}
