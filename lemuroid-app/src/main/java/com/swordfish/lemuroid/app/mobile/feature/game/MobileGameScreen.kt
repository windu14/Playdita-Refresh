package com.swordfish.lemuroid.app.mobile.feature.game

import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.ExpressiveMorphingLoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swordfish.lemuroid.app.shared.game.BackgroundSlot
import com.swordfish.lemuroid.app.shared.game.BackgroundThemeMode
import com.swordfish.lemuroid.app.shared.game.GameBackgroundThemeManager
import com.swordfish.lemuroid.app.shared.game.GameScreenSettingsManager
import java.io.File
import com.swordfish.lemuroid.app.utils.android.settings.booleanPreferenceState
import com.swordfish.lemuroid.app.shared.game.BaseGameScreenViewModel
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelTouchControls.Companion.MENU_LOADING_ANIMATION_MILLIS
import com.swordfish.lemuroid.app.shared.settings.HapticFeedbackMode
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.LemuroidPadTheme
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.sensors.TiltConfiguration
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.GlassSurface
import com.swordfish.touchinput.radial.ui.LemuroidButtonPressFeedback
import gg.padkit.PadKit
import gg.padkit.config.HapticFeedbackType
import gg.padkit.inputstate.InputState

@Composable
fun MobileGameScreen(viewModel: BaseGameScreenViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = constraints.maxWidth > constraints.maxHeight

        LaunchedEffect(isLandscape) {
            val orientation =
                if (isLandscape) {
                    TouchControllerSettingsManager.Orientation.LANDSCAPE
                } else {
                    TouchControllerSettingsManager.Orientation.PORTRAIT
                }
            viewModel.onScreenOrientationChanged(orientation)
        }

        val controllerConfigState = viewModel.getTouchControllerConfig().collectAsState(null)
        val touchControlsVisibleState = viewModel.isTouchControllerVisible().collectAsState(false)
        val touchControllerSettingsState =
            viewModel
                .getTouchControlsSettings(LocalDensity.current, WindowInsets.displayCutout)
                .collectAsState(null)

        val touchControllerSettings = touchControllerSettingsState.value
        val currentControllerConfig = controllerConfigState.value

        val tiltConfiguration = viewModel.getTiltConfiguration().collectAsState(TiltConfiguration.Disabled)
        val tiltSimulatedStates = viewModel.getSimulatedTiltEvents().collectAsState(InputState())
        val tiltSimulatedControls = remember { derivedStateOf { tiltConfiguration.value.controlIds() } }

        val touchGamePads = currentControllerConfig?.getTouchControllerConfig()
        val leftGamePad = touchGamePads?.leftComposable
        val rightGamePad = touchGamePads?.rightComposable

        val hapticFeedbackMode =
            viewModel
                .getTouchHapticFeedbackMode()
                .collectAsState(HapticFeedbackMode.NONE)

        val padHapticFeedback =
            when (hapticFeedbackMode.value) {
                HapticFeedbackMode.NONE -> HapticFeedbackType.NONE
                HapticFeedbackMode.PRESS -> HapticFeedbackType.PRESS
                HapticFeedbackMode.PRESS_RELEASE -> HapticFeedbackType.PRESS_RELEASE
            }

        val localContext = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current

        LaunchedEffect(Unit) {
            GameBackgroundThemeManager.init(localContext)
            GameScreenSettingsManager.init(localContext)
        }
        val backgroundUpdateKey by GameBackgroundThemeManager.backgroundUpdateFlow.collectAsState()
        val themeMode by GameBackgroundThemeManager.themeModeFlow.collectAsState()
        val screenSettings by GameScreenSettingsManager.screenSettingsFlow.collectAsState()

        val fullscreenBgFile = remember(backgroundUpdateKey, themeMode) {
            if (themeMode == BackgroundThemeMode.FULLSCREEN) {
                GameBackgroundThemeManager.getBackgroundFile(localContext, BackgroundSlot.FULLSCREEN)
            } else {
                null
            }
        }
        val topBgFile = remember(backgroundUpdateKey, themeMode) {
            if (themeMode == BackgroundThemeMode.SPLIT) {
                GameBackgroundThemeManager.getBackgroundFile(localContext, BackgroundSlot.TOP)
            } else {
                null
            }
        }
        val bottomBgFile = remember(backgroundUpdateKey, themeMode) {
            if (themeMode == BackgroundThemeMode.SPLIT) {
                GameBackgroundThemeManager.getBackgroundFile(localContext, BackgroundSlot.BOTTOM)
            } else {
                null
            }
        }

        val hasCustomBackground = (themeMode == BackgroundThemeMode.FULLSCREEN && fullscreenBgFile != null) ||
            (themeMode == BackgroundThemeMode.SPLIT && (topBgFile != null || bottomBgFile != null))

        val gameAspectRatio = remember(viewModel.system.id) {
            when (viewModel.system.id) {
                SystemID.GBA -> 3f / 2f
                SystemID.GB, SystemID.GBC, SystemID.GG -> 10f / 9f
                SystemID.PSP -> 16f / 9f
                SystemID.NDS -> 2f / 3f
                SystemID.LYNX -> 160f / 102f
                SystemID.NGP -> 20f / 19f
                SystemID.WS, SystemID.WSC -> 14f / 9f
                else -> 4f / 3f
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            if (themeMode == BackgroundThemeMode.FULLSCREEN && fullscreenBgFile != null && fullscreenBgFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(localContext)
                        .data(fullscreenBgFile)
                        .memoryCacheKey("game_bg_fullscreen_$backgroundUpdateKey")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                // Subtle dark overlay to ensure touch controls and retro game screen maintain great contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.22f))
                )
            }

            PadKit(
                modifier = Modifier.fillMaxSize(),
                onInputEvents = { viewModel.handleVirtualInputEvent(it) },
                hapticFeedbackType = padHapticFeedback,
                simulatedState = tiltSimulatedStates,
                simulatedControlIds = tiltSimulatedControls,
            ) {
                val fullScreenPosition = remember { mutableStateOf<Rect?>(null) }
                val viewportPosition = remember { mutableStateOf<Rect?>(null) }

                ConstraintLayout(
                    modifier = Modifier.fillMaxSize(),
                    constraintSet =
                        GameScreenLayout.buildConstraintSet(
                            isLandscape,
                            currentControllerConfig?.allowTouchOverlay ?: true,
                        ),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .layoutId(GameScreenLayout.CONSTRAINTS_GAME_VIEW)
                                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
                                .onGloballyPositioned { viewportPosition.value = it.boundsInRoot() },
                        contentAlignment = Alignment.Center,
                    ) {
                        // Render Top Background when SPLIT mode is active and topBgFile exists
                        if (themeMode == BackgroundThemeMode.SPLIT && topBgFile != null && topBgFile.exists()) {
                            AsyncImage(
                                model = ImageRequest.Builder(localContext)
                                    .data(topBgFile)
                                    .memoryCacheKey("game_bg_top_$backgroundUpdateKey")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.20f))
                            )
                        }

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = screenSettings.verticalOffsetDp.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            val containerAspect = maxWidth / maxHeight
                            val scale = screenSettings.scale.coerceIn(0.60f, 1.25f)
                            val sizeModifier = when {
                                screenSettings.stretch -> {
                                    Modifier.fillMaxSize(scale)
                                }
                                containerAspect > gameAspectRatio -> {
                                    Modifier
                                        .fillMaxHeight(scale)
                                        .aspectRatio(gameAspectRatio)
                                }
                                else -> {
                                    Modifier
                                        .fillMaxWidth(scale)
                                        .aspectRatio(gameAspectRatio)
                                }
                            }

                            AndroidView(
                                modifier = sizeModifier
                                    .onGloballyPositioned { fullScreenPosition.value = it.boundsInRoot() },
                                factory = {
                                    viewModel.createRetroView(localContext, lifecycle)
                                },
                            )
                        }
                    }

                    val isVisible =
                        touchControllerSettings != null &&
                            currentControllerConfig != null &&
                            touchControlsVisibleState.value

                    if (isVisible) {
                        CompositionLocalProvider(LocalLemuroidPadTheme provides LemuroidPadTheme()) {
                            if (!isLandscape) {
                                PadContainer(
                                    modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_BOTTOM_CONTAINER),
                                    hasFullscreenBackground = themeMode == BackgroundThemeMode.FULLSCREEN && fullscreenBgFile != null,
                                    bottomBackgroundFile = bottomBgFile,
                                    backgroundUpdateKey = backgroundUpdateKey,
                                )
                            } else if (!currentControllerConfig.allowTouchOverlay) {
                                PadContainer(
                                    modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_LEFT_CONTAINER),
                                    hasFullscreenBackground = themeMode == BackgroundThemeMode.FULLSCREEN && fullscreenBgFile != null,
                                    bottomBackgroundFile = bottomBgFile,
                                    backgroundUpdateKey = backgroundUpdateKey,
                                )
                                PadContainer(
                                    modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_CONTAINER),
                                    hasFullscreenBackground = themeMode == BackgroundThemeMode.FULLSCREEN && fullscreenBgFile != null,
                                    bottomBackgroundFile = bottomBgFile,
                                    backgroundUpdateKey = backgroundUpdateKey,
                                )
                            }

                            leftGamePad?.invoke(
                                this,
                                Modifier.layoutId(GameScreenLayout.CONSTRAINTS_LEFT_PAD),
                                touchControllerSettings,
                            )
                            rightGamePad?.invoke(
                                this,
                                Modifier.layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_PAD),
                                touchControllerSettings,
                            )

                            GameScreenRunningCentralMenu(
                                modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_GAME_CONTAINER),
                                controllerConfig = currentControllerConfig,
                                touchControllerSettings = touchControllerSettings,
                                viewModel = viewModel,
                            )
                        }
                    }
                }

                val fullPos = fullScreenPosition.value
                val viewPos = viewportPosition.value

                LaunchedEffect(fullPos, viewPos, hasCustomBackground, screenSettings) {
                    val gameView = viewModel.retroGameView.retroGameViewFlow()
                    if (fullPos == null || viewPos == null) return@LaunchedEffect
                    // With sizeModifier applying aspect ratio and scaling directly,
                    // viewport coordinates inside AndroidView map directly from (0,0) to (1,1)
                    val viewport = RectF(0f, 0f, 1f, 1f)
                    gameView.viewport = viewport
                }
            }
        }

        val isLoading =
            viewModel.loadingState
                .collectAsState(true)
                .value

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ExpressiveMorphingLoadingIndicator(
                    size = 48.dp,
                )
            }
        }
    }
}

