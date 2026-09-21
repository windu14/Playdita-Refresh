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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import kotlin.math.absoluteValue
import androidx.lifecycle.Lifecycle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.ContainedLoadingIndicator
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.ExperimentalMaterial3ExpressiveApi
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.Glassmorphism
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameCard
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
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
    var selectedFilter by rememberSaveable { mutableStateOf(HomeFilter.ALL) }

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

    val displayedAllGames = remember(selectedFilter, state.allGames, state.recentGames) {
        when (selectedFilter) {
            HomeFilter.ALL -> state.allGames
            HomeFilter.FAVORITES -> state.allGames.filter { it.isFavorite }
            HomeFilter.RECENT -> state.recentGames
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

            // 2026 Interactive Filter Pills (Semua, Favorit, Terbaru)
            HomeFilterPills(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                allCount = state.allGames.size,
                favoritesCount = state.allGames.count { it.isFavorite },
                recentCount = state.recentGames.size,
            )

            // Material 3 Expressive Loading Indicator (Sinkronisasi / Memindai Game)
            AnimatedVisibility(visible = state.indexInProgress) {
                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                val bannerShape = Glassmorphism.CardShape
                val containerColor = Glassmorphism.containerColor(isDark, alpha = 0.90f)
                val borderBrush = Glassmorphism.borderBrush(isDark)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = bannerShape,
                            spotColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.08f),
                        )
                        .border(1.dp, borderBrush, bannerShape),
                    shape = bannerShape,
                    color = containerColor,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Glassmorphism.specularOverlayBrush(isDark)),
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
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = stringResource(id = R.string.home_indexing_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
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

                // Section 1: 2026 Immersive Hero Banner (Terbaru / Quick Resume)
                val recents = remember(state.recentGames) { state.recentGames.take(5) }
                if (recents.isNotEmpty()) {
                    HomeHeroBanner(
                        title = stringResource(id = R.string.home_recent_games),
                        games = recents,
                        onGameClicked = onGameClicked,
                        onGameLongClick = onGameLongClick,
                    )
                }

                // Section 2: Game Grid (Filtered or Full Game) - Precision Grid Layout
                val gridTitle = when (selectedFilter) {
                    HomeFilter.ALL -> stringResource(id = R.string.home_all_games)
                    HomeFilter.FAVORITES -> stringResource(id = R.string.favorites)
                    HomeFilter.RECENT -> stringResource(id = R.string.recent)
                }

                if (displayedAllGames.isNotEmpty()) {
                    HomeAllGamesGrid(
                        title = gridTitle,
                        games = displayedAllGames,
                        onGameClicked = onGameClicked,
                        onGameLongClick = onGameLongClick,
                    )
                } else if (state.allGames.isNotEmpty()) {
                    // Empty state for current filter
                    HomeEmptyFilterView(
                        filter = selectedFilter,
                        onResetFilter = { selectedFilter = HomeFilter.ALL },
                    )
                }
            }
        }
    }
}

enum class HomeFilter {
    ALL,
    FAVORITES,
    RECENT,
}

