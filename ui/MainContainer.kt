package com.example.pulselog.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pulselog.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    navController: NavHostController,
    userRole: String, // "Admin" or "Finance Agent"
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
        NavigationItem("Add Mistakes", Screen.MistakeEntry.route, Icons.Default.Add),
        NavigationItem("Mistakes Analysis", Screen.Analytics.route, Icons.Default.BarChart),
        NavigationItem("User Management", "user_management", Icons.Default.People, adminOnly = true)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "PulseLog",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                navigationItems.forEach { item ->
                    if (!item.adminOnly || userRole == "Admin") {
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                if (item.route != currentRoute) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Home.route)
                                    }
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute != Screen.Login.route && currentRoute != Screen.Register.route) {
                    TopAppBar(
                        title = { Text(getTitleForRoute(currentRoute)) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            }
        ) { padding ->
            content(padding)
        }
    }
}

private data class NavigationItem(
    val title: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val adminOnly: Boolean = false
)

private fun getTitleForRoute(route: String?): String {
    return when (route) {
        Screen.Home.route -> "Dashboard"
        Screen.MistakeEntry.route -> "Add Mistake"
        Screen.Analytics.route -> "Mistakes Analysis"
        "user_management" -> "User Management"
        else -> "PulseLog"
    }
}
