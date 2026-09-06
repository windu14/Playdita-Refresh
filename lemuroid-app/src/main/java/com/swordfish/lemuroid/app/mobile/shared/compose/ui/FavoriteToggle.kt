package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R

@Composable
fun FavoriteToggle(
    isToggled: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
) {
    val haptics = rememberLemuroidHaptics()

    val scale by animateFloatAsState(
        targetValue = if (isToggled) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "favScale",
    )

    val tint by animateColorAsState(
        targetValue = if (isToggled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        },
        label = "favTint",
    )

    IconToggleButton(
        checked = isToggled,
        onCheckedChange = {
            haptics.toggle()
            onFavoriteToggle(it)
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        val image = if (isToggled) {
            Icons.Filled.Favorite
        } else {
            Icons.Outlined.FavoriteBorder
        }
        Icon(
            imageVector = image,
            contentDescription = stringResource(R.string.favorites),
            tint = tint,
            modifier = Modifier
                .size(24.dp)
                .scale(scale),
        )
    }
}
