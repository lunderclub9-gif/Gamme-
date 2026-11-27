package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.ControlProfileEntity
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
import kotlin.math.pow

@Composable
fun ControlStudioScreen(
    activeProfile: ControlProfileEntity?,
    accessibilityEnabled: Boolean,
    onSaveProfile: (ControlProfileEntity, Boolean) -> Unit,
    onOpenProfiles: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var profileName by rememberSaveable(activeProfile?.id) {
        mutableStateOf(activeProfile?.name ?: "Console Standard")
    }
    var leftDeadzone by rememberSaveable(activeProfile?.id) {
        mutableFloatStateOf(activeProfile?.leftStickDeadzone ?: 0.08f)
    }
    var rightDeadzone by rememberSaveable(activeProfile?.id) {
        mutableFloatStateOf(activeProfile?.rightStickDeadzone ?: 0.08f)
    }
    var leftSensitivity by rememberSaveable(activeProfile?.id) {
        mutableFloatStateOf(activeProfile?.leftStickSensitivity ?: 1.0f)
    }
    var rightSensitivity by rememberSaveable(activeProfile?.id) {
        mutableFloatStateOf(activeProfile?.rightStickSensitivity ?: 1.0f)
    }
    var triggerThreshold by rememberSaveable(activeProfile?.id) {
        mutableFloatStateOf(activeProfile?.triggerThreshold ?: 0.15f)
    }
    var invertRightY by rememberSaveable(activeProfile?.id) {
        mutableStateOf(activeProfile?.invertRightY ?: false)
    }
    var swapAB by rememberSaveable(activeProfile?.id) {
        mutableStateOf(activeProfile?.swapFaceButtonsAB ?: false)
    }
    var swapXY by rememberSaveable(activeProfile?.id) {
        mutableStateOf(activeProfile?.swapFaceButtonsXY ?: false)
    }
    var swapBumpers by rememberSaveable(activeProfile?.id) {
        mutableStateOf(activeProfile?.swapBumpersTriggers ?: false)
    }
    var touchNotes by rememberSaveable(activeProfile?.id) {
        mutableStateOf(activeProfile?.touchMappingNotes ?: "")
    }

    LaunchedEffect(activeProfile?.id) {
        if (activeProfile != null) {
            profileName = activeProfile.name
            leftDeadzone = activeProfile.leftStickDeadzone
            rightDeadzone = activeProfile.rightStickDeadzone
            leftSensitivity = activeProfile.leftStickSensitivity
            rightSensitivity = activeProfile.rightStickSensitivity
            triggerThreshold = activeProfile.triggerThreshold
            invertRightY = activeProfile.invertRightY
            swapAB = activeProfile.swapFaceButtonsAB
            swapXY = activeProfile.swapFaceButtonsXY
            swapBumpers = activeProfile.swapBumpersTriggers
            touchNotes = activeProfile.touchMappingNotes
        }
    }

    fun buildProfile(asNew: Boolean): ControlProfileEntity {
        val base = activeProfile ?: ControlProfileEntity(name = profileName)
        return base.copy(
            id = if (asNew) 0L else base.id,
            name = if (asNew) "${profileName.trim()} Copy" else profileName.trim().ifBlank { "Custom Profile" },
            leftStickDeadzone = leftDeadzone,
            rightStickDeadzone = rightDeadzone,
            leftStickSensitivity = leftSensitivity,
            rightStickSensitivity = rightSensitivity,
            triggerThreshold = triggerThreshold,
            invertRightY = invertRightY,
            swapFaceButtonsAB = swapAB,
            swapFaceButtonsXY = swapXY,
            swapBumpersTriggers = swapBumpers,
            touchMappingNotes = touchNotes
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("control_studio_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Control Studio",
                subtitle = activeProfile?.targetGameTitle ?: "Global & Per-Game Controller Mapping",
                actionLabel = "All Profiles",
                onActionClick = onOpenProfiles
            )
        }

        // Profile Name & Target Banner
        item {
            ConsoleSurfaceCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ACTIVE PROFILE",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_profile_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Target: ${activeProfile?.targetGameTitle ?: "All Native Games"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VioletGlow
                )
            }
        }

        // Analog Sensitivity & Response Curve Canvas
        item {
            ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ANALOG RESPONSE & DEADZONES",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricBlue
                )
                Spacer(modifier = Modifier.height(10.dp))

                ResponseCurvePreview(
                    deadzone = leftDeadzone,
                    sensitivity = leftSensitivity,
                    rightDeadzone = rightDeadzone,
                    rightSensitivity = rightSensitivity,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalSurfaceHigh)
                )

                Spacer(modifier = Modifier.height(12.dp))

                StudioSlider(
                    label = "Left Stick Deadzone",
                    valueText = "${(leftDeadzone * 100).toInt()}%",
                    value = leftDeadzone,
                    valueRange = 0.0f..0.35f,
                    onValueChange = { leftDeadzone = it },
                    tag = "slider_left_deadzone"
                )
                StudioSlider(
                    label = "Left Stick Sensitivity",
                    valueText = "${"%.2f".format(leftSensitivity)}x",
                    value = leftSensitivity,
                    valueRange = 0.5f..2.0f,
                    onValueChange = { leftSensitivity = it },
                    tag = "slider_left_sensitivity"
                )
                StudioSlider(
                    label = "Right Stick Deadzone",
                    valueText = "${(rightDeadzone * 100).toInt()}%",
                    value = rightDeadzone,
                    valueRange = 0.0f..0.35f,
                    onValueChange = { rightDeadzone = it },
                    tag = "slider_right_deadzone"
                )
                StudioSlider(
                    label = "Right Stick Sensitivity",
                    valueText = "${"%.2f".format(rightSensitivity)}x",
                    value = rightSensitivity,
                    valueRange = 0.5f..2.0f,
                    onValueChange = { rightSensitivity = it },
                    tag = "slider_right_sensitivity"
                )
                StudioSlider(
                    label = "Trigger Threshold (L2 / R2)",
                    valueText = "${(triggerThreshold * 100).toInt()}%",
                    value = triggerThreshold,
                    valueRange = 0.05f..0.90f,
                    onValueChange = { triggerThreshold = it },
                    tag = "slider_trigger_threshold"
                )
            }
        }

        // Button Configuration Matrix
        item {
            ConsoleSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "BUTTON REMAPPING CONFIGURATION",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricBlue
                )
                Spacer(modifier = Modifier.height(8.dp))

                StudioToggleRow(
                    title = "Invert Right Stick Y-Axis",
                    subtitle = "Flips vertical camera pitch input",
                    checked = invertRightY,
                    onCheckedChange = { invertRightY = it }
                )
                StudioToggleRow(
                    title = "Swap A / B (Nintendo / Classic Layout)",
                    subtitle = "Swaps Cross/A and Circle/B confirm actions",
                    checked = swapAB,
                    onCheckedChange = { swapAB = it }
                )
                StudioToggleRow(
                    title = "Swap X / Y Face Buttons",
                    subtitle = "Swaps Square/X and Triangle/Y bindings",
                    checked = swapXY,
                    onCheckedChange = { swapXY = it }
                )
                StudioToggleRow(
                    title = "Swap Bumpers & Triggers (L1/R1 <-> L2/R2)",
                    subtitle = "Preferred for shooters on PlayStation/Xbox pads",
                    checked = swapBumpers,
                    onCheckedChange = { swapBumpers = it }
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = touchNotes,
                    onValueChange = { touchNotes = it },
                    label = { Text("Touch-Zone & Game Notes") },
                    placeholder = { Text("e.g. Right stick -> Camera drag, A -> Jump button") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Save Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onSaveProfile(buildProfile(asNew = false), true) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("studio_save_profile_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = CharcoalVoid
                    )
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Profile")
                }

                OutlinedButton(
                    onClick = { onSaveProfile(buildProfile(asNew = true), true) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("studio_save_copy_button")
                ) {
                    Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Copy")
                }
            }
        }

        // Optional AccessibilityService Touch Mapping Architecture Card
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
                            text = "Optional Touch-Mapping Bridge",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (accessibilityEnabled) {
                                "AccessibilityService enabled voluntarily"
                            } else {
                                "Not enabled (Native controller games do not need this)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (accessibilityEnabled) ConsoleGreen else ConsoleAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "For Android games with native gamepad support, Android delivers controller events directly with zero extra permissions. For touch-only games, DroidPlay provides an optional AccessibilityService (API 24+ dispatchGesture) that you can enable voluntarily.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CharcoalSurfaceHigh,
                    border = BorderStroke(1.dp, CharcoalOutline)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = VioletGlow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Anti-Cheat & Android 13–16 Notice: DroidPlay never fakes background input or injects into protected games. On Android 13–16 (including HONOR X9d), sideloaded APKs require tapping 'Allow restricted settings' in App Info before System Accessibility allows toggling the service.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_open_accessibility_settings")
                ) {
                    Text("Open Android Accessibility Settings")
                }
            }
        }
    }
}

