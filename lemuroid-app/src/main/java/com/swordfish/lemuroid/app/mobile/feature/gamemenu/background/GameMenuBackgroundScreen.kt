package com.swordfish.lemuroid.app.mobile.feature.gamemenu.background

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.game.BackgroundSlot
import com.swordfish.lemuroid.app.shared.game.BackgroundThemeMode
import com.swordfish.lemuroid.app.shared.game.GameBackgroundThemeManager
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun GameMenuBackgroundScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val backgroundUpdateKey by GameBackgroundThemeManager.backgroundUpdateFlow.collectAsState()
    val themeMode by GameBackgroundThemeManager.themeModeFlow.collectAsState()

    var activeSlotForPicker by remember { mutableStateOf<BackgroundSlot?>(null) }

    val fullscreenFile = remember(backgroundUpdateKey, themeMode) {
        GameBackgroundThemeManager.getBackgroundFile(context, BackgroundSlot.FULLSCREEN)
    }
    val topFile = remember(backgroundUpdateKey, themeMode) {
        GameBackgroundThemeManager.getBackgroundFile(context, BackgroundSlot.TOP)
    }
    val bottomFile = remember(backgroundUpdateKey, themeMode) {
        GameBackgroundThemeManager.getBackgroundFile(context, BackgroundSlot.BOTTOM)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val slot = activeSlotForPicker
        if (uri != null && slot != null) {
            coroutineScope.launch {
                val success = GameBackgroundThemeManager.saveBackground(context, uri, slot)
                if (success) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.background_theme_applied),
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.background_theme_error),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
        activeSlotForPicker = null
    }

    val selectedTabIndex = if (themeMode == BackgroundThemeMode.SPLIT) 1 else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Mode Selector (Tabs)
        Text(
            text = stringResource(R.string.background_theme_mode_select),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = {
                    GameBackgroundThemeManager.setThemeMode(context, BackgroundThemeMode.FULLSCREEN)
                },
                text = { Text(stringResource(R.string.background_theme_mode_fullscreen)) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = {
                    GameBackgroundThemeManager.setThemeMode(context, BackgroundThemeMode.SPLIT)
                },
                text = { Text(stringResource(R.string.background_theme_mode_split)) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }

        if (selectedTabIndex == 0) {
            // MODE FULLSCREEN
            BackgroundSlotCard(
                title = stringResource(R.string.background_theme_slot_fullscreen),
                badgeText = if (fullscreenFile != null && themeMode == BackgroundThemeMode.FULLSCREEN) {
                    stringResource(R.string.background_theme_active_badge)
                } else {
                    null
                },
                specInfo = "Rekomendasi: 1080 x 2400 px (Rasio 9:16 ~ 9:20)\nMembentang dari atas hingga bawah layar.",
                imageFile = fullscreenFile,
                updateKey = backgroundUpdateKey,
                onPickImage = {
                    activeSlotForPicker = BackgroundSlot.FULLSCREEN
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onResetImage = {
                    coroutineScope.launch {
                        GameBackgroundThemeManager.resetBackground(context, BackgroundSlot.FULLSCREEN)
                        Toast.makeText(
                            context,
                            context.getString(R.string.background_theme_reset_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            )
        } else {
            // MODE SPLIT: TOP & BOTTOM
            Text(
                text = "Konfigurasi Tema Terpisah:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )

            // Slot 1: Top (Game Area)
            BackgroundSlotCard(
                title = stringResource(R.string.background_theme_slot_top),
                badgeText = if (topFile != null && themeMode == BackgroundThemeMode.SPLIT) {
                    stringResource(R.string.background_theme_active_badge)
                } else {
                    null
                },
                specInfo = "Rekomendasi: 1080 x 1080 s/d 1080 x 1200 px (Rasio 1:1 atau 4:3)\nMengisi area sekeliling game retro di bagian atas.",
                imageFile = topFile,
                updateKey = backgroundUpdateKey,
                onPickImage = {
                    activeSlotForPicker = BackgroundSlot.TOP
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onResetImage = {
                    coroutineScope.launch {
                        GameBackgroundThemeManager.resetBackground(context, BackgroundSlot.TOP)
                        Toast.makeText(
                            context,
                            context.getString(R.string.background_theme_reset_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            )

            // Slot 2: Bottom (Gamepad Pad Container)
            BackgroundSlotCard(
                title = stringResource(R.string.background_theme_slot_bottom),
                badgeText = if (bottomFile != null && themeMode == BackgroundThemeMode.SPLIT) {
                    stringResource(R.string.background_theme_active_badge)
                } else {
                    null
                },
                specInfo = "Rekomendasi: 1080 x 1100 s/d 1080 x 1300 px (Rasio 1:1 atau 9:8)\nMengisi wadah gamepad dengan radius sudut lengkung 28dp.",
                imageFile = bottomFile,
                updateKey = backgroundUpdateKey,
                onPickImage = {
                    activeSlotForPicker = BackgroundSlot.BOTTOM
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onResetImage = {
                    coroutineScope.launch {
                        GameBackgroundThemeManager.resetBackground(context, BackgroundSlot.BOTTOM)
                        Toast.makeText(
                            context,
                            context.getString(R.string.background_theme_reset_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            )
        }

        // Global Reset All Button
        val hasAnyBg = fullscreenFile != null || topFile != null || bottomFile != null
        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    val success = GameBackgroundThemeManager.resetAll(context)
                    if (success) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.background_theme_reset_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = hasAnyBg,
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.background_theme_reset_all))
        }

        // Specifications & Margin Guidelines Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.background_theme_specs_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Text(
                    text = "• Layar Penuh (Full Screen): Resolusi ideal 1080 x 2400 px (rasio 9:16 s/d 9:20). Gambar otomatis disesuaikan secara proporsional dari status bar atas hingga dasar kontroler.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
                Text(
                    text = "• Area Atas (Layar Game): Resolusi ideal 1080 x 1080 px atau 1080 x 1200 px (rasio 1:1 s/d 4:3). Mengisi bingkai belakang layar emulator retro.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
                Text(
                    text = "• Area Bawah (Kontroler): Resolusi ideal 1080 x 1100 px s/d 1080 x 1300 px (rasio 1:1 s/d 9:8). Dipotong pas mengikuti margin sudut melengkung 28dp kontainer kontroler.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun BackgroundSlotCard(
    title: String,
    badgeText: String?,
    specInfo: String,
    imageFile: File?,
    updateKey: Long,
    onPickImage: () -> Unit,
    onResetImage: () -> Unit,
) {
    val context = LocalContext.current
    val hasImage = imageFile != null && imageFile.exists()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }

            // Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (hasImage) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageFile)
                            .memoryCacheKey("preview_${imageFile.name}_$updateKey")
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(12.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu_image),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = Color.White.copy(alpha = 0.5f),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.background_theme_status_default),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            // Specs text
            Text(
                text = specInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onPickImage,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Pilih Gambar", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onResetImage,
                    enabled = hasImage,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
