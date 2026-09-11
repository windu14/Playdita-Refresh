package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.rememberLemuroidHaptics
import com.swordfish.lemuroid.app.shared.cheat.CheatManager
import com.swordfish.lemuroid.app.shared.cheat.GbaCheat
import com.swordfish.lemuroid.lib.library.db.entity.Game

/**
 * 2026 iOS-Inspired Clean Cheat Codes Management Screen.
 * Manual cheat code entry with automatic Master Code filtering and haptic tactile feedback.
 */
@Composable
fun GameMenuCheatsScreen(
    game: Game,
    onCheatsUpdated: () -> Unit = {},
) {
    val context = LocalContext.current
    val cheatManager = remember { CheatManager(context) }
    val haptics = rememberLemuroidHaptics()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    var cheatsList by remember { mutableStateOf<List<GbaCheat>>(emptyList()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var editingCheat by remember { mutableStateOf<GbaCheat?>(null) }

    fun refreshCheats() {
        cheatsList = cheatManager.getCheats(game)
        onCheatsUpdated()
    }

    LaunchedEffect(game) {
        refreshCheats()
    }

    val activeCount = cheatsList.count { it.enabled }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Status row & Tips button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Active count pill badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (activeCount > 0) {
                        Color(0xFF00E676).copy(alpha = if (isDark) 0.18f else 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (activeCount > 0) Color(0xFF00E676).copy(alpha = 0.4f) else Color.Transparent,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (activeCount > 0) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                ),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeCount > 0) "$activeCount Aktif" else "0 Aktif",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (activeCount > 0) {
                                if (isDark) Color(0xFF69F0AE) else Color(0xFF00897B)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }

                // Experimental indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                ) {
                    Text(
                        text = "EXPERIMENTAL",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            // Info & Tips button
            IconButton(
                onClick = {
                    haptics.click()
                    showInfoDialog = true
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = "Tips Format Cheat",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Large aesthetic "+ Tambah Cheat" button
        Button(
            onClick = {
                haptics.click()
                editingCheat = null
                showEditDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                ),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.cheat_add_button),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (cheatsList.isEmpty()) {
            // Aesthetic frosted empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                val cardShape = RoundedCornerShape(24.dp)
                val topHighlight = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.90f)
                val bottomBorder = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.08f)
                val borderBrush = Brush.verticalGradient(listOf(topHighlight, bottomBorder))

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .border(1.dp, borderBrush, cardShape),
                    shape = cardShape,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isDark) Color(0xEB181B26) else Color(0xF5FFFFFF),
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(28.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(64.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Code,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.cheat_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.cheat_empty_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = {
                                haptics.click()
                                editingCheat = null
                                showEditDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.cheat_add_button))
                        }
                    }
                }
            }
        } else {
            // Aesthetic list of cheats
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(cheatsList, key = { it.id }) { cheat ->
                    CheatItemCard(
                        cheat = cheat,
                        isDark = isDark,
                        onToggle = { newEnabled ->
                            haptics.click()
                            cheatManager.setCheatEnabled(game, cheat.id, newEnabled)
                            refreshCheats()
                        },
                        onEdit = {
                            haptics.click()
                            editingCheat = cheat
                            showEditDialog = true
                        },
                        onDelete = {
                            haptics.click()
                            cheatManager.deleteCheat(game, cheat.id)
                            refreshCheats()
                        },
                    )
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showEditDialog) {
        CheatEditDialog(
            initialCheat = editingCheat,
            onDismiss = { showEditDialog = false },
            onSave = { title, code ->
                if (editingCheat != null) {
                    cheatManager.saveCheat(
                        game,
                        editingCheat!!.copy(title = title, code = code),
                    )
                } else {
                    cheatManager.saveCheat(
                        game,
                        GbaCheat(title = title, code = code, enabled = true),
                    )
                }
                showEditDialog = false
                refreshCheats()
            },
            onDelete = if (editingCheat != null) {
                {
                    cheatManager.deleteCheat(game, editingCheat!!.id)
                    showEditDialog = false
                    refreshCheats()
                }
            } else null,
        )
    }

    // Help & Tips Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Panduan Cheat GBA", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "• Tanpa Master Code: Emulator mGBA langsung menerapkan efek cheat tanpa membutuhkan Master Code (M).\n" +
                            "• Filter Otomatis: Master Code (berawalan 0000... atau 9...) otomatis dilewati untuk mencegah game freeze/macet.\n" +
                            "• Format Didukung:\n" +
                            "  - CodeBreaker (8+4 digit, contoh: 820257C4 0063)\n" +
                            "  - GameShark v1/v2 (8+4 digit)\n" +
                            "  - GameShark v3/ActionReplay (8+8 digit)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Mengerti")
                }
            },
        )
    }
}

