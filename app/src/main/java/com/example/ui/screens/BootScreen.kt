package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CrtScanlineOverlay
import com.example.ui.theme.TerminalTheme

@Composable
fun BootScreen(
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060608)),
        contentAlignment = Alignment.Center
    ) {
        CrtScanlineOverlay(alpha = 0.08f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Biohazard symbol with glowing cyber aura
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(theme.primary.copy(alpha = 0.12f))
                    .border(2.dp, theme.primary, CircleShape)
                    .shadow(24.dp, CircleShape, spotColor = theme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "☣️",
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "DEAD-FEST TERMINAL",
                style = MaterialTheme.typography.headlineLarge,
                color = theme.textLight,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ESTABLISHING SATELLITE UPLINK…",
                style = MaterialTheme.typography.labelMedium,
                color = theme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Animated progress bar
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.surface2)
                    .border(1.dp, theme.primaryDim, RoundedCornerShape(4.dp))
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = theme.primary,
                    trackColor = theme.surface2
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SECURE PROTOCOL v3.8.4 // DECRYPTING ENCLAVE MESH",
                style = MaterialTheme.typography.labelSmall,
                color = theme.textGray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
