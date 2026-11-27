package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Games
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.games.InstalledGameInfo
import com.example.data.local.ControlProfileEntity
import com.example.ui.GamesUiState
import com.example.ui.components.ConsoleSurfaceCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CharcoalOutline
import com.example.ui.theme.CharcoalSurfaceElevated
import com.example.ui.theme.CharcoalVoid
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.VioletGlow

@Composable
fun MyGamesScreen(
    uiState: GamesUiState,
    profiles: List<ControlProfileEntity>,
    showAllAppsDefault: Boolean,
    onSearchChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onLaunchGame: (InstalledGameInfo) -> Unit,
    onTogglePin: (InstalledGameInfo) -> Unit,
    onOpenProfiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by rememberSaveable(showAllAppsDefault) {
        mutableIntStateOf(if (showAllAppsDefault) 2 else 0)
    }

    val filteredApps = uiState.allApps.filter { app ->
        val matchesTab = when (selectedFilter) {
            0 -> app.isCategorizedAsGame || app.isPinned
            1 -> app.isPinned
            else -> true
        }
        val matchesQuery = uiState.searchQuery.isBlank() ||
            app.title.contains(uiState.searchQuery, ignoreCase = true) ||
            app.packageName.contains(uiState.searchQuery, ignoreCase = true)
        matchesTab && matchesQuery
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("my_games_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "My Games",
                subtitle = "Installed Android games & pinned console titles",
                actionLabel = "Refresh",
                onActionClick = onRefresh
            )
        }

        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("games_search_input"),
                placeholder = { Text("Search installed games or packages") },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == 0,
                    onClick = { selectedFilter = 0 },
                    label = { Text("Games (${uiState.allApps.count { it.isCategorizedAsGame || it.isPinned }})") },
                    modifier = Modifier.testTag("filter_games")
                )
                FilterChip(
                    selected = selectedFilter == 1,
                    onClick = { selectedFilter = 1 },
                    label = { Text("Pinned (${uiState.allApps.count { it.isPinned }})") },
                    modifier = Modifier.testTag("filter_pinned")
                )
                FilterChip(
                    selected = selectedFilter == 2,
                    onClick = { selectedFilter = 2 },
                    label = { Text("All Apps (${uiState.allApps.size})") },
                    modifier = Modifier.testTag("filter_all_apps")
                )
            }
        }

        when {
            uiState.isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = ElectricBlue)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Scanning launchable Android games...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            uiState.errorMessage != null -> {
                item {
                    ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Package Discovery Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onRefresh) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry")
                        }
                    }
                }
            }

            filteredApps.isEmpty() -> {
                item {
                    ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Games,
                                contentDescription = null,
                                tint = VioletGlow,
                                modifier = Modifier.size(36.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedFilter == 0) {
                                        "No Android games tagged CATEGORY_GAME found"
                                    } else {
                                        "No matching apps found"
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Some games and emulators do not declare Android's game category flag. Switch to All Apps to launch or pin them to your library.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (selectedFilter != 2) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { selectedFilter = 2 }) {
                                Text("Show All Launchable Apps", color = ElectricBlue)
                            }
                        }
                    }
                }
            }

            else -> {
                items(filteredApps, key = { it.packageName }) { game ->
                    val assignedProfile = profiles.firstOrNull { it.id == game.assignedProfileId }
                    GameLibraryRow(
                        game = game,
                        assignedProfileName = assignedProfile?.name,
                        onLaunch = { onLaunchGame(game) },
                        onTogglePin = { onTogglePin(game) },
                        onOpenProfiles = onOpenProfiles
                    )
                }
            }
        }
    }
}

@Composable
private fun GameLibraryRow(
    game: InstalledGameInfo,
    assignedProfileName: String?,
    onLaunch: () -> Unit,
    onTogglePin: () -> Unit,
    onOpenProfiles: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_card_${game.packageName}"),
        shape = MaterialTheme.shapes.medium,
        color = CharcoalSurfaceElevated,
        border = BorderStroke(1.dp, CharcoalOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (game.iconBitmap != null) {
                Image(
                    bitmap = game.iconBitmap.asImageBitmap(),
                    contentDescription = "${game.title} icon",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = game.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (game.isCategorizedAsGame) {
                        TagChip(label = "GAME", accent = ElectricBlue)
                    }
                    if (!assignedProfileName.isNullOrBlank()) {
                        TagChip(label = assignedProfileName, accent = VioletGlow)
                    }
                }
            }

            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.testTag("pin_game_${game.packageName}")
            ) {
                Icon(
                    imageVector = if (game.isPinned) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = if (game.isPinned) "Unpin game" else "Pin game to library",
                    tint = if (game.isPinned) VioletGlow else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onLaunch,
                modifier = Modifier.testTag("launch_game_${game.packageName}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = CharcoalVoid
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Play")
            }
        }
    }
}

@Composable
private fun TagChip(
    label: String,
    accent: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = accent.copy(alpha = 0.16f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