@Composable
private fun HomeFilterPills(
    selectedFilter: HomeFilter,
    onFilterSelected: (HomeFilter) -> Unit,
    allCount: Int,
    favoritesCount: Int,
    recentCount: Int,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberLemuroidHaptics()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val filters = listOf(
            Triple(HomeFilter.ALL, stringResource(id = R.string.home_filter_all), allCount),
            Triple(HomeFilter.FAVORITES, stringResource(id = R.string.favorites), favoritesCount),
            Triple(HomeFilter.RECENT, stringResource(id = R.string.recent), recentCount),
        )

        filters.forEach { (filter, label, count) ->
            val isSelected = selectedFilter == filter
            val pillShape = RoundedCornerShape(20.dp)

            val pillBrush = if (isSelected) {
                if (isDark) {
                    Brush.horizontalGradient(
                        listOf(
                            Glassmorphism.AuroraGlowStart,
                            Glassmorphism.AuroraGlowEnd,
                        ),
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            } else {
                Brush.linearGradient(
                    listOf(
                        if (isDark) Color(0xFF1B2030) else Color(0xFFEFF2F8),
                        if (isDark) Color(0xFF141824) else Color(0xFFE4E9F2),
                    ),
                )
            }

            Surface(
                modifier = Modifier
                    .shadow(
                        elevation = if (isSelected) 6.dp else 1.dp,
                        shape = pillShape,
                        spotColor = if (isSelected) {
                            if (isDark) Color(0xFF00E5FF).copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        } else {
                            Color.Transparent
                        },
                    )
                    .border(
                        width = 1.dp,
                        brush = if (isSelected) {
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.1f),
                                ),
                            )
                        } else {
                            Glassmorphism.borderBrush(isDark)
                        },
                        shape = pillShape,
                    )
                    .clip(pillShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = MaterialTheme.colorScheme.primary),
                        onClick = {
                            haptics.click()
                            onFilterSelected(filter)
                        },
                    ),
                shape = pillShape,
                color = Color.Transparent,
            ) {
                Box(
                    modifier = Modifier
                        .background(pillBrush)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        when (filter) {
                            HomeFilter.ALL -> Icon(
                                Icons.Outlined.SportsEsports,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp),
                            )
                            HomeFilter.FAVORITES -> Icon(
                                if (isSelected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp),
                            )
                            HomeFilter.RECENT -> Icon(
                                Icons.Outlined.History,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.5.sp,
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        )
                        if (count > 0) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                    ),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2026 Immersive Hero Banner with 16:9 cinematic card, ambient background blur glow,
 * Quick Resume chip, and play action.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeHeroBanner(
    title: String,
    games: List<Game>,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
) {
    if (games.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { games.size })
    val haptics = rememberLemuroidHaptics()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        haptics.tick()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Section Header
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

        // 16:9 Cinematic Hero Carousel with ambient glow & Quick Resume chip
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp,
        ) { page ->
            val game = games[page]
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
            val absOffset = pageOffset.absoluteValue.coerceIn(0f, 2.0f)
            val isCurrent = pagerState.currentPage == page

            val scale = (1.0f - (absOffset * 0.06f)).coerceIn(0.92f, 1.0f)
            val alpha = (1.0f - (absOffset * 0.35f)).coerceIn(0.65f, 1.0f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.scaleX = scale
                        this.scaleY = scale
                        this.alpha = alpha
                    },
                contentAlignment = Alignment.Center,
            ) {
                HeroGameCard(
                    game = game,
                    isDark = isDark,
                    isCurrent = isCurrent,
                    onClick = {
                        haptics.click()
                        onGameClicked(game)
                    },
                    onLongClick = {
                        haptics.longPress()
                        onGameLongClick(game)
                    },
                )
            }
        }

        // Carousel Indicator Dots
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
                    val dotWidth = if (isCurrent) 22.dp else 6.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(
                                if (isCurrent) {
                                    if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroGameCard(
    game: Game,
    isDark: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val heroShape = RoundedCornerShape(26.dp)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .shadow(
                elevation = if (isCurrent) 16.dp else 4.dp,
                shape = heroShape,
                spotColor = if (isCurrent) {
                    if (isDark) Color(0xFF00E5FF).copy(alpha = 0.45f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                } else {
                    if (isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.08f)
                },
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.04f),
            )
            .border(
                width = if (isCurrent) 1.5.dp else 1.dp,
                brush = if (isCurrent) {
                    if (isDark) Glassmorphism.activeBorderBrush(true) else Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                    )
                } else {
                    Glassmorphism.borderBrush(isDark)
                },
                shape = heroShape,
            ),
        shape = heroShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Glassmorphism.containerColor(isDark, alpha = 0.94f),
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(heroShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = MaterialTheme.colorScheme.primary),
                    onClick = onClick,
                ),
        ) {
            // Background Artwork with ambient dark vignette
            LemuroidGameImage(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = if (isDark) 0.35f else 0.22f
                    },
                game = game,
            )

            // Aurora glow ambient gradient layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                if (isDark) Color(0x990A0D14) else Color(0x99F0F4F8),
                                if (isDark) Color(0xF00A0D14) else Color(0xF0FFFFFF),
                            ),
                        ),
                    ),
            )

            // Content Overlay
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Large Cover Art Preview Thumbnail
                Surface(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(8.dp, RoundedCornerShape(18.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) Color(0xFF151926) else Color(0xFFE2E7F0),
                ) {
                    LemuroidGameImage(
                        modifier = Modifier.fillMaxSize(),
                        game = game,
                    )
                }

                // Info Column: Title, Quick Resume Chip, Jump In Action
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Quick Resume Chip
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.16f) else MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color(0xFF00E5FF).copy(alpha = 0.35f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary),
                            )
                            Text(
                                text = stringResource(id = R.string.home_quick_resume),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                ),
                                color = if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    // Game Title
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = (-0.2).sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    // Quick Jump In Action Button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Glassmorphism.AuroraGlowStart else MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onClick),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(id = R.string.home_hero_jump_in),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                ),
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeEmptyFilterView(
    filter: HomeFilter,
    onResetFilter: () -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .border(1.dp, Glassmorphism.borderBrush(isDark), Glassmorphism.CardShape),
        shape = Glassmorphism.CardShape,
        color = Glassmorphism.containerColor(isDark, alpha = 0.85f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (filter == HomeFilter.FAVORITES) Icons.Outlined.FavoriteBorder else Icons.Outlined.History,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            Text(
                text = when (filter) {
                    HomeFilter.FAVORITES -> stringResource(id = R.string.home_no_favorites_filtered)
                    else -> stringResource(id = R.string.home_no_recent_filtered)
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilledTonalButton(
                onClick = onResetFilter,
                shape = CircleShape,
            ) {
                Text(text = stringResource(id = R.string.home_filter_all))
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
    val cardShape = Glassmorphism.LargeCardShape
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val containerColor = Glassmorphism.containerColor(isDark, alpha = if (isDark) 0.88f else 0.92f)
    val borderBrush = Glassmorphism.borderBrush(isDark)

    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = cardShape,
                    spotColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.08f),
                )
                .border(
                    width = 1.dp,
                    brush = borderBrush,
                    shape = cardShape,
                ),
        shape = cardShape,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 4.dp,
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Glassmorphism.specularOverlayBrush(isDark)),
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
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    spotColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.08f),
                )
                .border(
                    width = 1.dp,
                    brush = Glassmorphism.borderBrush(isDark),
                    shape = CircleShape,
                ),
            shape = CircleShape,
            color = Glassmorphism.containerColor(isDark, alpha = if (isDark) 0.88f else 0.92f),
            tonalElevation = 0.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Glassmorphism.specularOverlayBrush(isDark)),
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
                            tint = if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary,
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
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                modifier = Modifier.padding(end = 8.dp),
                            ) {
                                Text(
                                    text = "$totalGamesCount",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                    ),
                                    color = if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary,
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
            // Empty search result frosted glass card
            val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            val cardShape = Glassmorphism.LargeCardShape
            val containerColor = Glassmorphism.containerColor(isDark, alpha = if (isDark) 0.88f else 0.92f)
            val borderBrush = Glassmorphism.borderBrush(isDark)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = cardShape,
                        spotColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.08f),
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
                            .padding(vertical = 36.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary,
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
}

