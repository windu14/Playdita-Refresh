package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VideogameAssetOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R

/**
 * 2026 iOS-Inspired Frosted Glass Empty View.
 */
@Composable
fun LemuroidEmptyView(
    modifier: Modifier = Modifier,
    text: String = stringResource(id = R.string.empty_view_default),
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val cardShape = Glassmorphism.LargeCardShape
    val containerColor = Glassmorphism.containerColor(isDark, alpha = if (isDark) 0.88f else 0.92f)
    val borderBrush = Glassmorphism.borderBrush(isDark)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = cardShape,
                    spotColor = if (isDark) Color.Black.copy(alpha = 0.50f) else Color.Black.copy(alpha = 0.08f),
                )
                .border(1.dp, borderBrush, cardShape),
            shape = cardShape,
            color = containerColor,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Glassmorphism.specularOverlayBrush(isDark)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = (if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary).copy(alpha = 0.14f),
                        modifier = Modifier
                            .size(80.dp)
                            .border(
                                1.dp,
                                (if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary).copy(alpha = 0.35f),
                                CircleShape,
                            ),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.VideogameAssetOff,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = text,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            lineHeight = 24.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
