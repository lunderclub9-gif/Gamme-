package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.Games
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.controller.ControllerDeviceInfo
import com.example.data.games.InstalledGameInfo
import com.example.data.local.ControlProfileEntity
import com.example.data.preferences.UserPreferencesState
import com.example.ui.GamesUiState
import com.example.ui.components.ConsoleSurfaceCard
import com.example.ui.components.ControllerStatusPill
import com.example.ui.components.QuickNavTile
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CharcoalOutline
import com.example.ui.theme.CharcoalSurfaceElevated
import com.example.ui.theme.CharcoalVoid
import com.example.ui.theme.ConsoleGreen
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.VioletGlow

@Composable
fun HomeDashboardScreen(
    controllers: List<ControllerDeviceInfo>,
    gamesUiState: GamesUiState,
    activeProfile: ControlProfileEntity?,
    preferences: UserPreferencesState,
    onOpenGames: () -> Unit,
    onOpenHub: () -> Unit,
    onOpenLab: () -> Unit,
    onOpenStudio: () -> Unit,
    onOpenProfiles: () -> Unit,
    onOpenSettings: () -> Unit,
    onLaunchGame: (InstalledGameInfo) -> Unit,
    onToggleConsoleMode: () -> Unit,
    onExitToAndroidHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryController = controllers.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Console Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DROIDPLAY",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = ElectricBlue
                    )
                    Text(
                        text = if (preferences.consoleModeEnabled) {
                            "Console Mode Active"
                        } else {
                            "Normal Android Sidekick"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                ControllerStatusPill(
                    connectedCount = controllers.size,
                    primaryName = primaryController?.type?.displayName ?: primaryController?.name,
                    onClick = onOpenHub
                )
            }
        }

        // Hero Console Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large),
                shape = MaterialTheme.shapes.large,
                color = CharcoalSurfaceElevated,
                border = BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(ElectricBlue.copy(alpha = 0.55f), DeepPurple.copy(alpha = 0.55f))
                    )
                )
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_console_hero),
                        contentDescription = "DroidPlay console dashboard artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        CharcoalVoid.copy(alpha = 0.45f),
                                        CharcoalVoid.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = DeepPurple.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, VioletGlow.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = activeProfile?.name ?: "Console Standard",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VioletGlow,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (primaryController != null) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = ConsoleGreen.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, ConsoleGreen.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = primaryController.type.badgeCode,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ConsoleGreen,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (primaryController != null) {
                                "${primaryController.name} Ready"
                            } else {
                                "Handheld Console Ready"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (primaryController != null) {
                                "${primaryController.transport.label} · ${primaryController.battery.statusLabel}"
                            } else {
                                "Connect DualSense, DualShock 3/4, Xbox, Bluetooth, or USB OTG controller to test live hardware inputs."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onOpenLab,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_open_lab_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricBlue,
                                    contentColor = CharcoalVoid
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Science,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Controller Lab", maxLines = 1)
                            }

                            FilledTonalButton(
                                onClick = onOpenGames,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_open_games_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Games,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("My Games", maxLines = 1)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onToggleConsoleMode,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_toggle_mode_button"),
                                border = BorderStroke(1.dp, CharcoalOutline)
                            ) {
                                Text(
                                    text = if (preferences.consoleModeEnabled) {
                                        "Switch to Normal Mode"
                                    } else {
                                        "Enable Console Mode"
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = onExitToAndroidHome,
                                modifier = Modifier.testTag("home_exit_android_button"),
                                border = BorderStroke(1.dp, CharcoalOutline)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                                    contentDescription = "Return to Android Home",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Android")
                            }
                        }
                    }
                }
            }
        }

        // Recent Games Shelf
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = "Recent Games",
                    subtitle = "Games launched through DroidPlay",
                    actionLabel = "All Games",
                    onActionClick = onOpenGames
                )

                val shelfGames = if (gamesUiState.recentGames.isNotEmpty()) {
                    gamesUiState.recentGames
                } else {
                    gamesUiState.allApps.filter { it.isCategorizedAsGame || it.isPinned }.take(6)
                }

                if (shelfGames.isEmpty()) {
                    ConsoleSurfaceCard(
                        onClick = onOpenGames,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DeepPurple.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Games,
                                    contentDescription = null,
                                    tint = VioletGlow
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "No recent games launched yet",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Open My Games to launch installed games or pin apps to your console shelf.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(shelfGames, key = { it.packageName }) { game ->
                            RecentGameTile(
                                game = game,
                                onLaunch = { onLaunchGame(game) }
                            )
                        }
                    }
                }
            }
        }

        // Quick Access to All 7 Console Sections
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = "Console Modules",
                    subtitle = "Direct access to every DroidPlay workspace"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickNavTile(
                        title = "Controller Lab",
                        subtitle = "Live buttons, sticks & triggers",
                        icon = Icons.Rounded.Science,
                        accentColor = ElectricBlue,
                        tag = "tile_controller_lab",
                        onClick = onOpenLab,
                        modifier = Modifier.weight(1f)
                    )
                    QuickNavTile(
                        title = "Controller Hub",
                        subtitle = "${controllers.size} detected · Battery & specs",
                        icon = Icons.Rounded.SportsEsports,
                        accentColor = ConsoleGreen,
                        tag = "tile_controller_hub",
                        onClick = onOpenHub,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickNavTile(
                        title = "My Games",
                        subtitle = "${gamesUiState.allApps.count { it.isCategorizedAsGame }} detected games",
                        icon = Icons.Rounded.Games,
                        accentColor = VioletGlow,
                        tag = "tile_my_games",
                        onClick = onOpenGames,
                        modifier = Modifier.weight(1f)
                    )
                    QuickNavTile(
                        title = "Control Studio",
                        subtitle = "Deadzones, curves & mappings",
                        icon = Icons.Rounded.Tune,
                        accentColor = DeepPurple,
                        tag = "tile_control_studio",
                        onClick = onOpenStudio,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickNavTile(
                        title = "Game Profiles",
                        subtitle = "Per-game local presets",
                        icon = Icons.Rounded.Bookmark,
                        accentColor = Color(0xFF38BDF8),
                        tag = "tile_game_profiles",
                        onClick = onOpenProfiles,
                        modifier = Modifier.weight(1f)
                    )
                    QuickNavTile(
                        title = "Settings",
                        subtitle = "Console mode & Android OS info",
                        icon = Icons.Rounded.Settings,
                        accentColor = Color(0xFFA78BFA),
                        tag = "tile_settings",
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Controller Quick Tip Footer
        item {
            ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Cable,
                        contentDescription = null,
                        tint = ElectricBlue
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Native Android Gamepad Continuity",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "When you leave DroidPlay for Normal Android Mode or launch a game, Android's native HID gamepad driver continues working directly inside compatible games.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentGameTile(
    game: InstalledGameInfo,
    onLaunch: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(156.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onLaunch)
            .testTag("recent_game_${game.packageName}"),
        shape = MaterialTheme.shapes.medium,
        color = CharcoalSurfaceElevated,
        border = BorderStroke(1.dp, CharcoalOutline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (game.iconBitmap != null) {
                    Image(
                        bitmap = game.iconBitmap.asImageBitmap(),
                        contentDescription = "${game.title} icon",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepPurple.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Games,
                            contentDescription = null,
                            tint = ElectricBlue
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElectricBlue.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Launch ${game.title}",
                        tint = ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = game.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (game.launchCount > 0) "Played ${game.launchCount}x" else "Ready to launch",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
