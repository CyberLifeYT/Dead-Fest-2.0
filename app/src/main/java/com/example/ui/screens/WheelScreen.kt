package com.example.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameState
import com.example.data.model.User
import com.example.data.model.WheelSegment
import com.example.ui.components.TerminalBadge
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalCard
import com.example.ui.theme.TerminalTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WheelScreen(
    user: User,
    gameState: GameState,
    isSpinning: Boolean,
    rotationAngle: Float,
    cooldownRemaining: Int,
    resultSegment: WheelSegment?,
    onSpin: () -> Unit,
    onAcknowledgeResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    val items = gameState.wheelItems

    // Animate rotation smoothly
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(
            durationMillis = if (isSpinning) 3200 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "wheel_anim"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner
        TerminalCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = theme.primary.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎡", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "WHEEL OF FATE",
                            style = MaterialTheme.typography.titleMedium,
                            color = theme.primary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Test your survival probability. Cooldown: 5.0s",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.textGray
                        )
                    }
                }

                TerminalBadge(
                    text = if (cooldownRemaining > 0) "COOLDOWN ${cooldownRemaining}s" else "READY",
                    color = if (cooldownRemaining > 0) theme.secondary else theme.success
                )
            }
        }

        // Wheel Canvas Assembly
        Box(
            modifier = Modifier
                .size(310.dp)
                .testTag("wheel_container"),
            contentAlignment = Alignment.Center
        ) {
            // Pointer at Top
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .size(width = 24.dp, height = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▼",
                    color = theme.secondary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Spinning Wheel Canvas
            Canvas(
                modifier = Modifier
                    .size(270.dp)
                    .clip(CircleShape)
                    .rotate(animatedRotation)
            ) {
                drawWheel(items = items, theme = theme)
            }

            // Outer Neon Ring
            Box(
                modifier = Modifier
                    .size(274.dp)
                    .clip(CircleShape)
                    .border(3.dp, theme.primary, CircleShape)
            )

            // Center Cyber Hub
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(theme.surface1)
                    .border(2.dp, theme.secondary, CircleShape)
                    .shadow(12.dp, CircleShape, spotColor = theme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "☣️",
                    fontSize = 22.sp
                )
            }
        }

        // Spin Action Controls
        TerminalButton(
            text = if (isSpinning) "SPINNING FATE MATRIX…" else if (cooldownRemaining > 0) "RECHARGING (${cooldownRemaining}s)" else "SPIN THE WHEEL OF FATE",
            onClick = onSpin,
            enabled = !isSpinning && cooldownRemaining == 0 && gameState.wheelEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            icon = "⚡",
            testTag = "btn_spin_wheel"
        )

        if (!gameState.wheelEnabled) {
            Text(
                text = "WHEEL DEACTIVATED BY OVERSEER TERMINAL",
                style = MaterialTheme.typography.labelSmall,
                color = theme.error,
                fontWeight = FontWeight.Bold
            )
        }

        // Payout Matrix Legend
        TerminalCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = theme.primaryDim,
            backgroundColor = theme.surface1.copy(alpha = 0.85f)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FATE PROBABILITY MATRIX",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.textLight,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(10.dp))
                items.forEach { segment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val segColor = try {
                            Color(android.graphics.Color.parseColor(segment.colorHex))
                        } catch (_: Exception) {
                            theme.primary
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(segColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = segment.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.textLight
                            )
                        }

                        Text(
                            text = "WEIGHT: ${segment.weight}",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.textGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Outcome Modal Dialog
    if (resultSegment != null) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = theme.surface1,
            titleContentColor = theme.textLight,
            textContentColor = theme.textLight,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FATE HAS SPOKEN",
                        style = MaterialTheme.typography.titleLarge,
                        color = theme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TERMINAL OUTCOME:",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.textGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, theme.primary, RoundedCornerShape(12.dp)),
                        color = theme.surface2
                    ) {
                        Text(
                            text = resultSegment.label,
                            style = MaterialTheme.typography.headlineMedium,
                            color = theme.secondary,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = when (resultSegment.action) {
                            "coins_50" -> "+50 Coins will be credited to your vault."
                            "coins_100" -> "+100 Coins will be credited to your vault."
                            "coins_250" -> "+250 Coins will be credited to your vault."
                            "coins_500" -> "🔥 JACKPOT! +500 Coins awarded!"
                            "shield" -> "🛡️ Tactical Kinetic Shield deployed!"
                            "death_plus_1" -> "💀 Casualty +1 recorded in your primary sector."
                            "bankrupt" -> "💥 CRITICAL CORRUPTION: All coins wiped!"
                            else -> "🎁 Mystery crate telemetry received!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.textLight,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TerminalButton(
                    text = "ACKNOWLEDGE OUTCOME",
                    onClick = onAcknowledgeResult,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_acknowledge_wheel"
                )
            }
        )
    }
}

fun DrawScope.drawWheel(
    items: List<WheelSegment>,
    theme: com.example.ui.theme.TerminalThemeConfig
) {
    if (items.isEmpty()) return
    val sliceAngle = 360f / items.size
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    val paint = Paint().apply {
        isAntiAlias = true
        textSize = 28f
        typeface = Typeface.MONOSPACE
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.RIGHT
        isFakeBoldText = true
    }

    items.forEachIndexed { index, segment ->
        val startAngle = index * sliceAngle - 90f

        val segColor = try {
            Color(android.graphics.Color.parseColor(segment.colorHex))
        } catch (_: Exception) {
            if (index % 2 == 0) theme.primary else theme.surface3
        }

        // Draw Slice Arc
        drawArc(
            color = segColor,
            startAngle = startAngle,
            sweepAngle = sliceAngle,
            useCenter = true,
            size = Size(radius * 2, radius * 2),
            topLeft = Offset(center.x - radius, center.y - radius)
        )

        // Draw Divider
        val angleRad = Math.toRadians((startAngle).toDouble())
        val endX = (center.x + radius * cos(angleRad)).toFloat()
        val endY = (center.y + radius * sin(angleRad)).toFloat()
        drawLine(
            color = Color.Black.copy(alpha = 0.5f),
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 3f
        )

        // Draw Text Label inside Slice
        val midAngle = startAngle + sliceAngle / 2f
        val midRad = Math.toRadians(midAngle.toDouble())

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.save()
            canvas.nativeCanvas.rotate(midAngle, center.x, center.y)
            canvas.nativeCanvas.drawText(
                segment.label.take(12),
                center.x + radius - 20f,
                center.y + 8f,
                paint
            )
            canvas.nativeCanvas.restore()
        }
    }

    // Outer wheel border
    drawCircle(
        color = theme.primaryDim,
        radius = radius,
        center = center,
        style = Stroke(width = 4f)
    )
}
