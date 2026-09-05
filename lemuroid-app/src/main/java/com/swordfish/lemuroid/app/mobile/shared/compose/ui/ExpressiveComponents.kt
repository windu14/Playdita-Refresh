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
 * Material 3 Expressive Wavy/Snake Progress Bar.
 * Draws an animated sinusoidal wave with rounded endpoints, as shown in the M3 Expressive design system.
 */
@Composable
fun WavyLinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null, // null for indeterminate wave animation
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
    waveAmplitude: Dp = 4.dp,
    wavelength: Dp = 24.dp,
    strokeWidth: Dp = 4.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WavyProgressTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "WavePhase",
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(waveAmplitude * 2 + strokeWidth + 6.dp)
            .padding(horizontal = 4.dp),
    ) {
        val width = size.width
        val centerY = size.height / 2f
        val amp = waveAmplitude.toPx()
        val wl = wavelength.toPx().coerceAtLeast(20f)
        val stroke = strokeWidth.toPx()

        // 1. Draw track line
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )

        // 2. Draw animated sinusoidal wavy progress
        val activeWidth = if (progress != null) width * progress.coerceIn(0f, 1f) else width
        if (activeWidth > 2f) {
            val wavePath = Path()
            var first = true
            var x = 0f
            val step = 3f

            while (x <= activeWidth) {
                val waveFactor = if (progress != null) {
                    // Smooth transition from straight line to wave near the head
                    (x / activeWidth).coerceIn(0f, 1f)
                } else 1f

                val currentAmp = amp * waveFactor
                val y = centerY + currentAmp * sin((x / wl) * 2 * PI.toFloat() - phase)

                if (first) {
                    wavePath.moveTo(x, y)
                    first = false
                } else {
                    wavePath.lineTo(x, y)
                }
                x += step
            }

            drawPath(
                path = wavePath,
                color = color,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )

            // Draw leading cap dot
            val leadY = centerY + (amp * if (progress != null) 1f else 1f) * sin((activeWidth / wl) * 2 * PI.toFloat() - phase)
            drawCircle(
                color = color,
                radius = stroke * 0.9f,
                center = Offset(activeWidth, leadY),
            )
        }
    }
}

/**
 * Material 3 Expressive Scallop Circular Loading Indicator.
 * Creates an undulating flower/scallop shape with pulsing breath animation.
 */
@Composable
fun WavyCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
    size: Dp = 48.dp,
    lobes: Int = 8,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CircularWave")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "CircularWaveRotation",
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "CircularWavePulse",
    )

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val baseRadius = (this.size.width / 2f - 6.dp.toPx()) * pulse
        val waveAmplitude = 4.dp.toPx()

        val path = Path()
        val totalPoints = 120
        for (i in 0..totalPoints) {
            val angle = (i.toFloat() / totalPoints) * 2 * PI.toFloat()
            val r = baseRadius + waveAmplitude * sin(lobes * angle + Math.toRadians(rotation.toDouble()).toFloat())
            val x = center.x + r * cos(angle)
            val y = center.y + r * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round),
        )
    }
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
