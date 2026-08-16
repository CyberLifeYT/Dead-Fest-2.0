package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CrtScanlineOverlay
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalCard
import com.example.ui.theme.TerminalTheme

data class QuickSurvivorOption(
    val title: String,
    val email: String,
    val pass: String,
    val avatar: String
)

val QUICK_SURVIVORS = listOf(
    QuickSurvivorOption("Overlord (Admin)", "commander@deadfest.terminal", "overlord123", "☣️"),
    QuickSurvivorOption("Neon Reaper", "reaper@wasteland.net", "reaper123", "💀"),
    QuickSurvivorOption("Cyber Valkyrie", "valk@neo-haven.org", "valkyrie123", "⚡")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onForgotPassword: (String) -> Unit,
    isLoading: Boolean = false,
    performanceMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    var isRegisterMode by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("commander@deadfest.terminal") }
    var password by remember { mutableStateOf("overlord123") }
    var confirmPassword by remember { mutableStateOf("") }
    var callsign by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var inlineNotice by remember { mutableStateOf<String?>(null) }
    var isNoticeError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bgDark),
        contentAlignment = Alignment.Center
    ) {
        CrtScanlineOverlay(alpha = 0.04f, enabled = !performanceMode)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emblem
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(theme.surface2)
                    .border(2.dp, theme.primary, CircleShape)
                    .shadow(16.dp, CircleShape, spotColor = theme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "☣️", fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "DEAD-FEST TERMINAL",
                style = MaterialTheme.typography.headlineLarge,
                color = theme.textLight,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )

            Text(
                text = "SECURE SURVIVOR NEURAL ACCESS // FIREBASE ACTIVE",
                style = MaterialTheme.typography.labelSmall,
                color = theme.secondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Auth Card
            TerminalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                shape = RoundedCornerShape(20.dp),
                borderColor = theme.primary.copy(alpha = 0.4f),
                backgroundColor = theme.surface1.copy(alpha = 0.95f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Switcher Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.surface2)
                            .border(1.dp, theme.primaryDim, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isRegisterMode) theme.primary else Color.Transparent)
                                .clickable {
                                    isRegisterMode = false
                                    inlineNotice = null
                                }
                                .padding(vertical = 10.dp)
                                .testTag("auth_tab_login"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LOGIN",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (!isRegisterMode) theme.bgDark else theme.textGray,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isRegisterMode) theme.primary else Color.Transparent)
                                .clickable {
                                    isRegisterMode = true
                                    inlineNotice = null
                                }
                                .padding(vertical = 10.dp)
                                .testTag("auth_tab_register"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NEW SURVIVOR (SIGN UP)",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isRegisterMode) theme.bgDark else theme.textGray,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Callsign input (when registering)
                    AnimatedVisibility(
                        visible = isRegisterMode,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = callsign,
                                onValueChange = { callsign = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_input_callsign"),
                                label = { Text("CALLSIGN / CODENAME", color = theme.textGray) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = theme.primary)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = theme.primary,
                                    unfocusedBorderColor = theme.surface3,
                                    focusedTextColor = theme.textLight,
                                    unfocusedTextColor = theme.textLight,
                                    focusedContainerColor = theme.surface2,
                                    unfocusedContainerColor = theme.surface2
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_input_email"),
                        label = { Text("SURVIVOR EMAIL", color = theme.textGray) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = theme.primary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.surface3,
                            focusedTextColor = theme.textLight,
                            unfocusedTextColor = theme.textLight,
                            focusedContainerColor = theme.surface2,
                            unfocusedContainerColor = theme.surface2
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_input_password"),
                        label = { Text("SECURITY PASSPHRASE (MIN 6 CHARS)", color = theme.textGray) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = theme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password",
                                    tint = theme.textGray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.surface3,
                            focusedTextColor = theme.textLight,
                            unfocusedTextColor = theme.textLight,
                            focusedContainerColor = theme.surface2,
                            unfocusedContainerColor = theme.surface2
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (isRegisterMode) ImeAction.Next else ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (!isRegisterMode) onLogin(email, password)
                        }),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Confirm Password (when registering)
                    AnimatedVisibility(
                        visible = isRegisterMode,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_input_confirm_password"),
                                label = { Text("CONFIRM PASSPHRASE", color = theme.textGray) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = theme.primary)
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (confirmPassword.isNotEmpty() && confirmPassword != password) theme.error else theme.primary,
                                    unfocusedBorderColor = theme.surface3,
                                    focusedTextColor = theme.textLight,
                                    unfocusedTextColor = theme.textLight,
                                    focusedContainerColor = theme.surface2,
                                    unfocusedContainerColor = theme.surface2
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Forgot Password / Reset action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "LOST PASSPHRASE? [SEND RESET KEY]",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.secondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    if (email.isBlank() || !email.contains("@")) {
                                        inlineNotice = "Please enter your email above to receive a reset key."
                                        isNoticeError = true
                                    } else {
                                        onForgotPassword(email)
                                        inlineNotice = "RESET PROTOCOL: Password reset link dispatched to $email."
                                        isNoticeError = false
                                    }
                                }
                                .padding(vertical = 4.dp)
                                .testTag("auth_reset_link")
                        )
                    }

                    // Inline notice banner
                    if (inlineNotice != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNoticeError) theme.error.copy(alpha = 0.15f) else theme.success.copy(alpha = 0.15f))
                                .border(1.dp, if (isNoticeError) theme.error else theme.success, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = inlineNotice ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isNoticeError) theme.error else theme.success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Action
                    TerminalButton(
                        text = if (isLoading) "AUTHENTICATING..." else if (isRegisterMode) "CREATE SURVIVOR ACCOUNT" else "INITIALIZE TERMINAL UPLINK",
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                inlineNotice = "AUTHENTICATION ERROR: Email and password required."
                                isNoticeError = true
                            } else if (password.length < 6) {
                                inlineNotice = "AUTHENTICATION ERROR: Passphrase must be at least 6 characters."
                                isNoticeError = true
                            } else if (isRegisterMode && confirmPassword.isNotEmpty() && password != confirmPassword) {
                                inlineNotice = "AUTHENTICATION ERROR: Passwords do not match."
                                isNoticeError = true
                            } else {
                                inlineNotice = null
                                if (isRegisterMode) {
                                    onRegister(email, password, callsign.ifBlank { email.substringBefore("@") })
                                } else {
                                    onLogin(email, password)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = if (isLoading) "⏳" else if (isRegisterMode) "⚡" else "🔓",
                        testTag = "auth_submit_btn"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Google Satellite Sign In
                    TerminalButton(
                        text = "SATELLITE UPLINK (GOOGLE / CREDENTIAL MESH)",
                        onClick = onGoogleSignIn,
                        modifier = Modifier.fillMaxWidth(),
                        isPrimary = false,
                        icon = "🛰️",
                        testTag = "auth_google_btn"
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Demo Survivor Selector Chips (1-tap fill for fast testing)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FAST-FILL SURVIVOR CREDENTIALS (1-TAP TEST):",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.textGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QUICK_SURVIVORS.forEach { opt ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.surface2)
                                .border(1.dp, theme.surface3, RoundedCornerShape(12.dp))
                                .clickable {
                                    email = opt.email
                                    password = opt.pass
                                    isRegisterMode = false
                                    inlineNotice = "Selected ${opt.title} profile"
                                    isNoticeError = false
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("quick_fill_${opt.email.take(6)}"),
                            color = Color.Transparent
                        ) {
                            Text(
                                text = "${opt.avatar} ${opt.title}",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.textLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DEAD-FEST ENCRYPTION MESH // NO UNAUTHORIZED SURVIVORS",
                style = MaterialTheme.typography.labelSmall,
                color = theme.textGray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
