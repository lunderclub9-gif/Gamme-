package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.games.InstalledGameInfo
import com.example.data.local.ControlProfileEntity
import com.example.ui.components.ConsoleSurfaceCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CharcoalVoid
import com.example.ui.theme.ConsoleGreen
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.VioletGlow

@Composable
fun GameProfilesScreen(
    profiles: List<ControlProfileEntity>,
    installedGames: List<InstalledGameInfo>,
    onActivateProfile: (Long) -> Unit,
    onEditInStudio: (Long) -> Unit,
    onCreateProfile: (ControlProfileEntity) -> Unit,
    onDeleteProfile: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewDialog by rememberSaveable { mutableStateOf(false) }
    var newProfileName by rememberSaveable { mutableStateOf("") }
    var selectedPackage by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedGameTitle by rememberSaveable { mutableStateOf("All Native Games") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_profiles_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Game Profiles",
                subtitle = "Per-game controller presets stored locally in Room",
                actionLabel = "New Profile",
                onActionClick = { showNewDialog = true }
            )
        }

        if (profiles.isEmpty()) {
            item {
                ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No Saved Profiles",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Create a profile to store deadzone, analog sensitivity, and button remaps per game.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { showNewDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Profile")
                    }
                }
            }
        } else {
            items(profiles, key = { it.id }) { profile ->
                ConsoleSurfaceCard(
                    highlighted = profile.isActive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_card_${profile.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (profile.isActive) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = ConsoleGreen.copy(alpha = 0.16f),
                                        border = BorderStroke(1.dp, ConsoleGreen.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ConsoleGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Target: ${profile.targetGameTitle ?: "Global Default"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = VioletGlow
                            )
                        }

                        if (profiles.size > 1) {
                            IconButton(
                                onClick = { onDeleteProfile(profile.id) },
                                modifier = Modifier.testTag("delete_profile_${profile.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete profile ${profile.name}",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "L-Stick: ${(profile.leftStickDeadzone * 100).toInt()}% DZ · ${"%.2f".format(profile.leftStickSensitivity)}x | " +
                            "R-Stick: ${(profile.rightStickDeadzone * 100).toInt()}% DZ · ${"%.2f".format(profile.rightStickSensitivity)}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (profile.swapFaceButtonsAB || profile.swapFaceButtonsXY || profile.swapBumpersTriggers || profile.invertRightY) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val flags = buildList {
                            if (profile.invertRightY) add("Invert Y")
                            if (profile.swapFaceButtonsAB) add("Swap A/B")
                            if (profile.swapFaceButtonsXY) add("Swap X/Y")
                            if (profile.swapBumpersTriggers) add("Swap L1/L2")
                        }
                        Text(
                            text = "Remaps: ${flags.joinToString(" · ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!profile.isActive) {
                            Button(
                                onClick = { onActivateProfile(profile.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricBlue,
                                    contentColor = CharcoalVoid
                                )
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Activate")
                            }
                        }

                        OutlinedButton(
                            onClick = { onEditInStudio(profile.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit in Studio")
                        }
                    }
                }
            }
        }
    }

    if (showNewDialog) {
        AlertDialog(
            onDismissRequest = { showNewDialog = false },
            title = { Text("Create Control Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Profile Name") },
                        placeholder = { Text("e.g. Shooter Precision") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Target Game (Optional):",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricBlue
                    )
                    val choices = installedGames.take(5)
                    if (choices.isEmpty()) {
                        Text(
                            text = "Global preset (no tagged games detected)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            choices.forEach { game ->
                                val isSelected = selectedPackage == game.packageName
                                TextButton(
                                    onClick = {
                                        selectedPackage = game.packageName
                                        selectedGameTitle = game.title
                                        if (newProfileName.isBlank()) {
                                            newProfileName = "${game.title} Pad"
                                        }
                                    }
                                ) {
                                    Text(
                                        text = if (isSelected) "✓ ${game.title}" else game.title,
                                        color = if (isSelected) ConsoleGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = newProfileName.trim().ifBlank { "Custom Profile" }
                        onCreateProfile(
                            ControlProfileEntity(
                                name = finalName,
                                targetPackageName = selectedPackage,
                                targetGameTitle = selectedGameTitle,
                                isActive = true
                            )
                        )
                        newProfileName = ""
                        selectedPackage = null
                        selectedGameTitle = "All Native Games"
                        showNewDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepPurple,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
