package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.lib.library.db.entity.Game

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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp,
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
