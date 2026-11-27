package com.example.ui.screens

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.controller.ControllerDeviceInfo
import com.example.controller.InputEventType
import com.example.controller.InputLogEntry
import com.example.controller.LiveGamepadState
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ControllerLabScreen(
    controllers: List<ControllerDeviceInfo>,
    liveState: LiveGamepadState,
    inputLog: List<InputLogEntry>,
    lockBackKey: Boolean,
    onToggleLockBackKey: (Boolean) -> Unit,
    onClearLog: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onOpenHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeController = controllers.firstOrNull { it.deviceId == liveState.activeDeviceId }
        ?: controllers.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("controller_lab_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Controller Lab",
                subtitle = "Real-time KeyEvent & MotionEvent hardware diagnostics",
                actionLabel = "Hub",
                onActionClick = onOpenHub
            )
        }

        // Active Device & Lock Back Key Strip
        item {
            ConsoleSurfaceCard(
                highlighted = activeController != null,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (activeController != null) ConsoleGreen else ConsoleAmber)
                        )
                        Column {
                            Text(
                                text = activeController?.name
                                    ?: liveState.activeDeviceName
                                    ?: "Waiting for physical controller",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = activeController?.let {
                                    "${it.type.displayName} · ${it.battery.statusLabel}"
                                } ?: "Pair via Bluetooth or plug in via USB OTG — events are never simulated.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (activeController == null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onOpenBluetoothSettings,
                        modifier = Modifier.testTag("lab_bluetooth_settings_btn")
                    ) {
                        Icon(Icons.Rounded.Bluetooth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pair Controller in Bluetooth Settings")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalSurfaceHigh)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Lock Back / B Button in Lab",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Prevents B/Circle from exiting the Lab while testing",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = lockBackKey,
                        onCheckedChange = onToggleLockBackKey,
                        modifier = Modifier.testTag("lab_lock_back_switch")
                    )
                }
            }
        }

        // Button / Axis Identification Banner
        item {
            ConsoleSurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lab_last_input_card")
            ) {
                Text(
                    text = "LIVE INPUT IDENTIFIER",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalSurfaceHigh)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "LAST BUTTON",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = liveState.lastKeyLabel ?: "Press any button",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (liveState.lastKeyLabel != null) ConsoleGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (liveState.lastScanCode != null) {
                            Text(
                                text = "${liveState.lastKeyAction} · scanCode=${liveState.lastScanCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = VioletGlow
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalSurfaceHigh)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "LAST AXIS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = liveState.lastAxisLabel ?: "Move stick / trigger",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (liveState.lastAxisLabel != null) ElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (liveState.lastAxisValue != null) {
                            Text(
                                text = "value = ${"%.3f".format(liveState.lastAxisValue)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = VioletGlow
                            )
                        }
                    }
                }
            }
        }

        // Dual Analog Stick Canvas Visualizations
        item {
            ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ANALOG STICKS (AXIS_X/Y & AXIS_Z/RZ)",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricBlue
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AnalogStickVisualizer(
                        label = "Left Stick",
                        x = liveState.leftStickX,
                        y = liveState.leftStickY,
                        isClicked = KeyEvent.KEYCODE_BUTTON_THUMBL in liveState.pressedKeys,
                        clickLabel = "L3",
                        accentColor = ElectricBlue
                    )
                    AnalogStickVisualizer(
                        label = "Right Stick",
                        x = liveState.rightStickX,
                        y = liveState.rightStickY,
                        isClicked = KeyEvent.KEYCODE_BUTTON_THUMBR in liveState.pressedKeys,
                        clickLabel = "R3",
                        accentColor = VioletGlow
                    )
                }
            }
        }

        // Analog Triggers + D-Pad Compass
        item {
            ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TRIGGERS & D-PAD",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricBlue
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TriggerGauge(
                        label = "L2 / LT",
                        value = liveState.leftTrigger,
                        digitalPressed = KeyEvent.KEYCODE_BUTTON_L2 in liveState.pressedKeys,
                        accent = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    TriggerGauge(
                        label = "R2 / RT",
                        value = liveState.rightTrigger,
                        digitalPressed = KeyEvent.KEYCODE_BUTTON_R2 in liveState.pressedKeys,
                        accent = DeepPurple,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "D-Pad / Hat Switch",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Reads both KEYCODE_DPAD_* and AXIS_HAT_X/Y",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "HAT_X=${"%.2f".format(liveState.dpadX)} · HAT_Y=${"%.2f".format(liveState.dpadY)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue
                        )
                    }

                    DPadCompass(
                        up = KeyEvent.KEYCODE_DPAD_UP in liveState.pressedKeys || liveState.dpadY < -0.5f,
                        down = KeyEvent.KEYCODE_DPAD_DOWN in liveState.pressedKeys || liveState.dpadY > 0.5f,
                        left = KeyEvent.KEYCODE_DPAD_LEFT in liveState.pressedKeys || liveState.dpadX < -0.5f,
                        right = KeyEvent.KEYCODE_DPAD_RIGHT in liveState.pressedKeys || liveState.dpadX > 0.5f
                    )
                }
            }
        }

        // Button Matrix
        item {
            ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PHYSICAL BUTTON MATRIX",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricBlue
                )
                Spacer(modifier = Modifier.height(10.dp))

                val matrixButtons = listOf(
                    KeyEvent.KEYCODE_BUTTON_A to "A / Cross",
                    KeyEvent.KEYCODE_BUTTON_B to "B / Circle",
                    KeyEvent.KEYCODE_BUTTON_X to "X / Square",
                    KeyEvent.KEYCODE_BUTTON_Y to "Y / Triangle",
                    KeyEvent.KEYCODE_BUTTON_L1 to "L1 / LB",
                    KeyEvent.KEYCODE_BUTTON_R1 to "R1 / RB",
                    KeyEvent.KEYCODE_BUTTON_L2 to "L2 (Dig)",
                    KeyEvent.KEYCODE_BUTTON_R2 to "R2 (Dig)",
                    KeyEvent.KEYCODE_BUTTON_THUMBL to "L3",
                    KeyEvent.KEYCODE_BUTTON_THUMBR to "R3",
                    KeyEvent.KEYCODE_BUTTON_SELECT to "Select",
                    KeyEvent.KEYCODE_BUTTON_START to "Start",
                    KeyEvent.KEYCODE_BUTTON_MODE to "Home / PS"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    matrixButtons.forEach { (code, label) ->
                        ButtonIndicatorChip(
                            label = label,
                            isPressed = code in liveState.pressedKeys
                        )
                    }
                }
            }
        }

        // Input Debugging Packet Log
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Input Event Debugger",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${inputLog.size} captured hardware packets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onClearLog,
                    modifier = Modifier.testTag("lab_clear_log_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CharcoalSurfaceHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear")
                }
            }
        }

        if (inputLog.isEmpty()) {
            item {
                ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SportsEsports,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No input events logged yet. Press buttons or move analog sticks on a connected physical controller to stream real KeyEvent and MotionEvent packets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(inputLog, key = { it.id }) { entry ->
                InputPacketRow(entry)
            }
        }
    }
}

