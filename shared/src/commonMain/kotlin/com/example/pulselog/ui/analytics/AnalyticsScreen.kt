package com.example.pulselog.ui.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.pulselog.platform
import com.example.pulselog.PlatformType
import com.example.pulselog.data.model.Mistake
import com.example.pulselog.ui.BackHandler
import com.example.pulselog.ui.mistake.MistakeViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class AnalyticsScreen(val isOverall: Boolean = false) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<MistakeViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val mistakes by viewModel.allMistakes.collectAsState()
        val designations by viewModel.designations.collectAsState()

        var selectedShift by remember { mutableStateOf<String?>("All") }
        var selectedDesignation by remember { mutableStateOf<String?>("All") }
        var showResults by remember { mutableStateOf(false) }

        BackHandler(enabled = showResults) {
            showResults = false
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWasm = platform == PlatformType.WASM
            val isCompact = maxWidth < 600.dp && !isWasm
            val isDesktop = platform == PlatformType.WASM

            Column(
                modifier = Modifier
                    .widthIn(max = 1000.dp)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (isOverall) {
                    if (isWasm) {
                        Column {
                            Text(
                                "Overall Performance",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Aggregated data across all shifts and designations",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    SummaryMetrics(mistakes, isCompact)
                    
                    if (isCompact) {
                        val typeData = mistakes.groupBy { it.type }.mapValues { it.value.size.toFloat() }
                        AnalyticsChart("Mistake Type Distribution", typeData, modifier = Modifier.fillMaxWidth())
                        
                        val shiftData = mistakes.groupBy { it.shift }.mapValues { it.value.size.toFloat() }
                        AnalyticsChart("Shift-wise Distribution", shiftData, modifier = Modifier.fillMaxWidth())
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val typeData = mistakes.groupBy { it.type }.mapValues { it.value.size.toFloat() }
                            AnalyticsChart("Mistake Type Distribution", typeData, modifier = Modifier.weight(1f))
                            
                            val shiftData = mistakes.groupBy { it.shift }.mapValues { it.value.size.toFloat() }
                            AnalyticsChart("Shift-wise Distribution", shiftData, modifier = Modifier.weight(1f))
                        }
                    }

                    MistakeBreakdownTable(mistakes, isCompact)
                } else {
                    if (!showResults) {
                        SelectionView(
                            designations = designations.map { it.name },
                            selectedShift = selectedShift,
                            onShiftSelected = { selectedShift = it },
                            selectedDesignation = selectedDesignation,
                            onDesignationSelected = { selectedDesignation = it },
                            onProceed = { showResults = true }
                        )
                    } else {
                        val filteredMistakes = mistakes.filter { mistake ->
                            (selectedShift == "All" || mistake.shift == selectedShift) &&
                                    (selectedDesignation == "All" || mistake.designation == selectedDesignation)
                        }
                        FilteredMistakesView(
                            mistakes = filteredMistakes,
                            shift = selectedShift,
                            designation = selectedDesignation,
                            onBack = { showResults = false }
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun MistakeBreakdownTable(mistakes: List<Mistake>, isCompact: Boolean = false) {
    val breakdown = mistakes.groupBy { it.designation to it.shift }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp)) {
            Text(
                "Designation & Shift Breakdown",
                style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Designation", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge)
                Text("Shift", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge)
                Text("Count", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge)
            }

            if (breakdown.isEmpty()) {
                Text("No data available", modifier = Modifier.padding(16.dp))
            } else {
                breakdown.forEachIndexed { index, entry ->
                    val designation = entry.first.first
                    val shift = entry.first.second
                    val count = entry.second

                    val bgColor = if (index % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = designation, 
                            modifier = Modifier.weight(2f), 
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        // Shift Badge
                        Box(modifier = Modifier.weight(1.5f), contentAlignment = Alignment.Center) {
                            val shiftColor = when(shift) {
                                "Morning" -> Color(0xFFBBDEFB)
                                "Evening" -> Color(0xFFFFE0B2)
                                "Night" -> Color(0xFFE1BEE7)
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }
                            Surface(
                                color = shiftColor,
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = shift,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black
                                )
                            }
                        }

                        Text(
                            text = count.toString(),
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryMetrics(mistakes: List<Mistake>, isCompact: Boolean = false) {
    val totalMistakes = mistakes.size
    val totalAmount = mistakes.sumOf { it.amount }

    val depositMistakes = mistakes.filter { it.type == "Deposit" }
    val totalDepositAmount = depositMistakes.sumOf { it.amount }

    val redeemMistakes = mistakes.filter { it.type == "Redeem" }
    val totalRedeemAmount = redeemMistakes.sumOf { it.amount }

    if (isCompact) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Records",
                    value = totalMistakes.toString(),
                    icon = Icons.Default.History,
                    modifier = Modifier.weight(1f),
                    compact = true
                )
                MetricCard(
                    title = "Impact",
                    value = "$${totalAmount.toInt()}",
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f),
                    compact = true
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Deposits",
                    value = "$${totalDepositAmount.toInt()}",
                    icon = Icons.Default.ArrowDownward,
                    color = Color(0xFFC62828),
                    modifier = Modifier.weight(1f),
                    compact = true
                )
                MetricCard(
                    title = "Redeems",
                    value = "$${totalRedeemAmount.toInt()}",
                    icon = Icons.Default.ArrowUpward,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f),
                    compact = true
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                title = "Total Records",
                value = totalMistakes.toString(),
                icon = Icons.Default.History,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Discrepancy",
                value = "$${totalAmount.toInt()}",
                icon = Icons.Default.AccountBalanceWallet,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Deposits",
                value = "$${totalDepositAmount.toInt()}",
                icon = Icons.Default.ArrowDownward,
                color = Color(0xFFC62828),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Redeems",
                value = "$${totalRedeemAmount.toInt()}",
                icon = Icons.Default.ArrowUpward,
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String, 
    value: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 12.dp else 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 16.dp)
        ) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(if (compact) 8.dp else 12.dp).size(if (compact) 20.dp else 24.dp)
                )
            }
            Column {
                Text(text = title, style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                Text(text = value, style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}


@Composable
fun AnalyticsChart(
    title: String, 
    data: Map<String, Float>, 
    showValueInLabel: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            if (data.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("No data available", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    PieChart3D(
                        data = data,
                        showValueInLabel = showValueInLabel,
                        modifier = Modifier.size(140.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        data.keys.forEachIndexed { index, label ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(getChartColor(index), androidx.compose.foundation.shape.CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$label: ${data[label]?.toInt()}", 
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PieChart3D(
    data: Map<String, Float>,
    showValueInLabel: Boolean,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum()
    if (total == 0f) return

    val colors = data.keys.indices.map { getChartColor(it) }

    Canvas(modifier = modifier) {
        val thickness = 20f
        val chartSize = Size(size.width, size.height - thickness)
        
        // Draw depth layers
        var startAngle = -90f
        data.values.forEachIndexed { index, value ->
            val sweepAngle = (value / total) * 360f
            if (sweepAngle > 0) {
                val baseColor = colors[index]
                val shadowColor = baseColor.darker(0.7f)
                
                // Draw multiple layers for depth
                for (i in 1..thickness.toInt()) {
                    drawArc(
                        color = shadowColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(0f, i.toFloat()),
                        size = chartSize
                    )
                }
            }
            startAngle += sweepAngle
        }

        // Draw top surface
        startAngle = -90f
        data.entries.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value / total) * 360f
            if (sweepAngle > 0) {
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(0f, 0f),
                    size = chartSize
                )
            }
            startAngle += sweepAngle
        }
    }
}

fun getChartColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF6200EE),
        Color(0xFF03DAC5),
        Color(0xFF018786),
        Color(0xFFBB86FC),
        Color(0xFF3700B3),
        Color(0xFFCF6679)
    )
    return colors[index % colors.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionView(
    designations: List<String>,
    selectedShift: String?,
    onShiftSelected: (String) -> Unit,
    selectedDesignation: String?,
    onDesignationSelected: (String) -> Unit,
    onProceed: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                "Filter Mistakes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Select a shift and designation to view specific records.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SimpleDropdownField(
                    label = "Shift",
                    options = listOf("All", "Morning", "Evening", "Night"),
                    selectedOption = selectedShift ?: "All",
                    onOptionSelected = onShiftSelected
                )

                SimpleDropdownField(
                    label = "Designation",
                    options = listOf("All") + designations,
                    selectedOption = selectedDesignation ?: "All",
                    onOptionSelected = onDesignationSelected
                )
            }

            Button(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("View Mistakes", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
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
        }
    }
}

@Composable
fun FilteredMistakesView(
    mistakes: List<Mistake>,
    shift: String?,
    designation: String?,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(
                    text = "Results for",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${shift ?: "All"} - ${designation ?: "All"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onBack) {
                Icon(Icons.Default.FilterList, contentDescription = "Change Filters")
            }
        }

        if (mistakes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No records found for this selection.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    mistakes.forEachIndexed { index, mistake ->
                        FilteredMistakeItem(mistake)
                        if (index < mistakes.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilteredMistakeItem(mistake: Mistake) {
    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(mistake.clientName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text("$${mistake.amount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(mistake.reason, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(mistake.pageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            val dateStr = Instant.fromEpochMilliseconds(mistake.date)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date.toString()
            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

fun Color.darker(factor: Float = 0.8f): Color {
    return Color(
        red = (this.red * factor).coerceIn(0f, 1f),
        green = (this.green * factor).coerceIn(0f, 1f),
        blue = (this.blue * factor).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}
