package com.swordfish.lemuroid.app.mobile.feature.main

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.ContainedLoadingIndicator
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.ExperimentalMaterial3ExpressiveApi
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.ExpressiveLinearProgressIndicator
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.Glassmorphism
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainTopBar(
    currentRoute: MainRoute,
    navController: NavHostController,
    onUpdateQueryString: (String) -> Unit,
    mainUIState: MainViewModel.UiState,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
    ) {
        LemuroidTopAppBar(
            route = currentRoute,
            navController = navController,
            mainUIState = mainUIState,
            onUpdateQueryString = onUpdateQueryString,
        )

        AnimatedVisibility(mainUIState.operationInProgress) {
            ExpressiveLinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LemuroidTopAppBar(
    route: MainRoute,
    navController: NavController,
    mainUIState: MainViewModel.UiState,
    onUpdateQueryString: (String) -> Unit,
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    TopAppBar(
        title = {
            if (route == MainRoute.HOME) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Playdita",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 25.sp,
                            letterSpacing = (-0.6).sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            } else {
                Text(
                    text = stringResource(route.titleId),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Glassmorphism.containerColor(isDark, alpha = 0.92f),
        ),
        navigationIcon = {
            AnimatedVisibility(
                visible = route.parent != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(40.dp)
                        .shadow(3.dp, CircleShape, spotColor = if (isDark) Color.Black.copy(0.4f) else Color.Black.copy(0.06f))
                        .clip(CircleShape)
                        .background(Glassmorphism.elevatedContainerColor(isDark, alpha = 0.85f))
                        .border(1.dp, Glassmorphism.borderBrush(isDark), CircleShape),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        actions = {
            LemuroidTopBarActions(
                route = route,
                navController = navController,
                context = context,
                saveSyncEnabled = mainUIState.saveSyncEnabled,
                operationsInProgress = mainUIState.operationInProgress,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LemuroidTopBarActions(
    route: MainRoute,
    navController: NavController,
    context: Context,
    saveSyncEnabled: Boolean,
    operationsInProgress: Boolean,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(end = 12.dp),
    ) {
        if (operationsInProgress) {
            ContainedLoadingIndicator(
                modifier = Modifier.size(36.dp),
            )
        }
        if (saveSyncEnabled) {
            IconButton(
                onClick = { SaveSyncWork.enqueueManualWork(context.applicationContext) },
                enabled = !operationsInProgress,
                modifier = Modifier
                    .size(40.dp)
                    .shadow(3.dp, CircleShape, spotColor = if (isDark) Color.Black.copy(0.4f) else Color.Black.copy(0.06f))
                    .clip(CircleShape)
                    .background(Glassmorphism.elevatedContainerColor(isDark, alpha = 0.85f))
                    .border(1.dp, Glassmorphism.borderBrush(isDark), CircleShape),
            ) {
                Icon(
                    Icons.Outlined.CloudSync,
                    contentDescription = stringResource(R.string.save_sync),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        if (route.showBottomNavigation) {
            IconButton(
                onClick = { navController.navigate(MainRoute.SETTINGS.route) },
                modifier = Modifier
                    .size(40.dp)
                    .shadow(3.dp, CircleShape, spotColor = if (isDark) Color.Black.copy(0.4f) else Color.Black.copy(0.06f))
                    .clip(CircleShape)
                    .background(Glassmorphism.elevatedContainerColor(isDark, alpha = 0.85f))
                    .border(1.dp, Glassmorphism.borderBrush(isDark), CircleShape),
            ) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun LemuroidSearchView(
    mainUIState: MainViewModel.UiState,
    onUpdateQueryString: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(4.dp, CircleShape, spotColor = if (isDark) Color.Black.copy(0.4f) else Color.Black.copy(0.08f))
                .border(1.dp, Glassmorphism.borderBrush(isDark), CircleShape),
            shape = CircleShape,
            color = Glassmorphism.containerColor(isDark, alpha = 0.88f),
        ) {
            TextField(
                value = mainUIState.searchQuery,
                onValueChange = { onUpdateQueryString(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyMedium,
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary,
                    )
                },
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus(true) },
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
        }
    }
}