@Composable
private fun AnalogStickVisualizer(
    label: String,
    x: Float,
    y: Float,
    isClicked: Boolean,
    clickLabel: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(132.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f - 8f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Outer boundary
                drawCircle(
                    color = CharcoalSurfaceHigh,
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = if (isClicked) ConsoleGreen else CharcoalOutline,
                    radius = radius,
                    center = center,
                    style = Stroke(width = if (isClicked) 4f else 2f)
                )

                // 15% deadzone ring
                drawCircle(
                    color = CharcoalOutline.copy(alpha = 0.7f),
                    radius = radius * 0.15f,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                // Crosshair axes
                drawLine(
                    color = CharcoalOutline.copy(alpha = 0.55f),
                    start = Offset(center.x - radius, center.y),
                    end = Offset(center.x + radius, center.y),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = CharcoalOutline.copy(alpha = 0.55f),
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y + radius),
                    strokeWidth = 1.5f
                )

                // Live thumbstick vector
                val stickCenter = Offset(
                    x = center.x + (x.coerceIn(-1f, 1f) * (radius - 12f)),
                    y = center.y + (y.coerceIn(-1f, 1f) * (radius - 12f))
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.6f),
                    start = center,
                    end = stickCenter,
                    strokeWidth = 4f
                )
                drawCircle(
                    color = accentColor.copy(alpha = 0.3f),
                    radius = 18f,
                    center = stickCenter
                )
                drawCircle(
                    color = if (isClicked) ConsoleGreen else accentColor,
                    radius = 10f,
                    center = stickCenter
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isClicked) "$label ($clickLabel)" else label,
            style = MaterialTheme.typography.titleSmall,
            color = if (isClicked) ConsoleGreen else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "X=${"%.2f".format(x)}  Y=${"%.2f".format(y)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TriggerGauge(
    label: String,
    value: Float,
    digitalPressed: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalSurfaceHigh)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = if (value > 0.05f || digitalPressed) accent else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = accent,
            trackColor = CharcoalVoid
        )
    }
}

@Composable
private fun DPadCompass(
    up: Boolean,
    down: Boolean,
    left: Boolean,
    right: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DPadNode("U", up)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DPadNode("L", left)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CharcoalSurfaceHigh)
            )
            DPadNode("R", right)
        }
        DPadNode("D", down)
    }
}

@Composable
private fun DPadNode(label: String, active: Boolean) {
    val bg by animateColorAsState(
        targetValue = if (active) ElectricBlue else CharcoalSurfaceHigh,
        label = "dpadBg"
    )
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (active) CharcoalVoid else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ButtonIndicatorChip(
    label: String,
    isPressed: Boolean
) {
    val bgColor by animateColorAsState(
        targetValue = if (isPressed) ConsoleGreen else CharcoalSurfaceHigh,
        label = "btnBg"
    )
    val textColor = if (isPressed) CharcoalVoid else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (isPressed) ConsoleGreen else CharcoalOutline
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun InputPacketRow(entry: InputLogEntry) {
    val badgeColor = when (entry.eventType) {
        InputEventType.KEY_DOWN -> ConsoleGreen
        InputEventType.KEY_UP -> VioletGlow
        InputEventType.AXIS_MOTION -> ElectricBlue
    }
    val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = entry.eventType.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = badgeColor
                )
                Text(
                    text = entry.codeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "dev#${entry.deviceId} · ${entry.valueSummary}" +
                    if (entry.scanCode > 0) " · scan=${entry.scanCode}" else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatter.format(Date(entry.timestampMs)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
