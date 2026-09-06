package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import android.view.HapticFeedbackConstants
import android.view.View
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
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
 * 2D geometric shape with rounded vertices for Material 3 Expressive LoadingIndicator.
 */
class RoundedPolygon(
    val numVertices: Int,
    val innerRadiusRatio: Float = 1f,
    val rounding: Float = 0.5f,
    val type: PolygonType = PolygonType.Star,
) {
    enum class PolygonType {
        Star,
        Clover,
        Circle,
        Squircle,
    }

    /**
     * Normalized radius profile [0, 1] as a function of angle in radians.
     */
    fun radiusAt(theta: Float, rotationOffsetRad: Float = 0f): Float {
        val angle = theta - rotationOffsetRad
        return when (type) {
            PolygonType.Circle -> 1f
            PolygonType.Squircle -> {
                val cosA = kotlin.math.abs(cos(angle))
                val sinA = kotlin.math.abs(sin(angle))
                val p = 4.0
                val denom = Math.pow(Math.pow(cosA.toDouble(), p) + Math.pow(sinA.toDouble(), p), 1.0 / p).toFloat()
                if (denom > 0.001f) (1f / denom).coerceIn(0.82f, 1f) else 1f
            }
            PolygonType.Clover -> {
                val lobes = numVertices.coerceAtLeast(3)
                val wave = (cos(lobes * angle) + 1f) / 2f
                innerRadiusRatio + (1f - innerRadiusRatio) * wave
            }
            PolygonType.Star -> {
                // 10-scallop / star with smooth rounded lobes (matching official M3 Expressive design)
                val lobes = numVertices.coerceAtLeast(3)
                val u = (cos(lobes * angle) + 1f) / 2f
                val smoothWave = u * u * (3f - 2f * u) // Smoothstep curvature
                innerRadiusRatio + (1f - innerRadiusRatio) * smoothWave
            }
        }
    }

    companion object {
        fun star(
            numVertices: Int = 10,
            innerRadius: Float = 0.72f,
            rounding: Float = 0.5f,
        ): RoundedPolygon = RoundedPolygon(
            numVertices = numVertices,
            innerRadiusRatio = innerRadius,
            rounding = rounding,
            type = PolygonType.Star,
        )

        fun clover(
            numVertices: Int = 8,
            innerRadius: Float = 0.80f,
            rounding: Float = 0.5f,
        ): RoundedPolygon = RoundedPolygon(
            numVertices = numVertices,
            innerRadiusRatio = innerRadius,
            rounding = rounding,
            type = PolygonType.Clover,
        )

        fun circle(): RoundedPolygon = RoundedPolygon(
            numVertices = 36,
            innerRadiusRatio = 1f,
            rounding = 1f,
            type = PolygonType.Circle,
        )

        fun squircle(cornerRounding: Float = 0.5f): RoundedPolygon = RoundedPolygon(
            numVertices = 4,
            innerRadiusRatio = 0.86f,
            rounding = cornerRounding,
            type = PolygonType.Squircle,
        )
    }
}

/**
 * Default configurations for Material 3 Expressive LoadingIndicator.
 */
object LoadingIndicatorDefaults {
    val indicatorColor: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    val containerColor: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)

    val IndeterminateIndicatorPolygons: List<RoundedPolygon> = listOf(
        RoundedPolygon.star(numVertices = 10, innerRadius = 0.72f, rounding = 0.5f), // 10-scallop starburst
        RoundedPolygon.clover(numVertices = 8, innerRadius = 0.80f, rounding = 0.5f),
        RoundedPolygon.circle(),
        RoundedPolygon.squircle(cornerRounding = 0.6f),
        RoundedPolygon.star(numVertices = 12, innerRadius = 0.76f, rounding = 0.5f),
    )

    val DeterminateIndicatorPolygons: List<RoundedPolygon> = listOf(
        RoundedPolygon.circle(),
        RoundedPolygon.star(numVertices = 10, innerRadius = 0.72f, rounding = 0.5f),
    )
}

