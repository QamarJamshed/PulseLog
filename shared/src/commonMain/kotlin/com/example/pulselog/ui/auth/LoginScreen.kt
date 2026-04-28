package com.example.pulselog.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.pulselog.platform
import com.example.pulselog.PlatformType
import com.example.pulselog.ui.home.HomeScreen
import com.example.pulselog.ui.mistake.MistakeEntryScreen

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<AuthViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val focusRequester = remember { FocusRequester() }
        
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var pin by remember { mutableStateOf("") }
        var isFetchingUser by remember { mutableStateOf(false) }
        
        val error by viewModel.loginError.collectAsState()

        // Auto-fetch password when username changes
        LaunchedEffect(username) {
            if (username.isNotBlank()) {
                isFetchingUser = true
                val user = viewModel.getUserByUsername(username)
                if (user != null) {
                    password = user.password
                    // Auto focus the PIN field when user is recognized
                    // Added a small delay to prevent crash on focus request before layout
                    delay(100)
                    try {
                        focusRequester.requestFocus()
                    } catch (e: Exception) {
                        // Ignore if focus request fails
                    }
                } else {
                    password = ""
                }
                isFetchingUser = false
            } else {
                password = ""
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val isWasm = platform == PlatformType.WASM
                Card(
                    modifier = Modifier
                        .widthIn(max = 450.dp)
                        .padding(if (isWasm) 24.dp else 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = if (isWasm) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(if (isWasm) 32.dp else 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                Icons.Default.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp).size(48.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "PulseLog",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Mistakes Analysis System",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        
                        Spacer(modifier = Modifier.height(48.dp))

                        if (error != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                            ) {
                                Text(
                                    text = error!!,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            trailingIcon = {
                                if (isFetchingUser) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            }
                        )
                        
                        // Password is auto-fetched, showing it as a hidden/disabled field or just omitting it
                        // as per "they only need to enter their PIN"
                        AnimatedVisibility(visible = password.isNotEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        "Account recognized. Please enter PIN.",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        PinInputView(
                            pin = pin,
                            focusRequester = focusRequester,
                            onPinChange = { pin = it },
                            onComplete = {
                                // Removed auto-login on complete to support manual login button
                            }
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        Button(
                            onClick = { 
                                viewModel.login(username, password, pin) { user ->
                                    navigator.replaceAll(HomeScreen())
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = MaterialTheme.shapes.large,
                            enabled = username.isNotBlank() && password.isNotBlank() && pin.length == 5
                        ) {
                            Text("Login", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun PinInputView(
    pin: String,
    focusRequester: FocusRequester,
    onPinChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        repeat(5) { index ->
            val char = when {
                index < pin.length -> pin[index].toString()
                else -> ""
            }
            val isFocused = index == pin.length

            Box(
                modifier = Modifier
                    .size(50.dp, 60.dp)
                    .background(
                        color = if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (char.isNotEmpty()) "●" else "",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    // Hidden TextField to capture input
    // Using alpha(0f) and size(1.dp) instead of Box(0.dp) to ensure it's placed in layout
    TextField(
        value = pin,
        onValueChange = {
            if (it.length <= 5 && it.all { c -> c.isDigit() }) {
                onPinChange(it)
                if (it.length == 5) {
                    // We call onComplete which performs login and navigation
                    onComplete()
                }
            }
        },
        modifier = Modifier
            .size(1.dp)
            .alpha(0f)
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
    )
}