@Composable
private fun PadContainer(
    modifier: Modifier = Modifier,
    hasFullscreenBackground: Boolean = false,
    bottomBackgroundFile: File? = null,
    backgroundUpdateKey: Long = 0L,
) {
    val theme = LocalLemuroidPadTheme.current
    val context = LocalContext.current

    Box(
        modifier = modifier.clip(
            RoundedCornerShape(
                topStart = theme.level0CornerRadius,
                topEnd = theme.level0CornerRadius,
            )
        )
    ) {
        if (bottomBackgroundFile != null && bottomBackgroundFile.exists()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(bottomBackgroundFile)
                    .memoryCacheKey("game_bg_bottom_$backgroundUpdateKey")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // Subtle dark overlay to ensure touch controls maintain great contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.20f))
            )
        }

        val hasAnyBg = hasFullscreenBackground || (bottomBackgroundFile != null && bottomBackgroundFile.exists())

        GlassSurface(
            modifier = Modifier.fillMaxSize(),
            cornerRadius = theme.level0CornerRadius,
            fillColor = if (hasAnyBg) Color.Black.copy(alpha = 0.18f) else theme.level0Fill,
            shadowColor = theme.level0Shadow,
            shadowWidth = theme.level0ShadowWidth,
        )
    }
}

@Composable
private fun GameScreenRunningCentralMenu(
    modifier: Modifier = Modifier,
    viewModel: BaseGameScreenViewModel,
    touchControllerSettings: TouchControllerSettingsManager.Settings,
    controllerConfig: ControllerConfig,
) {
    val menuPressed = viewModel.isMenuPressed().collectAsState(false)
    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.Center,
    ) {
        LemuroidButtonPressFeedback(
            pressed = menuPressed.value,
            animationDurationMillis = MENU_LOADING_ANIMATION_MILLIS,
            icon = R.drawable.button_menu,
        )
        MenuEditTouchControls(viewModel, controllerConfig, touchControllerSettings)
    }
}

