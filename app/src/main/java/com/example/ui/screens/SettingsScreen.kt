package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.components.SurvivorAvatar
import com.example.ui.components.TerminalBadge
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalCard
import com.example.ui.theme.TerminalTheme
import com.example.ui.theme.ThemeList

val AVATAR_OPTIONS = listOf(
    "☣️", "💀", "🤖", "🩸", "👾", "🛡️", "🚀", "⚡", "👑", "🐺", "🦾", "🎯", "🔥", "🔮", "👽", "🧬"
)

val COLOR_OPTIONS = listOf(
    "#39FF14", "#00FFFF", "#FFD700", "#FF2A55", "#BF5AF2", "#FF9500", "#FF1493", "#FFFFFF", "#00E5FF", "#76FF03"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentUser: User,
    allUsers: List<User>,
    performanceMode: Boolean = false,
    onTogglePerformanceMode: (Boolean) -> Unit = {},
    onUpdateProfile: (String, String, String, String) -> Unit,
    onSwitchUser: (User) -> Unit,
    onLogout: () -> Unit,
    onOpenAdmin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current

    var displayName by remember(currentUser) { mutableStateOf(currentUser.displayName) }
    var selectedAvatar by remember(currentUser) { mutableStateOf(currentUser.playerData.avatar) }
    var selectedColor by remember(currentUser) { mutableStateOf(currentUser.playerData.color) }
    var selectedThemeId by remember(currentUser) { mutableStateOf(currentUser.playerData.selectedTheme) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Live Profile Preview
        item {
            TerminalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_preview_card"),
                borderColor = theme.primary,
                backgroundColor = theme.surface1.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SurvivorAvatar(
                        avatar = selectedAvatar,
                        colorHex = selectedColor,
                        size = 56.dp,
                        hasShield = currentUser.playerData.shield
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        val previewColor = try {
                            Color(android.graphics.Color.parseColor(selectedColor))
                        } catch (_: Exception) {
                            theme.textLight
                        }
                        Text(
                            text = displayName.ifBlank { "SURVIVOR CALLSIGN" },
                            style = MaterialTheme.typography.titleLarge,
                            color = previewColor,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "[ ${currentUser.playerData.title} ] // UID: ${currentUser.uid.take(8)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.secondary
                        )
                    }
                }
            }
        }

        // Performance & Low-End Phone Optimization Matrix
        item {
            TerminalCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (performanceMode) theme.primary else theme.surface3
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚡ PERFORMANCE & LOW-END MODE",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = theme.primary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (performanceMode) "HIGH FPS MODE: CRT scanlines and heavy shader overlays disabled for maximum responsiveness." else "STANDARD GRAPHICS: CRT mesh overlays enabled.",
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.textGray
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        androidx.compose.material3.Switch(
                            checked = performanceMode,
                            onCheckedChange = onTogglePerformanceMode,
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = theme.bgDark,
                                checkedTrackColor = theme.primary,
                                uncheckedThumbColor = theme.textGray,
                                uncheckedTrackColor = theme.surface2
                            ),
                            modifier = Modifier.testTag("switch_performance_mode")
                        )
                    }
                }
            }
        }

        // Section 1: Callsign & Avatar
        item {
            TerminalCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SURVIVOR IDENTIFIER",
                        style = MaterialTheme.typography.labelLarge,
                        color = theme.secondary,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_display_name"),
                        label = { Text("CALLSIGN / DISPLAY NAME", color = theme.textGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.surface3,
                            focusedTextColor = theme.textLight,
                            unfocusedTextColor = theme.textLight,
                            focusedContainerColor = theme.surface2,
                            unfocusedContainerColor = theme.surface2
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "AVATAR GLYPH",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.textGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AVATAR_OPTIONS.forEach { emoji ->
                            val isSelected = selectedAvatar == emoji
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) theme.primaryDim else theme.surface2)
                                    .border(
                                        2.dp,
                                        if (isSelected) theme.primary else theme.surface3,
                                        CircleShape
                                    )
                                    .clickable { selectedAvatar = emoji }
                                    .testTag("avatar_opt_$emoji"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "CALLSIGN COLOR SIGNATURE",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.textGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        COLOR_OPTIONS.forEach { hex ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (_: Exception) {
                                theme.primary
                            }
                            val isSelected = selectedColor.equals(hex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        3.dp,
                                        if (isSelected) theme.textLight else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                                    .testTag("color_opt_$hex")
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Terminal Color Schemes
        item {
            TerminalCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "TERMINAL HARDWARE THEMES",
                        style = MaterialTheme.typography.labelLarge,
                        color = theme.primary,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ThemeList.forEach { themeConfig ->
                        val isOwned = currentUser.playerData.ownedThemes.contains(themeConfig.id)
                        val isSelected = selectedThemeId == themeConfig.id

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) themeConfig.primary else theme.surface3,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = isOwned) {
                                    selectedThemeId = themeConfig.id
                                }
                                .testTag("settings_theme_${themeConfig.id}"),
                            color = if (isSelected) themeConfig.primaryDim else theme.surface2
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(themeConfig.bgDark)
                                            .border(2.dp, themeConfig.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(themeConfig.secondary)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = themeConfig.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = themeConfig.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = themeConfig.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = theme.textGray
                                        )
                                    }
                                }

                                if (isSelected) {
                                    TerminalBadge(text = "EQUIPPED", color = themeConfig.primary)
                                } else if (!isOwned) {
                                    TerminalBadge(text = "LOCKED 🔒", color = theme.textGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Save Profile Button
        item {
            TerminalButton(
                text = "SAVE SURVIVOR CONFIGURATION",
                onClick = {
                    onUpdateProfile(displayName, selectedAvatar, selectedColor, selectedThemeId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                icon = "💾",
                testTag = "btn_save_settings"
            )
        }

        // Section 3: Switch Active Terminal User (Quick Tester)
        item {
            TerminalCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "QUICK SWAP TERMINAL (DEBUG & MULTIPLAYER)",
                        style = MaterialTheme.typography.labelMedium,
                        color = theme.secondary,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Switch active survivor context without re-authenticating.",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.textGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    allUsers.forEach { user ->
                        val isCurrent = user.uid == currentUser.uid
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, if (isCurrent) theme.secondary else theme.surface3, RoundedCornerShape(8.dp))
                                .clickable { onSwitchUser(user) }
                                .testTag("swap_user_${user.displayName.lowercase()}"),
                            color = if (isCurrent) theme.secondary.copy(alpha = 0.15f) else theme.surface2
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${user.playerData.avatar} ${user.displayName} (${user.playerData.coins} coins)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = theme.textLight,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isCurrent) {
                                    TerminalBadge(text = "CURRENT", color = theme.secondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overseer Admin Panel Shortcut
        if (currentUser.admin) {
            item {
                TerminalCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = theme.primary,
                    backgroundColor = theme.primary.copy(alpha = 0.1f),
                    onClick = onOpenAdmin
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛡️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "OVERSEER ROOT CONSOLE",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = theme.primary,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Manage global game matrix, sales, and economy.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = theme.textLight
                                )
                            }
                        }
                        TerminalBadge(text = "ADMIN ONLY", color = theme.primary)
                    }
                }
            }
        }

        // Logout
        item {
            TerminalButton(
                text = "TERMINATE TERMINAL SESSION (LOGOUT)",
                onClick = onLogout,
                isPrimary = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                icon = "🚪",
                testTag = "btn_logout"
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
