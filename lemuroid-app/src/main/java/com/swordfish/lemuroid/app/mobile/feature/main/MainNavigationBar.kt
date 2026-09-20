package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.Glassmorphism
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.rememberLemuroidHaptics

/**
 * 2026 iOS-Style Floating Island Navigation Dock.
 * Built with seamless frosted glass acrylic, multi-stop specular highlight hairline border,
 * tactile spring animations, and refined typography.
 */
@Composable
fun MainNavigationBar(
    currentRoute: MainRoute?,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val isVisible = currentRoute?.showBottomNavigation != false
    val haptics = rememberLemuroidHaptics()

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + expandVertically(),
        exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + shrinkVertically(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            val islandShape = Glassmorphism.IslandShape

            // 2026 iOS Frosted Glass Surface
            val containerColor = Glassmorphism.islandContainerColor(isDark)
            val borderBrush = Glassmorphism.borderBrush(isDark)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = islandShape,
                        spotColor = if (isDark) Color.Black.copy(alpha = 0.70f) else Color.Black.copy(alpha = 0.14f),
                        ambientColor = if (isDark) Color.Black.copy(alpha = 0.40f) else Color.Black.copy(alpha = 0.08f),
                    ),
                shape = islandShape,
                color = containerColor,
                border = BorderStroke(
                    width = 1.dp,
                    brush = borderBrush,
                ),
            ) {
                // Frosted refractive gradient overlay for luminous depth
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Glassmorphism.specularOverlayBrush(isDark)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MainNavigationRoutes.values().forEach { destination ->
                            val isSelected = currentRoute?.root == destination.route
                            val isCenter = destination.isCenterAction

                            ExpressiveNavTab(
                                destination = destination,
                                isSelected = isSelected,
                                isCenter = isCenter,
                                isDark = isDark,
                                onClick = {
                                    haptics.click()
                                    navController.navigate(destination.route.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveNavTab(
    destination: MainNavigationRoutes,
    isSelected: Boolean,
    isCenter: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "TabScale",
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected && isCenter -> Color.White
            isSelected -> if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "TabContentColor",
    )

    val tabShape = RoundedCornerShape(24.dp)

    // Smooth glass inset pill for active tab
    val tabModifier = if (isSelected) {
        if (isCenter) {
            Modifier
                .clip(tabShape)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF00C6FF),
                            Color(0xFF0072FF),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.60f),
                            Color.White.copy(alpha = 0.10f),
                        ),
                    ),
                    shape = tabShape,
                )
        } else {
            Modifier
                .clip(tabShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            (if (isDark) Color(0xFF232A3E) else Color(0xFFE8EEF8)).copy(alpha = 0.92f),
                            (if (isDark) Color(0xFF1B2032) else Color(0xFFDFE6F2)).copy(alpha = 0.75f),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            (if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.85f)),
                            (if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.06f)),
                        ),
                    ),
                    shape = tabShape,
                )
        }
    } else {
        Modifier.clip(tabShape)
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .then(tabModifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                onClick = onClick,
            )
            .padding(horizontal = if (isSelected) 16.dp else 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val iconVec = if (isSelected) destination.selectedIcon else destination.unselectedIcon
            val iconRes = if (isSelected) destination.selectedDrawableRes else destination.unselectedDrawableRes ?: destination.selectedDrawableRes
            if (iconVec != null) {
                Icon(
                    imageVector = iconVec,
                    contentDescription = stringResource(destination.titleId),
                    tint = contentColor,
                    modifier = Modifier.size(if (isCenter) 23.dp else 21.dp),
                )
            } else if (iconRes != null) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = stringResource(destination.titleId),
                    tint = contentColor,
                    modifier = Modifier.size(if (isCenter) 23.dp else 21.dp),
                )
            }
            Text(
                text = stringResource(destination.titleId),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = (-0.2).sp,
                ),
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}
