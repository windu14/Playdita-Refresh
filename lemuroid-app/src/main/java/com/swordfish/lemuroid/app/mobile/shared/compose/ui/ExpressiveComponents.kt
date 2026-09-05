package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Opt-in annotation for Material 3 Expressive APIs across the application.
 */
@RequiresOptIn(
    message = "This Material 3 Expressive API is experimental and designed for rich, fluid expressive UI.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class ExperimentalMaterial3ExpressiveApi

/**
 * Material 3 Expressive Morphing Loading Indicator.
 * Smoothly and continuously morphs between organic geometric shapes (Circle -> Squircle -> 4-Lobed Clover)
 * with kinetic rotation, dynamic scaling, and an inner harmonic pulse.
 */
@Composable
fun ExpressiveMorphingLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
    size: Dp = 44.dp,
    strokeWidth: Dp = 3.5.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ExpressiveMorphingTransition")

    // Kinetic rotation with fluid acceleration & deceleration (FastOutSlowInEasing)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "KineticRotation",
    )

    // Shape morphing cycle between circle (0.04f) and expressive 4-lobed squircle/clover (0.26f)
    val morphFactor by infiniteTransition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.26f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "MorphFactor",
    )

    // Breathing pulse scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PulseScale",
    )

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val maxRadius = (this.size.width / 2f - strokeWidth.toPx() - 2.dp.toPx()) * pulseScale
        val rotRad = Math.toRadians(rotation.toDouble()).toFloat()

        // 1. Draw subtle ambient track circle
        drawCircle(
            color = trackColor,
            radius = maxRadius,
            center = center,
            style = Stroke(width = strokeWidth.toPx() * 0.75f),
        )

        // 2. Draw animated M3 Expressive Morphing contour
        val path = Path()
        val steps = 90
        val nLobes = 4 // 4-lobed expressive clover / squircle

        for (i in 0..steps) {
            val angle = (i.toFloat() / steps) * 2f * PI.toFloat()
            val r = maxRadius * (1f + morphFactor * cos(nLobes * (angle - rotRad)))
            val x = center.x + r * cos(angle)
            val y = center.y + r * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
        )

        // 3. Central expressive nucleus
        drawCircle(
            color = color,
            radius = strokeWidth.toPx() * (1.1f - morphFactor * 1.5f),
            center = center,
        )
    }
}

/**
 * Material 3 Expressive Linear Progress Bar.
 * Clean, smooth indeterminate progress pill with dynamic length morphing and rounded caps,
 * replacing wavy/snake animations with modern M3 Expressive motion.
 */
@Composable
fun ExpressiveLinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
    strokeWidth: Dp = 4.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ExpressiveLinearTransition")

    // Dynamic head and tail progress for indeterminate morphing pill
    val headProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "LinearHead",
    )

    val tailProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, delayMillis = 180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "LinearTail",
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(strokeWidth + 4.dp)
            .padding(horizontal = 2.dp),
    ) {
        val width = size.width
        val centerY = size.height / 2f
        val stroke = strokeWidth.toPx()

        // 1. Background rounded track
        drawLine(
            color = trackColor,
            start = Offset(stroke / 2f, centerY),
            end = Offset(width - stroke / 2f, centerY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )

        // 2. Active morphing indicator
        if (progress != null) {
            val endX = (stroke / 2f + (width - stroke) * progress.coerceIn(0f, 1f)).coerceAtLeast(stroke / 2f)
            drawLine(
                color = color,
                start = Offset(stroke / 2f, centerY),
                end = Offset(endX, centerY),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        } else {
            val startX = (stroke / 2f + (width - stroke) * tailProgress).coerceIn(stroke / 2f, width - stroke / 2f)
            val endX = (stroke / 2f + (width - stroke) * headProgress).coerceIn(stroke / 2f, width - stroke / 2f)

            if (endX > startX) {
                drawLine(
                    color = color,
                    start = Offset(startX, centerY),
                    end = Offset(endX, centerY),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            } else {
                drawLine(
                    color = color,
                    start = Offset(startX, centerY),
                    end = Offset(width - stroke / 2f, centerY),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(stroke / 2f, centerY),
                    end = Offset(endX, centerY),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

/**
 * Global bridge: replaces old wavy snake with M3 Expressive linear indicator.
 */
@Composable
fun WavyLinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
    waveAmplitude: Dp = 4.dp,
    wavelength: Dp = 24.dp,
    strokeWidth: Dp = 4.dp,
) {
    ExpressiveLinearProgressIndicator(
        modifier = modifier,
        progress = progress,
        color = color,
        trackColor = trackColor,
        strokeWidth = strokeWidth,
    )
}

/**
 * Global bridge: replaces old circular wave with M3 Expressive morphing indicator.
 */
@Composable
fun WavyCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
    size: Dp = 48.dp,
    lobes: Int = 8,
) {
    ExpressiveMorphingLoadingIndicator(
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        size = size,
    )
}

/**
 * Expressive Scallop Petal Badge for counts, notifications, or active highlights.
 */
@Composable
fun ScallopBadge(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    text: String = "",
    size: Dp = 28.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, shape = CircleShape, ambientColor = backgroundColor.copy(alpha = 0.4f))
            .background(backgroundColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (text.isNotEmpty()) {
            Text(
                text = text,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Expressive Pill Action Chip / Button.
 */
@Composable
fun ExpressivePillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    badgeText: String? = null,
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = backgroundColor,
        tonalElevation = if (selected) 4.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            icon?.invoke()
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = badgeText,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/**
 * Expressive Morphic Card Container with generous 24dp - 28dp radius and fluid subtle gradient borders.
 */
@Composable
fun ExpressiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = 2.dp,
    content: @Composable () -> Unit,
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = clickableModifier
            .shadow(elevation, shape = shape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .clip(shape),
        shape = shape,
        color = containerColor,
        tonalElevation = elevation,
    ) {
        content()
    }
}
