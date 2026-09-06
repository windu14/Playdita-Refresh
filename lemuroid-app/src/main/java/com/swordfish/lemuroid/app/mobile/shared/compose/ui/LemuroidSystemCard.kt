package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo

import androidx.compose.ui.unit.dp

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

    val cardShape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
    val haptics = rememberLemuroidHaptics()

    ElevatedCard(
        modifier = modifier,
        onClick = {
            haptics.click()
            onClick()
        },
        shape = cardShape,
        elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp,
        ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(),
        ) {
            LemuroidSystemImage(system)
            LemuroidTexts(title = title, subtitle = subtitle)
        }
    }
}
