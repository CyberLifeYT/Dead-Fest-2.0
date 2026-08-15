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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameState
import com.example.data.model.ShopItem
import com.example.data.model.ShopTheme
import com.example.data.model.ShopTitle
import com.example.data.model.User
import com.example.ui.components.SurvivorAvatar
import com.example.ui.components.TerminalBadge
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalCard
import com.example.ui.theme.TerminalTheme
import com.example.ui.theme.getTerminalTheme

enum class ShopTab(val label: String, val icon: String) {
    GEAR("TACTICAL GEAR", "🛡️"),
    TITLES("HONOR TITLES", "🎖️"),
    THEMES("TERMINAL THEMES", "🎨")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    user: User,
    gameState: GameState,
    allUsers: List<User>,
    onBuyItem: (ShopItem) -> Unit,
    onExecuteCurse: (String) -> Unit,
    onExecuteRevive: (String) -> Unit,
    onBuyTitle: (ShopTitle) -> Unit,
    onBuyTheme: (ShopTheme) -> Unit,
    showCurseModal: Boolean,
    onDismissCurseModal: () -> Unit,
    showReviveModal: Boolean,
    onDismissReviveModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    var activeShopTab by remember { mutableStateOf(ShopTab.GEAR) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun calculateDiscountedPrice(base: Int): Int {
        return if (gameState.flashSaleActive) {
            val discount = (100 - gameState.flashSaleDiscount).coerceIn(10, 100)
            ((base * discount) / 100).coerceAtLeast(1)
        } else {
            base
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Wallet & Sale Banner
        item {
            TerminalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_wallet_card"),
                borderColor = if (gameState.flashSaleActive) theme.secondary else theme.primaryDim,
                backgroundColor = theme.surface1.copy(alpha = 0.95f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🪙", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SURVIVOR VAULT BALANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = theme.textGray
                                )
                                Text(
                                    text = "${user.playerData.coins} COINS",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = theme.secondary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        if (gameState.flashSaleActive) {
                            TerminalBadge(
                                text = "FLASH SALE -${gameState.flashSaleDiscount}%",
                                color = theme.secondary,
                                backgroundColor = theme.secondary.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }

        // Tabs Header (GEAR, TITLES, THEMES)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(theme.surface2)
                    .border(1.dp, theme.primaryDim, RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                ShopTab.entries.forEach { tab ->
                    val isSelected = activeShopTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) theme.primary else Color.Transparent)
                            .clickable { activeShopTab = tab }
                            .padding(vertical = 10.dp)
                            .testTag("shop_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = tab.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (tab) {
                                    ShopTab.GEAR -> "GEAR"
                                    ShopTab.TITLES -> "TITLES"
                                    ShopTab.THEMES -> "THEMES"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) theme.bgDark else theme.textLight,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Content by Tab
        when (activeShopTab) {
            ShopTab.GEAR -> {
                if (gameState.shopItems.isEmpty()) {
                    item {
                        Text(
                            text = "NO TACTICAL ITEMS IN BLACK MARKET INVENTORY.",
                            color = theme.textGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(gameState.shopItems, key = { it.id }) { item ->
                        val finalPrice = calculateDiscountedPrice(item.basePrice)
                        val canAfford = user.playerData.coins >= finalPrice
                        val isShieldOwned = item.id == "shield" && user.playerData.shield

                        TerminalCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shop_item_${item.id}"),
                            borderColor = if (isShieldOwned) theme.tertiary else theme.primaryDim,
                            backgroundColor = theme.surface1.copy(alpha = 0.9f)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(theme.surface2)
                                            .border(1.dp, theme.primaryDim, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = item.icon, fontSize = 24.sp)
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = theme.textLight,
                                                fontWeight = FontWeight.Black
                                            )

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (gameState.flashSaleActive) {
                                                    Text(
                                                        text = "${item.basePrice}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = theme.textGray,
                                                        textDecoration = TextDecoration.LineThrough
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }
                                                Text(
                                                    text = "🪙 $finalPrice",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = theme.secondary,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = item.desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = theme.textGray
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (isShieldOwned) {
                                        TerminalBadge(
                                            text = "🛡️ SHIELD DEPLOYED",
                                            color = theme.tertiary,
                                            backgroundColor = theme.tertiary.copy(alpha = 0.2f)
                                        )
                                    } else {
                                        TerminalButton(
                                            text = when (item.id) {
                                                "curse" -> "DEPLOY BIO-CURSE"
                                                "revive" -> "USE DEFIBRILLATOR"
                                                "shield" -> "EQUIP SHIELD"
                                                else -> "PURCHASE"
                                            },
                                            onClick = { onBuyItem(item) },
                                            enabled = canAfford,
                                            modifier = Modifier.fillMaxWidth(),
                                            icon = "🛒",
                                            testTag = "buy_btn_${item.id}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ShopTab.TITLES -> {
                items(gameState.shopTitles, key = { it.name }) { title ->
                    val finalPrice = calculateDiscountedPrice(title.price)
                    val isOwned = user.playerData.ownedTitles.contains(title.name)
                    val isEquipped = user.playerData.title == title.name
                    val canAfford = user.playerData.coins >= finalPrice

                    TerminalCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_title_${title.name.lowercase().replace(" ", "_")}"),
                        borderColor = if (isEquipped) theme.primary else theme.primaryDim,
                        backgroundColor = theme.surface1.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isEquipped) theme.primary else theme.textLight,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isEquipped) "CURRENTLY EQUIPPED" else if (isOwned) "OWNED IN ARSENAL" else "Honorary survivor designation",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isEquipped) theme.primary else theme.textGray
                                )
                            }

                            if (isEquipped) {
                                TerminalBadge(text = "EQUIPPED", color = theme.primary)
                            } else if (isOwned) {
                                TerminalButton(
                                    text = "EQUIP",
                                    onClick = { onBuyTitle(title) },
                                    isPrimary = false,
                                    testTag = "equip_title_${title.name.take(6)}"
                                )
                            } else {
                                TerminalButton(
                                    text = "🪙 $finalPrice",
                                    onClick = { onBuyTitle(title) },
                                    enabled = canAfford,
                                    testTag = "buy_title_${title.name.take(6)}"
                                )
                            }
                        }
                    }
                }
            }

            ShopTab.THEMES -> {
                items(gameState.shopThemes, key = { it.id }) { shopTheme ->
                    val finalPrice = calculateDiscountedPrice(shopTheme.price)
                    val isOwned = user.playerData.ownedThemes.contains(shopTheme.id)
                    val isSelected = user.playerData.selectedTheme == shopTheme.id
                    val canAfford = user.playerData.coins >= finalPrice
                    val config = getTerminalTheme(shopTheme.id)

                    TerminalCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_theme_${shopTheme.id}"),
                        borderColor = if (isSelected) config.primary else theme.primaryDim,
                        backgroundColor = theme.surface1.copy(alpha = 0.9f)
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(config.bgDark)
                                        .border(2.dp, config.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(config.secondary)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = shopTheme.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = config.primary,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = config.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = theme.textGray
                                    )
                                }
                            }

                            if (isSelected) {
                                TerminalBadge(text = "ACTIVE", color = config.primary)
                            } else if (isOwned) {
                                TerminalButton(
                                    text = "ACTIVATE",
                                    onClick = { onBuyTheme(shopTheme) },
                                    isPrimary = false,
                                    testTag = "activate_theme_${shopTheme.id}"
                                )
                            } else {
                                TerminalButton(
                                    text = "🪙 $finalPrice",
                                    onClick = { onBuyTheme(shopTheme) },
                                    enabled = canAfford,
                                    testTag = "buy_theme_${shopTheme.id}"
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

    // Curse Target Modal Sheet
    if (showCurseModal) {
        val targets = allUsers.filter { it.uid != user.uid }
        var selectedTargetUid by remember { mutableStateOf<String?>(null) }

        ModalBottomSheet(
            onDismissRequest = onDismissCurseModal,
            sheetState = sheetState,
            containerColor = theme.surface1,
            contentColor = theme.textLight,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SELECT CURSE TARGET",
                        style = MaterialTheme.typography.titleLarge,
                        color = theme.error,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismissCurseModal) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = theme.textGray)
                    }
                }

                Text(
                    text = "Deploy bio-curse beacon. If target possesses a Tactical Shield, it shatters. Otherwise inflicts +1 casualty.",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.textGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                targets.forEach { target ->
                    val isSelected = selectedTargetUid == target.uid
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isSelected) theme.error else theme.primaryDim,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTargetUid = target.uid }
                            .testTag("curse_target_${target.displayName.lowercase()}"),
                        color = if (isSelected) theme.error.copy(alpha = 0.15f) else theme.surface2
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
                                    avatar = target.playerData.avatar,
                                    colorHex = target.playerData.color,
                                    size = 38.dp,
                                    hasShield = target.playerData.shield
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = target.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = theme.textLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Casualties: ${target.playerData.totalDeaths} | Shield: ${if (target.playerData.shield) "ACTIVE 🛡️" else "OFFLINE"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (target.playerData.shield) theme.tertiary else theme.textGray
                                    )
                                }
                            }

                            if (isSelected) {
                                TerminalBadge(text = "TARGETED", color = theme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TerminalButton(
                    text = "EXECUTE BIO-CURSE STRIKE",
                    onClick = {
                        val targetUid = selectedTargetUid
                        if (targetUid != null) {
                            onExecuteCurse(targetUid)
                        }
                    },
                    enabled = selectedTargetUid != null,
                    modifier = Modifier.fillMaxWidth(),
                    icon = "☣️",
                    testTag = "btn_confirm_curse"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Revive Sector Modal Sheet
    if (showReviveModal) {
        val eligibleSectors = user.playerData.games.filter { it.value.deaths > 0 }.keys.toList()
        var selectedSectorToRevive by remember { mutableStateOf<String?>(eligibleSectors.firstOrNull()) }

        ModalBottomSheet(
            onDismissRequest = onDismissReviveModal,
            sheetState = sheetState,
            containerColor = theme.surface1,
            contentColor = theme.textLight,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "EMERGENCY DEFIBRILLATOR",
                        style = MaterialTheme.typography.titleLarge,
                        color = theme.success,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismissReviveModal) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = theme.textGray)
                    }
                }

                Text(
                    text = "Select a combat sector with logged casualties to subtract 1 death count.",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.textGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (eligibleSectors.isEmpty()) {
                    Text(
                        text = "You have 0 casualties across all combat sectors. Defibrillator not needed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.textGray
                    )
                } else {
                    eligibleSectors.forEach { sector ->
                        val isSelected = selectedSectorToRevive == sector
                        val deaths = user.playerData.games[sector]?.deaths ?: 0

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) theme.success else theme.primaryDim,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedSectorToRevive = sector }
                                .testTag("revive_sector_${sector.take(9)}"),
                            color = if (isSelected) theme.success.copy(alpha = 0.15f) else theme.surface2
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = sector,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = theme.textLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Current Casualties: $deaths -> Will become: ${deaths - 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = theme.success
                                    )
                                }

                                if (isSelected) {
                                    TerminalBadge(text = "SELECTED", color = theme.success)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TerminalButton(
                    text = "DISCHARGE DEFIBRILLATOR (-1 CASUALTY)",
                    onClick = {
                        val sec = selectedSectorToRevive
                        if (sec != null) {
                            onExecuteRevive(sec)
                        }
                    },
                    enabled = selectedSectorToRevive != null,
                    modifier = Modifier.fillMaxWidth(),
                    icon = "💉",
                    testTag = "btn_confirm_revive"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
