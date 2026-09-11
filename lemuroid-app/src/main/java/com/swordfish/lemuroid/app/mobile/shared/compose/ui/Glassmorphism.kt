package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
 * 2026 iOS-Inspired Glassmorphic Design System Tokens & Modifiers.
 * Engineered for Jetpack Compose & Material 3 with ultra-smooth translucent acrylic surfaces,
 * specular highlight hairline borders, and fluid continuous curvature.
 */
object Glassmorphism {
    // Standard Corner Radii for 2026 Glassmorphic aesthetic
    val CardShape = RoundedCornerShape(20.dp)
    val LargeCardShape = RoundedCornerShape(24.dp)
    val PillShape = RoundedCornerShape(32.dp)
    val DialogShape = RoundedCornerShape(26.dp)
    val ChipShape = RoundedCornerShape(14.dp)
    val SmallShape = RoundedCornerShape(10.dp)

    @Composable
    fun isDarkSurface(): Boolean {
        return MaterialTheme.colorScheme.surface.luminance() < 0.5f || isSystemInDarkTheme()
    }

    @Composable
    fun containerColor(isDark: Boolean = isDarkSurface(), alpha: Float = 0.82f): Color {
        return if (isDark) {
            Color(0xFF141722).copy(alpha = alpha)
        } else {
            Color(0xFFFFFFFF).copy(alpha = alpha)
        }
    }

    @Composable
    fun elevatedContainerColor(isDark: Boolean = isDarkSurface(), alpha: Float = 0.88f): Color {
        return if (isDark) {
            Color(0xFF1C2030).copy(alpha = alpha)
        } else {
            Color(0xFFF6F8FC).copy(alpha = alpha)
        }
    }

    @Composable
    fun borderBrush(isDark: Boolean = isDarkSurface()): Brush {
        val topHighlight = if (isDark) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.90f)
        val bottomBorder = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.08f)
        return Brush.verticalGradient(
            listOf(topHighlight, bottomBorder),
        )
    }

    @Composable
    fun activeBorderBrush(isDark: Boolean = isDarkSurface()): Brush {
        val primary = MaterialTheme.colorScheme.primary
        val topHighlight = primary.copy(alpha = if (isDark) 0.85f else 0.70f)
        val bottomBorder = primary.copy(alpha = if (isDark) 0.25f else 0.20f)
        return Brush.verticalGradient(
            listOf(topHighlight, bottomBorder),
        )
    }

    @Composable
    fun specularOverlayBrush(isDark: Boolean = isDarkSurface()): Brush {
        return Brush.verticalGradient(
            colors = listOf(
                (if (isDark) Color.White else Color.White).copy(alpha = if (isDark) 0.06f else 0.15f),
                Color.Transparent,
                (if (isDark) Color.Black else Color.Black).copy(alpha = if (isDark) 0.05f else 0.02f),
            ),
        )
    }
}

/**
 * Applies a 2026 iOS-style frosted glass effect with specular highlight border,
 * translucent container, soft ambient shadow, and rounded corners.
 */
@Composable
fun Modifier.glassmorphicCard(
    shape: Shape = Glassmorphism.CardShape,
    elevation: Dp = 6.dp,
    isDark: Boolean = Glassmorphism.isDarkSurface(),
    borderWidth: Dp = 1.dp,
    alpha: Float = 0.82f,
): Modifier {
    val container = Glassmorphism.containerColor(isDark, alpha)
    val borderStroke = Glassmorphism.borderBrush(isDark)
    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.40f) else Color.Black.copy(alpha = 0.07f)

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
 * A ready-to-use 2026 iOS frosted glass surface container.
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
    content: @Composable BoxScope.() -> Unit,
) {
    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.08f)

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
            .border(width = borderWidth, brush = borderBrush, shape = shape),
    ) {
        // Subtle specular highlight gradient overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Glassmorphism.specularOverlayBrush(isDark)),
        )
        content()
    }
}
