package com.swordfish.lemuroid.app.shared.game

import android.content.Context
import com.swordfish.lemuroid.R
import com.swordfish.touchinput.radial.LemuroidPadTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TouchButtonSkin(val id: String, val titleResId: Int) {
    CLASSIC("classic", R.string.touch_skin_classic),
    DARK("dark", R.string.touch_skin_dark),
    PINK("pink", R.string.touch_skin_pink);

    fun toTheme(): LemuroidPadTheme = when (this) {
        CLASSIC -> LemuroidPadTheme.CLASSIC
        DARK -> LemuroidPadTheme.DARK
        PINK -> LemuroidPadTheme.PINK
    }

    companion object {
        fun fromId(id: String): TouchButtonSkin =
            values().firstOrNull { it.id == id } ?: CLASSIC
    }
}

object TouchButtonSkinManager {
    private const val PREFS_NAME = "touch_button_skin_prefs"
    private const val KEY_SKIN = "button_skin"

    private val _skinFlow = MutableStateFlow(TouchButtonSkin.CLASSIC)
    val skinFlow: StateFlow<TouchButtonSkin> = _skinFlow.asStateFlow()

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val skinId = prefs.getString(KEY_SKIN, TouchButtonSkin.CLASSIC.id) ?: TouchButtonSkin.CLASSIC.id
            _skinFlow.value = TouchButtonSkin.fromId(skinId)
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
    }
}