/**
 * Official Material 3 Expressive Morphing Shape Loading Indicator.
 * Smoothly morphs between rounded polygons with kinetic rotation, dynamic scaling,
 * and optional circular container support.
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = LoadingIndicatorDefaults.indicatorColor,
    polygons: List<RoundedPolygon> = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons,
    containerColor: Color? = null,
) {
    require(polygons.size >= 2) { "The polygons list holds less than two items" }

    val infiniteTransition = rememberInfiniteTransition(label = "M3ExpressiveLoadingTransition")

    // Kinetic rotation with fluid acceleration & deceleration
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "LoadingRotation",
    )

    // Morph cycle continuous progress through polygon sequence
    val numPolygons = polygons.size
    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = numPolygons.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100 * numPolygons, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "MorphProgress",
    )

    // Organic breathing scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PulseScale",
    )

    val effectiveModifier = if (modifier == Modifier) Modifier.size(48.dp) else modifier

    Canvas(modifier = effectiveModifier) {
        val width = size.width
        val height = size.height
        val minDim = minOf(width, height)
        val center = Offset(width / 2f, height / 2f)

        // 1. Optional background circular container (as in M3 Expressive specification & image)
        val hasContainer = containerColor != null && containerColor != Color.Unspecified
        if (hasContainer) {
            drawCircle(
                color = containerColor!!,
                radius = minDim / 2f,
                center = center,
            )
        }

        // 2. Active morphing polygon contour (filled shape)
        val p = morphProgress % numPolygons
        val idx1 = p.toInt() % numPolygons
        val idx2 = (idx1 + 1) % numPolygons
        val rawFraction = (p - idx1).coerceIn(0f, 1f)
        // Smoothstep interpolation for soft organic transition
        val smoothFraction = rawFraction * rawFraction * (3f - 2f * rawFraction)

        val poly1 = polygons[idx1]
        val poly2 = polygons[idx2]

        val rotRad = Math.toRadians(rotation.toDouble()).toFloat()
        val baseRadius = (if (hasContainer) minDim * 0.35f else minDim * 0.44f) * pulseScale

        val path = Path()
        val steps = 120

        for (i in 0..steps) {
            val angle = (i.toFloat() / steps) * 2f * PI.toFloat()
            val r1 = poly1.radiusAt(angle, rotRad)
            val r2 = poly2.radiusAt(angle, rotRad)
            val blendedRatio = r1 * (1f - smoothFraction) + r2 * smoothFraction
            val radius = baseRadius * blendedRatio
            val x = center.x + radius * cos(angle)
            val y = center.y + radius * sin(angle)

            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(
            path = path,
            color = color,
        )
    }
}

/**
 * Determinate Material 3 Expressive Loading Indicator.
 * Morphs between polygons based on the progress callback (0.0 to 1.0).
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun LoadingIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = LoadingIndicatorDefaults.indicatorColor,
    polygons: List<RoundedPolygon> = LoadingIndicatorDefaults.DeterminateIndicatorPolygons,
    containerColor: Color? = null,
) {
    require(polygons.size >= 2) { "The polygons list holds less than two items" }

    val currentProgress = progress().coerceIn(0f, 1f)
    val effectiveModifier = if (modifier == Modifier) Modifier.size(48.dp) else modifier

    Canvas(modifier = effectiveModifier) {
        val width = size.width
        val height = size.height
        val minDim = minOf(width, height)
        val center = Offset(width / 2f, height / 2f)

        val hasContainer = containerColor != null && containerColor != Color.Unspecified
        if (hasContainer) {
            drawCircle(
                color = containerColor!!,
                radius = minDim / 2f,
                center = center,
            )
        }

        val totalIntervals = (polygons.size - 1).coerceAtLeast(1)
        val progressScaled = currentProgress * totalIntervals
        val idx1 = progressScaled.toInt().coerceIn(0, polygons.size - 2)
        val idx2 = (idx1 + 1).coerceAtMost(polygons.size - 1)
        val rawFraction = (progressScaled - idx1).coerceIn(0f, 1f)
        val smoothFraction = rawFraction * rawFraction * (3f - 2f * rawFraction)

        val poly1 = polygons[idx1]
        val poly2 = polygons[idx2]

        val rotRad = Math.toRadians((currentProgress * 360f).toDouble()).toFloat()
        val baseRadius = if (hasContainer) minDim * 0.35f else minDim * 0.44f

        val path = Path()
        val steps = 120

        for (i in 0..steps) {
            val angle = (i.toFloat() / steps) * 2f * PI.toFloat()
            val r1 = poly1.radiusAt(angle, rotRad)
            val r2 = poly2.radiusAt(angle, rotRad)
            val blendedRatio = r1 * (1f - smoothFraction) + r2 * smoothFraction
            val radius = baseRadius * blendedRatio
            val x = center.x + radius * cos(angle)
            val y = center.y + radius * sin(angle)

            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(
            path = path,
            color = color,
        )
    }
}

/**
 * Material 3 Expressive Morphing Loading Indicator bridge.
 * Directly renders the new M3 Expressive LoadingIndicator.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveMorphingLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = LoadingIndicatorDefaults.indicatorColor,
    trackColor: Color = LoadingIndicatorDefaults.containerColor,
    size: Dp = 48.dp,
    showContainer: Boolean = true,
) {
    LoadingIndicator(
        modifier = modifier.size(size),
        color = color,
        containerColor = if (showContainer) trackColor else null,
    )
}

/**
 * Material 3 Expressive Linear Progress Bar (Single Unified Component).
 * Strictly renders a single continuous sliding pill across the track to eliminate duplicate lines.
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

    // Single unified travel cycle from -0.3f to 1.3f
    val travelProgress by infiniteTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "LinearTravel",
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

        // 1. Single rounded background track
        drawLine(
            color = trackColor,
            start = Offset(stroke / 2f, centerY),
            end = Offset(width - stroke / 2f, centerY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )

        // 2. Single active progress pill (strictly 1 continuous segment)
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
            val pillWidth = width * 0.34f
            val centerXPx = width * travelProgress
            val startXPx = (centerXPx - pillWidth / 2f).coerceIn(stroke / 2f, width - stroke / 2f)
            val endXPx = (centerXPx + pillWidth / 2f).coerceIn(stroke / 2f, width - stroke / 2f)

            if (endXPx > startXPx + 0.5f) {
                drawLine(
                    color = color,
                    start = Offset(startXPx, centerY),
                    end = Offset(endXPx, centerY),
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
 * Global bridge: replaces old circular wave with M3 Expressive LoadingIndicator.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WavyCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = LoadingIndicatorDefaults.indicatorColor,
    trackColor: Color = LoadingIndicatorDefaults.containerColor,
    size: Dp = 48.dp,
    lobes: Int = 8,
) {
    LoadingIndicator(
        modifier = modifier.size(size),
        color = color,
        containerColor = trackColor,
    )
}

/**
 * Centralized Haptic Feedback helper for tactile tactile vibrations across the app.
 */
class LemuroidHapticFeedback(
    private val haptic: HapticFeedback,
    private val view: View,
) {
    fun click() {
        try {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } catch (_: Throwable) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun tick() {
        try {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } catch (_: Throwable) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun longPress() {
        try {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        } catch (_: Throwable) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun toggle() {
        try {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        } catch (_: Throwable) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

/**
 * Remembers a centralized LemuroidHapticFeedback instance.
 */
@Composable
fun rememberLemuroidHaptics(): LemuroidHapticFeedback {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    return remember(haptic, view) {
        LemuroidHapticFeedback(haptic, view)
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
