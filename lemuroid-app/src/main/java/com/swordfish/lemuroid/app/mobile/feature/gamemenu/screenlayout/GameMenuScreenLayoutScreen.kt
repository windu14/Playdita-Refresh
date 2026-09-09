package com.swordfish.lemuroid.app.mobile.feature.gamemenu.screenlayout

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.game.GameScreenSettingsManager
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameMenuScreenLayoutScreen() {
    val context = LocalContext.current
    val settings by GameScreenSettingsManager.screenSettingsFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Mini Live Preview
        Text(
            text = stringResource(R.string.screen_layout_preview_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(Color.Black, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                // Simulated screen moving with scale & offset
                val previewScale = settings.scale.coerceIn(0.70f, 1.15f)
                val previewOffset = (settings.verticalOffsetDp * 0.7f).roundToInt().dp

                Box(
                    modifier = Modifier
                        .offset(y = previewOffset)
                        .fillMaxHeight(previewScale * 0.85f)
                        .then(
                            if (settings.stretch) {
                                Modifier.fillMaxWidth(previewScale * 0.90f)
                            } else {
                                Modifier.aspectRatio(4f / 3f)
                            }
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
                        .border(1.5.dp, MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${(settings.scale * 100).roundToInt()}%",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // Section 1: Screen Scale (Zoom)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.screen_layout_scale_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "${(settings.scale * 100).roundToInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Text(
                    text = stringResource(R.string.screen_layout_scale_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Slider(
                    value = settings.scale,
                    onValueChange = { GameScreenSettingsManager.updateScale(context, it) },
                    valueRange = 0.70f..1.15f,
                    steps = 8,
                )

                // Quick presets
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        0.80f to "80%",
                        0.90f to "90%",
                        1.00f to "100% (Normal)",
                        1.10f to "110%",
                    ).forEach { (scaleVal, label) ->
                        val selected = (settings.scale * 100).roundToInt() == (scaleVal * 100).roundToInt()
                        FilterChip(
                            selected = selected,
                            onClick = { GameScreenSettingsManager.updateScale(context, scaleVal) },
                            label = { Text(text = label) },
                        )
                    }
                }
            }
        }

        // Section 2: Vertical Position (Offset)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerticalAlignCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.screen_layout_pos_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    val posLabel = when {
                        settings.verticalOffsetDp < -15 -> "${stringResource(R.string.screen_layout_pos_top)} (${settings.verticalOffsetDp} dp)"
                        settings.verticalOffsetDp > 15 -> "${stringResource(R.string.screen_layout_pos_bottom)} (+${settings.verticalOffsetDp} dp)"
                        else -> "${stringResource(R.string.screen_layout_pos_center)} (0 dp)"
                    }
                    Text(
                        text = posLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Text(
                    text = stringResource(R.string.screen_layout_pos_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Slider(
                    value = settings.verticalOffsetDp.toFloat(),
                    onValueChange = { GameScreenSettingsManager.updateVerticalOffset(context, it.roundToInt()) },
                    valueRange = -50f..50f,
                    steps = 9,
                )

                // Presets
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        -35 to stringResource(R.string.screen_layout_pos_top),
                        0 to stringResource(R.string.screen_layout_pos_center),
                        35 to stringResource(R.string.screen_layout_pos_bottom),
                    ).forEach { (offsetVal, label) ->
                        val selected = when (offsetVal) {
                            0 -> settings.verticalOffsetDp in -10..10
                            -35 -> settings.verticalOffsetDp < -10
                            else -> settings.verticalOffsetDp > 10
                        }
                        FilterChip(
                            selected = selected,
                            onClick = { GameScreenSettingsManager.updateVerticalOffset(context, offsetVal) },
                            label = { Text(text = label) },
                        )
                    }
                }
            }
        }

        // Section 3: Aspect Ratio Mode
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.screen_layout_aspect_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (settings.stretch) {
                            stringResource(R.string.screen_layout_aspect_stretch)
                        } else {
                            stringResource(R.string.screen_layout_aspect_original)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Switch(
                    checked = settings.stretch,
                    onCheckedChange = { GameScreenSettingsManager.updateStretch(context, it) },
                )
            }
        }

        // Action: Reset
        OutlinedButton(
            onClick = {
                GameScreenSettingsManager.reset(context)
                Toast.makeText(
                    context,
                    context.getString(R.string.screen_layout_reset_success),
                    Toast.LENGTH_SHORT,
                ).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.screen_layout_reset))
        }

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.screen_layout_tips),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
