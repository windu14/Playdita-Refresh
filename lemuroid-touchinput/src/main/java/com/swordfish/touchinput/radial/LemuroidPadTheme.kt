package com.swordfish.touchinput.radial

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

open class LemuroidPadTheme(
    val foregroundPadding: Dp = 8.dp,
    val padding: Dp = 4.dp,
    private val icons: Color = Color(0.0f, 0.0f, 0.0f, 0.50f),
    private val iconsPressed: Color = Color(1.0f, 1.0f, 1.0f, 0.50f),
    private val level3Fill: Color = Color(1.0f, 1.0f, 1.0f, 0.50f),
    private val level3FillPressed: Color = Color(0.0f, 0.0f, 0.0f, 0.50f),
    val level3Shadow: Color = DefaultShadowColor.copy(0.05f),
    val level3ShadowWidth: Dp = 4.dp,
    private val level2Fill: Color = Color(1.0f, 1.0f, 1.0f, 0.125f),
    private val level2FillPressed: Color = Color(0.0f, 0.0f, 0.0f, 0.125f),
    val level2Shadow: Color = DefaultShadowColor.copy(0.05f),
    val level2ShadowWidth: Dp = 4.dp,
    val level1Fill: Color = Color(1.0f, 1.0f, 1.0f, 0.10f),
    val level1Shadow: Color = DefaultShadowColor.copy(0.10f),
    val level1ShadowWidth: Dp = 4.dp,
    val level0CornerRadius: Dp = 0.dp,
    val level0Fill: Color = Color(1.0f, 1.0f, 1.0f, 0.05f),
    val level0Shadow: Color = DefaultShadowColor.copy(0.10f),
    val level0ShadowWidth: Dp = 2.dp,
) {
    open fun compositeFill(pressed: Boolean): Color {
        return if (pressed) {
            level2FillPressed
        } else {
            level2Fill
        }
    }

    open fun foregroundFill(pressed: Boolean): Color {
        return if (pressed) {
            level3FillPressed
        } else {
            level3Fill
        }
    }

    open fun icons(pressed: Boolean): Color {
        return if (pressed) {
            iconsPressed
        } else {
            icons
        }
    }

    companion object {
        val CLASSIC = LemuroidPadTheme()

        val DARK = LemuroidPadTheme(
            icons = Color(0.95f, 0.95f, 0.95f, 0.85f),
            iconsPressed = Color(1.0f, 1.0f, 1.0f, 1.0f),
            level3Fill = Color(0.20f, 0.20f, 0.22f, 0.85f),
            level3FillPressed = Color(0.08f, 0.08f, 0.10f, 0.95f),
            level3Shadow = Color.Black.copy(0.40f),
            level2Fill = Color(0.15f, 0.15f, 0.18f, 0.55f),
            level2FillPressed = Color(0.05f, 0.05f, 0.07f, 0.75f),
            level2Shadow = Color.Black.copy(0.35f),
            level1Fill = Color(0.10f, 0.10f, 0.12f, 0.45f),
            level1Shadow = Color.Black.copy(0.30f),
            level0Fill = Color(0.06f, 0.06f, 0.08f, 0.25f),
            level0Shadow = Color.Black.copy(0.20f),
        )

        val PINK = LemuroidPadTheme(
            icons = Color(0.55f, 0.08f, 0.28f, 0.85f),
            iconsPressed = Color(1.0f, 1.0f, 1.0f, 0.95f),
            level3Fill = Color(1.0f, 0.42f, 0.68f, 0.70f),
            level3FillPressed = Color(0.88f, 0.18f, 0.48f, 0.85f),
            level3Shadow = Color(0xFFC2185B).copy(0.30f),
            level2Fill = Color(1.0f, 0.55f, 0.78f, 0.30f),
            level2FillPressed = Color(0.90f, 0.25f, 0.55f, 0.45f),
            level2Shadow = Color(0xFFC2185B).copy(0.20f),
            level1Fill = Color(1.0f, 0.65f, 0.82f, 0.22f),
            level1Shadow = Color(0xFFC2185B).copy(0.15f),
            level0Fill = Color(1.0f, 0.75f, 0.88f, 0.12f),
            level0Shadow = Color(0xFFC2185B).copy(0.10f),
        )

        val CYAN = createColorTheme(Color(0xFF00E5FF))
        val EMERALD = createColorTheme(Color(0xFF00E676))
        val AMBER = createColorTheme(Color(0xFFFFAB00))
        val VIOLET = createColorTheme(Color(0xFF7C4DFF))

        fun createColorTheme(color: Color): LemuroidPadTheme {
            val r = color.red
            val g = color.green
            val b = color.blue
            val lum = r * 0.299f + g * 0.587f + b * 0.114f
            val iconColor = if (lum > 0.65f) Color(0.10f, 0.10f, 0.12f, 0.85f) else Color.White.copy(alpha = 0.90f)

            return LemuroidPadTheme(
                icons = iconColor,
                iconsPressed = Color.White,
                level3Fill = color.copy(alpha = 0.70f),
                level3FillPressed = Color(r * 0.75f, g * 0.75f, b * 0.75f, 0.90f),
                level3Shadow = color.copy(alpha = 0.35f),
                level2Fill = color.copy(alpha = 0.30f),
                level2FillPressed = color.copy(alpha = 0.55f),
                level2Shadow = color.copy(alpha = 0.20f),
                level1Fill = color.copy(alpha = 0.20f),
                level1Shadow = color.copy(alpha = 0.15f),
                level0Fill = color.copy(alpha = 0.10f),
                level0Shadow = color.copy(alpha = 0.10f),
            )
        }
    }
}

val LocalLemuroidPadTheme =
    compositionLocalOf<LemuroidPadTheme> {
        error("LemuroidPadTheme is missing")
    }
