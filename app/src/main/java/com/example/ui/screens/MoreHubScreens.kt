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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.DiscordMedia
import com.example.data.model.EventLog
import com.example.data.model.GameState
import com.example.data.model.PatchNote
import com.example.data.model.User
import com.example.data.model.VotePoll
import com.example.ui.components.SurvivorAvatar
import com.example.ui.components.TerminalBadge
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalCard
import com.example.ui.theme.TerminalTheme
import com.example.ui.viewmodel.MoreSubScreen

fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    if (diff < 0) return "Just now"
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "Just now"
    }
}

@Composable
fun MoreHubScreen(
    currentSubScreen: MoreSubScreen,
    currentUser: User,
    allUsers: List<User>,
    gameState: GameState,
    recentEvents: List<EventLog>,
    mediaFeed: List<DiscordMedia>,
    patchNotes: List<PatchNote>,
    votePolls: List<VotePoll>,
    chatMessages: List<ChatMessage>,
    activeChatRecipient: User?,
    onNavigateSubScreen: (MoreSubScreen, User?) -> Unit,
    onTransferCoins: (String, Int) -> Unit,
    onCastVote: (String, String) -> Unit,
    onSendMessage: (String, String) -> Unit,
    onMarkPatchNoteRead: (String) -> Unit,
    onToggleLikeMedia: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (currentSubScreen) {
        MoreSubScreen.HUB -> {
            MoreHubGrid(
                onNavigate = { onNavigateSubScreen(it, null) },
                modifier = modifier
            )
        }
        MoreSubScreen.MEDIA -> {
            MediaFeedScreen(
                mediaList = mediaFeed,
                currentUserId = currentUser.uid,
                onToggleLike = onToggleLikeMedia,
                onBack = { onNavigateSubScreen(MoreSubScreen.HUB, null) },
                modifier = modifier
            )
        }
        MoreSubScreen.VOTING -> {
            VotingBoxScreen(
                polls = votePolls,
                currentUser = currentUser,
                onCastVote = onCastVote,
                onBack = { onNavigateSubScreen(MoreSubScreen.HUB, null) },
                modifier = modifier
            )
        }
        MoreSubScreen.TRANSFER -> {
            CoinTransferScreen(
                currentUser = currentUser,
                allUsers = allUsers,
                onTransfer = onTransferCoins,
                onBack = { onNavigateSubScreen(MoreSubScreen.HUB, null) },
                modifier = modifier
            )
        }
        MoreSubScreen.MESSAGES -> {
            MessagesScreen(
                currentUser = currentUser,
                allUsers = allUsers,
                chatMessages = chatMessages,
                activeRecipient = activeChatRecipient,
                onSelectRecipient = { onNavigateSubScreen(MoreSubScreen.MESSAGES, it) },
                onSendMessage = onSendMessage,
                onBack = { onNavigateSubScreen(MoreSubScreen.HUB, null) },
                modifier = modifier
            )
        }
        MoreSubScreen.PATCH_NOTES -> {
            PatchNotesScreen(
                patchNotes = patchNotes,
                currentUser = currentUser,
                onMarkRead = onMarkPatchNoteRead,
                onBack = { onNavigateSubScreen(MoreSubScreen.HUB, null) },
                modifier = modifier
            )
        }
        MoreSubScreen.ARCHIVES -> {
            ArchivesScreen(
                allUsers = allUsers,
                gameState = gameState,
                recentEvents = recentEvents,
                onBack = { onNavigateSubScreen(MoreSubScreen.HUB, null) },
                modifier = modifier
            )
        }
        MoreSubScreen.DOWNLOADS -> {
            DownloadsScreen(
                onBack = { onNavigateSubScreen(MoreSubScreen.HUB, null) },
                modifier = modifier
            )
        }
        MoreSubScreen.ADMIN -> {
            // Handled at top-level Settings tab navigation
        }
    }
}

