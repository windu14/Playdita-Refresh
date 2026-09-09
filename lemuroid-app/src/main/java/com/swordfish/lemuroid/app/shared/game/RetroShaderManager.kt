package com.swordfish.lemuroid.app.shared.game

import android.content.Context
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RetroShaderManager {
    private val _filterFlow = MutableStateFlow("auto")
    val filterFlow: StateFlow<String> = _filterFlow.asStateFlow()

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val filter = prefs.getString(context.getString(R.string.pref_key_shader_filter), "auto") ?: "auto"
            _filterFlow.value = filter
            initialized = true
        }
    }

    fun setFilter(context: Context, filter: String) {
        init(context)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
            .putString(context.getString(R.string.pref_key_shader_filter), filter)
            .apply()
        _filterFlow.value = filter
    }

    fun getFilter(context: Context): String {
        init(context)
        return _filterFlow.value
    }
}
