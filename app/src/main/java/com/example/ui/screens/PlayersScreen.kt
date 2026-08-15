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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    users: List<User>,
    currentUser: User,
    onOpenChat: (User) -> Unit,
    onOpenTransfer: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedDossierUser by remember { mutableStateOf<User?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Sort players by coins descending
    val sortedPlayers = remember(users, searchQuery) {
        users
            .sortedByDescending { it.playerData.coins }
            .filter {
                searchQuery.isEmpty() ||
                        it.displayName.contains(searchQuery, ignoreCase = true) ||
                        it.playerData.title.contains(searchQuery, ignoreCase = true)
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("players_search_input"),
            placeholder = { Text("SEARCH SURVIVOR CALLSIGN OR TITLE…", color = theme.textGray, fontSize = 12.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = theme.primary)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.primary,
                unfocusedBorderColor = theme.surface3,
                focusedTextColor = theme.textLight,
                unfocusedTextColor = theme.textLight,
                focusedContainerColor = theme.surface1,
                unfocusedContainerColor = theme.surface1
            ),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Survivor Count Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SURVIVOR REGISTRY // ${sortedPlayers.size} ACTIVE UNITS",
                style = MaterialTheme.typography.labelMedium,
                color = theme.secondary,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "SORT: COIN RESERVES",
                style = MaterialTheme.typography.labelSmall,
                color = theme.textGray
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ranked Roster
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(sortedPlayers, key = { _, user -> user.uid }) { index, player ->
                val rank = index + 1
                val isMe = player.uid == currentUser.uid

                val rankBadgeColor = when (rank) {
                    1 -> Color(0xFFFFD700) // Gold
                    2 -> Color(0xFFC0C0C0) // Silver
                    3 -> Color(0xFFCD7F32) // Bronze
                    else -> theme.textGray
                }

                val rankIcon = when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "#$rank"
                }

                val playerNameColor = try {
                    Color(android.graphics.Color.parseColor(player.playerData.color))
                } catch (_: Exception) {
                    theme.textLight
                }

                TerminalCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_row_${player.displayName.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    borderColor = if (isMe) theme.primary.copy(alpha = 0.8f) else theme.primaryDim,
                    backgroundColor = if (isMe) theme.primaryDim else theme.surface1.copy(alpha = 0.9f),
                    onClick = { selectedDossierUser = player }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank Indicator
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.surface2)
                                .border(1.dp, rankBadgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = rankIcon,
                                style = MaterialTheme.typography.labelLarge,
                                color = rankBadgeColor,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Avatar
                        SurvivorAvatar(
                            avatar = player.playerData.avatar,
                            colorHex = player.playerData.color,
                            size = 44.dp,
                            hasShield = player.playerData.shield
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Name & Title
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = player.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = playerNameColor,
                                    fontWeight = FontWeight.Black
                                )
                                if (player.admin) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    TerminalBadge(text = "ADMIN", color = theme.primary)
                                }
                                if (isMe) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    TerminalBadge(text = "YOU", color = theme.secondary)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = player.playerData.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.textGray
                            )
                        }

                        // Stats: Coins & Deaths
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "🪙 ${player.playerData.coins}",
                                style = MaterialTheme.typography.titleMedium,
                                color = theme.secondary,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "💀 ${player.playerData.totalDeaths} Casualties",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Survivor Dossier Modal Sheet
    if (selectedDossierUser != null) {
        val player = selectedDossierUser!!
        ModalBottomSheet(
            onDismissRequest = { selectedDossierUser = null },
            sheetState = sheetState,
            containerColor = theme.surface1,
            contentColor = theme.textLight,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SURVIVOR DOSSIER",
                        style = MaterialTheme.typography.labelLarge,
                        color = theme.secondary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    IconButton(onClick = { selectedDossierUser = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = theme.textGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dossier Top Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SurvivorAvatar(
                        avatar = player.playerData.avatar,
                        colorHex = player.playerData.color,
                        size = 64.dp,
                        hasShield = player.playerData.shield
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        val nameColor = try {
                            Color(android.graphics.Color.parseColor(player.playerData.color))
                        } catch (_: Exception) {
                            theme.textLight
                        }
                        Text(
                            text = player.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = nameColor,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "TITLE: ${player.playerData.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "TERMINAL UID: ${player.uid.take(12)}…",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.textGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Key Statistics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, theme.primaryDim, RoundedCornerShape(12.dp)),
                        color = theme.surface2
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🪙 COIN VAULT", style = MaterialTheme.typography.labelSmall, color = theme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${player.playerData.coins}",
                                style = MaterialTheme.typography.titleLarge,
                                color = theme.textLight,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, theme.primaryDim, RoundedCornerShape(12.dp)),
                        color = theme.surface2
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "💀 TOTAL DEATHS", style = MaterialTheme.typography.labelSmall, color = theme.error)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${player.playerData.totalDeaths}",
                                style = MaterialTheme.typography.titleLarge,
                                color = theme.error,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, theme.primaryDim, RoundedCornerShape(12.dp)),
                        color = theme.surface2
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🛡️ DEFENSE", style = MaterialTheme.typography.labelSmall, color = theme.tertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (player.playerData.shield) "ACTIVE" else "OFFLINE",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (player.playerData.shield) theme.tertiary else theme.textGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Per-Sector Death Breakdown
                Text(
                    text = "SECTOR CASUALTY MATRIX",
                    style = MaterialTheme.typography.labelLarge,
                    color = theme.textLight,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (player.playerData.games.isEmpty()) {
                    Text(
                        text = "No recorded sector terminations on record.",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.textGray
                    )
                } else {
                    player.playerData.games.forEach { (sector, record) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sector,
                                style = MaterialTheme.typography.bodyMedium,
                                color = theme.textLight
                            )
                            Text(
                                text = "${record.deaths} Casualties",
                                style = MaterialTheme.typography.bodyMedium,
                                color = theme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Inter-Terminal Actions
                if (player.uid != currentUser.uid) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TerminalButton(
                            text = "TRANSMIT MSG",
                            onClick = {
                                selectedDossierUser = null
                                onOpenChat(player)
                            },
                            modifier = Modifier.weight(1f),
                            icon = "📨",
                            testTag = "dossier_chat_btn"
                        )

                        TerminalButton(
                            text = "SEND COINS",
                            onClick = {
                                selectedDossierUser = null
                                onOpenTransfer(player)
                            },
                            modifier = Modifier.weight(1f),
                            icon = "💸",
                            isPrimary = false,
                            testTag = "dossier_transfer_btn"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
