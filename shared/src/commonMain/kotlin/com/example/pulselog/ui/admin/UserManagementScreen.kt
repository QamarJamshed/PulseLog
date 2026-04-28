package com.example.pulselog.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.example.pulselog.platform
import com.example.pulselog.PlatformType
import com.example.pulselog.data.model.User
import com.example.pulselog.ui.BackHandler
import com.example.pulselog.ui.auth.AuthViewModel
import com.example.pulselog.ui.mistake.MistakeViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random

class UserManagementScreen : Screen {
    @Composable
    override fun Content() {
        val authViewModel = koinScreenModel<AuthViewModel>()
        val currentUser by authViewModel.currentUser.collectAsState()
        val viewModel = koinScreenModel<MistakeViewModel>()
        val users by viewModel.allUsers.collectAsState()
        var showAddDialog by remember { mutableStateOf(false) }
        var userToEdit by remember { mutableStateOf<User?>(null) }
        var userToDelete by remember { mutableStateOf<User?>(null) }
        var generatedPassword by remember { mutableStateOf<String?>(null) }

        BackHandler(enabled = showAddDialog || userToEdit != null || userToDelete != null || generatedPassword != null) {
            showAddDialog = false
            userToEdit = null
            userToDelete = null
            generatedPassword = null
        }

        val clipboardManager = LocalClipboardManager.current
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        val isWasm = platform == PlatformType.WASM

        Scaffold(
            floatingActionButton = {
                if (isWasm) {
                    ExtendedFloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.large,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Add New User", fontWeight = FontWeight.Bold) }
                    )
                } else {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add User")
                    }
                }
            }
        ) { padding ->
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 1000.dp)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (isWasm) {
                        Text(
                            "System Users",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Manage system accounts and permissions.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }

                    Card(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        shape = MaterialTheme.shapes.extraLarge,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(users) { user ->
                                UserListItem(
                                    user = user,
                                    isSelf = user.id == currentUser?.id,
                                    onEdit = { userToEdit = user },
                                    onDelete = { userToDelete = user }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }


        if (showAddDialog) {
            UserDialog(
                title = "Add New User",
                onDismiss = { showAddDialog = false },
                onConfirm = { username, role, pin ->
                    val password = generateRandomPassword()
                    viewModel.saveUser(username, role, pin, password)
                    generatedPassword = password
                    showAddDialog = false
                }
            )
        }

        if (userToEdit != null) {
            UserDialog(
                title = "Edit User",
                user = userToEdit,
                onDismiss = { userToEdit = null },
                onConfirm = { username, role, pin ->
                    viewModel.updateUser(userToEdit!!.id, username, role, pin)
                    userToEdit = null
                }
            )
        }

        if (userToDelete != null) {
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Delete User?") },
                text = { Text("Are you sure you want to delete ${userToDelete?.username}?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteUser(userToDelete!!)
                        userToDelete = null
                    }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (generatedPassword != null) {
            AlertDialog(
                onDismissRequest = { generatedPassword = null },
                title = { Text("User Created Successfully") },
                text = {
                    Column {
                        Text("The account has been created with a generated password.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = generatedPassword!!,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(generatedPassword!!))
                        generatedPassword = null
                        scope.launch { snackbarHostState.showSnackbar("Password copied to clipboard") }
                    }) {
                        Text("Copy & Close")
                    }
                }
            )
        }
    }
}

@Composable
fun UserListItem(user: User, isSelf: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        modifier = Modifier.padding(vertical = 8.dp),
        headlineContent = { 
            Text(
                user.username, 
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            ) 
        },
        supportingContent = { 
            Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (user.role == "Admin") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        user.role.uppercase(), 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (user.role == "Admin") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                if (isSelf) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "(You)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        leadingContent = {
            Surface(
                color = if (user.role == "Admin") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(
                    if (user.role == "Admin") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(24.dp),
                    tint = if (user.role == "Admin") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                }
                if (!isSelf) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDialog(
    title: String,
    user: User? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var username by remember { mutableStateOf(user?.username ?: "") }
    var pin by remember { mutableStateOf(user?.pin ?: "") }
    var role by remember { mutableStateOf(user?.role ?: "Finance Agent") }
    val roles = listOf("Admin", "Finance Agent")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 5 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("5-Digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = MaterialTheme.shapes.medium,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        roles.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    role = r
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(username, role, pin) },
                enabled = username.isNotBlank() && pin.length == 5,
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun generateRandomPassword(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
    return (1..10)
        .map { Random.nextInt(0, chars.length).let { chars[it] } }
        .joinToString("")
}