@Composable
private fun MenuEditTouchControls(
    viewModel: BaseGameScreenViewModel,
    controllerConfig: ControllerConfig,
    touchControllerSettings: TouchControllerSettingsManager.Settings,
) {
    val showEditControls = viewModel.isEditControlShown().collectAsState(false)
    if (!showEditControls.value) return

    Dialog(onDismissRequest = { viewModel.showEditControls(false) }) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MenuEditTouchControlRow(Icons.Default.OpenInFull, "Scale", 0f) {
                    Slider(
                        value = touchControllerSettings.scale,
                        onValueChange = {
                            viewModel.updateTouchControllerSettings(
                                touchControllerSettings.copy(scale = it),
                            )
                        },
                    )
                }
                MenuEditTouchControlRow(Icons.Default.Height, "Horizontal Margin", 90f) {
                    Slider(
                        value = touchControllerSettings.marginX,
                        onValueChange = {
                            viewModel.updateTouchControllerSettings(
                                touchControllerSettings.copy(marginX = it),
                            )
                        },
                    )
                }
                MenuEditTouchControlRow(Icons.Default.Height, "Vertical Margin", 0f) {
                    Slider(
                        value = touchControllerSettings.marginY,
                        onValueChange = {
                            viewModel.updateTouchControllerSettings(
                                touchControllerSettings.copy(marginY = it),
                            )
                        },
                    )
                }
                if (controllerConfig.allowTouchRotation) {
                    MenuEditTouchControlRow(Icons.Default.RotateLeft, "Rotate", 0f) {
                        Slider(
                            value = touchControllerSettings.rotation,
                            onValueChange = {
                                viewModel.updateTouchControllerSettings(
                                    touchControllerSettings.copy(rotation = it),
                                )
                            },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { viewModel.resetTouchControls() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(text = stringResource(R.string.touch_customize_button_reset))
                    }
                    TextButton(
                        onClick = { viewModel.showEditControls(false) },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(text = stringResource(R.string.touch_customize_button_done))
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuEditTouchControlRow(
    icon: ImageVector,
    label: String,
    rotation: Float,
    slider: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.rotate(rotation),
            imageVector = icon,
            contentDescription = label,
        )
        slider()
    }
}