@Composable
private fun CheatItemCard(
    cheat: GbaCheat,
    isDark: Boolean,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val cardShape = RoundedCornerShape(18.dp)
    val containerColor = if (cheat.enabled) {
        if (isDark) Color(0xEB1C2030) else Color(0xF7FFFFFF)
    } else {
        if (isDark) Color(0x66141722) else Color(0x66F5F5F5)
    }

    val topHighlight = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.90f)
    val bottomBorder = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.07f)
    val borderBrush = Brush.verticalGradient(listOf(topHighlight, bottomBorder))

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (cheat.enabled) 3.dp else 0.dp,
                shape = cardShape,
                spotColor = if (isDark) Color.Black.copy(alpha = 0.40f) else Color.Black.copy(alpha = 0.06f),
            )
            .border(width = 1.dp, brush = borderBrush, shape = cardShape)
            .clickable { onEdit() },
        shape = cardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cheat.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (cheat.enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (cheat.hasMasterCodes) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                text = "M skipped",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Monospace code preview in tinted rounded pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.05f),
                ) {
                    Text(
                        text = cheat.code.trim().lines().take(2).joinToString(" | "),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (cheat.enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action: Edit
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.cheat_edit_title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }

            // Action: Delete
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.cheat_delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Toggle Switch
            Switch(
                checked = cheat.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

@Composable
private fun CheatEditDialog(
    initialCheat: GbaCheat?,
    onDismiss: () -> Unit,
    onSave: (title: String, code: String) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var title by remember { mutableStateOf(initialCheat?.title ?: "") }
    var code by remember { mutableStateOf(initialCheat?.code ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val testCheat = remember(title, code) { GbaCheat(title = title, code = code) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(
                    if (initialCheat == null) R.string.cheat_add_title else R.string.cheat_edit_title,
                ),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (errorText != null) errorText = null
                    },
                    label = { Text(stringResource(R.string.cheat_name_label)) },
                    placeholder = { Text(stringResource(R.string.cheat_name_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        if (errorText != null) errorText = null
                    },
                    label = { Text(stringResource(R.string.cheat_code_label)) },
                    placeholder = { Text(stringResource(R.string.cheat_code_hint)) },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (testCheat.hasMasterCodes) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "ℹ️ Master Code terdeteksi dan diabaikan otomatis agar game tidak macet/freeze. Efek cheat tetap bekerja langsung!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.cheat_format_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val trimmedCode = code.trim()

                    if (trimmedTitle.isEmpty()) {
                        errorText = context.getString(R.string.cheat_error_empty_name)
                        return@Button
                    }
                    if (trimmedCode.isEmpty()) {
                        errorText = context.getString(R.string.cheat_error_empty_code)
                        return@Button
                    }

                    val candidate = GbaCheat(title = trimmedTitle, code = trimmedCode)
                    if (candidate.executableLines.isEmpty()) {
                        errorText = context.getString(R.string.cheat_error_only_master)
                        return@Button
                    }

                    onSave(trimmedTitle, trimmedCode)
                },
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.cheat_save))
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text(stringResource(R.string.cheat_delete))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cheat_cancel))
                }
            }
        },
    )
}
