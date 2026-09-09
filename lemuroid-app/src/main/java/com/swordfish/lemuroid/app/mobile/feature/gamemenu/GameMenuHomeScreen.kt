package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.alorma.compose.settings.storage.memory.rememberMemoryBooleanSettingState
import com.alorma.compose.settings.storage.memory.rememberMemoryIntSettingState
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.tilt.TiltConfigurationMenuEntry
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.game.BackgroundThemeMode
import com.swordfish.lemuroid.app.shared.game.GameBackgroundThemeManager
import com.swordfish.lemuroid.app.shared.game.GameScreenSettingsManager
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsList
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSwitch
import kotlin.reflect.KFunction1

@Composable
fun GameMenuHomeScreen(
    navController: NavController,
    gameMenuRequest: GameMenuActivity.GameMenuRequest,
    onResult: KFunction1<Intent.() -> Unit, Unit>,
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        if (gameMenuRequest.coreConfig.statesSupported) {
            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.game_menu_save)) },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_save),
                        contentDescription = stringResource(id = R.string.game_menu_save),
                    )
                },
                onClick = { navController.navigateToRoute(GameMenuRoute.SAVE) },
            )

            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.game_menu_load)) },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_load),
                        contentDescription = stringResource(id = R.string.game_menu_load),
                    )
                },
                onClick = { navController.navigateToRoute(GameMenuRoute.LOAD) },
            )
        }

        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_cheats)) },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_cheat),
                    contentDescription = stringResource(id = R.string.game_menu_cheats),
                )
            },
            onClick = { navController.navigateToRoute(GameMenuRoute.CHEATS) },
        )

        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_quit)) },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_quit),
                    contentDescription = stringResource(id = R.string.game_menu_quit),
                )
            },
            onClick = {
                onResult { putExtra(GameMenuContract.RESULT_QUIT, true) }
            },
        )

        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_restart)) },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_restart),
                    contentDescription = stringResource(id = R.string.game_menu_restart),
                )
            },
            onClick = {
                onResult { putExtra(GameMenuContract.RESULT_RESET, true) }
            },
        )

        LemuroidSettingsSwitch(
            title = { Text(text = stringResource(id = R.string.game_menu_mute_audio)) },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_mute),
                    contentDescription = stringResource(id = R.string.game_menu_mute_audio),
                )
            },
            state = rememberMemoryBooleanSettingState(!gameMenuRequest.audioEnabled),
            onCheckedChange = {
                onResult { putExtra(GameMenuContract.RESULT_ENABLE_AUDIO, !it) }
            },
        )

        if (gameMenuRequest.fastForwardSupported) {
            LemuroidSettingsSwitch(
                title = { Text(text = stringResource(id = R.string.game_menu_fast_forward)) },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_fast_forward),
                        contentDescription = stringResource(id = R.string.game_menu_fast_forward),
                    )
                },
                state = rememberMemoryBooleanSettingState(gameMenuRequest.fastForwardEnabled),
                onCheckedChange = {
                    onResult { putExtra(GameMenuContract.RESULT_ENABLE_FAST_FORWARD, it) }
                },
            )
        }

        if (gameMenuRequest.numDisks > 1) {
            LemuroidSettingsList(
                title = { Text(text = stringResource(id = R.string.game_menu_change_disk_button)) },
                items = (1..gameMenuRequest.numDisks).map { stringResource(R.string.game_menu_change_disk_disk, it) },
                useSelectedValueAsSubtitle = false,
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_disk),
                        contentDescription = stringResource(id = R.string.game_menu_change_disk_button),
                    )
                },
                state = rememberMemoryIntSettingState(gameMenuRequest.currentDisk),
                onItemSelected = { index, _ ->
                    onResult { putExtra(GameMenuContract.RESULT_CHANGE_DISK, index) }
                },
            )
        }

        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_edit_touch_controls)) },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_controls),
                    contentDescription = stringResource(id = R.string.game_menu_edit_touch_controls),
                )
            },
            onClick = {
                onResult { putExtra(GameMenuContract.RESULT_EDIT_TOUCH_CONTROLS, true) }
            },
        )

        val hasCustomBg by GameBackgroundThemeManager.hasCustomBackgroundFlow.collectAsState()
        val themeMode by GameBackgroundThemeManager.themeModeFlow.collectAsState()
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_background_theme)) },
            subtitle = {
                Text(
                    text = if (hasCustomBg) {
                        when (themeMode) {
                            BackgroundThemeMode.FULLSCREEN -> stringResource(R.string.background_theme_mode_fullscreen)
                            BackgroundThemeMode.SPLIT -> stringResource(R.string.background_theme_mode_split)
                            else -> stringResource(R.string.game_menu_background_theme_custom)
                        }
                    } else {
                        stringResource(R.string.game_menu_background_theme_default)
                    },
                )
            },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_image),
                    contentDescription = stringResource(id = R.string.game_menu_background_theme),
                )
            },
            onClick = { navController.navigateToRoute(GameMenuRoute.BACKGROUND) },
        )

        val screenSettings by GameScreenSettingsManager.screenSettingsFlow.collectAsState()
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_screen_layout)) },
            subtitle = {
                Text(
                    text = "${(screenSettings.scale * 100).toInt()}% • " +
                        when {
                            screenSettings.verticalOffsetDp < -15 -> stringResource(R.string.screen_layout_pos_top)
                            screenSettings.verticalOffsetDp > 15 -> stringResource(R.string.screen_layout_pos_bottom)
                            else -> stringResource(R.string.screen_layout_pos_center)
                        }
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = stringResource(id = R.string.game_menu_screen_layout),
                )
            },
            onClick = { navController.navigateToRoute(GameMenuRoute.SCREEN_LAYOUT) },
        )

        if (gameMenuRequest.advancedCoreOptions.isNotEmpty() || gameMenuRequest.coreOptions.isNotEmpty()) {
            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.game_menu_settings)) },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_settings),
                        contentDescription = stringResource(id = R.string.game_menu_settings),
                    )
                },
                onClick = { navController.navigateToRoute(GameMenuRoute.OPTIONS) },
            )
        }

        if (gameMenuRequest.allTiltConfigurations.isNotEmpty()) {
            val tiltConfigurationEntries =
                gameMenuRequest.allTiltConfigurations
                    .map { TiltConfigurationMenuEntry.fromTiltConfiguration(it) }

            val selectedIndex =
                gameMenuRequest.allTiltConfigurations
                    .indexOf(gameMenuRequest.currentTiltConfiguration)

            LemuroidSettingsList(
                title = { Text(text = stringResource(id = R.string.game_menu_tilt_sensor)) },
                items = tiltConfigurationEntries.map { stringResource(it.descriptionId) },
                useSelectedValueAsSubtitle = false,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = stringResource(id = R.string.game_menu_tilt_sensor),
                    )
                },
                state = rememberMemoryIntSettingState(selectedIndex),
                onItemSelected = { index, _ ->
                    onResult {
                        putExtra(
                            GameMenuContract.RESULT_CHANGE_TILT_CONFIG,
                            tiltConfigurationEntries[index].configuration,
                        )
                    }
                },
            )
        }
    }
}
