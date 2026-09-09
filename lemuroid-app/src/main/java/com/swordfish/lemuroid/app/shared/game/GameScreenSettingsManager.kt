package com.swordfish.lemuroid.app.shared.game

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GameScreenSettings(
    val scale: Float = 1.0f,
    val verticalOffsetDp: Int = 0,
    val stretch: Boolean = false,
)

object GameScreenSettingsManager {
    private const val PREFS_NAME = "game_screen_settings_prefs"
    private const val KEY_SCALE = "screen_scale"
    private const val KEY_OFFSET_DP = "screen_vertical_offset_dp"
    private const val KEY_STRETCH = "screen_stretch"

    private val _screenSettingsFlow = MutableStateFlow(GameScreenSettings())
    val screenSettingsFlow: StateFlow<GameScreenSettings> = _screenSettingsFlow.asStateFlow()

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val scale = prefs.getFloat(KEY_SCALE, 1.0f).coerceIn(0.70f, 1.15f)
            val offsetDp = prefs.getInt(KEY_OFFSET_DP, 0).coerceIn(-60, 60)
            val stretch = prefs.getBoolean(KEY_STRETCH, false)
            _screenSettingsFlow.value = GameScreenSettings(scale, offsetDp, stretch)
            initialized = true
        }
    }

    fun updateScale(context: Context, scale: Float) {
        init(context)
        val clamped = scale.coerceIn(0.70f, 1.15f)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_SCALE, clamped)
            .apply()
        _screenSettingsFlow.value = _screenSettingsFlow.value.copy(scale = clamped)
    }

    fun updateVerticalOffset(context: Context, offsetDp: Int) {
        init(context)
        val clamped = offsetDp.coerceIn(-60, 60)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_OFFSET_DP, clamped)
            .apply()
        _screenSettingsFlow.value = _screenSettingsFlow.value.copy(verticalOffsetDp = clamped)
    }

    fun updateStretch(context: Context, stretch: Boolean) {
        init(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_STRETCH, stretch)
            .apply()
        _screenSettingsFlow.value = _screenSettingsFlow.value.copy(stretch = stretch)
    }

    fun reset(context: Context) {
        init(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_SCALE, 1.0f)
            .putInt(KEY_OFFSET_DP, 0)
            .putBoolean(KEY_STRETCH, false)
            .apply()
        _screenSettingsFlow.value = GameScreenSettings(1.0f, 0, false)
    }
}
