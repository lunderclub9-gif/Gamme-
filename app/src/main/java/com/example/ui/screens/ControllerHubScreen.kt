package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.controller.BluetoothScanState
import com.example.controller.ControllerDeviceInfo
import com.example.ui.components.ConsoleSurfaceCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CharcoalOutline
import com.example.ui.theme.CharcoalSurfaceHigh
import com.example.ui.theme.CharcoalVoid
import com.example.ui.theme.ConsoleAmber
import com.example.ui.theme.ConsoleGreen
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.VioletGlow

@Composable
fun ControllerHubScreen(
    controllers: List<ControllerDeviceInfo>,
    allExternalDevices: List<ControllerDeviceInfo>,
    bluetoothScanState: BluetoothScanState,
    hasBluetoothPermission: Boolean,
    onRefreshDevices: () -> Unit,
    onOpenLab: () -> Unit,
    onTestRumble: (Int) -> Unit,
    onInspectPairedBluetooth: () -> Unit,
    onBluetoothPermissionDenied: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRawExternalDevices by rememberSaveable { mutableStateOf(false) }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onInspectPairedBluetooth()
        } else {
            onBluetoothPermissionDenied()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("controller_hub_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Controller Hub",
                subtitle = "Live hardware discovery via Android InputManager",
                actionLabel = "Refresh",
                onActionClick = onRefreshDevices
            )
        }

        if (controllers.isEmpty()) {
            item {
                ConsoleSurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hub_empty_state")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_controller_showcase),
                        contentDescription = "Wireless controller pairing guide",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Active Gamepad Detected",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "DroidPlay listens for DualSense (PS5), DualShock 3/4, Xbox Wireless/USB, generic Bluetooth HID gamepads, and USB OTG controllers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenBluetoothSettings,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hub_open_bt_settings"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = CharcoalVoid
                            )
                        ) {
                            Icon(Icons.Rounded.Bluetooth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bluetooth")
                        }
                        OutlinedButton(
                            onClick = onRefreshDevices,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hub_refresh_button")
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rescan")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onOpenLab,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Open Controller Lab Anyway", color = VioletGlow)
                    }
                }
            }
        } else {
            items(controllers, key = { it.deviceId }) { controller ->
                ControllerDeviceCard(
                    controller = controller,
                    onOpenLab = onOpenLab,
                    onTestRumble = { onTestRumble(controller.deviceId) }
                )
            }
        }

        // Optional Paired Bluetooth Inspector (Android 8-11 vs Android 12+ permission aware)
        item {
            ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bluetooth,
                            contentDescription = null,
                            tint = ElectricBlue
                        )
                        Column {
                            Text(
                                text = "Paired Bluetooth Inspector",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    "Android 12+ uses optional BLUETOOTH_CONNECT permission"
                                } else {
                                    "Android 8–11 uses install-time BLUETOOTH permission"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasBluetoothPermission) {
                                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                            } else {
                                onInspectPairedBluetooth()
                            }
                        },
                        modifier = Modifier.testTag("hub_scan_bt_bonded")
                    ) {
                        Text("Scan Paired")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (bluetoothScanState) {
                    is BluetoothScanState.Idle -> {
                        Text(
                            text = "Optional: Inspect bonded Bluetooth controllers without affecting active USB or HID input detection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is BluetoothScanState.PermissionRequired -> {
                        Text(
                            text = "Tap Scan Paired to grant BLUETOOTH_CONNECT (Android 12+) if you want to inspect paired Bluetooth devices.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ConsoleAmber
                        )
                    }
                    is BluetoothScanState.PermissionDenied -> {
                        Text(
                            text = "Bluetooth permission was declined. Active gamepads still work normally through Android's HID InputManager.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ConsoleAmber
                        )
                    }
                    is BluetoothScanState.BluetoothUnavailableOrOff -> {
                        Text(
                            text = "Bluetooth adapter is off or unavailable on this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ConsoleAmber
                        )
                    }
                    is BluetoothScanState.Error -> {
                        Text(
                            text = bluetoothScanState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is BluetoothScanState.Loaded -> {
                        if (bluetoothScanState.devices.isEmpty()) {
                            Text(
                                text = "No bonded Bluetooth devices found.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                bluetoothScanState.devices.forEach { bt ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CharcoalSurfaceHigh)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = bt.name,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                text = bt.addressSuffix,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (bt.looksLikeController) {
                                                ConsoleGreen.copy(alpha = 0.16f)
                                            } else {
                                                DeepPurple.copy(alpha = 0.16f)
                                            }
                                        ) {
                                            Text(
                                                text = if (bt.looksLikeController) "Gamepad HID" else "Bluetooth",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (bt.looksLikeController) ConsoleGreen else VioletGlow,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Raw External Input Devices Diagnostic
        item {
            ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "All External Input Devices (${allExternalDevices.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Raw Android InputManager inventory for USB OTG & HID diagnostics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { showRawExternalDevices = !showRawExternalDevices }) {
                        Text(if (showRawExternalDevices) "Hide" else "Inspect")
                        Icon(
                            imageVector = if (showRawExternalDevices) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null
                        )
                    }
                }

                if (showRawExternalDevices) {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (allExternalDevices.isEmpty()) {
                        Text(
                            text = "No external input devices reported by InputManager.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            allExternalDevices.forEach { dev ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CharcoalSurfaceHigh)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "#${dev.deviceId} · ${dev.name}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "${dev.vidPidLabel} · ${dev.sourcesDescription}",
                                        style = MaterialTheme.typography.labelSmall,
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ControllerDeviceCard(
    controller: ControllerDeviceInfo,
    onOpenLab: () -> Unit,
    onTestRumble: () -> Unit
) {
    var expandedSpecs by rememberSaveable(controller.deviceId) { mutableStateOf(false) }

    ConsoleSurfaceCard(
        highlighted = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("controller_card_${controller.deviceId}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ConsoleGreen.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SportsEsports,
                        contentDescription = null,
                        tint = ConsoleGreen
                    )
                }
                Column {
                    Text(
                        text = controller.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = controller.type.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricBlue
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ConsoleGreen.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, ConsoleGreen.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "CONNECTED",
                    style = MaterialTheme.typography.labelSmall,
                    color = ConsoleGreen,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoChip(
                icon = Icons.Rounded.Cable,
                text = controller.transport.label,
                modifier = Modifier.weight(1f)
            )
            InfoChip(
                icon = Icons.Rounded.BatteryChargingFull,
                text = controller.battery.statusLabel,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${controller.vidPidLabel} · Device ID #${controller.deviceId} · ${controller.sourcesDescription}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val cap = controller.capabilities
            if (cap.hasFaceButtons) CapabilityBadge("A/B/X/Y")
            if (cap.hasDPad) CapabilityBadge("D-Pad")
            if (cap.hasLeftStick) CapabilityBadge("Left Stick")
            if (cap.hasRightStick) CapabilityBadge("Right Stick")
            if (cap.hasShoulderBumpers) CapabilityBadge("L1/R1")
            if (cap.hasAnalogTriggers) CapabilityBadge("Analog Triggers")
            if (cap.hasThumbPress) CapabilityBadge("L3/R3")
            if (cap.hasVibrator) CapabilityBadge("Haptics")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenLab,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = CharcoalVoid
                )
            ) {
                Icon(Icons.Rounded.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test in Lab")
            }

            if (controller.capabilities.hasVibrator) {
                OutlinedButton(onClick = onTestRumble) {
                    Icon(Icons.Rounded.Vibration, contentDescription = "Test rumble", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rumble")
                }
            }
        }

        TextButton(
            onClick = { expandedSpecs = !expandedSpecs },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(if (expandedSpecs) "Hide Axes & Keys" else "View Axes (${controller.capabilities.motionAxes.size}) & Keys (${controller.capabilities.availableKeyLabels.size})")
        }

        if (expandedSpecs) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Detected Keys: ${controller.capabilities.availableKeyLabels.joinToString().ifBlank { "None reported" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            controller.capabilities.motionAxes.forEach { axis ->
                Text(
                    text = "${axis.axisName}: [${axis.min} .. ${axis.max}] flat=${axis.flat}",
                    style = MaterialTheme.typography.labelSmall,
                    color = VioletGlow
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CharcoalSurfaceHigh)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricBlue,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

@Composable
private fun CapabilityBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = DeepPurple.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, CharcoalOutline)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = VioletGlow,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
