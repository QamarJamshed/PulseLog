package com.example.pulselog.ui.admin.mistakes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.pulselog.platform
import com.example.pulselog.PlatformType
import com.example.pulselog.data.model.Mistake
import com.example.pulselog.ui.BackHandler
import com.example.pulselog.ui.auth.AuthViewModel
import com.example.pulselog.ui.mistake.MistakeEntryScreen
import com.example.pulselog.ui.mistake.MistakeViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class AllMistakesScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<MistakeViewModel>()
        val authViewModel = koinScreenModel<AuthViewModel>()
        val currentUser by authViewModel.currentUser.collectAsState()
        val mistakes by viewModel.allMistakes.collectAsState()
        val designations by viewModel.designations.collectAsState()
        
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Filter States
        var searchQuery by remember { mutableStateOf("") }
        var selectedShift by remember { mutableStateOf("All") }
        var selectedDesignation by remember { mutableStateOf("All") }
        var selectedType by remember { mutableStateOf("All") }
        var startDate by remember { mutableStateOf<LocalDate?>(null) }
        var endDate by remember { mutableStateOf<LocalDate?>(null) }
        var showFilters by remember { mutableStateOf(false) }

        // Dialog States
        var showStartDatePicker by remember { mutableStateOf(false) }
        var showEndDatePicker by remember { mutableStateOf(false) }
        var mistakeToDelete by remember { mutableStateOf<Mistake?>(null) }
        var selectedMistake by remember { mutableStateOf<Mistake?>(null) }
        
        val isAdmin = currentUser?.role == "Admin"

        BackHandler(enabled = showFilters) {
            showFilters = false
        }

        // Filtering Logic
        val filteredMistakes = remember(mistakes, searchQuery, selectedShift, selectedDesignation, selectedType, startDate, endDate) {
            mistakes.filter { mistake ->
                val matchesPlayer = searchQuery.isEmpty() || mistake.clientName.contains(searchQuery, ignoreCase = true)
                val matchesShift = selectedShift == "All" || mistake.shift == selectedShift
                val matchesDesignation = selectedDesignation == "All" || mistake.designation == selectedDesignation
                val matchesType = selectedType == "All" || mistake.type == selectedType
                
                val mDate = Instant.fromEpochMilliseconds(mistake.date).toLocalDateTime(TimeZone.currentSystemDefault()).date
                val matchesDateRange = (startDate == null || mDate >= startDate!!) && (endDate == null || mDate <= endDate!!)
                
                matchesPlayer && matchesShift && matchesDesignation && matchesType && matchesDateRange
            }
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWasm = platform == PlatformType.WASM
            val isCompact = maxWidth < 600.dp && !isWasm

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = { Text("Mistake History", fontWeight = FontWeight.Bold) },
                        actions = {
                            TextButton(
                                onClick = { showFilters = !showFilters },
                                colors = ButtonDefaults.textButtonColors(contentColor = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                            ) {
                                Icon(
                                    if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList, 
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                if (!isCompact) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (showFilters) "Hide Filters" else "Show Filters")
                                }
                            }
                        }
                    )
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).widthIn(max = 1000.dp).fillMaxSize().padding(horizontal = 16.dp)) {
                    // Header Section
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                "Mistake History",
                                style = if (isCompact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${filteredMistakes.size} records found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Filter Section
                    AnimatedVisibility(visible = showFilters) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Filter Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                
                                // Player Name Search
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text("Search by Client Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    shape = MaterialTheme.shapes.medium
                                )

                                if (isCompact) {
                                    FilterDropdown(
                                        label = "Shift",
                                        options = listOf("All", "Morning", "Evening", "Night"),
                                        selected = selectedShift,
                                        onSelected = { selectedShift = it },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    FilterDropdown(
                                        label = "Designation",
                                        options = listOf("All") + designations.map { it.name },
                                        selected = selectedDesignation,
                                        onSelected = { selectedDesignation = it },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    FilterDropdown(
                                        label = "Type",
                                        options = listOf("All", "Deposit", "Redeem"),
                                        selected = selectedType,
                                        onSelected = { selectedType = it },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { showStartDatePicker = true },
                                            modifier = Modifier.weight(1f),
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Text(startDate?.toString() ?: "Start Date", style = MaterialTheme.typography.labelMedium)
                                        }
                                        OutlinedButton(
                                            onClick = { showEndDatePicker = true },
                                            modifier = Modifier.weight(1f),
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Text(endDate?.toString() ?: "End Date", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                } else {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        FilterDropdown(
                                            label = "Shift",
                                            options = listOf("All", "Morning", "Evening", "Night"),
                                            selected = selectedShift,
                                            onSelected = { selectedShift = it },
                                            modifier = Modifier.weight(1f)
                                        )
                                        FilterDropdown(
                                            label = "Designation",
                                            options = listOf("All") + designations.map { it.name },
                                            selected = selectedDesignation,
                                            onSelected = { selectedDesignation = it },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        FilterDropdown(
                                            label = "Type",
                                            options = listOf("All", "Deposit", "Redeem"),
                                            selected = selectedType,
                                            onSelected = { selectedType = it },
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = { showStartDatePicker = true },
                                                modifier = Modifier.weight(1f),
                                                shape = MaterialTheme.shapes.medium
                                            ) {
                                                Text(startDate?.toString() ?: "Start Date", style = MaterialTheme.typography.labelMedium)
                                            }
                                            OutlinedButton(
                                                onClick = { showEndDatePicker = true },
                                                modifier = Modifier.weight(1f),
                                                shape = MaterialTheme.shapes.medium
                                            ) {
                                                Text(endDate?.toString() ?: "End Date", style = MaterialTheme.typography.labelMedium)
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (searchQuery.isNotEmpty() || selectedShift != "All" || selectedDesignation != "All" || selectedType != "All" || startDate != null || endDate != null) {
                                        TextButton(
                                            onClick = {
                                                searchQuery = ""
                                                selectedShift = "All"
                                                selectedDesignation = "All"
                                                selectedType = "All"
                                                startDate = null
                                                endDate = null
                                            }
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reset Filters")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (filteredMistakes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No matching mistakes found.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            shape = MaterialTheme.shapes.large
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 32.dp)
                            ) {
                                items(filteredMistakes) { mistake ->
                                    MistakeListItem(
                                        mistake = mistake,
                                        isAdmin = isAdmin,
                                        onEdit = { navigator.push(MistakeEntryScreen(mistake)) },
                                        onView = { selectedMistake = mistake },
                                        onDelete = { mistakeToDelete = mistake }
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        // Date Picker Dialogs
        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
                        }
                        showStartDatePicker = false
                    }) { Text("OK") }
                }
            ) { DatePicker(state = datePickerState) }
        }

        if (showEndDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            endDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
                        }
                        showEndDatePicker = false
                    }) { Text("OK") }
                }
            ) { DatePicker(state = datePickerState) }
        }

        // Detail Dialog
        if (selectedMistake != null) {
            MistakeDetailDialog(
                mistake = selectedMistake!!,
                isAdmin = isAdmin,
                onDismiss = { selectedMistake = null },
                onEdit = {
                    val mistake = selectedMistake!!
                    selectedMistake = null
                    navigator.push(MistakeEntryScreen(mistake))
                },
                onDelete = {
                    mistakeToDelete = selectedMistake
                    selectedMistake = null
                }
            )
        }

        if (mistakeToDelete != null) {
            AlertDialog(
                onDismissRequest = { mistakeToDelete = null },
                title = { Text("Delete Mistake Log?") },
                text = { Text("This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        val toDelete = mistakeToDelete
                        if (toDelete != null) {
                            scope.launch { 
                                viewModel.deleteMistake(toDelete) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Record deleted successfully")
                                    }
                                }
                            }
                        }
                        mistakeToDelete = null
                    }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mistakeToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MistakeListItem(
    mistake: Mistake,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    val instant = Instant.fromEpochMilliseconds(mistake.date)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val dateStr = "${localDateTime.month.name.substring(0, 3)} ${localDateTime.dayOfMonth}, ${localDateTime.year}"

    ListItem(
        modifier = Modifier.clickable { onView() }.padding(vertical = 4.dp),
        headlineContent = { 
            Text(
                mistake.clientName, 
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            ) 
        },
        supportingContent = {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    mistake.reason, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            mistake.shift,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    Text(
                        "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    Text(
                        mistake.designation,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        leadingContent = {
            val (icon, iconColor) = when(mistake.type) {
                "Deposit" -> Icons.Default.ArrowDownward to Color(0xFFC62828)
                "Redeem" -> Icons.Default.ArrowUpward to Color(0xFF1565C0)
                else -> Icons.Default.Info to MaterialTheme.colorScheme.primary
            }
            Surface(
                color = iconColor.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = mistake.type,
                    tint = iconColor,
                    modifier = Modifier.padding(10.dp).size(24.dp)
                )
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        "$${mistake.amount.toInt()}", 
                        fontWeight = FontWeight.ExtraBold, 
                        style = MaterialTheme.typography.titleMedium,
                        color = if (mistake.type == "Deposit") Color(0xFFC62828) else Color(0xFF1565C0)
                    )
                    Text(
                        mistake.type, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (isAdmin) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                    }
                } else {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}


@Composable
fun MistakeDetailDialog(
    mistake: Mistake,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val instant = Instant.fromEpochMilliseconds(mistake.date)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val dateStr = "${localDateTime.month.name} ${localDateTime.dayOfMonth}, ${localDateTime.year} at ${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mistake Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Client", mistake.clientName)
                DetailRow("Amount", "$${mistake.amount}")
                DetailRow("Type", mistake.type)
                DetailRow("Page", mistake.pageName)
                DetailRow("Designation", mistake.designation)
                DetailRow("Shift", mistake.shift)
                DetailRow("Date", dateStr)
                if (!mistake.batchId.isNullOrBlank()) DetailRow("Batch ID", mistake.batchId!!)
                if (!mistake.piId.isNullOrBlank()) DetailRow("PI ID", mistake.piId!!)
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Reasoning:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(mistake.reason, style = MaterialTheme.typography.bodySmall)
                
                if (!mistake.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Additional Notes:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(mistake.notes!!, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            if (isAdmin) {
                Row {
                    TextButton(onClick = onEdit) {
                        Text("Edit")
                    }
                    TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Delete")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
