package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.preferences.UserPreferencesState
import com.example.ui.DeviceCompatibilitySummary
import com.example.ui.components.ConsoleSurfaceCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CharcoalVoid
import com.example.ui.theme.ConsoleAmber
import com.example.ui.theme.ConsoleGreen
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.VioletGlow

@Composable
fun SettingsScreen(
    preferences: UserPreferencesState,
    compatibility: DeviceCompatibilitySummary,
    accessibilityEnabled: Boolean,
    onSetConsoleMode: (Boolean) -> Unit,
    onSetShowAllApps: (Boolean) -> Unit,
    onSetLabLockBack: (Boolean) -> Unit,
    onSetHapticFeedback: (Boolean) -> Unit,
    onExitToAndroidHome: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Settings",
                subtitle = "Console mode, compatibility diagnostics & privacy"
            )
        }

        // Console Mode vs Normal Android Mode
        item {
            ConsoleSurfaceCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "EXPERIENCE MODE",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSwitchRow(
                    title = "Console Mode Interface",
                    subtitle = "Optimized handheld launcher layout with console navigation.",
                    checked = preferences.consoleModeEnabled,
                    onCheckedChange = onSetConsoleMode,
                    tag = "settings_console_mode_switch"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onExitToAndroidHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_exit_android_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = CharcoalVoid
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Return to Normal Android Launcher")
                }
            }
        }

        // Preferences
        item {
            ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "LIBRARY & INPUT LAB",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSwitchRow(
                    title = "Default My Games to All Launchable Apps",
                    subtitle = "Includes emulators and apps not tagged CATEGORY_GAME.",
                    checked = preferences.showAllLaunchableApps,
                    onCheckedChange = onSetShowAllApps,
                    tag = "settings_show_all_apps_switch"
                )
                SettingsSwitchRow(
                    title = "Lock Back / B Button in Controller Lab",
                    subtitle = "Prevents accidental screen exit while testing face buttons.",
                    checked = preferences.labLockBackKey,
                    onCheckedChange = onSetLabLockBack,
                    tag = "settings_lock_back_switch"
                )
                SettingsSwitchRow(
                    title = "Console UI Tactile Feedback",
                    subtitle = "Use device vibration cues for console interactions.",
                    checked = preferences.hapticFeedbackEnabled,
                    onCheckedChange = onSetHapticFeedback,
                    tag = "settings_haptic_switch"
                )
            }
        }

        // Accessibility Status
        item {
            ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessibilityNew,
                        contentDescription = null,
                        tint = if (accessibilityEnabled) ConsoleGreen else ConsoleAmber
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Optional Accessibility Touch Bridge",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (accessibilityEnabled) "Status: Enabled" else "Status: Disabled (Recommended unless touch-mapping)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (accessibilityEnabled) ConsoleGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage in Android Accessibility Settings")
                }
            }
        }

        // Device & Android Version Compatibility Matrix
        item {
            ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Devices,
                        contentDescription = null,
                        tint = ElectricBlue
                    )
                    Column {
                        Text(
                            text = "Device & OS Compatibility",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${compatibility.manufacturer} ${compatibility.model} · ${compatibility.androidVersionLabel} (API ${compatibility.apiLevel})",
                            style = MaterialTheme.typography.labelSmall,
                            color = VioletGlow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• Minimum Supported OS: Android 8.0 (API 26) · Target SDK: Android 16 (API 36)\n" +
                        "• Core InputManager & KeyEvent/MotionEvent: Supported on all API 26–36 devices\n" +
                        "• Controller BatteryState API: ${if (compatibility.supportsBatteryApi) "Supported on this device (API 31+)" else "Requires Android 12+ (API 31)"}\n" +
                        "• BLUETOOTH_CONNECT Runtime Gate: ${if (compatibility.supportsBluetoothConnectPermission) "Active (API 31+)" else "Legacy install-time BLUETOOTH (API 26–30)"}\n" +
                        "• VibratorManager Haptics: ${if (compatibility.supportsVibratorManager) "Active (API 31+)" else "Legacy InputDevice.vibrator fallback (API 26–30)"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Zero-Backend Privacy Guarantee
        item {
            ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = ConsoleGreen
                    )
                    Column {
                        Text(
                            text = "100% Local & Ad-Free",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "DroidPlay runs zero background services, collects zero analytics, requires no account, and keeps all profiles in local Room & DataStore storage.",
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
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag)
        )
    }
}