@Composable
private fun ResponseCurvePreview(
    deadzone: Float,
    sensitivity: Float,
    rightDeadzone: Float,
    rightSensitivity: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.padding(14.dp)) {
        val w = size.width
        val h = size.height

        // Grid lines
        drawLine(CharcoalOutline, Offset(0f, h), Offset(w, h), strokeWidth = 2f)
        drawLine(CharcoalOutline, Offset(0f, 0f), Offset(0f, h), strokeWidth = 2f)
        drawLine(
            CharcoalOutline.copy(alpha = 0.4f),
            Offset(0f, h),
            Offset(w, 0f),
            strokeWidth = 1.5f
        )

        fun buildCurvePath(dz: Float, sens: Float): Path {
            val path = Path()
            val steps = 40
            val exponent = (1.6f - (sens - 0.5f) * 0.6f).coerceIn(0.45f, 1.8f)
            for (i in 0..steps) {
                val xNorm = i / steps.toFloat()
                val yNorm = if (xNorm <= dz) {
                    0f
                } else {
                    val rem = ((xNorm - dz) / (1f - dz).coerceAtLeast(0.05f)).coerceIn(0f, 1f)
                    (rem.pow(exponent) * sens).coerceIn(0f, 1f)
                }
                val px = xNorm * w
                val py = h - (yNorm * h)
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            return path
        }

        drawPath(
            path = buildCurvePath(rightDeadzone, rightSensitivity),
            color = DeepPurple,
            style = Stroke(width = 4f)
        )
        drawPath(
            path = buildCurvePath(deadzone, sensitivity),
            color = ElectricBlue,
            style = Stroke(width = 5f)
        )
    }
}

@Composable
private fun StudioSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    tag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                color = ElectricBlue
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.testTag(tag),
            colors = SliderDefaults.colors(
                thumbColor = ElectricBlue,
                activeTrackColor = ElectricBlue,
                inactiveTrackColor = CharcoalSurfaceHigh
            )
        )
    }
}

@Composable
private fun StudioToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
