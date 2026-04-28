package com.example.pulselog.ui.mistake

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.pulselog.platform
import com.example.pulselog.PlatformType
import com.example.pulselog.ui.BackHandler
import com.example.pulselog.ui.auth.AuthViewModel
import com.example.pulselog.ui.home.HomeScreen
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class MistakeEntryScreen(val mistakeToEdit: com.example.pulselog.data.model.Mistake? = null) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<MistakeViewModel>()
        val authViewModel = koinScreenModel<AuthViewModel>()
        val currentUser by authViewModel.currentUser.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        
        val pages by viewModel.pages.collectAsState()
        val designations by viewModel.designations.collectAsState()

        var clientName by remember { mutableStateOf(mistakeToEdit?.clientName ?: "") }
        var amount by remember { mutableStateOf(mistakeToEdit?.amount?.toString() ?: "") }
        var pageName by remember { mutableStateOf(mistakeToEdit?.pageName ?: "") }
        var reason by remember { mutableStateOf(mistakeToEdit?.reason ?: "") }
        var type by remember { mutableStateOf(mistakeToEdit?.type ?: "Deposit") }
        var customType by remember { mutableStateOf(if (mistakeToEdit?.type != "Deposit" && mistakeToEdit?.type != "Redeem" && mistakeToEdit != null) mistakeToEdit.type else "") }
        var designation by remember { mutableStateOf(mistakeToEdit?.designation ?: "") }
        var batchId by remember { mutableStateOf(mistakeToEdit?.batchId ?: "") }
        var notes by remember { mutableStateOf(mistakeToEdit?.notes ?: "") }
        var shift by remember { mutableStateOf(mistakeToEdit?.shift ?: viewModel.calculateShift(Clock.System.now().toEpochMilliseconds())) }

        var showReviewDialog by remember { mutableStateOf(false) }

        BackHandler(enabled = showReviewDialog) {
            showReviewDialog = false
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(if (mistakeToEdit == null) "New Entry" else "Edit Entry", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                val isWasm = platform == PlatformType.WASM
                val isCompact = maxWidth < 600.dp && !isWasm

                Column(
                    modifier = Modifier
                        .widthIn(max = 800.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // SECTION 1: Transaction Details
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Transaction Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            OutlinedTextField(
                                value = clientName,
                                onValueChange = { clientName = it },
                                label = { Text("Client Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            )

                            if (isCompact) {
                                OutlinedTextField(
                                    value = amount,
                                    onValueChange = { amount = it },
                                    label = { Text("Amount ($)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = MaterialTheme.shapes.medium,
                                    leadingIcon = { Text("$ ", modifier = Modifier.padding(start = 12.dp)) }
                                )
                                DynamicDropdownField(
                                    label = "Page",
                                    options = pages.map { it.name },
                                    selectedOption = pageName,
                                    onOptionSelected = { pageName = it },
                                    onAddOption = { viewModel.addPage(it) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = amount,
                                        onValueChange = { amount = it },
                                        label = { Text("Amount ($)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = MaterialTheme.shapes.medium,
                                        leadingIcon = { Text("$ ", modifier = Modifier.padding(start = 12.dp)) }
                                    )
                                    DynamicDropdownField(
                                        label = "Page",
                                        options = pages.map { it.name },
                                        selectedOption = pageName,
                                        onOptionSelected = { pageName = it },
                                        onAddOption = { viewModel.addPage(it) },
                                        modifier = Modifier.weight(1.5f)
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 2: Shift & Context
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Shift & Context", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            
                            OutlinedTextField(
                                value = reason,
                                onValueChange = { reason = it },
                                label = { Text("Detailed Reasoning") },
                                placeholder = { Text("Provide a comprehensive explanation of the discrepancy...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4,
                                shape = MaterialTheme.shapes.medium
                            )
                        }
                    }

                    // SECTION 3: Classification
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Classification & Responsibility", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            // Shift Selection for Designation
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Responsible Shift", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                                val shifts = listOf("Morning", "Evening", "Night")
                                val shiftColors = mapOf(
                                    "Morning" to Color(0xFFBBDEFB),
                                    "Evening" to Color(0xFFFFE0B2),
                                    "Night" to Color(0xFFE1BEE7)
                                )

                                if (isCompact) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        shifts.forEach { s ->
                                            val isSelected = shift == s
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { shift = s },
                                                label = { Text(s, modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = shiftColors[s]?.copy(alpha = 0.7f) ?: MaterialTheme.colorScheme.primaryContainer
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        shifts.forEach { s ->
                                            val isSelected = shift == s
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { shift = s },
                                                label = { Text(s, modifier = Modifier.padding(vertical = 8.dp)) },
                                                modifier = Modifier.weight(1f),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = shiftColors[s]?.copy(alpha = 0.7f) ?: MaterialTheme.colorScheme.primaryContainer
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            DynamicDropdownField(
                                label = "Responsible Designation",
                                options = designations.map { it.name },
                                selectedOption = designation,
                                onOptionSelected = { designation = it },
                                onAddOption = { viewModel.addDesignation(it) }
                            )

                            // Type Selection
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Mistake Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                                if (isCompact) {
                                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Deposit", "Redeem", "Custom").forEach { t ->
                                            val isSelected = if (t == "Custom") (type != "Deposit" && type != "Redeem") else type == t
                                            val color = when(t) {
                                                "Deposit" -> Color(0xFFC62828)
                                                "Redeem" -> Color(0xFF1565C0)
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                            
                                            OutlinedButton(
                                                onClick = { type = t },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = MaterialTheme.shapes.medium,
                                                colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.1f), contentColor = color) else ButtonDefaults.outlinedButtonColors(),
                                                border = BorderStroke(1.dp, if (isSelected) color else MaterialTheme.colorScheme.outline)
                                            ) {
                                                Text(t)
                                            }
                                        }
                                    }
                                } else {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        listOf("Deposit", "Redeem", "Custom").forEach { t ->
                                            val isSelected = if (t == "Custom") (type != "Deposit" && type != "Redeem") else type == t
                                            val color = when(t) {
                                                "Deposit" -> Color(0xFFC62828)
                                                "Redeem" -> Color(0xFF1565C0)
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                            
                                            OutlinedButton(
                                                onClick = { type = t },
                                                modifier = Modifier.weight(1f),
                                                shape = MaterialTheme.shapes.medium,
                                                colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.1f), contentColor = color) else ButtonDefaults.outlinedButtonColors(),
                                                border = BorderStroke(1.dp, if (isSelected) color else MaterialTheme.colorScheme.outline)
                                            ) {
                                                Text(t)
                                            }
                                        }
                                    }
                                }

                                if (type == "Custom" || (type != "Deposit" && type != "Redeem")) {
                                    OutlinedTextField(
                                        value = customType,
                                        onValueChange = { customType = it },
                                        label = { Text("Specify Custom Type") },
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                        singleLine = true,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                }
                            }
                        }
                    }

                    // SUBMIT BUTTONS
                    if (isCompact) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (mistakeToEdit == null) {
                                OutlinedButton(
                                    onClick = {
                                        if (clientName.isBlank() || amount.isBlank() || pageName.isBlank() || reason.isBlank() || designation.isBlank()) {
                                            scope.launch { snackbarHostState.showSnackbar("Please fill all required fields") }
                                        } else {
                                            val finalType = if (type == "Custom") customType else type
                                            viewModel.saveMistake(
                                                clientName, amount.toDoubleOrNull() ?: 0.0, pageName, reason,
                                                finalType, designation, notes, currentUser?.id ?: 1L, isDraft = true, batchId = batchId, manualShift = shift,
                                                onSuccess = { 
                                                    scope.launch {
                                                        launch { snackbarHostState.showSnackbar("Draft saved successfully") }
                                                        if (navigator.canPop) navigator.pop() else navigator.replaceAll(HomeScreen())
                                                    }
                                                }
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save Draft")
                                }
                            }
                            Button(
                                onClick = {
                                    if (clientName.isBlank() || amount.isBlank() || pageName.isBlank() || reason.isBlank() || designation.isBlank()) {
                                        scope.launch { snackbarHostState.showSnackbar("Please fill all required fields") }
                                    } else {
                                        showReviewDialog = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(if (mistakeToEdit == null) Icons.Default.Check else Icons.Default.Update, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (mistakeToEdit == null) "Submit Record" else "Update Record")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), 
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (mistakeToEdit == null) {
                                OutlinedButton(
                                    onClick = {
                                        if (clientName.isBlank() || amount.isBlank() || pageName.isBlank() || reason.isBlank() || designation.isBlank()) {
                                            scope.launch { snackbarHostState.showSnackbar("Please fill all required fields") }
                                        } else {
                                            val finalType = if (type == "Custom") customType else type
                                            viewModel.saveMistake(
                                                clientName, amount.toDoubleOrNull() ?: 0.0, pageName, reason,
                                                finalType, designation, notes, currentUser?.id ?: 1L, isDraft = true, batchId = batchId, manualShift = shift,
                                                onSuccess = { 
                                                    scope.launch {
                                                        launch { snackbarHostState.showSnackbar("Draft saved successfully") }
                                                        if (navigator.canPop) navigator.pop() else navigator.replaceAll(HomeScreen())
                                                    }
                                                }
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save Draft")
                                }
                            }
                            Button(
                                onClick = {
                                    if (clientName.isBlank() || amount.isBlank() || pageName.isBlank() || reason.isBlank() || designation.isBlank()) {
                                        scope.launch { snackbarHostState.showSnackbar("Please fill all required fields") }
                                    } else {
                                        showReviewDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(if (mistakeToEdit == null) Icons.Default.Check else Icons.Default.Update, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (mistakeToEdit == null) "Submit Record" else "Update Record")
                            }
                        }
                    }
                }
            }
        }


        if (showReviewDialog) {
            val finalType = if (type == "Custom") customType else type
            AlertDialog(
                onDismissRequest = { showReviewDialog = false },
                title = { Text("Review Mistake Details", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReviewRow("Client", clientName)
                        ReviewRow("Amount", "$$amount")
                        ReviewRow("Type", finalType)
                        ReviewRow("Page", pageName)
                        ReviewRow("Designation", designation)
                        ReviewRow("Shift", shift)
                        if (batchId.isNotBlank()) ReviewRow("Batch ID", batchId)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reasoning:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(reason, style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showReviewDialog = false
                        val agentId = currentUser?.id ?: 1L
                        if (mistakeToEdit == null) {
                            viewModel.saveMistake(
                                clientName, amount.toDoubleOrNull() ?: 0.0, pageName, reason,
                                finalType, designation, notes, agentId, isDraft = false, batchId = batchId, manualShift = shift,
                                onSuccess = { 
                                    scope.launch {
                                        launch { snackbarHostState.showSnackbar("Record submitted successfully") }
                                        if (navigator.canPop) navigator.pop() else navigator.replaceAll(HomeScreen())
                                    }
                                }
                            )
                        } else {
                            viewModel.updateMistake(
                                id = mistakeToEdit.id,
                                agentId = mistakeToEdit.agentId,
                                clientName = clientName,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                pageName = pageName,
                                reason = reason,
                                designation = designation,
                                shift = shift,
                                date = mistakeToEdit.date,
                                notes = notes,
                                isDraft = false,
                                batchId = batchId,
                                piId = mistakeToEdit.piId,
                                type = finalType,
                                onSuccess = { 
                                    scope.launch {
                                        launch { snackbarHostState.showSnackbar("Record updated successfully") }
                                        if (navigator.canPop) navigator.pop() else navigator.replaceAll(HomeScreen())
                                    }
                                }
                            )
                        }
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showReviewDialog = false 
                        scope.launch { snackbarHostState.showSnackbar("Entry Cancelled") }
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicDropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onAddOption: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newOptionName by remember { mutableStateOf("") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Add New $label", fontWeight = FontWeight.Bold) },
                onClick = {
                    showAddDialog = true
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
            )
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New $label") },
            text = {
                OutlinedTextField(
                    value = newOptionName,
                    onValueChange = { newOptionName = it },
                    label = { Text("$label Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newOptionName.isNotBlank()) {
                        onAddOption(newOptionName)
                        onOptionSelected(newOptionName)
                        newOptionName = ""
                        showAddDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
