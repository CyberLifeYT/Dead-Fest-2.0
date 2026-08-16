package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerminalTheme
import com.example.ui.viewmodel.AppNavTab

@Composable
fun CrtScanlineOverlay(
    alpha: Float = 0.03f,
    enabled: Boolean = true
) {
    if (!enabled || alpha <= 0f) return

    Canvas(modifier = Modifier.fillMaxSize()) {
        val scanlineSpacing = 6.dp.toPx()
        val numLines = (size.height / scanlineSpacing).toInt()
        val lineColor = Color.White.copy(alpha = alpha)

        for (i in 0..numLines) {
            val y = i * scanlineSpacing
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
fun TerminalHeader(
    eyebrow: String,
    title: String,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp), spotColor = theme.primary),
        color = theme.bgDark.copy(alpha = 0.98f),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = theme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(theme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = eyebrow.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = theme.textLight,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    trailingContent?.invoke()

                    // Online live indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surface2)
                            .border(1.dp, theme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LIVE 🟢",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.surface2)
                            .border(1.dp, theme.surface3, RoundedCornerShape(10.dp))
                            .testTag("terminal_refresh_btn"),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = theme.textLight
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = if (isRefreshing) Modifier.rotate(rotation) else Modifier,
                            tint = theme.textLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TerminalCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val theme = TerminalTheme.current
    val strokeColor = borderColor ?: theme.surface3
    val containerColor = backgroundColor ?: theme.surface1

    val baseModifier = modifier
        .clip(shape)
        .background(containerColor)
        .border(1.dp, strokeColor, shape)

    val clickableModifier = if (onClick != null) {
        baseModifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else {
        baseModifier
    }

    Box(modifier = clickableModifier.padding(14.dp)) {
        content()
    }
}

@Composable
fun TerminalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String? = null,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    testTag: String = "terminal_btn"
) {
    val theme = TerminalTheme.current

    if (isPrimary) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .heightIn(min = 50.dp)
                .testTag(testTag)
                .shadow(if (enabled) 8.dp else 0.dp, RoundedCornerShape(16.dp), spotColor = theme.primary),
            colors = ButtonDefaults.buttonColors(
                containerColor = theme.primary,
                contentColor = Color.Black,
                disabledContainerColor = theme.surface3,
                disabledContentColor = theme.textGray
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Text(text = icon, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = Color.Black
                )
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .heightIn(min = 48.dp)
                .testTag(testTag),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = theme.surface2,
                contentColor = theme.textLight
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = enabled).copy(
                brush = Brush.horizontalGradient(listOf(theme.surface3, theme.surface3))
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Text(text = icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = text.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = theme.textLight
                )
            }
        }
    }
}

@Composable
fun TerminalBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    backgroundColor: Color? = null
) {
    val theme = TerminalTheme.current
    val fg = color ?: theme.primary
    val bg = backgroundColor ?: fg.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            fontSize = 9.sp
        )
    }
}

@Composable
fun SurvivorAvatar(
    avatar: String,
    colorHex: String,
    size: Dp = 44.dp,
    hasShield: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        TerminalTheme.current.primary
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(TerminalTheme.current.surface2)
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = avatar,
            fontSize = (size.value * 0.5f).sp,
            textAlign = TextAlign.Center
        )

        if (hasShield) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size((size.value * 0.4f).dp)
                    .clip(CircleShape)
                    .background(TerminalTheme.current.tertiary)
                    .border(1.dp, TerminalTheme.current.bgDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🛡️",
                    fontSize = (size.value * 0.22f).sp
                )
            }
        }
    }
}

@Composable
fun TerminalBottomNav(
    activeTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    isAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    val tabs = AppNavTab.entries

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), spotColor = theme.primary),
        color = theme.surface1.copy(alpha = 0.98f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.surface3, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isSelected = tab == activeTab
                val itemBg = if (isSelected) theme.primary.copy(alpha = 0.15f) else Color.Transparent
                val itemBorder = if (isSelected) theme.primary.copy(alpha = 0.4f) else Color.Transparent
                val textColor = if (isSelected) theme.primary else theme.textGray

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(itemBg)
                        .border(1.dp, itemBorder, RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tab.icon,
                        fontSize = if (isSelected) 18.sp else 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (tab) {
                            AppNavTab.DASHBOARD -> "OVERVIEW"
                            AppNavTab.PLAYERS -> "SURVIVORS"
                            AppNavTab.MARKET -> "MARKET"
                            AppNavTab.WHEEL -> "WHEEL"
                            AppNavTab.COMMS -> "INTEL"
                            AppNavTab.SETTINGS -> "SYSTEM"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        letterSpacing = 0.4.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmergencyBroadcastBanner(
    text: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(theme.error.copy(alpha = 0.15f))
            .border(1.dp, theme.error.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alert",
                tint = theme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EMERGENCY SYSTEM BROADCAST",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.error,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.textLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "[ DISMISS ]",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }
        }
    }
}
