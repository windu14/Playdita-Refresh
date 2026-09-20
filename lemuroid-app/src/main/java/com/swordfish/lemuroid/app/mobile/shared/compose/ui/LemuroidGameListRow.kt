package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.lib.library.db.entity.Game

/**
 * 2026 iOS-Inspired Glassmorphic Game List Row.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LemuroidGameListRow(
    modifier: Modifier = Modifier,
    game: Game,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
) {
    val rowShape = RoundedCornerShape(20.dp)
    val haptics = rememberLemuroidHaptics()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val containerColor = Glassmorphism.containerColor(isDark, alpha = if (isDark) 0.88f else 0.92f)
    val borderBrush = Glassmorphism.borderBrush(isDark)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .shadow(
                elevation = 3.dp,
                shape = rowShape,
                spotColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.07f),
            )
            .border(1.dp, borderBrush, rowShape)
            .clip(rowShape)
            .combinedClickable(
                onClick = {
                    haptics.click()
                    onClick()
                },
                onLongClick = {
                    haptics.longPress()
                    onLongClick()
                },
            ),
        shape = rowShape,
        color = containerColor,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Glassmorphism.specularOverlayBrush(isDark)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp)),
                ) {
                    LemuroidSmallGameImage(
                        modifier = Modifier.size(48.dp),
                        game = game,
                    )
                }
                LemuroidGameTexts(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    game = game,
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center,
                ) {
                    FavoriteToggle(
                        isToggled = game.isFavorite,
                        onFavoriteToggle = onFavoriteToggle,
                    )
                }
            }
        }
    }
}
