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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

@Composable
fun MainNavigationBar(
    currentRoute: MainRoute?,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val isVisible = currentRoute?.showBottomNavigation != false

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
                .padding(horizontal = 22.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            val isDark = isSystemInDarkTheme()
            val barShape = RoundedCornerShape(32.dp)

            // Solid container color (100% opaque, truly solid)
            val solidContainerColor = if (isDark) {
                Color(0xFF1A1B20)
            } else {
                Color(0xFFFFFFFF)
            }

            // Smooth inward depth effect bevel border
            val topHighlight = if (isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.90f)
            val bottomShadow = if (isDark) Color.Black.copy(alpha = 0.50f) else Color.Black.copy(alpha = 0.16f)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = barShape,
                        spotColor = if (isDark) Color.Black.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.22f),
                        ambientColor = if (isDark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.12f),
                    ),
                shape = barShape,
                color = solidContainerColor,
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(topHighlight, bottomShadow),
                    ),
                ),
            ) {
                // Subtle gradient overlay for smooth inward recessed depth
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    (if (isDark) Color.White else Color.Black).copy(alpha = 0.03f),
                                    Color.Transparent,
                                    (if (isDark) Color.Black else Color.Black).copy(alpha = 0.04f),
                                ),
                            ),
                        ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
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
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "TabScale",
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected && isCenter -> MaterialTheme.colorScheme.onPrimary
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "TabContentColor",
    )

    val tabShape = RoundedCornerShape(22.dp)

    // Smooth inset effect for active tab
    val tabModifier = if (isSelected) {
        if (isCenter) {
            Modifier
                .clip(tabShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                        ),
                    ),
                )
        } else {
            Modifier
                .clip(tabShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.90f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            (if (isDark) Color.Black else Color.Black).copy(alpha = 0.12f),
                            (if (isDark) Color.White else Color.White).copy(alpha = 0.15f),
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
            .padding(horizontal = if (isSelected) 15.dp else 11.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                contentDescription = stringResource(destination.titleId),
                tint = contentColor,
                modifier = Modifier.size(if (isCenter) 23.dp else 21.dp),
            )
            Text(
                text = stringResource(destination.titleId),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = (-0.1).sp,
                ),
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

