package com.example.pulselog.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.example.pulselog.ui.BackHandler
import com.example.pulselog.platform
import com.example.pulselog.PlatformType
import com.example.pulselog.ui.auth.AuthViewModel
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.pulselog.ui.auth.LoginScreen
import com.example.pulselog.ui.home.HomeScreen
import com.example.pulselog.ui.mistake.MistakeEntryScreen
import com.example.pulselog.ui.analytics.AnalyticsScreen
import com.example.pulselog.ui.admin.UserManagementScreen
import com.example.pulselog.ui.admin.mistakes.AllMistakesScreen
import com.example.pulselog.ui.theme.PulseLogTheme
import com.example.pulselog.data.model.User
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@Composable
fun App() {
    PulseLogTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val authViewModel: AuthViewModel = koinInject()
            val currentUserState: User? by authViewModel.currentUser.collectAsState(null)

            Navigator(HomeScreen()) { navigator ->
                val user: User? = currentUserState

                // Global BackHandler to open drawer instead of exiting
                // Handled in MainContainer when user is logged in

                // Use LaunchedEffect to handle navigation when user state changes (e.g., logout)
                LaunchedEffect(user) {
                    if (user == null && navigator.lastItem !is LoginScreen) {
                        navigator.replaceAll(LoginScreen())
                    }
                }

                if (user != null) {
                    MainContainer(
                        navigator = navigator,
                        userRole = user.role,
                        onLogout = {
                            authViewModel.logout()
                        }
                    ) {
                        SlideTransition(navigator)
                    }
                } else {
                    // When logged out, only show the LoginScreen
                    // CurrentScreen() here will be LoginScreen due to LaunchedEffect above
                    SlideTransition(navigator)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    navigator: Navigator,
    userRole: String,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentScreen = navigator.lastItem

    val navigationItems = listOf(
        NavigationSection("MAIN", listOf(
            NavigationItem("Dashboard", HomeScreen(), Icons.Default.Dashboard)
        )),
        NavigationSection("OPERATIONS", listOf(
            NavigationItem("New Entry", MistakeEntryScreen(), Icons.Default.AddCircle),
            NavigationItem("History", AllMistakesScreen(), Icons.AutoMirrored.Filled.List)
        )),
        NavigationSection("REPORTS", listOf(
            NavigationItem("Detailed Analysis", AnalyticsScreen(isOverall = false), Icons.AutoMirrored.Filled.TrendingUp),
            NavigationItem("Overall Metrics", AnalyticsScreen(isOverall = true), Icons.Default.Assessment)
        )),
        NavigationSection("SYSTEM", listOf(
            NavigationItem("User Management", UserManagementScreen(), Icons.Default.ManageAccounts, adminOnly = true)
        ))
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isDesktop = platform == PlatformType.WASM
        
        // Global BackHandler for Android
        if (platform == PlatformType.ANDROID) {
            BackHandler(enabled = true) {
                if (drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                } else if (navigator.canPop) {
                    // If we can pop (e.g., from Edit to History), do that first
                    navigator.pop()
                } else {
                    // If we are at the root of a stack (Top-level screen), open drawer
                    scope.launch { drawerState.open() }
                }
            }
        }

        if (isDesktop) {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(280.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        drawerContentColor = MaterialTheme.colorScheme.onSurface,
                        drawerTonalElevation = 0.dp
                    ) {
                        NavigationDrawerContent(
                            navigationItems = navigationItems,
                            currentScreen = currentScreen,
                            userRole = userRole,
                            navigator = navigator,
                            onLogout = onLogout
                        )
                    }
                }
            ) {
                MainScaffold(
                    showMenuButton = false,
                    currentScreen = currentScreen,
                    onMenuClick = {},
                    content = content
                )
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(300.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        drawerContentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        NavigationDrawerContent(
                            navigationItems = navigationItems,
                            currentScreen = currentScreen,
                            userRole = userRole,
                            navigator = navigator,
                            onLogout = onLogout,
                            onItemClick = { scope.launch { drawerState.close() } }
                        )
                    }
                }
            ) {
                MainScaffold(
                    showMenuButton = true,
                    currentScreen = currentScreen,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    content = content
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    showMenuButton: Boolean,
    currentScreen: Screen,
    onMenuClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            if (showMenuButton) {
                TopAppBar(
                    title = { Text(getTitleForScreen(currentScreen), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            content()
        }
    }
}

@Composable
private fun NavigationDrawerContent(
    navigationItems: List<NavigationSection>,
    currentScreen: Screen,
    userRole: String,
    navigator: Navigator,
    onLogout: () -> Unit,
    onItemClick: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp).fillMaxHeight()) {
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "PulseLog",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            navigationItems.forEach { section ->
                val isSystemSection = section.title == "SYSTEM"
                val isAgent = userRole == "Finance Agent"
                
                if (!(isSystemSection && isAgent)) {
                    val visibleItems = section.items.filter { !it.adminOnly || userRole == "Admin" }
                    if (visibleItems.isNotEmpty()) {
                        Text(
                            text = section.title,
                            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Bold
                        )
                        visibleItems.forEach { item ->
                            val isSelected = if (item.screen is AnalyticsScreen && currentScreen is AnalyticsScreen) {
                                item.screen.isOverall == currentScreen.isOverall
                            } else {
                                currentScreen::class == item.screen::class
                            }

                            NavigationDrawerItem(
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(item.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                selected = isSelected,
                                onClick = {
                                    if (!isSelected) {
                                        navigator.replaceAll(item.screen)
                                    }
                                    onItemClick()
                                },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.padding(vertical = 2.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            label = { Text("Logout") },
            selected = false,
            onClick = {
                onLogout()
                onItemClick()
            },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}


private data class NavigationSection(
    val title: String,
    val items: List<NavigationItem>
)

private data class NavigationItem(
    val title: String,
    val screen: Screen,
    val icon: ImageVector,
    val adminOnly: Boolean = false
)

private fun getTitleForScreen(screen: Screen): String {
    return when (screen) {
        is HomeScreen -> "Dashboard"
        is MistakeEntryScreen -> {
            if (screen.mistakeToEdit != null) "Edit Mistake Log" else "New Mistake Entry"
        }
        is AllMistakesScreen -> "Mistake History"
        is AnalyticsScreen -> if (screen.isOverall) "Overall Report" else "Analytics"
        is UserManagementScreen -> "User Management"
        else -> "PulseLog"
    }
}
