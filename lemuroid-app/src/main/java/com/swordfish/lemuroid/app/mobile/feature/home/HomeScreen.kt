package com.swordfish.lemuroid.app.mobile.feature.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import kotlin.math.absoluteValue
import androidx.lifecycle.Lifecycle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.ContainedLoadingIndicator
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.ExperimentalMaterial3ExpressiveApi
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameCard
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.ScallopBadge
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.rememberLemuroidHaptics
import com.swordfish.lemuroid.app.utils.android.ComposableLifecycle
import com.swordfish.lemuroid.common.displayDetailsSettingsScreen
import com.swordfish.lemuroid.lib.library.db.entity.Game

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onOpenCoreSelection: () -> Unit,
) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                viewModel.updatePermissions(applicationContext)
            }
            else -> { }
        }
    }

    val permissionsLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (!isGranted) {
                context.displayDetailsSettingsScreen()
            }
        }

    val state = viewModel.getViewStates().collectAsState(HomeViewModel.UIState())
    HomeScreen(
        modifier,
        state.value,
        onRefresh = { viewModel.refreshLibrary(context) },
        onGameClicked = onGameClick,
        onGameLongClick = onGameLongClick,
        onOpenCoreSelection = onOpenCoreSelection,
        onEnableNotificationsClicked = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                return@HomeScreen
            }

            permissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        },
        onEnableMicrophoneClicked = { permissionsLauncher.launch(Manifest.permission.RECORD_AUDIO) },
        onSetDirectoryClicked = { viewModel.changeLocalStorageFolder(context) },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeViewModel.UIState,
    onRefresh: () -> Unit,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onOpenCoreSelection: () -> Unit,
    onEnableNotificationsClicked: () -> Unit,
    onEnableMicrophoneClicked: () -> Unit,
    onSetDirectoryClicked: () -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredGames = remember(searchQuery, state.allGames) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase()
            state.allGames.filter { game ->
                game.title.lowercase().contains(q) || game.fileName.lowercase().contains(q)
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = state.indexInProgress,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Material 3 Expressive Search Component (Paling Atas Beranda)
            HomeExpressiveSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = { searchQuery = "" },
                totalGamesCount = state.allGames.size,
            )

            // Material 3 Expressive Loading Indicator (Sinkronisasi / Memindai Game)
            AnimatedVisibility(visible = state.indexInProgress) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        ContainedLoadingIndicator(
                            modifier = Modifier.size(42.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.home_indexing_title),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = stringResource(id = R.string.home_indexing_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            )
                        }
                    }
                }
            }

            if (searchQuery.isNotBlank()) {
                // Tampilan Hasil Pencarian Material 3 Expressive
                HomeSearchResultsView(
                    query = searchQuery,
                    games = filteredGames,
                    onGameClicked = onGameClicked,
                    onGameLongClick = onGameLongClick,
                    onClearSearch = { searchQuery = "" },
                )
            } else {
                // Tampilan Konten Reguler Beranda
                // Notification / Permission cards
                AnimatedVisibility(state.showNoNotificationPermissionCard) {
                    HomeNotification(
                        titleId = R.string.home_notification_title,
                        messageId = R.string.home_notification_message,
                        actionId = R.string.home_notification_action,
                        onAction = onEnableNotificationsClicked,
                    )
                }
                AnimatedVisibility(state.showNoGamesCard) {
                    HomeNotification(
                        titleId = R.string.home_empty_title,
                        messageId = R.string.home_empty_message,
                        actionId = R.string.home_empty_action,
                        onAction = onSetDirectoryClicked,
                        enabled = !state.indexInProgress,
                    )
                }
                AnimatedVisibility(state.showNoMicrophonePermissionCard) {
                    HomeNotification(
                        titleId = R.string.home_microphone_title,
                        messageId = R.string.home_microphone_message,
                        actionId = R.string.home_microphone_action,
                        onAction = onEnableMicrophoneClicked,
                    )
                }
                AnimatedVisibility(state.showDesmumeDeprecatedCard) {
                    HomeNotification(
                        titleId = R.string.home_notification_desmume_deprecated_title,
                        messageId = R.string.home_notification_desmume_deprecated_message,
                        actionId = R.string.home_notification_desmume_deprecated_action,
                        onAction = onOpenCoreSelection,
                    )
                }

                // Global Centered Loading Indicator saat inisialisasi / memindai pertama kali
                if (state.allGames.isEmpty() && state.indexInProgress) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            ContainedLoadingIndicator(
                                modifier = Modifier.size(56.dp),
                            )
                            Text(
                                text = stringResource(id = R.string.home_indexing_desc),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Section 1: Terbaru / Terakhir Dimainkan (Carousel Max 5 Cards)
                val recents = remember(state.recentGames) { state.recentGames.take(5) }
                if (recents.isNotEmpty()) {
                    HomeRecentCarousel(
                        title = stringResource(id = R.string.home_recent_games),
                        games = recents,
                        onGameClicked = onGameClicked,
                        onGameLongClick = onGameLongClick,
                    )
                }

                // Section 2: Full Game (Semua Game) - Precision Grid Layout
                if (state.allGames.isNotEmpty()) {
                    HomeAllGamesGrid(
                        title = stringResource(id = R.string.home_all_games),
                        games = state.allGames,
                        onGameClicked = onGameClicked,
                        onGameLongClick = onGameLongClick,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeRecentCarousel(
    title: String,
    games: List<Game>,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
) {
    if (games.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { games.size })
    val haptics = rememberLemuroidHaptics()

    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        haptics.tick()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    letterSpacing = (-0.2).sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = "${games.size}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }

        // Nintendo Switch style Carousel: clean gap, bouncy scale, and glowing active border
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(288.dp),
            contentPadding = PaddingValues(horizontal = 72.dp),
            pageSpacing = 16.dp,
        ) { page ->
            val game = games[page]
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
            val absOffset = pageOffset.absoluteValue.coerceIn(0f, 2.0f)
            val isCurrent = pagerState.currentPage == page

            // Bouncy scale & subtle fade for non-active cards
            val scale = (1.04f - (absOffset * 0.12f)).coerceIn(0.88f, 1.04f)
            val alpha = (1.0f - (absOffset * 0.30f)).coerceIn(0.68f, 1.0f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(if (isCurrent) 10f else 1f - absOffset)
                    .graphicsLayer {
                        this.scaleX = scale
                        this.scaleY = scale
                        this.alpha = alpha
                    },
                contentAlignment = Alignment.Center,
            ) {
                LemuroidGameCard(
                    modifier = Modifier.width(196.dp),
                    game = game,
                    isSelected = isCurrent,
                    onClick = { onGameClicked(game) },
                    onLongClick = { onGameLongClick(game) },
                )
            }
        }

        if (games.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(minOf(games.size, 6)) { index ->
                    val isCurrent = pagerState.currentPage == index
                    val dotWidth = if (isCurrent) 18.dp else 6.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(
                                if (isCurrent) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeAllGamesGrid(
    title: String,
    games: List<Game>,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.SportsEsports,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    letterSpacing = (-0.2).sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = "${games.size}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }

        // Spacious 2-column Grid with large rounded cards
        val columns = 2
        val chunkedGames = remember(games) { games.chunked(columns) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            chunkedGames.forEach { rowGames ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    for (game in rowGames) {
                        Box(modifier = Modifier.weight(1f)) {
                            LemuroidGameCard(
                                game = game,
                                onClick = { onGameClicked(game) },
                                onLongClick = { onGameLongClick(game) },
                            )
                        }
                    }
                    repeat(columns - rowGames.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeNotification(
    titleId: Int,
    messageId: Int,
    actionId: Int,
    enabled: Boolean = true,
    onAction: () -> Unit = { },
) {
    val cardShape = RoundedCornerShape(24.dp)
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = cardShape,
                ),
        shape = cardShape,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(titleId),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(messageId),
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilledTonalButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onAction,
                enabled = enabled,
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Text(
                    text = stringResource(id = actionId),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

/**
 * Material 3 Expressive Search Component positioned at the very top of the Home feed.
 */
@Composable
private fun HomeExpressiveSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    totalGamesCount: Int,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val haptics = rememberLemuroidHaptics()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            ),
            tonalElevation = 2.dp,
            shadowElevation = 1.dp,
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.home_search_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = stringResource(id = R.string.title_search),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                haptics.tick()
                                onClear()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = stringResource(id = R.string.home_search_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else if (totalGamesCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.padding(end = 6.dp),
                        ) {
                            Text(
                                text = "$totalGamesCount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus(true) },
                    onSearch = { focusManager.clearFocus(true) },
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

/**
 * Material 3 Expressive Search Results section displaying filtered games or an empty state.
 */
@Composable
private fun HomeSearchResultsView(
    query: String,
    games: List<Game>,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onClearSearch: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = stringResource(id = R.string.home_search_results_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            ) {
                Text(
                    text = stringResource(id = R.string.home_search_count_format, games.size),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        if (games.isNotEmpty()) {
            val rows = (games.size + 1) / 2
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                for (rowIndex in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val leftIndex = rowIndex * 2
                        val rightIndex = leftIndex + 1

                        val leftGame = games[leftIndex]
                        Box(modifier = Modifier.weight(1f)) {
                            LemuroidGameCard(
                                game = leftGame,
                                onClick = { onGameClicked(leftGame) },
                                onLongClick = { onGameLongClick(leftGame) },
                            )
                        }

                        if (rightIndex < games.size) {
                            val rightGame = games[rightIndex]
                            Box(modifier = Modifier.weight(1f)) {
                                LemuroidGameCard(
                                    game = rightGame,
                                    onClick = { onGameClicked(rightGame) },
                                    onLongClick = { onGameLongClick(rightGame) },
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            // Empty search result card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = stringResource(id = R.string.home_search_no_results_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(id = R.string.home_search_no_results_desc, query),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FilledTonalButton(
                        onClick = onClearSearch,
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    ) {
                        Text(text = stringResource(id = R.string.home_search_clear))
                    }
                }
            }
        }
    }
}

