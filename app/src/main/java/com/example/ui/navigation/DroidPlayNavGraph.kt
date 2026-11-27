package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Games
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Games
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.DroidPlayViewModel
import com.example.ui.screens.ControlStudioScreen
import com.example.ui.screens.ControllerHubScreen
import com.example.ui.screens.ControllerLabScreen
import com.example.ui.screens.GameProfilesScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MyGamesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalVoid
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueDeep

object DroidPlayRoutes {
    const val HOME = "home_dashboard"
    const val GAMES = "my_games"
    const val HUB = "controller_hub"
    const val LAB = "controller_lab"
    const val STUDIO = "control_studio"
    const val PROFILES = "game_profiles"
    const val SETTINGS = "settings"
}

private data class TopLevelNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

private val TOP_LEVEL_ITEMS = listOf(
    TopLevelNavItem(
        route = DroidPlayRoutes.HOME,
        label = "Home",
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home,
        tag = "nav_home"
    ),
    TopLevelNavItem(
        route = DroidPlayRoutes.GAMES,
        label = "Games",
        selectedIcon = Icons.Rounded.Games,
        unselectedIcon = Icons.Outlined.Games,
        tag = "nav_games"
    ),
    TopLevelNavItem(
        route = DroidPlayRoutes.LAB,
        label = "Lab",
        selectedIcon = Icons.Rounded.Science,
        unselectedIcon = Icons.Outlined.Science,
        tag = "nav_lab"
    ),
    TopLevelNavItem(
        route = DroidPlayRoutes.STUDIO,
        label = "Studio",
        selectedIcon = Icons.Rounded.Tune,
        unselectedIcon = Icons.Outlined.Tune,
        tag = "nav_studio"
    ),
    TopLevelNavItem(
        route = DroidPlayRoutes.SETTINGS,
        label = "Settings",
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        tag = "nav_settings"
    )
)

