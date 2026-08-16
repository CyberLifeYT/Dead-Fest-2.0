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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameState
import com.example.data.model.PatchNote
import com.example.data.model.ShopItem
import com.example.data.model.ShopTheme
import com.example.data.model.ShopTitle
import com.example.data.model.User
import com.example.data.model.VotePoll
import com.example.ui.components.SurvivorAvatar
import com.example.ui.components.TerminalBadge
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalCard
import com.example.ui.theme.TerminalTheme

@Composable
fun AdminScreen(
    gameState: GameState,
    allUsers: List<User>,
    patchNotes: List<PatchNote>,
    votePolls: List<VotePoll>,
    onToggleMarket: (Boolean) -> Unit,
    onToggleWheel: (Boolean) -> Unit,
    onUpdateBroadcast: (String, String, String) -> Unit,
    onTriggerFlashSale: (Int) -> Unit,
    onStopFlashSale: () -> Unit,
    onUpdateEconomyMultiplier: (Double) -> Unit = {},
    onSetSectorMode: (String, String) -> Unit = { _, _ -> },
    onAddSector: (String) -> Unit,
    onToggleLockSector: (String) -> Unit,
    onDeleteSector: (String) -> Unit,
    onAddShopItem: (ShopItem) -> Unit,
    onDeleteShopItem: (String) -> Unit,
    onAddShopTitle: (ShopTitle) -> Unit,
    onDeleteShopTitle: (String) -> Unit,
    onAddShopTheme: (ShopTheme) -> Unit,
    onDeleteShopTheme: (String) -> Unit,
    onCreatePatchNote: (String, String, String, String) -> Unit,
    onDeletePatchNote: (String) -> Unit,
    onCreatePoll: (String, String, List<String>) -> Unit,
    onToggleClosePoll: (String) -> Unit,
    onDeletePoll: (String) -> Unit,
    onAdjustCoins: (String, Int) -> Unit,
    onToggleAdmin: (String) -> Unit,
    onWipeUser: (String) -> Unit,
    onPurgeMedia: () -> Unit,
    onResetEventData: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("GLOBAL", "SECTORS", "MARKET", "CONTENT", "SURVIVORS", "RESET")

    // Remembered form states placed at top level
    var broadcastText by remember(gameState.featuredVideoText) { mutableStateOf(gameState.featuredVideoText) }
    var broadcastUrl by remember(gameState.featuredVideoUrl) { mutableStateOf(gameState.featuredVideoUrl) }
    var saleDiscount by remember { mutableFloatStateOf(30f) }
    var economyMult by remember(gameState.economyMultiplier) { mutableFloatStateOf(gameState.economyMultiplier.toFloat()) }

    var newSectorName by remember { mutableStateOf("") }

    var newItemName by remember { mutableStateOf("") }
    var newItemDesc by remember { mutableStateOf("") }
    var newItemIcon by remember { mutableStateOf("⚡") }
    var newItemPrice by remember { mutableStateOf("100") }

    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var noteVersion by remember { mutableStateOf("v3.8.5") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🛡️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OVERSEER CONTROL",
                        style = MaterialTheme.typography.titleMedium,
                        color = theme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TerminalBadge(text = "SUPERUSER", color = theme.primary)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surface2)
                            .clickable { onBack() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "EXIT ✕",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.textLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Tab Navigation
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = theme.surface2,
                contentColor = theme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = theme.primary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, theme.primaryDim, RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, name ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == index) theme.primary else theme.textGray,
                                fontSize = 10.sp
                            )
                        }
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // TAB 0: GLOBAL CONTROLS
        // -------------------------------------------------------------
        if (selectedTab == 0) {
            item {
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "SYSTEM TOGGLES", style = MaterialTheme.typography.labelLarge, color = theme.secondary, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Market toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "BLACK MARKET GLOBAL ACCESS", style = MaterialTheme.typography.bodyMedium, color = theme.textLight)
                            Switch(
                                checked = gameState.marketEnabled,
                                onCheckedChange = onToggleMarket,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = theme.bgDark,
                                    checkedTrackColor = theme.primary
                                )
                            )
                        }

                        Divider(color = theme.surface3, modifier = Modifier.padding(vertical = 8.dp))

                        // Fate wheel toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "WHEEL OF FATE SYSTEM", style = MaterialTheme.typography.bodyMedium, color = theme.textLight)
                            Switch(
                                checked = gameState.wheelEnabled,
                                onCheckedChange = onToggleWheel,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = theme.bgDark,
                                    checkedTrackColor = theme.primary
                                )
                            )
                        }
                    }
                }
            }

            // Broadcast message form
            item {
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "EMERGENCY SYSTEM BROADCAST", style = MaterialTheme.typography.labelLarge, color = theme.error, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = broadcastText,
                            onValueChange = { broadcastText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("BROADCAST ALERT TEXT", color = theme.textGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.error,
                                unfocusedBorderColor = theme.surface3,
                                focusedTextColor = theme.textLight,
                                unfocusedTextColor = theme.textLight,
                                focusedContainerColor = theme.surface2,
                                unfocusedContainerColor = theme.surface2
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = broadcastUrl,
                            onValueChange = { broadcastUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("MEDIA / VIDEO URL (OPTIONAL)", color = theme.textGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.error,
                                unfocusedBorderColor = theme.surface3,
                                focusedTextColor = theme.textLight,
                                unfocusedTextColor = theme.textLight,
                                focusedContainerColor = theme.surface2,
                                unfocusedContainerColor = theme.surface2
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TerminalButton(
                            text = "TRANSMIT BROADCAST",
                            onClick = {
                                onUpdateBroadcast(broadcastText, broadcastUrl, "bc_${System.currentTimeMillis()}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            icon = "📡",
                            testTag = "btn_admin_broadcast"
                        )
                    }
                }
            }

            // Flash Sale Controls
            item {
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "BLACK MARKET FLASH SALE", style = MaterialTheme.typography.labelLarge, color = theme.secondary, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "DISCOUNT: ${saleDiscount.toInt()}%", style = MaterialTheme.typography.titleMedium, color = theme.secondary, fontWeight = FontWeight.Bold)

                        Slider(
                            value = saleDiscount,
                            onValueChange = { saleDiscount = it },
                            valueRange = 10f..90f,
                            steps = 7,
                            colors = SliderDefaults.colors(thumbColor = theme.secondary, activeTrackColor = theme.secondary)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TerminalButton(
                                text = "START SALE (-${saleDiscount.toInt()}%)",
                                onClick = { onTriggerFlashSale(saleDiscount.toInt()) },
                                modifier = Modifier.weight(1f),
                                icon = "🔥",
                                testTag = "btn_start_flash_sale"
                            )

                            if (gameState.flashSaleActive) {
                                TerminalButton(
                                    text = "HALT SALE",
                                    onClick = onStopFlashSale,
                                    isPrimary = false,
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_stop_flash_sale"
                                )
                            }
                        }
                    }
                }
            }

            // Global Economy Multiplier Controls
            item {
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "GLOBAL ECONOMY MULTIPLIER", style = MaterialTheme.typography.labelLarge, color = theme.primary, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "CURRENT MULTIPLIER: ${String.format(java.util.Locale.US, "%.1f", economyMult)}x",
                            style = MaterialTheme.typography.titleMedium,
                            color = theme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Slider(
                            value = economyMult,
                            onValueChange = { economyMult = it },
                            valueRange = 0.5f..5.0f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = theme.primary, activeTrackColor = theme.primary)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TerminalButton(
                            text = "APPLY MULTIPLIER (${String.format(java.util.Locale.US, "%.1f", economyMult)}x)",
                            onClick = { onUpdateEconomyMultiplier(economyMult.toDouble()) },
                            modifier = Modifier.fillMaxWidth(),
                            icon = "⚡",
                            testTag = "btn_apply_economy_mult"
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // TAB 1: SECTORS
        // -------------------------------------------------------------
        if (selectedTab == 1) {
            item {
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "REGISTER NEW COMBAT SECTOR", style = MaterialTheme.typography.labelLarge, color = theme.primary, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSectorName,
                                onValueChange = { newSectorName = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("SECTOR CODENAME…", color = theme.textGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = theme.primary,
                                    unfocusedBorderColor = theme.surface3,
                                    focusedTextColor = theme.textLight,
                                    unfocusedTextColor = theme.textLight,
                                    focusedContainerColor = theme.surface2,
                                    unfocusedContainerColor = theme.surface2
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TerminalButton(
                                text = "ADD",
                                onClick = {
                                    if (newSectorName.isNotBlank()) {
                                        onAddSector(newSectorName)
                                        newSectorName = ""
                                    }
                                },
                                testTag = "btn_add_sector"
                            )
                        }
                    }
                }
            }

            items(gameState.games) { sector ->
                val isLocked = gameState.lockedGames.contains(sector)
                val currentMode = gameState.sectorModes[sector] ?: "NORMAL"
                val isShooter = currentMode.equals("SHOOTER", ignoreCase = true)
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = sector, style = MaterialTheme.typography.titleMedium, color = theme.textLight, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isShooter) theme.error.copy(alpha = 0.2f) else theme.surface2,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isShooter) theme.error.copy(alpha = 0.5f) else theme.surface3),
                                    modifier = Modifier.clickable {
                                        val newMode = if (isShooter) "NORMAL" else "SHOOTER"
                                        onSetSectorMode(sector, newMode)
                                    }
                                ) {
                                    Text(
                                        text = if (isShooter) "🎯 SHOOTER" else "📍 NORMAL",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isShooter) theme.error else theme.textLight,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = if (isLocked) "QUARANTINED" else "ACTIVE COMBAT ZONE",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isLocked) theme.error else theme.success
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onToggleLockSector(sector) }) {
                                Icon(
                                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Lock",
                                    tint = if (isLocked) theme.error else theme.success
                                )
                            }
                            IconButton(onClick = { onDeleteSector(sector) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = theme.error)
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // TAB 2: MARKET CATALOG
        // -------------------------------------------------------------
        if (selectedTab == 2) {
            item {
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "ADD BLACK MARKET ITEM", style = MaterialTheme.typography.labelLarge, color = theme.primary, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("ITEM NAME", color = theme.textGray) },
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = newItemDesc,
                            onValueChange = { newItemDesc = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("DESCRIPTION", color = theme.textGray) },
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newItemIcon,
                                onValueChange = { newItemIcon = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("ICON", color = theme.textGray) },
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = newItemPrice,
                                onValueChange = { newItemPrice = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("PRICE", color = theme.textGray) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TerminalButton(
                            text = "REGISTER ITEM",
                            onClick = {
                                val pr = newItemPrice.toIntOrNull() ?: 100
                                val id = newItemName.lowercase().replace(" ", "_")
                                onAddShopItem(ShopItem(id, newItemName, newItemDesc, newItemIcon, pr))
                                newItemName = ""
                                newItemDesc = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_admin_add_shop_item"
                        )
                    }
                }
            }

            items(gameState.shopItems) { item ->
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = item.name, style = MaterialTheme.typography.titleMedium, color = theme.textLight, fontWeight = FontWeight.Bold)
                                Text(text = "${item.basePrice} coins", style = MaterialTheme.typography.labelSmall, color = theme.secondary)
                            }
                        }

                        IconButton(onClick = { onDeleteShopItem(item.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = theme.error)
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // TAB 3: CONTENT (PATCH NOTES & POLLS)
        // -------------------------------------------------------------
        if (selectedTab == 3) {
            item {
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "PUBLISH NEW PATCH LOG", style = MaterialTheme.typography.labelLarge, color = theme.secondary, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("PATCH TITLE", color = theme.textGray) },
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("CHANGELOG CONTENT", color = theme.textGray) },
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = noteVersion,
                            onValueChange = { noteVersion = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("VERSION TAG", color = theme.textGray) },
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TerminalButton(
                            text = "BROADCAST PATCH LOG",
                            onClick = {
                                if (noteTitle.isNotBlank()) {
                                    onCreatePatchNote(noteTitle, noteContent, "OVERSEER ROOT", noteVersion)
                                    noteTitle = ""
                                    noteContent = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_admin_create_patch"
                        )
                    }
                }
            }

            items(patchNotes) { note ->
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "${note.versionTag}: ${note.title}", style = MaterialTheme.typography.titleMedium, color = theme.textLight, fontWeight = FontWeight.Bold)
                            Text(text = note.content, style = MaterialTheme.typography.bodySmall, color = theme.textGray)
                        }

                        IconButton(onClick = { onDeletePatchNote(note.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = theme.error)
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // TAB 4: SURVIVOR MANAGEMENT
        // -------------------------------------------------------------
        if (selectedTab == 4) {
            items(allUsers) { player ->
                TerminalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SurvivorAvatar(avatar = player.playerData.avatar, colorHex = player.playerData.color, size = 36.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = player.displayName, style = MaterialTheme.typography.titleMedium, color = theme.textLight, fontWeight = FontWeight.Bold)
                                    Text(text = "🪙 ${player.playerData.coins} coins | 💀 ${player.playerData.totalDeaths} deaths", style = MaterialTheme.typography.labelSmall, color = theme.textGray)
                                }
                            }

                            TerminalBadge(
                                text = if (player.admin) "ADMIN" else "SURVIVOR",
                                color = if (player.admin) theme.primary else theme.textGray
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TerminalButton(
                                text = "+100 🪙",
                                onClick = { onAdjustCoins(player.uid, 100) },
                                modifier = Modifier.weight(1f),
                                testTag = "admin_add_coins_${player.displayName.lowercase()}"
                            )

                            TerminalButton(
                                text = "-100 🪙",
                                onClick = { onAdjustCoins(player.uid, -100) },
                                modifier = Modifier.weight(1f),
                                isPrimary = false,
                                testTag = "admin_sub_coins_${player.displayName.lowercase()}"
                            )

                            TerminalButton(
                                text = if (player.admin) "REVOKE" else "GRANT",
                                onClick = { onToggleAdmin(player.uid) },
                                modifier = Modifier.weight(1f),
                                isPrimary = false,
                                testTag = "admin_toggle_role_${player.displayName.lowercase()}"
                            )

                            IconButton(
                                onClick = { onWipeUser(player.uid) },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.error.copy(alpha = 0.2f))
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Wipe", tint = theme.error)
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // TAB 5: RESET & PURGE
        // -------------------------------------------------------------
        if (selectedTab == 5) {
            item {
                TerminalCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = theme.error,
                    backgroundColor = theme.error.copy(alpha = 0.12f)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "☣️ EMERGENCY SYSTEM PURGE", style = MaterialTheme.typography.titleLarge, color = theme.error, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dangerous overseer commands. Wipe media buffers or zero global casualties and telemetry matrices.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.textLight
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TerminalButton(
                            text = "PURGE DISCORD MEDIA FEED",
                            onClick = onPurgeMedia,
                            modifier = Modifier.fillMaxWidth(),
                            icon = "🗑️",
                            testTag = "btn_purge_media"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TerminalButton(
                            text = "RESET CASUALTIES & LOG MATRIX",
                            onClick = onResetEventData,
                            modifier = Modifier.fillMaxWidth(),
                            icon = "☣️",
                            testTag = "btn_reset_event_data"
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