// -------------------------------------------------------------
// Hub Grid Screen
// -------------------------------------------------------------

data class HubTile(
    val subScreen: MoreSubScreen,
    val title: String,
    val subtitle: String,
    val icon: String,
    val badge: String? = null
)

@Composable
fun MoreHubGrid(
    onNavigate: (MoreSubScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current

    val tiles = listOf(
        HubTile(MoreSubScreen.MEDIA, "MEDIA FEED", "Survivor transmissions & captures", "🎬", "LIVE"),
        HubTile(MoreSubScreen.VOTING, "VOTING BOX", "Decide sector quarantine orders", "🗳️", "ACTIVE"),
        HubTile(MoreSubScreen.TRANSFER, "COIN TRANSFER", "Atomic terminal coin wire", "💸", null),
        HubTile(MoreSubScreen.MESSAGES, "SECURE COMMS", "Encrypted point-to-point chat", "📨", "P2P"),
        HubTile(MoreSubScreen.PATCH_NOTES, "PATCH NOTES", "Terminal engineering changelogs", "📝", "v3.8.4"),
        HubTile(MoreSubScreen.ARCHIVES, "ARCHIVES", "Hall of Fame & telemetry matrix", "🏛️", null),
        HubTile(MoreSubScreen.DOWNLOADS, "DOWNLOADS", "System specs & APK distribution", "📱", "DOWNLINK")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "COMMUNICATION & SYSTEM SUB-NET",
                style = MaterialTheme.typography.labelMedium,
                color = theme.secondary,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(tiles) { tile ->
            TerminalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hub_tile_${tile.subScreen.name.lowercase()}"),
                borderColor = theme.primaryDim,
                backgroundColor = theme.surface1.copy(alpha = 0.9f),
                onClick = { onNavigate(tile.subScreen) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.surface2)
                                .border(1.dp, theme.primaryDim, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = tile.icon, fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = tile.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = theme.textLight,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tile.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.textGray
                            )
                        }
                    }

                    if (tile.badge != null) {
                        TerminalBadge(text = tile.badge, color = theme.primary)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// -------------------------------------------------------------
// SubScreen: Media Feed
// -------------------------------------------------------------

@Composable
fun MediaFeedScreen(
    mediaList: List<DiscordMedia>,
    currentUserId: String = "",
    onToggleLike: (String) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(title = "MEDIA FEED (DISCORD)", onBack = onBack)
        }

        if (mediaList.isEmpty()) {
            item {
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "NO RECENT SURVEILLANCE MEDIA CAPTURES.",
                        color = theme.textGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            items(mediaList, key = { it.id }) { item ->
                val hasLiked = item.likedBy.contains(currentUserId)
                TerminalCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = theme.primaryDim,
                    backgroundColor = theme.surface1.copy(alpha = 0.9f)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = if (item.type == "Video") "🎬 VIDEO" else "📷 PHOTO", fontSize = 12.sp, color = theme.secondary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "BY ${item.authorName.uppercase()}", style = MaterialTheme.typography.labelSmall, color = theme.textLight)
                            }
                            Text(text = formatRelativeTime(item.timestamp), style = MaterialTheme.typography.labelSmall, color = theme.textGray)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Preview box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.surface2)
                                .border(1.dp, theme.primaryDim, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = if (item.type == "Video") "▶️" else "🛰️", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "[ ENCRYPTED SATELLITE CAPTURE ]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = theme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.caption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.textLight
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (hasLiked) theme.error.copy(alpha = 0.2f) else theme.surface2,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (hasLiked) theme.error.copy(alpha = 0.5f) else theme.surface3),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onToggleLike(item.id) }
                                    .testTag("media_like_${item.id.take(6)}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Likes",
                                        tint = if (hasLiked) theme.error else theme.textGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${item.likes} ${if (hasLiked) "LIKED" else "UPVOTES"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (hasLiked) theme.error else theme.textGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "RAW LINK // ENCRYPTED",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.tertiary
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// -------------------------------------------------------------
// SubScreen: Voting Box
// -------------------------------------------------------------

@Composable
fun VotingBoxScreen(
    polls: List<VotePoll>,
    currentUser: User,
    onCastVote: (String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(title = "SECTOR COUNCIL VOTING", onBack = onBack)
        }

        items(polls, key = { it.id }) { poll ->
            val myVote = poll.votes[currentUser.uid]
            val totalVotes = poll.votes.size

            TerminalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("poll_card_${poll.id}"),
                borderColor = if (myVote != null) theme.primary else theme.primaryDim,
                backgroundColor = theme.surface1.copy(alpha = 0.9f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = poll.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = theme.primary,
                            fontWeight = FontWeight.Black
                        )
                        TerminalBadge(
                            text = if (poll.closed) "CLOSED" else "$totalVotes VOTES",
                            color = if (poll.closed) theme.error else theme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = poll.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.textLight
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Options List with live percentage bars
                    poll.options.forEach { option ->
                        val optionVotes = poll.votes.values.count { it == option }
                        val percentage = if (totalVotes > 0) (optionVotes.toFloat() / totalVotes.toFloat()) else 0f
                        val isMyChoice = myVote == option

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    if (isMyChoice) theme.primary else theme.primaryDim,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = !poll.closed) {
                                    onCastVote(poll.id, option)
                                }
                                .testTag("poll_opt_${option.take(6)}"),
                            color = if (isMyChoice) theme.primary.copy(alpha = 0.15f) else theme.surface2
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isMyChoice) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = theme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isMyChoice) theme.primary else theme.textLight,
                                            fontWeight = if (isMyChoice) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }

                                    Text(
                                        text = "${(percentage * 100).toInt()}% ($optionVotes)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isMyChoice) theme.primary else theme.textGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { percentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = if (isMyChoice) theme.primary else theme.secondary,
                                    trackColor = theme.surface3
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// -------------------------------------------------------------
// SubScreen: Coin Transfer
// -------------------------------------------------------------

@Composable
fun CoinTransferScreen(
    currentUser: User,
    allUsers: List<User>,
    onTransfer: (String, Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    val eligibleRecipients = allUsers.filter { it.uid != currentUser.uid }

    var selectedRecipientUid by remember { mutableStateOf<String?>(eligibleRecipients.firstOrNull()?.uid) }
    var transferAmount by remember { mutableFloatStateOf(25f) }
    val maxAvailable = currentUser.playerData.coins

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(title = "ATOMIC COIN TRANSFER", onBack = onBack)
        }

        item {
            TerminalCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = theme.secondary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "CURRENT SURVIVOR BALANCE", style = MaterialTheme.typography.labelSmall, color = theme.textGray)
                        Text(
                            text = "${currentUser.playerData.coins} COINS",
                            style = MaterialTheme.typography.headlineMedium,
                            color = theme.secondary,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(text = "🪙", fontSize = 32.sp)
                }
            }
        }

        item {
            Text(
                text = "SELECT RECIPIENT TERMINAL",
                style = MaterialTheme.typography.labelMedium,
                color = theme.textLight,
                fontWeight = FontWeight.Black
            )
        }

        items(eligibleRecipients, key = { it.uid }) { recipient ->
            val isSelected = selectedRecipientUid == recipient.uid
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, if (isSelected) theme.primary else theme.primaryDim, RoundedCornerShape(12.dp))
                    .clickable { selectedRecipientUid = recipient.uid }
                    .testTag("xfer_recipient_${recipient.displayName.lowercase()}"),
                color = if (isSelected) theme.primaryDim else theme.surface1
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SurvivorAvatar(
                            avatar = recipient.playerData.avatar,
                            colorHex = recipient.playerData.color,
                            size = 38.dp,
                            hasShield = recipient.playerData.shield
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = recipient.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = theme.textLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Title: ${recipient.playerData.title} | Coins: ${recipient.playerData.coins}",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.textGray
                            )
                        }
                    }

                    if (isSelected) {
                        TerminalBadge(text = "RECIPIENT", color = theme.primary)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "TRANSFER AMOUNT: ${transferAmount.toInt()} COINS",
                style = MaterialTheme.typography.labelLarge,
                color = theme.secondary,
                fontWeight = FontWeight.Black
            )

            Slider(
                value = transferAmount.coerceIn(0f, maxAvailable.toFloat().coerceAtLeast(1f)),
                onValueChange = { transferAmount = it },
                valueRange = 0f..maxAvailable.toFloat().coerceAtLeast(1f),
                steps = if (maxAvailable > 10) 10 else 0,
                colors = SliderDefaults.colors(
                    thumbColor = theme.secondary,
                    activeTrackColor = theme.secondary,
                    inactiveTrackColor = theme.surface3
                ),
                modifier = Modifier.testTag("transfer_slider")
            )

            Spacer(modifier = Modifier.height(10.dp))

            TerminalButton(
                text = "CONFIRM WIRE (${transferAmount.toInt()} COINS)",
                onClick = {
                    val uid = selectedRecipientUid
                    val amt = transferAmount.toInt()
                    if (uid != null && amt > 0) {
                        onTransfer(uid, amt)
                    }
                },
                enabled = selectedRecipientUid != null && transferAmount.toInt() > 0 && transferAmount.toInt() <= maxAvailable,
                modifier = Modifier.fillMaxWidth(),
                icon = "💸",
                testTag = "btn_submit_coin_transfer"
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// -------------------------------------------------------------
// SubScreen: Messages / Direct Chats
// -------------------------------------------------------------

@Composable
fun MessagesScreen(
    currentUser: User,
    allUsers: List<User>,
    chatMessages: List<ChatMessage>,
    activeRecipient: User?,
    onSelectRecipient: (User?) -> Unit,
    onSendMessage: (String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    val eligibleUsers = allUsers.filter { it.uid != currentUser.uid }

    if (activeRecipient == null) {
        // User list to select
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SubScreenHeader(title = "SECURE ENCLAVE MESSAGING", onBack = onBack)
            }

            items(eligibleUsers, key = { it.uid }) { target ->
                TerminalCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = theme.primaryDim,
                    backgroundColor = theme.surface1.copy(alpha = 0.9f),
                    onClick = { onSelectRecipient(target) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SurvivorAvatar(
                                avatar = target.playerData.avatar,
                                colorHex = target.playerData.color,
                                size = 42.dp,
                                hasShield = target.playerData.shield
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = target.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = theme.textLight,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Title: ${target.playerData.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = theme.textGray
                                )
                            }
                        }

                        TerminalButton(
                            text = "CHAT",
                            onClick = { onSelectRecipient(target) },
                            isPrimary = false,
                            icon = "💬",
                            testTag = "chat_with_${target.displayName.lowercase()}"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    } else {
        // Active Chat Conversation
        val chatId = if (currentUser.uid < activeRecipient.uid) "${currentUser.uid}_${activeRecipient.uid}" else "${activeRecipient.uid}_${currentUser.uid}"
        val threadMessages = chatMessages.filter { it.chatId == chatId }
        var messageInput by remember { mutableStateOf("") }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Chat Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onSelectRecipient(null) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.primary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                SurvivorAvatar(
                    avatar = activeRecipient.playerData.avatar,
                    colorHex = activeRecipient.playerData.color,
                    size = 36.dp,
                    hasShield = activeRecipient.playerData.shield
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = activeRecipient.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = theme.textLight,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "ENCRYPTED CHANNEL // ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.success
                    )
                }
            }

            Divider(color = theme.primaryDim)

            // Message Bubble List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (threadMessages.isEmpty()) {
                    item {
                        Text(
                            text = "NO PRIOR COMMUNICATIONS. TYPE BELOW TO TRANSMIT.",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                    }
                } else {
                    items(threadMessages, key = { it.id }) { msg ->
                        val isMine = msg.senderId == currentUser.uid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        1.dp,
                                        if (isMine) theme.primary else theme.surface3,
                                        RoundedCornerShape(12.dp)
                                    ),
                                color = if (isMine) theme.primaryDim else theme.surface2
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (isMine) "YOU" else msg.senderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isMine) theme.primary else theme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = theme.textLight
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 76.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text"),
                    placeholder = { Text("TRANSMIT MESSAGE…", color = theme.textGray, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primary,
                        unfocusedBorderColor = theme.surface3,
                        focusedTextColor = theme.textLight,
                        unfocusedTextColor = theme.textLight,
                        focusedContainerColor = theme.surface1,
                        unfocusedContainerColor = theme.surface1
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            onSendMessage(activeRecipient.uid, messageInput)
                            messageInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.primary)
                        .testTag("btn_send_chat_msg")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = theme.bgDark)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SubScreen: Patch Notes
// -------------------------------------------------------------

@Composable
fun PatchNotesScreen(
    patchNotes: List<PatchNote>,
    currentUser: User,
    onMarkRead: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(title = "TERMINAL PATCH LOGS", onBack = onBack)
        }

        items(patchNotes, key = { it.id }) { note ->
            val isRead = currentUser.playerData.lastReadPatchNoteId == note.id

            TerminalCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (!isRead) theme.secondary else theme.primaryDim,
                backgroundColor = theme.surface1.copy(alpha = 0.9f),
                onClick = { onMarkRead(note.id) }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TerminalBadge(text = note.versionTag, color = theme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = theme.textLight,
                                fontWeight = FontWeight.Black
                            )
                        }
                        if (!isRead) {
                            TerminalBadge(text = "NEW", color = theme.secondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "LOGGED BY: ${note.author} // ${formatRelativeTime(note.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.textGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.textLight
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// -------------------------------------------------------------
// SubScreen: Archives (Hall of Fame, Sector Matrix, Full Logs)
// -------------------------------------------------------------

@Composable
fun ArchivesScreen(
    allUsers: List<User>,
    gameState: GameState,
    recentEvents: List<EventLog>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    var selectedArchiveTab by remember { mutableIntStateOf(0) }
    val archiveTabs = listOf("HALL OF FAME", "SECTOR STATS", "SYSTEM LOGS")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(title = "TERMINAL ARCHIVES", onBack = onBack)
        }

        item {
            TabRow(
                selectedTabIndex = selectedArchiveTab,
                containerColor = theme.surface2,
                contentColor = theme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedArchiveTab]),
                        color = theme.primary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, theme.primaryDim, RoundedCornerShape(12.dp))
            ) {
                archiveTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedArchiveTab == index,
                        onClick = { selectedArchiveTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedArchiveTab == index) theme.primary else theme.textGray
                            )
                        }
                    )
                }
            }
        }

        when (selectedArchiveTab) {
            0 -> {
                // Hall of Fame
                val topCoins = allUsers.sortedByDescending { it.playerData.coins }.take(5)
                item {
                    Text(text = "SURVIVOR WEALTH LEADERS", style = MaterialTheme.typography.labelLarge, color = theme.secondary, fontWeight = FontWeight.Black)
                }
                items(topCoins) { player ->
                    TerminalCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SurvivorAvatar(avatar = player.playerData.avatar, colorHex = player.playerData.color, size = 38.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = player.displayName, style = MaterialTheme.typography.titleMedium, color = theme.textLight, fontWeight = FontWeight.Bold)
                                    Text(text = player.playerData.title, style = MaterialTheme.typography.labelSmall, color = theme.textGray)
                                }
                            }
                            Text(text = "🪙 ${player.playerData.coins}", style = MaterialTheme.typography.titleMedium, color = theme.secondary, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            1 -> {
                // Sector Casualties breakdown
                item {
                    Text(text = "SECTOR TELEMETRY BREAKDOWN", style = MaterialTheme.typography.labelLarge, color = theme.primary, fontWeight = FontWeight.Black)
                }
                items(gameState.games) { sector ->
                    val totalSectorCasualties = allUsers.sumOf { it.playerData.games[sector]?.deaths ?: 0 }
                    val isLocked = gameState.lockedGames.contains(sector)

                    TerminalCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = sector, style = MaterialTheme.typography.titleMedium, color = theme.textLight, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (isLocked) "STATUS: QUARANTINED" else "STATUS: COMBAT ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isLocked) theme.error else theme.success
                                )
                            }
                            Text(text = "💀 $totalSectorCasualties", style = MaterialTheme.typography.headlineMedium, color = theme.error, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            2 -> {
                // System logs
                item {
                    Text(text = "COMPLETE INCIDENT MATRIX (LAST 100)", style = MaterialTheme.typography.labelLarge, color = theme.textLight, fontWeight = FontWeight.Black)
                }
                items(recentEvents) { event ->
                    EventLogItemCard(event = event)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// -------------------------------------------------------------
// SubScreen: Downloads / Downlink Page
// -------------------------------------------------------------

@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(title = "SYSTEM DOWNLINK", onBack = onBack)
        }

        item {
            TerminalCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = theme.primary,
                backgroundColor = theme.surface1.copy(alpha = 0.95f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📱", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "DEAD-FEST CLIENT BUILD v3.8.4",
                                style = MaterialTheme.typography.titleLarge,
                                color = theme.primary,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Dystopian survival terminal mobile package",
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.textGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "SYSTEM REQUIREMENTS:",
                        style = MaterialTheme.typography.labelMedium,
                        color = theme.secondary,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Android 8.0 (Oreo) or Higher", style = MaterialTheme.typography.bodySmall, color = theme.textLight)
                    Text(text = "• 50 MB Free Tactical Storage", style = MaterialTheme.typography.bodySmall, color = theme.textLight)
                    Text(text = "• Continuous Satellite Internet Uplink", style = MaterialTheme.typography.bodySmall, color = theme.textLight)
                    Text(text = "• Hardware Monospace Display Matrix", style = MaterialTheme.typography.bodySmall, color = theme.textLight)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "INSTALLATION INSTRUCTIONS:",
                        style = MaterialTheme.typography.labelMedium,
                        color = theme.secondary,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "1. Authorize application installations from secure sources.", style = MaterialTheme.typography.bodySmall, color = theme.textLight)
                    Text(text = "2. Download APK package from authorized distribution.", style = MaterialTheme.typography.bodySmall, color = theme.textLight)
                    Text(text = "3. Launch Dead-Fest Terminal and calibrate CRT screen settings.", style = MaterialTheme.typography.bodySmall, color = theme.textLight)
                    Text(text = "4. Authenticate callsig and report first casualty.", style = MaterialTheme.typography.bodySmall, color = theme.textLight)

                    Spacer(modifier = Modifier.height(20.dp))

                    TerminalButton(
                        text = "DOWNLOAD APK // GITHUB RELEASES",
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        icon = "⬇️",
                        testTag = "btn_download_apk"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TerminalButton(
                        text = "RETURN TO TERMINAL",
                        onClick = onBack,
                        isPrimary = false,
                        modifier = Modifier.fillMaxWidth(),
                        icon = "🔙",
                        testTag = "btn_return_from_downloads"
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SubScreenHeader(
    title: String,
    onBack: () -> Unit
) {
    val theme = TerminalTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(theme.surface2)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = theme.primary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = theme.textLight,
            fontWeight = FontWeight.Black
        )
    }
}
