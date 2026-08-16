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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameState
import com.example.data.model.ShopItem
import com.example.data.model.ShopTheme
import com.example.data.model.ShopTitle
import com.example.data.model.User
import com.example.data.model.WheelSegment
import com.example.ui.components.SurvivorAvatar
import com.example.ui.components.TerminalBadge
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalCard
import com.example.ui.theme.TerminalTheme
import com.example.ui.theme.getTerminalTheme

enum class ShopTab(val label: String, val icon: String) {
    GEAR("TACTICAL GEAR", "🛡️"),
    TITLES("TITLES & THEMES", "🎖️"),
    WHEEL("FATE WHEEL", "🎡")
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
    isWheelSpinning: Boolean = false,
    wheelRotation: Float = 0f,
    wheelResult: WheelSegment? = null,
    wheelCooldown: Int = 0,
    onSpinWheel: () -> Unit = {},
    onAcknowledgeWheelResult: () -> Unit = {},
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

    if (activeShopTab == ShopTab.WHEEL) {
        WheelScreen(
            user = user,
            gameState = gameState,
            isSpinning = isWheelSpinning,
            rotationAngle = wheelRotation,
            cooldownRemaining = wheelCooldown,
            resultSegment = wheelResult,
            onSpin = onSpinWheel,
            onAcknowledgeResult = onAcknowledgeWheelResult,
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // Wallet Balance Card
        item {
            TerminalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_wallet_card"),
                borderColor = if (gameState.flashSaleActive) theme.secondary else theme.surface3,
                backgroundColor = theme.surface1
            ) {
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
                                text = "VAULT BALANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.textGray
                            )
                            Text(
                                text = "${user.playerData.coins} COINS",
                                style = MaterialTheme.typography.titleLarge,
                                color = theme.secondary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    if (gameState.flashSaleActive) {
                        TerminalBadge(
                            text = "SALE -${gameState.flashSaleDiscount}%",
                            color = theme.secondary,
                            backgroundColor = theme.secondary.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        // Tabs Header (GEAR, TITLES, WHEEL)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surface2)
                    .border(1.dp, theme.surface3, RoundedCornerShape(12.dp))
                    .padding(3.dp)
            ) {
                ShopTab.entries.forEach { tab ->
                    val isSelected = activeShopTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) theme.primary else Color.Transparent)
                            .clickable { activeShopTab = tab }
                            .padding(vertical = 8.dp)
                            .testTag("shop_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = tab.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tab.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.Black else theme.textLight,
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
                            text = "NO ITEMS IN BLACK MARKET INVENTORY.",
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
                            borderColor = if (isShieldOwned) theme.tertiary else theme.surface3,
                            backgroundColor = theme.surface1
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(theme.surface2),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = item.icon, fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = theme.textLight,
                                            fontWeight = FontWeight.Black
                                        )
                                        if (isShieldOwned) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            TerminalBadge(text = "ACTIVE", color = theme.tertiary)
                                        }
                                    }
                                    Text(
                                        text = item.desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = theme.textGray
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable(enabled = canAfford && !isShieldOwned) { onBuyItem(item) }
                                        .testTag("buy_btn_${item.id}"),
                                    color = if (isShieldOwned) theme.surface3 else if (canAfford) theme.primary else theme.surface2,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (canAfford) theme.primary else theme.surface3
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (isShieldOwned) {
                                            Text(
                                                text = "EQUIPPED",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = theme.textGray,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            if (gameState.flashSaleActive) {
                                                Text(
                                                    text = "${item.basePrice}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.Black.copy(alpha = 0.5f),
                                                    textDecoration = TextDecoration.LineThrough,
                                                    fontSize = 9.sp
                                                )
                                            }
                                            Text(
                                                text = "$finalPrice 🪙",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (canAfford) Color.Black else theme.textGray,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ShopTab.TITLES -> {
                item {
                    Text(
                        text = "HONOR TITLES",
                        style = MaterialTheme.typography.labelMedium,
                        color = theme.secondary,
                        fontWeight = FontWeight.Black
                    )
                }

                items(gameState.shopTitles, key = { it.name }) { titleItem ->
                    val isOwned = user.playerData.ownedTitles.contains(titleItem.name)
                    val isEquipped = user.playerData.title == titleItem.name
                    val canAfford = user.playerData.coins >= titleItem.price

                    TerminalCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (isEquipped) theme.primary else theme.surface3
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = titleItem.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = theme.textLight,
                                        fontWeight = FontWeight.Black
                                    )
                                    if (isEquipped) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        TerminalBadge(text = "EQUIPPED", color = theme.primary)
                                    } else if (isOwned) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        TerminalBadge(text = "OWNED", color = theme.success)
                                    }
                                }
                                Text(
                                    text = "Survivor Custom Honorific",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = theme.textGray
                                )
                            }

                            if (!isOwned) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(enabled = canAfford) { onBuyTitle(titleItem) },
                                    color = if (canAfford) theme.secondary else theme.surface2
                                ) {
                                    Text(
                                        text = "${titleItem.price} 🪙",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (canAfford) Color.Black else theme.textGray,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TERMINAL THEMES",
                        style = MaterialTheme.typography.labelMedium,
                        color = theme.secondary,
                        fontWeight = FontWeight.Black
                    )
                }

                items(gameState.shopThemes, key = { it.id }) { themeItem ->
                    val isOwned = user.playerData.ownedThemes.contains(themeItem.id)
                    val isEquipped = user.playerData.selectedTheme == themeItem.id
                    val canAfford = user.playerData.coins >= themeItem.price

                    TerminalCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (isEquipped) theme.primary else theme.surface3
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = themeItem.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = theme.textLight,
                                        fontWeight = FontWeight.Black
                                    )
                                    if (isEquipped) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        TerminalBadge(text = "ACTIVE", color = theme.primary)
                                    } else if (isOwned) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        TerminalBadge(text = "OWNED", color = theme.success)
                                    }
                                }
                                Text(
                                    text = "Custom CRT Phosphor Matrix",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = theme.textGray
                                )
                            }

                            if (!isOwned) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(enabled = canAfford) { onBuyTheme(themeItem) },
                                    color = if (canAfford) theme.primary else theme.surface2
                                ) {
                                    Text(
                                        text = "${themeItem.price} 🪙",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (canAfford) Color.Black else theme.textGray,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            ShopTab.WHEEL -> {
                // Handled at top of composable
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Curse Target Modal Sheet
    if (showCurseModal) {
        ModalBottomSheet(
            onDismissRequest = onDismissCurseModal,
            sheetState = sheetState,
            containerColor = theme.surface1,
            contentColor = theme.textLight,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
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
                    Column {
                        Text(
                            text = "SELECT CURSE TARGET",
                            style = MaterialTheme.typography.titleMedium,
                            color = theme.error,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Target will receive +1 death unless protected by a shield.",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textGray
                        )
                    }
                    IconButton(onClick = onDismissCurseModal) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = theme.textGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                allUsers.filter { it.uid != user.uid }.forEach { targetUser ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onExecuteCurse(targetUser.uid) },
                        color = theme.surface2
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
                                    avatar = targetUser.playerData.avatar,
                                    colorHex = targetUser.playerData.color,
                                    size = 36.dp,
                                    hasShield = targetUser.playerData.shield
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = targetUser.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = theme.textLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = targetUser.playerData.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = theme.textGray
                                    )
                                }
                            }

                            if (targetUser.playerData.shield) {
                                TerminalBadge(text = "SHIELDED 🛡️", color = theme.tertiary)
                            } else {
                                Text(
                                    text = "CAST ☣️",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = theme.error,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Revive Sector Modal Sheet
    if (showReviveModal) {
        ModalBottomSheet(
            onDismissRequest = onDismissReviveModal,
            sheetState = sheetState,
            containerColor = theme.surface1,
            contentColor = theme.textLight,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
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
                    Column {
                        Text(
                            text = "SELECT SECTOR TO REVIVE",
                            style = MaterialTheme.typography.titleMedium,
                            color = theme.success,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Reduces your death count in selected sector by 1.",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textGray
                        )
                    }
                    IconButton(onClick = onDismissReviveModal) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = theme.textGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                gameState.games.forEach { sector ->
                    val userDeaths = user.playerData.games[sector]?.deaths ?: 0
                    val canRevive = userDeaths > 0

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = canRevive) { onExecuteRevive(sector) },
                        color = if (canRevive) theme.surface2 else theme.surface2.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = sector,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (canRevive) theme.textLight else theme.textGray,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = if (canRevive) "$userDeaths deaths (-1 💉)" else "0 deaths (clean)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (canRevive) theme.success else theme.textGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
