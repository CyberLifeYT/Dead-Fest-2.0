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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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

@Composable
fun AuthScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = TerminalTheme.current
    var isRegisterMode by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("commander@deadfest.terminal") }
    var password by remember { mutableStateOf("overlord123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var inlineNotice by remember { mutableStateOf<String?>(null) }
    var isNoticeError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bgDark),
        contentAlignment = Alignment.Center
    ) {
        CrtScanlineOverlay(alpha = 0.05f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Biohazard Mark
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(theme.surface2)
                    .border(2.dp, theme.primary, CircleShape)
                    .shadow(16.dp, CircleShape, spotColor = theme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "☣️", fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DEAD-FEST TERMINAL",
                style = MaterialTheme.typography.headlineLarge,
                color = theme.textLight,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "RESTRICTED SURVIVOR NETWORK ACCESS",
                style = MaterialTheme.typography.labelSmall,
                color = theme.secondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Auth Box
            TerminalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                shape = RoundedCornerShape(20.dp),
                borderColor = theme.primary.copy(alpha = 0.4f),
                backgroundColor = theme.surface1.copy(alpha = 0.95f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Mode Tabs (LOGIN / REGISTER)
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
                                text = "REGISTER",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isRegisterMode) theme.bgDark else theme.textGray,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_input_email"),
                        label = { Text("SURVIVOR EMAIL / CALLSIGN", color = theme.textGray) },
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_input_password"),
                        label = { Text("ENCLAVE PASSPHRASE", color = theme.textGray) },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (isRegisterMode) onRegister(email, password) else onLogin(email, password)
                        }),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Options Row: Remember Me & Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = theme.primary,
                                    uncheckedColor = theme.textGray,
                                    checkmarkColor = theme.bgDark
                                )
                            )
                            Text(
                                text = "REMEMBER ME",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.textLight,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "RESET KEY",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.secondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                inlineNotice = "RESET PROTOCOL: Password reset link transmitted to $email."
                                isNoticeError = false
                            }
                        )
                    }

                    // Inline notice banner
                    if (inlineNotice != null) {
                        Spacer(modifier = Modifier.height(12.dp))
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    TerminalButton(
                        text = if (isRegisterMode) "INITIALIZE ACCESS" else "AUTHORIZE ACCESS",
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                inlineNotice = "AUTHENTICATION ERROR: Callsign and passphrase required."
                                isNoticeError = true
                            } else {
                                if (isRegisterMode) onRegister(email, password) else onLogin(email, password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = if (isRegisterMode) "⚡" else "🔓",
                        testTag = "auth_submit_btn"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Satellite Uplink (Google)
                    TerminalButton(
                        text = "SATELLITE UPLINK (GOOGLE)",
                        onClick = onGoogleSignIn,
                        modifier = Modifier.fillMaxWidth(),
                        isPrimary = false,
                        icon = "🛰️",
                        testTag = "auth_google_btn"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