@Composable
fun DroidPlayApp(
    viewModel: DroidPlayViewModel,
    onLabRouteActiveChanged: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: DroidPlayRoutes.HOME
    val context = LocalContext.current

    val controllers by viewModel.controllers.collectAsStateWithLifecycle()
    val allExternalDevices by viewModel.allExternalDevices.collectAsStateWithLifecycle()
    val liveGamepadState by viewModel.liveGamepadState.collectAsStateWithLifecycle()
    val inputLog by viewModel.inputLog.collectAsStateWithLifecycle()
    val bluetoothScanState by viewModel.bluetoothScanState.collectAsStateWithLifecycle()
    val gamesUiState by viewModel.gamesUiState.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val accessibilityEnabled by viewModel.accessibilityEnabled.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentRoute) {
        onLabRouteActiveChanged(currentRoute == DroidPlayRoutes.LAB)
    }

    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    val activeProfile = profiles.firstOrNull { it.isActive } ?: profiles.firstOrNull()

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateDirect(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 640.dp

        Scaffold(
            containerColor = CharcoalVoid,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = CharcoalSurface,
                        tonalElevation = 4.dp
                    ) {
                        TOP_LEVEL_ITEMS.forEach { item ->
                            val selected = currentRoute == item.route ||
                                (item.route == DroidPlayRoutes.LAB && currentRoute == DroidPlayRoutes.HUB) ||
                                (item.route == DroidPlayRoutes.STUDIO && currentRoute == DroidPlayRoutes.PROFILES)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ElectricBlue,
                                    selectedTextColor = ElectricBlue,
                                    indicatorColor = ElectricBlueDeep.copy(alpha = 0.24f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = CharcoalSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        TOP_LEVEL_ITEMS.forEach { item ->
                            val selected = currentRoute == item.route ||
                                (item.route == DroidPlayRoutes.LAB && currentRoute == DroidPlayRoutes.HUB) ||
                                (item.route == DroidPlayRoutes.STUDIO && currentRoute == DroidPlayRoutes.PROFILES)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label, maxLines = 1) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = ElectricBlue,
                                    selectedTextColor = ElectricBlue,
                                    indicatorColor = ElectricBlueDeep.copy(alpha = 0.24f)
                                ),
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = DroidPlayRoutes.HOME,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp)
                    ) {
                        composable(DroidPlayRoutes.HOME) {
                            HomeDashboardScreen(
                                controllers = controllers,
                                gamesUiState = gamesUiState,
                                activeProfile = activeProfile,
                                preferences = preferences,
                                onOpenGames = { navigateTopLevel(DroidPlayRoutes.GAMES) },
                                onOpenHub = { navigateDirect(DroidPlayRoutes.HUB) },
                                onOpenLab = { navigateTopLevel(DroidPlayRoutes.LAB) },
                                onOpenStudio = { navigateTopLevel(DroidPlayRoutes.STUDIO) },
                                onOpenProfiles = { navigateDirect(DroidPlayRoutes.PROFILES) },
                                onOpenSettings = { navigateTopLevel(DroidPlayRoutes.SETTINGS) },
                                onLaunchGame = viewModel::launchGame,
                                onToggleConsoleMode = {
                                    viewModel.setConsoleModeEnabled(!preferences.consoleModeEnabled)
                                },
                                onExitToAndroidHome = {
                                    runCatching {
                                        context.startActivity(viewModel.createExitToAndroidHomeIntent())
                                    }
                                }
                            )
                        }

                        composable(DroidPlayRoutes.GAMES) {
                            MyGamesScreen(
                                uiState = gamesUiState,
                                profiles = profiles,
                                showAllAppsDefault = preferences.showAllLaunchableApps,
                                onSearchChanged = viewModel::updateSearchQuery,
                                onRefresh = viewModel::refreshInstalledGames,
                                onLaunchGame = viewModel::launchGame,
                                onTogglePin = viewModel::togglePinnedGame,
                                onOpenProfiles = { navigateDirect(DroidPlayRoutes.PROFILES) }
                            )
                        }

                        composable(DroidPlayRoutes.HUB) {
                            ControllerHubScreen(
                                controllers = controllers,
                                allExternalDevices = allExternalDevices,
                                bluetoothScanState = bluetoothScanState,
                                hasBluetoothPermission = viewModel.controllerRepository.hasBluetoothInspectPermission(),
                                onRefreshDevices = viewModel::refreshControllers,
                                onOpenLab = { navigateTopLevel(DroidPlayRoutes.LAB) },
                                onTestRumble = viewModel::testControllerRumble,
                                onInspectPairedBluetooth = viewModel::inspectPairedBluetooth,
                                onBluetoothPermissionDenied = viewModel::markBluetoothPermissionDenied,
                                onOpenBluetoothSettings = {
                                    runCatching {
                                        context.startActivity(viewModel.createBluetoothSettingsIntent())
                                    }
                                }
                            )
                        }

                        composable(DroidPlayRoutes.LAB) {
                            ControllerLabScreen(
                                controllers = controllers,
                                liveState = liveGamepadState,
                                inputLog = inputLog,
                                lockBackKey = preferences.labLockBackKey,
                                onToggleLockBackKey = viewModel::setLabLockBackKey,
                                onClearLog = viewModel::clearInputLog,
                                onOpenBluetoothSettings = {
                                    runCatching {
                                        context.startActivity(viewModel.createBluetoothSettingsIntent())
                                    }
                                },
                                onOpenHub = { navigateDirect(DroidPlayRoutes.HUB) }
                            )
                        }

                        composable(DroidPlayRoutes.STUDIO) {
                            ControlStudioScreen(
                                activeProfile = activeProfile,
                                accessibilityEnabled = accessibilityEnabled,
                                onSaveProfile = viewModel::saveProfile,
                                onOpenProfiles = { navigateDirect(DroidPlayRoutes.PROFILES) },
                                onOpenAccessibilitySettings = {
                                    runCatching {
                                        context.startActivity(viewModel.createAccessibilitySettingsIntent())
                                    }
                                }
                            )
                        }

                        composable(DroidPlayRoutes.PROFILES) {
                            GameProfilesScreen(
                                profiles = profiles,
                                installedGames = gamesUiState.allApps,
                                onActivateProfile = viewModel::activateProfile,
                                onEditInStudio = { profileId ->
                                    viewModel.activateProfile(profileId)
                                    navigateTopLevel(DroidPlayRoutes.STUDIO)
                                },
                                onCreateProfile = { profile ->
                                    viewModel.saveProfile(profile, activateImmediately = true)
                                },
                                onDeleteProfile = viewModel::deleteProfile
                            )
                        }

                        composable(DroidPlayRoutes.SETTINGS) {
                            SettingsScreen(
                                preferences = preferences,
                                compatibility = viewModel.compatibilitySummary,
                                accessibilityEnabled = accessibilityEnabled,
                                onSetConsoleMode = viewModel::setConsoleModeEnabled,
                                onSetShowAllApps = viewModel::setShowAllLaunchableApps,
                                onSetLabLockBack = viewModel::setLabLockBackKey,
                                onSetHapticFeedback = viewModel::setHapticFeedbackEnabled,
                                onExitToAndroidHome = {
                                    runCatching {
                                        context.startActivity(viewModel.createExitToAndroidHomeIntent())
                                    }
                                },
                                onOpenAccessibilitySettings = {
                                    runCatching {
                                        context.startActivity(viewModel.createAccessibilitySettingsIntent())
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
