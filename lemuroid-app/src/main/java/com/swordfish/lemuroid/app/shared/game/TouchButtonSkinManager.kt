package com.swordfish.lemuroid.app.shared.game

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.swordfish.lemuroid.R
import com.swordfish.touchinput.radial.LemuroidPadTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TouchButtonSkin(val id: String, val titleResId: Int) {
    CLASSIC("classic", R.string.touch_skin_classic),
    DARK("dark", R.string.touch_skin_dark),
    PINK("pink", R.string.touch_skin_pink),
    CYAN("cyan", R.string.touch_skin_cyan),
    EMERALD("emerald", R.string.touch_skin_emerald),
    AMBER("amber", R.string.touch_skin_amber),
    VIOLET("violet", R.string.touch_skin_violet),
    CUSTOM("custom", R.string.touch_skin_custom);

    fun toTheme(customColor: Long = 0xFF00E5FF): LemuroidPadTheme = when (this) {
        CLASSIC -> LemuroidPadTheme.CLASSIC
        DARK -> LemuroidPadTheme.DARK
        PINK -> LemuroidPadTheme.PINK
        CYAN -> LemuroidPadTheme.CYAN
        EMERALD -> LemuroidPadTheme.EMERALD
        AMBER -> LemuroidPadTheme.AMBER
        VIOLET -> LemuroidPadTheme.VIOLET
        CUSTOM -> LemuroidPadTheme.createColorTheme(Color(customColor))
    }

    companion object {
        fun fromId(id: String): TouchButtonSkin =
            values().firstOrNull { it.id == id } ?: CLASSIC
    }
}

object TouchButtonSkinManager {
    private const val PREFS_NAME = "touch_button_skin_prefs"
    private const val KEY_SKIN = "button_skin"
    private const val KEY_CUSTOM_COLOR = "custom_button_color"
    const val DEFAULT_CUSTOM_COLOR = 0xFF00E5FFL

    private val _skinFlow = MutableStateFlow(TouchButtonSkin.CLASSIC)
    val skinFlow: StateFlow<TouchButtonSkin> = _skinFlow.asStateFlow()

    private val _customColorFlow = MutableStateFlow(DEFAULT_CUSTOM_COLOR)
    val customColorFlow: StateFlow<Long> = _customColorFlow.asStateFlow()

    private val _activeThemeFlow = MutableStateFlow(LemuroidPadTheme.CLASSIC)
    val activeThemeFlow: StateFlow<LemuroidPadTheme> = _activeThemeFlow.asStateFlow()

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val skinId = prefs.getString(KEY_SKIN, TouchButtonSkin.CLASSIC.id) ?: TouchButtonSkin.CLASSIC.id
            val customColor = prefs.getLong(KEY_CUSTOM_COLOR, DEFAULT_CUSTOM_COLOR)
            val skin = TouchButtonSkin.fromId(skinId)

            _skinFlow.value = skin
            _customColorFlow.value = customColor
            _activeThemeFlow.value = skin.toTheme(customColor)
            initialized = true
        }
    }

    fun setSkin(context: Context, skin: TouchButtonSkin) {
        init(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SKIN, skin.id)
            .apply()
        _skinFlow.value = skin
        _activeThemeFlow.value = skin.toTheme(_customColorFlow.value)
    }

    fun setCustomColor(context: Context, color: Long) {
        init(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_CUSTOM_COLOR, color)
            .apply()
        _customColorFlow.value = color
        if (_skinFlow.value == TouchButtonSkin.CUSTOM) {
            _activeThemeFlow.value = TouchButtonSkin.CUSTOM.toTheme(color)
        }
    }
}

