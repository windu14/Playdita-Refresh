package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun LemuroidGameCard(
    modifier: Modifier = Modifier,
    game: Game,
    isSelected: Boolean = false,
    onClick: () -> Unit = { },
    onLongClick: () -> Unit = { },
) {
    val cardShape = RoundedCornerShape(22.dp)
    val haptics = rememberLemuroidHaptics()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // 2026 iOS Frosted Glass & Specular highlight
    val switchCyan = Color(0xFF00E5FF)
    val topHighlight = if (isDark) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.90f)
    val bottomBorder = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.08f)
    val unselectedBrush = remember(isDark) { Brush.verticalGradient(listOf(topHighlight, bottomBorder)) }
    val selectedBrush = remember { Brush.verticalGradient(listOf(switchCyan, switchCyan.copy(alpha = 0.8f))) }

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.5.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "BorderWidth",
    )
    val cardElevation by animateDpAsState(
        targetValue = if (isSelected) 10.dp else 3.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "Elevation",
    )

    val containerColor = if (isDark) Color(0xEB181B26) else Color(0xF5FFFFFF)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = cardShape,
                        spotColor = switchCyan.copy(alpha = 0.60f),
                        ambientColor = switchCyan.copy(alpha = 0.25f),
                    )
                } else {
                    Modifier.shadow(
                        elevation = 3.dp,
                        shape = cardShape,
                        spotColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.08f),
                    )
                }
            )
            .border(
                width = borderWidth,
                brush = if (isSelected) selectedBrush else unselectedBrush,
                shape = cardShape,
            ),
        shape = cardShape,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 4.dp,
            focusedElevation = 4.dp,
            hoveredElevation = 4.dp,
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = MaterialTheme.colorScheme.primary),
                    onClick = {
                        haptics.click()
                        onClick()
                    },
                    onLongClick = {
                        haptics.longPress()
                        onLongClick()
                    },
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.0f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color(0xFF141518)),
            ) {
                LemuroidGameImage(
                    modifier = Modifier.fillMaxSize(),
                    game = game,
                )

                // Subtle bottom gradient on the image to blend smoothly into card body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.18f),
                                ),
                            ),
                        ),
                )

                if (game.isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
            }

            LemuroidGameTexts(
                modifier = Modifier.fillMaxWidth(),
                game = game,
            )
        }
    }
}

