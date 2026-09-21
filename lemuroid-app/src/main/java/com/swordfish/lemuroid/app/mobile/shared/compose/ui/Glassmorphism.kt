package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 2026 iOS-Inspired "Perfect Glassmorphism" Design System Tokens & Modifiers.
 * Engineered for Jetpack Compose & Material 3 with ultra-smooth translucent acrylic surfaces,
 * specular highlight hairline borders, organic refractive gradients, and fluid continuous curvature.
 */
object Glassmorphism {
    // 2026 Continuous Curvature Squircles
    val CardShape = RoundedCornerShape(22.dp)
    val LargeCardShape = RoundedCornerShape(26.dp)
    val IslandShape = RoundedCornerShape(34.dp)
    val PillShape = CircleShape
    val DialogShape = RoundedCornerShape(28.dp)
    val ChipShape = RoundedCornerShape(16.dp)
    val SmallShape = RoundedCornerShape(12.dp)

    // iOS 2026 Vibrant Neon & Aurora Accents
    val NeonCyan = Color(0xFF00E5FF)
    val NeonViolet = Color(0xFF8B5CF6)
    val ElectricBlue = Color(0xFF0A84FF)
    val SunsetOrange = Color(0xFFFF9F0A)
    val EmeraldGreen = Color(0xFF30D158)
    val AuroraGlowStart = Color(0xFF00E5FF)
    val AuroraGlowEnd = Color(0xFFFF3366)
    val ObsidianSurface = Color(0xFF0F121C)
    val ObsidianCard = Color(0xFF141824)

    @Composable
    fun isDarkSurface(): Boolean {
        return MaterialTheme.colorScheme.surface.luminance() < 0.5f || isSystemInDarkTheme()
    }

    /**
     * Frosted Glass acrylic background color with calibrated opacity for depth.
     */
    @Composable
    fun containerColor(isDark: Boolean = isDarkSurface(), alpha: Float = if (isDark) 0.86f else 0.88f): Color {
        return if (isDark) {
            Color(0xFF121623).copy(alpha = alpha)
        } else {
            Color(0xFFFCFDFF).copy(alpha = alpha)
        }
    }

    @Composable
    fun elevatedContainerColor(isDark: Boolean = isDarkSurface(), alpha: Float = if (isDark) 0.90f else 0.94f): Color {
        return if (isDark) {
            Color(0xFF181D2D).copy(alpha = alpha)
        } else {
            Color(0xFFF4F6FB).copy(alpha = alpha)
        }
    }

    /**
     * Floating Island / Dynamic Dock acrylic color with deep obsidian tint in dark mode.
     */
    @Composable
    fun islandContainerColor(isDark: Boolean = isDarkSurface()): Color {
        return if (isDark) {
            Color(0xF00D111A)
        } else {
            Color(0xF4FFFFFF)
        }
    }

    /**
     * 2026 Aurora Ambient Glow Brush for hero banners and dynamic highlight backdrops.
     */
    @Composable
    fun auroraGlowBrush(isDark: Boolean = isDarkSurface()): Brush {
        return if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.28f),
                    Color(0xFF8B5CF6).copy(alpha = 0.22f),
                    Color(0xFFFF3366).copy(alpha = 0.16f),
                    Color.Transparent,
                ),
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF00B0FF).copy(alpha = 0.20f),
                    Color(0xFF7C4DFF).copy(alpha = 0.15f),
                    Color(0xFFFF4081).copy(alpha = 0.10f),
                    Color.Transparent,
                ),
            )
        }
    }

    /**
     * Beveled specular hairline edge brush: bright reflection at top, transparent/dark rim at bottom.
     */
    @Composable
    fun borderBrush(isDark: Boolean = isDarkSurface()): Brush {
        val topHighlight = if (isDark) Color.White.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.95f)
        val middleSheen = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.40f)
        val bottomBorder = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.08f)
        return Brush.verticalGradient(
            listOf(topHighlight, middleSheen, bottomBorder),
        )
    }

    /**
     * Active/focused glowing border brush with iOS 2026 neon reflection.
     */
    @Composable
    fun activeBorderBrush(isDark: Boolean = isDarkSurface()): Brush {
        return Brush.verticalGradient(
            listOf(
                NeonCyan.copy(alpha = if (isDark) 0.95f else 0.85f),
                NeonViolet.copy(alpha = if (isDark) 0.70f else 0.60f),
            ),
        )
    }

    /**
     * Specular light sweep gradient across frosted acrylic surface.
     */
    @Composable
    fun specularOverlayBrush(isDark: Boolean = isDarkSurface()): Brush {
        return Brush.verticalGradient(
            colors = listOf(
                (if (isDark) Color.White else Color.White).copy(alpha = if (isDark) 0.08f else 0.22f),
                (if (isDark) Color.White else Color.White).copy(alpha = if (isDark) 0.02f else 0.06f),
                Color.Transparent,
                (if (isDark) Color.Black else Color.Black).copy(alpha = if (isDark) 0.06f else 0.02f),
            ),
        )
    }
}

/**
 * Applies 2026 iOS-style frosted glass effect with specular highlight border,
 * translucent container, soft ambient shadow, and rounded corners.
 */
@Composable
fun Modifier.glassmorphicCard(
    shape: Shape = Glassmorphism.CardShape,
    elevation: Dp = 6.dp,
    isDark: Boolean = Glassmorphism.isDarkSurface(),
    borderWidth: Dp = 1.dp,
    alpha: Float = if (isDark) 0.84f else 0.88f,
): Modifier {
    val container = Glassmorphism.containerColor(isDark, alpha)
    val borderStroke = Glassmorphism.borderBrush(isDark)
    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.08f)

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            spotColor = shadowColor,
            ambientColor = shadowColor,
        )
        .clip(shape)
        .background(container)
        .border(width = borderWidth, brush = borderStroke, shape = shape)
}

/**
 * A ready-to-use 2026 iOS frosted glass surface container with specular reflection.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = Glassmorphism.CardShape,
    elevation: Dp = 4.dp,
    borderWidth: Dp = 1.dp,
    isDark: Boolean = Glassmorphism.isDarkSurface(),
    borderBrush: Brush = Glassmorphism.borderBrush(isDark),
    containerColor: Color = Glassmorphism.containerColor(isDark),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.08f)
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = MaterialTheme.colorScheme.primary),
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                spotColor = shadowColor,
                ambientColor = shadowColor,
            )
            .clip(shape)
            .background(containerColor)
            .border(width = borderWidth, brush = borderBrush, shape = shape)
            .then(clickableModifier),
    ) {
        // Specular highlight gradient overlay for realistic frosted glass refraction
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Glassmorphism.specularOverlayBrush(isDark)),
        )
        content()
    }
}
