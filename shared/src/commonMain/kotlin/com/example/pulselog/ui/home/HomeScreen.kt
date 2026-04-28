package com.example.pulselog.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.pulselog.platform
import com.example.pulselog.PlatformType
import com.example.pulselog.ui.admin.UserManagementScreen
import com.example.pulselog.ui.admin.mistakes.AllMistakesScreen
import com.example.pulselog.ui.analytics.AnalyticsScreen
import com.example.pulselog.ui.auth.AuthViewModel
import com.example.pulselog.ui.auth.LoginScreen
import com.example.pulselog.ui.mistake.MistakeEntryScreen
import com.example.pulselog.ui.mistake.MistakeViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val authViewModel = getScreenModel<AuthViewModel>()
        val mistakeViewModel = getScreenModel<MistakeViewModel>()
        val currentUser by authViewModel.currentUser.collectAsState()
        val mistakes by mistakeViewModel.allMistakes.collectAsState()

        val totalDepositAmount = mistakes.filter { it.type == "Deposit" }.sumOf { it.amount }
        val depositCount = mistakes.count { it.type == "Deposit" }
        val totalRedeemAmount = mistakes.filter { it.type == "Redeem" }.sumOf { it.amount }
        val redeemCount = mistakes.count { it.type == "Redeem" }
        val totalAmount = totalDepositAmount + totalRedeemAmount
        val totalMistakesCount = mistakes.size

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWasm = platform == PlatformType.WASM
            val isCompact = maxWidth < 600.dp && platform != PlatformType.WASM
            
            Column(
                modifier = Modifier
                    .widthIn(max = 1000.dp)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                if (isWasm) { // Only show header on desktop as mobile has it in TopAppBar
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Welcome back, ${currentUser?.username ?: "User"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                Text(
                    text = "Quick Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isCompact) {
                    // Stacked summary tiles for mobile
                    SummaryTile(
                        title = "Total Mistakes",
                        value = "$totalMistakesCount",
                        icon = Icons.Default.BugReport,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    SummaryTile(
                        title = "Impact Amount",
                        value = "$${totalAmount.toInt()}",
                        icon = Icons.Default.AttachMoney,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    SummaryTile(
                        title = "Deposit Mistakes",
                        value = "$${totalDepositAmount.toInt()}",
                        subValue = "$depositCount entries",
                        icon = Icons.Default.ArrowDownward,
                        color = Color(0xFFC62828),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    SummaryTile(
                        title = "Redeem Mistakes",
                        value = "$${totalRedeemAmount.toInt()}",
                        subValue = "$redeemCount entries",
                        icon = Icons.Default.ArrowUpward,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                } else {
                    // Grid summary tiles for desktop/tablet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SummaryTile(
                            title = "Total Mistakes",
                            value = "$totalMistakesCount",
                            icon = Icons.Default.BugReport,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryTile(
                            title = "Impact Amount",
                            value = "$${totalAmount.toInt()}",
                            icon = Icons.Default.AttachMoney,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SummaryTile(
                            title = "Deposit Mistakes",
                            value = "$${totalDepositAmount.toInt()}",
                            subValue = "$depositCount entries",
                            icon = Icons.Default.ArrowDownward,
                            color = Color(0xFFC62828),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryTile(
                            title = "Redeem Mistakes",
                            value = "$${totalRedeemAmount.toInt()}",
                            subValue = "$redeemCount entries",
                            icon = Icons.Default.ArrowUpward,
                            color = Color(0xFF1565C0),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Recent Activity",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { /* Navigate to History */ }) {
                                Text("View All")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (mistakes.isEmpty()) {
                            Text(
                                "No recent activity found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(vertical = 24.dp).align(Alignment.CenterHorizontally)
                            )
                        } else {
                            mistakes.take(10).forEachIndexed { index, mistake ->
                                ActivityItem(mistake)
                                if (index < mistakes.take(10).size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
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
    }

    @Composable
    fun SummaryTile(
        title: String, 
        value: String, 
        subValue: String? = null,
        icon: ImageVector, 
        color: Color, 
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp), 
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            title, 
                            style = MaterialTheme.typography.labelLarge, 
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            value, 
                            style = MaterialTheme.typography.headlineMedium, 
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Surface(
                        color = color.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            icon, 
                            contentDescription = null, 
                            tint = color, 
                            modifier = Modifier.padding(12.dp).size(28.dp)
                        )
                    }
                }
                
                if (subValue != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = color.copy(alpha = 0.05f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            subValue, 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ActivityItem(mistake: com.example.pulselog.data.model.Mistake) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, tint) = when(mistake.type) {
                "Deposit" -> Icons.Default.ArrowDownward to Color(0xFFC62828)
                "Redeem" -> Icons.Default.ArrowUpward to Color(0xFF1565C0)
                else -> Icons.Default.History to MaterialTheme.colorScheme.primary
            }
            
            Surface(
                color = tint.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = tint,
                    modifier = Modifier.padding(10.dp).size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mistake.clientName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${mistake.type} • ${mistake.designation} • ${mistake.shift}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${mistake.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (mistake.type == "Deposit") Color(0xFFC62828) else Color(0xFF1565C0)
                )
                val dateStr = kotlinx.datetime.Instant.fromEpochMilliseconds(mistake.date)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date.toString()
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
