package com.example.controller

enum class ControllerType(val displayName: String, val badgeCode: String) {
    DUALSENSE("PlayStation 5 DualSense", "PS5"),
    DUALSHOCK_4("PlayStation 4 DualShock 4", "PS4"),
    DUALSHOCK_3("PlayStation 3 DualShock 3", "PS3"),
    XBOX("Xbox Controller", "XBOX"),
    USB_CONTROLLER("USB Game Controller", "USB"),
    GENERIC_GAMEPAD("Bluetooth / HID Gamepad", "HID"),
    UNKNOWN("Unclassified Input Device", "DEV")
}

enum class ConnectionTransport(val label: String) {
    BLUETOOTH_OR_WIRELESS("Bluetooth / Wireless"),
    USB_OR_WIRED("USB OTG / Wired"),
    EXTERNAL("External HID"),
    UNKNOWN("Transport Undisclosed")
}

data class MotionAxisSpec(
    val axis: Int,
    val axisName: String,
    val min: Float,
    val max: Float,
    val flat: Float,
    val fuzz: Float
)

data class ControllerCapabilities(
    val hasDPad: Boolean,
    val hasFaceButtons: Boolean,
    val hasShoulderBumpers: Boolean,
    val hasAnalogTriggers: Boolean,
    val hasThumbPress: Boolean,
    val hasLeftStick: Boolean,
    val hasRightStick: Boolean,
    val hasVibrator: Boolean,
    val availableKeyLabels: List<String>,
    val motionAxes: List<MotionAxisSpec>
)

data class ControllerBatteryInfo(
    val isSupportedOnAndroidVersion: Boolean,
    val isPresent: Boolean,
    val percentage: Int?,
    val statusLabel: String
)

data class ControllerDeviceInfo(
    val deviceId: Int,
    val name: String,
    val descriptor: String,
    val vendorId: Int,
    val productId: Int,
    val type: ControllerType,
    val transport: ConnectionTransport,
    val isExternal: Boolean,
    val sourcesDescription: String,
    val battery: ControllerBatteryInfo,
    val capabilities: ControllerCapabilities,
    val isGamepadClass: Boolean
) {
    val vidPidLabel: String
        get() = "VID 0x%04X · PID 0x%04X".format(vendorId, productId)
}

data class LiveGamepadState(
    val activeDeviceId: Int? = null,
    val activeDeviceName: String? = null,
    val pressedKeys: Set<Int> = emptySet(),
    val leftStickX: Float = 0f,
    val leftStickY: Float = 0f,
    val rightStickX: Float = 0f,
    val rightStickY: Float = 0f,
    val leftTrigger: Float = 0f,
    val rightTrigger: Float = 0f,
    val dpadX: Float = 0f,
    val dpadY: Float = 0f,
    val lastKeyLabel: String? = null,
    val lastKeyAction: String? = null,
    val lastScanCode: Int? = null,
    val lastAxisLabel: String? = null,
    val lastAxisValue: Float? = null
)

enum class InputEventType(val label: String) {
    KEY_DOWN("KEY_DOWN"),
    KEY_UP("KEY_UP"),
    AXIS_MOTION("MOTION")
}

data class InputLogEntry(
    val id: Long,
    val timestampMs: Long,
    val deviceId: Int,
    val deviceName: String,
    val eventType: InputEventType,
    val codeLabel: String,
    val rawCode: Int,
    val scanCode: Int,
    val valueSummary: String
)

data class PairedBluetoothDeviceInfo(
    val name: String,
    val addressSuffix: String,
    val looksLikeController: Boolean
)

sealed interface BluetoothScanState {
    data object Idle : BluetoothScanState
    data object PermissionRequired : BluetoothScanState
    data object PermissionDenied : BluetoothScanState
    data object BluetoothUnavailableOrOff : BluetoothScanState
    data class Loaded(val devices: List<PairedBluetoothDeviceInfo>) : BluetoothScanState
    data class Error(val message: String) : BluetoothScanState
}
