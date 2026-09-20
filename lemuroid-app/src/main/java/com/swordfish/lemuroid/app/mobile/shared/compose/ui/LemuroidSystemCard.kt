package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo

/**
 * 2026 iOS-Style Glassmorphic System Card.
 */
@Composable
fun LemuroidSystemCard(
    modifier: Modifier = Modifier,
    system: MetaSystemInfo,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    val title =
        remember(system.metaSystem.titleResId) {
            system.getName(context)
        }

    val subtitle =
        remember(system.metaSystem.titleResId) {
            context.getString(
                R.string.system_grid_details,
                system.count.toString(),
            )
        }

    val cardShape = Glassmorphism.CardShape
    val haptics = rememberLemuroidHaptics()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val containerColor = Glassmorphism.containerColor(isDark, alpha = if (isDark) 0.88f else 0.92f)
    val borderBrush = Glassmorphism.borderBrush(isDark)

    ElevatedCard(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = cardShape,
                spotColor = if (isDark) Color.Black.copy(alpha = 0.50f) else Color.Black.copy(alpha = 0.08f),
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.04f),
            )
            .border(1.dp, borderBrush, cardShape),
        onClick = {
            haptics.click()
            onClick()
        },
        shape = cardShape,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 6.dp,
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
        ),
    ) {
        Box {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                LemuroidSystemImage(system)
                LemuroidTexts(title = title, subtitle = subtitle)
            }
        }
    }
}
