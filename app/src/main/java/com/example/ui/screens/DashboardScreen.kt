package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventLog
import com.example.data.model.GameState
import com.example.data.model.User
import com.example.ui.components.EmergencyBroadcastBanner
import com.example.ui.components.SurvivorAvatar
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalCard
import com.example.ui.theme.TerminalTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: User,
    gameState: GameState,
    recentEvents: List<EventLog>,
    onReportCasualty: (String) -> Unit,
    onReportKill: (String) -> Unit = {},
    onNavigateToShop: () -> Unit,
    showBroadcast: Boolean,
    onDismissBroadcast: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    var showSectorPickerSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // Emergency Broadcast
        if (showBroadcast && gameState.featuredVideoText.isNotEmpty()) {
            item {
                EmergencyBroadcastBanner(
                    text = gameState.featuredVideoText,
                    onDismiss = onDismissBroadcast
                )
            }
        }

        // Flash Sale Alert Banner
        if (gameState.flashSaleActive) {
            item {
                TerminalCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = theme.secondary.copy(alpha = 0.5f),
                    backgroundColor = theme.secondary.copy(alpha = 0.1f),
                    onClick = onNavigateToShop
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(text = "🔥", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FLASH SALE: -${gameState.flashSaleDiscount}% OFF",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = theme.secondary,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Black Market gear and themes discounted. Tap to view.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = theme.textLight
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Shop",
                            tint = theme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Clean Survivor Identity Card
        item {
            TerminalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_vitals_card"),
                borderColor = theme.surface3,
                backgroundColor = theme.surface1
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SurvivorAvatar(
                            avatar = user.playerData.avatar,
                            colorHex = user.playerData.color,
                            size = 46.dp,
                            hasShield = user.playerData.shield
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = user.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = theme.textLight,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = user.playerData.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Coins & Shield badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = theme.surface2,
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.surface3)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🪙", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${user.playerData.coins}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = theme.secondary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        if (user.playerData.shield) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = theme.tertiary.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.tertiary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "🛡️ SHIELD",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = theme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Global Casualties Counter Card
        item {
            TerminalCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = theme.surface3,
                backgroundColor = theme.surface1
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "GLOBAL CASUALTIES",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.textGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%,d", gameState.grandTotal),
                            style = MaterialTheme.typography.displaySmall,
                            color = theme.primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        )
                        Text(
                            text = "total deaths logged",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textGray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    val progressFraction = ((gameState.grandTotal % 1000).toFloat() / 1000f).coerceIn(0.05f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(theme.bgDark)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progressFraction)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(theme.primary)
                        )
                    }
                }
            }
        }

        // Prominent Report Casualty Button
        item {
            TerminalButton(
                text = "REPORT CASUALTY",
                onClick = { showSectorPickerSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                icon = "☣️",
                testTag = "btn_report_casualty"
            )
        }

        // Live Feed Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.textGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${recentEvents.size} LOGS",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.primary,
                    fontSize = 10.sp
                )
            }
        }

        // Event Stream
        if (recentEvents.isEmpty()) {
            item {
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "NO CASUALTIES RECORDED YET. TAP 'REPORT CASUALTY' ABOVE.",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.textGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            }
        } else {
            items(recentEvents.take(15), key = { it.id }) { event ->
                EventLogItemCard(event = event)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Modal Bottom Sheet: Sector Picker for Casualty Report
    if (showSectorPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSectorPickerSheet = false },
            sheetState = sheetState,
            containerColor = theme.surface1,
            contentColor = theme.textLight,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "REPORT CASUALTY",
                            style = MaterialTheme.typography.titleMedium,
                            color = theme.primary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Select zone to record casualty (+5 Coins bounty)",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textGray
                        )
                    }

                    IconButton(
                        onClick = { showSectorPickerSheet = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = theme.textGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                gameState.games.forEach { sector ->
                    val isLocked = gameState.lockedGames.contains(sector)
                    val isShooter = gameState.sectorModes[sector]?.equals("SHOOTER", ignoreCase = true) == true
                    val stats = user.playerData.games[sector]
                    val userSectorDeaths = stats?.deaths ?: 0
                    val userSectorKills = stats?.kills ?: 0

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = if (isLocked) theme.surface2.copy(alpha = 0.5f) else theme.surface2,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isLocked) theme.surface3 else theme.surface3
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isLocked) "🔒" else if (isShooter) "🎯" else "📍",
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = sector,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isLocked) theme.textGray else theme.textLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isShooter) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = theme.error.copy(alpha = 0.2f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.error.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = "SHOOTER",
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = theme.error,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = if (isShooter) "Casualties: $userSectorDeaths  |  Kills: $userSectorKills" else "Casualties: $userSectorDeaths",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = theme.textGray
                                    )
                                }
                            }

                            if (isLocked) {
                                Text(
                                    text = "LOCKED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = theme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = theme.primary.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primary.copy(alpha = 0.4f)),
                                        modifier = Modifier.clickable {
                                            onReportCasualty(sector)
                                            showSectorPickerSheet = false
                                        }.testTag("report_death_${sector.take(6)}")
                                    ) {
                                        Text(
                                            text = "+1 💀",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = theme.primary,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    if (isShooter) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = theme.tertiary.copy(alpha = 0.15f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.tertiary.copy(alpha = 0.4f)),
                                            modifier = Modifier.clickable {
                                                onReportKill(sector)
                                                showSectorPickerSheet = false
                                            }.testTag("report_kill_${sector.take(6)}")
                                        ) {
                                            Text(
                                                text = "+1 🎯",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = theme.tertiary,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
fun EventLogItemCard(event: EventLog) {
    val theme = TerminalTheme.current

    val icon = when (event.category) {
        "death" -> "💀"
        "curse_success" -> "☣️"
        "curse_blocked" -> "🛡️"
        "revive" -> "💉"
        "transfer" -> "🪙"
        "wheel" -> "🎡"
        else -> "📡"
    }

    val iconColor = when (event.category) {
        "death" -> theme.primary
        "curse_success" -> theme.error
        "curse_blocked" -> theme.tertiary
        "revive" -> theme.success
        "transfer" -> theme.secondary
        "wheel" -> theme.secondary
        else -> theme.primary
    }

    val formattedTime = remember(event.timestamp) {
        try {
            val diff = System.currentTimeMillis() - event.timestamp
            when {
                diff < 60_000 -> "just now"
                diff < 3600_000 -> "${diff / 60_000}m ago"
                diff < 86400_000 -> "${diff / 3600_000}h ago"
                else -> SimpleDateFormat("MMM dd", Locale.US).format(Date(event.timestamp))
            }
        } catch (_: Exception) {
            ""
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = theme.surface1,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.surface3)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.textLight,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (event.sector != null) {
                    Text(
                        text = "Sector: ${event.sector}",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.textGray,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = theme.textGray,
                fontSize = 10.sp
            )
        }
    }
}
