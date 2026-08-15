package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.components.TerminalBadge
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
    onNavigateToShop: () -> Unit,
    showBroadcast: Boolean,
    onDismissBroadcast: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    var showSectorPickerSheet by remember { mutableStateOf(false) }
    var selectedSectorToReport by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Emergency Broadcast Banner if visible
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
                    borderColor = theme.secondary.copy(alpha = 0.6f),
                    backgroundColor = theme.secondary.copy(alpha = 0.12f),
                    onClick = onNavigateToShop
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔥", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "BLACK MARKET FLASH SALE: -${gameState.flashSaleDiscount}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = theme.secondary,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "All gear, titles, and themes discounted. Tap to browse market.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = theme.textLight
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Shop",
                            tint = theme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Top 2-Column Grid (Credits & Shield)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Credits Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, theme.surface3, RoundedCornerShape(24.dp)),
                    color = theme.surface1
                ) {
                    Box(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "🪙",
                            fontSize = 32.sp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(CircleShape),
                            color = Color.White.copy(alpha = 0.15f)
                        )
                        Column {
                            Text(
                                text = "CREDITS",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.textGray,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = String.format(Locale.US, "%,d", user.playerData.coins),
                                style = MaterialTheme.typography.headlineMedium,
                                color = theme.secondary,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                        }
                    }
                }

                // Shield Status Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, theme.surface3, RoundedCornerShape(24.dp)),
                    color = theme.surface1
                ) {
                    Box(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "🛡️",
                            fontSize = 32.sp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(CircleShape),
                            color = Color.White.copy(alpha = 0.15f)
                        )
                        Column {
                            Text(
                                text = "SHIELD",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.textGray,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (user.playerData.shield) "ACTIVE" else "OFFLINE",
                                style = MaterialTheme.typography.headlineMedium,
                                color = if (user.playerData.shield) theme.tertiary else theme.textGray,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }
        }

        // Survivor Profile Identity Card
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
                                text = "[ ${user.playerData.title} ]",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    // Personal Deaths Metric
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(theme.surface2)
                            .border(1.dp, theme.surface3, RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "MY CASUALTIES",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.textGray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "💀 ${user.playerData.totalDeaths}",
                                style = MaterialTheme.typography.labelMedium,
                                color = theme.primary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Feature Stat Block: Global Casualties Card
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
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%,d", gameState.grandTotal),
                            style = MaterialTheme.typography.displayMedium,
                            color = theme.primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 38.sp
                        )
                        val todayCasualties = recentEvents.count { it.category == "death" }.coerceAtLeast(1)
                        Text(
                            text = "+$todayCasualties today",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.primary.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Tactical Progress Gauge
                    val progressFraction = (gameState.grandTotal % 1000).toFloat() / 1000f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(theme.bgDark)
                            .border(1.dp, theme.surface3, RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progressFraction.coerceIn(0.08f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(theme.primary)
                        )
                    }
                }
            }
        }

        // Giant Glowing "REPORT CASUALTY" Button
        item {
            TerminalButton(
                text = "REPORT CASUALTY",
                onClick = { showSectorPickerSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = theme.primary),
                icon = "☣",
                testTag = "btn_report_casualty"
            )
        }

        // Recent Activity Feed Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(theme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RECENT ACTIVITY",
                        style = MaterialTheme.typography.labelLarge,
                        color = theme.textGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.primary.copy(alpha = 0.1f))
                        .border(1.dp, theme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "LIVE FEED",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        // Recent Activity Feed List (Last events)
        if (recentEvents.isEmpty()) {
            item {
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "NO RECENT CASUALTIES OR TRANSMISSIONS DETECTED.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.textGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            items(recentEvents.take(10), key = { it.id }) { event ->
                EventLogItemCard(event = event)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal Bottom Sheet: Sector Picker for Casualty Report
    if (showSectorPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSectorPickerSheet = false },
            sheetState = sheetState,
            containerColor = theme.surface1,
            contentColor = theme.textLight,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
                            text = "SELECT CASUALTY SECTOR",
                            style = MaterialTheme.typography.titleLarge,
                            color = theme.primary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Log confirmed survivor termination in combat sector",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textGray
                        )
                    }
                    IconButton(onClick = { showSectorPickerSheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = theme.textGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                gameState.games.forEach { sector ->
                    val isLocked = gameState.lockedGames.contains(sector)
                    val isSelected = selectedSectorToReport == sector

                    val cardBorder = when {
                        isLocked -> theme.surface3
                        isSelected -> theme.primary
                        else -> theme.primaryDim
                    }
                    val cardBg = when {
                        isLocked -> theme.surface2.copy(alpha = 0.5f)
                        isSelected -> theme.primaryDim
                        else -> theme.surface2
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .clickable(enabled = !isLocked) {
                                selectedSectorToReport = sector
                            }
                            .testTag("sector_option_${sector.take(9)}"),
                        color = cardBg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isLocked) "🔒" else "📍",
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = sector,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isLocked) theme.textGray else theme.textLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val userDeathsInSector = user.playerData.games[sector]?.deaths ?: 0
                                    Text(
                                        text = if (isLocked) "QUARANTINED BY OVERSEER" else "Your Recorded Casualties: $userDeathsInSector",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isLocked) theme.error else theme.secondary
                                    )
                                }
                            }

                            if (!isLocked && isSelected) {
                                TerminalBadge(text = "SELECTED", color = theme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TerminalButton(
                    text = "TRANSMIT CASUALTY LOG (+1)",
                    onClick = {
                        val sector = selectedSectorToReport
                        if (sector != null) {
                            onReportCasualty(sector)
                            showSectorPickerSheet = false
                            selectedSectorToReport = null
                        }
                    },
                    enabled = selectedSectorToReport != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    icon = "⚡",
                    testTag = "btn_confirm_casualty_report"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun EventLogItemCard(
    event: EventLog,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current

    val (categoryIcon, categoryColor, categoryLabel) = when (event.category) {
        "death" -> Triple("💀", theme.error, "CASUALTY")
        "curse_success" -> Triple("☣️", theme.secondary, "BIO-CURSE")
        "curse_blocked" -> Triple("🛡️", theme.tertiary, "DEFLECTED")
        "revive" -> Triple("💉", theme.success, "REVIVE")
        "transfer" -> Triple("💸", theme.secondary, "TRANSFER")
        "wheel" -> Triple("🎡", theme.primary, "FATE WHEEL")
        else -> Triple("📡", theme.primary, "SYSTEM")
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val formattedTime = remember(event.timestamp) {
        if (event.timestamp > 0) timeFormat.format(Date(event.timestamp)) else "--:--"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, theme.surface3, RoundedCornerShape(16.dp)),
        color = theme.surface2.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "[$formattedTime]",
                style = MaterialTheme.typography.labelSmall,
                color = theme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(categoryColor.copy(alpha = 0.12f))
                    .border(1.dp, categoryColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = categoryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.4.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = event.message,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.textLight,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "JUST NOW"
        minutes < 60 -> "${minutes}m AGO"
        hours < 24 -> "${hours}h AGO"
        else -> "${days}d AGO"
    }
}
