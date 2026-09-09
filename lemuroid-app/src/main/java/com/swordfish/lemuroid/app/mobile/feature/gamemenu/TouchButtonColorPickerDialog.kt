package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.rememberLemuroidHaptics
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TouchButtonColorPickerDialog(
    initialColorLong: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val haptics = rememberLemuroidHaptics()
    val initialColor = remember { Color(initialColorLong) }

    // Convert RGB to HSV for smooth wheel/hue slider
    val initialHsv = remember {
        FloatArray(3).also {
            android.graphics.Color.colorToHSV(initialColor.toArgb(), it)
        }
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(if (initialHsv[1] <= 0f) 0.85f else initialHsv[1]) }
    var value by remember { mutableFloatStateOf(if (initialHsv[2] <= 0f) 0.95f else initialHsv[2]) }

    val currentColor = remember(hue, saturation, value) {
        val rgb = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
        Color(rgb)
    }

    val quickColors = remember {
        listOf(
            0xFF00E5FFL, // Cyber Cyan
            0xFFFF1493L, // Neon Pink
            0xFF00E676L, // Mint Green
            0xFFFFAB00L, // Retro Amber
            0xFF7C4DFFL, // Ultra Violet
            0xFFFF3D00L, // Neon Orange
            0xFFE040FBL, // Fuchsia
            0xFF00B0FFL, // Ice Blue
            0xFFFF5252L, // Crimson
            0xFFFFFFFFL, // Pure White
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Pilih Warna Tombol",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Live preview card of the touch button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF16181D)),
                    contentAlignment = Alignment.Center,
                ) {
                    // Preview Button (simulates radial glass button)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape, spotColor = currentColor)
                            .clip(CircleShape)
                            .background(currentColor.copy(alpha = 0.65f))
                            .border(2.dp, currentColor.copy(alpha = 0.90f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "A",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                        )
                    }
                }

                // Continuous Rainbow Hue Spectrum Bar
                Text(
                    text = "Spektrum Warna",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val rainbowColors = remember {
                    listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                        Color.Red,
                    )
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.horizontalGradient(rainbowColors))
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                hue = fraction * 360f
                                haptics.tick()
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                hue = fraction * 360f
                                haptics.tick()
                            }
                        },
                ) {
                    val indicatorOffset = ((hue / 360f) * maxWidth.value).dp - 14.dp
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(indicatorOffset.roundToPx().coerceIn(0, (maxWidth - 28.dp).roundToPx()), 4.dp.roundToPx()) }
                            .size(28.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color.Black.copy(alpha = 0.25f), CircleShape),
                    )
                }

                // Saturation / Intensity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Kepekatan",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(68.dp),
                    )
                    Slider(
                        value = saturation,
                        onValueChange = { saturation = it },
                        valueRange = 0.2f..1.0f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = currentColor,
                            activeTrackColor = currentColor,
                        ),
                    )
                }

                // Quick Color Preset Palette Chips
                Text(
                    text = "Warna Populer",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    quickColors.forEach { colorVal ->
                        val chipColor = Color(colorVal)
                        val isSelected = currentColor.toArgb() == chipColor.toArgb()
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(chipColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.25f),
                                    shape = CircleShape,
                                )
                                .clickable {
                                    haptics.click()
                                    val hsv = FloatArray(3)
                                    android.graphics.Color.colorToHSV(chipColor.toArgb(), hsv)
                                    hue = hsv[0]
                                    saturation = hsv[1]
                                    value = hsv[2]
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (saturation < 0.3f) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    haptics.click()
                    val finalColorLong = (currentColor.toArgb().toLong()) and 0xFFFFFFFFL
                    onColorSelected(finalColorLong)
                },
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(text = "Terapkan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    haptics.click()
                    onDismiss()
                },
            ) {
                Text(text = "Batal")
            }
        },
    )
}
