package com.swordfish.lemuroid.app.shared.cheat

import java.io.Serializable
import java.util.UUID

data class GbaCheat(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val code: String,
    val enabled: Boolean = true,
) : Serializable {
    /**
     * Libretro mGBA format: multiline codes joined with '+'
     */
    val formattedCode: String
        get() {
            return code
                .split("\n", "\r", "+")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString("+")
        }
}
