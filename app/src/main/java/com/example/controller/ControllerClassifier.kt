package com.example.controller

import android.hardware.BatteryState
import android.os.Build
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import kotlin.math.roundToInt

object ControllerClassifier {

    private const val SONY_VENDOR_ID = 0x054C
    private const val MICROSOFT_VENDOR_ID = 0x045E

    private val DUALSENSE_PIDS = setOf(0x0CE6, 0x0DF2)
    private val DUALSHOCK_4_PIDS = setOf(0x05C4, 0x09CC, 0x0BA0)
    private val DUALSHOCK_3_PIDS = setOf(0x0268)

    private val XBOX_PIDS = setOf(
        0x028E, 0x028F, 0x02D1, 0x02DD, 0x02E0, 0x02E3, 0x02EA,
        0x02FD, 0x0B00, 0x0B05, 0x0B12, 0x0B13, 0x0B20, 0x0B22
    )

    val STANDARD_GAMEPAD_KEYS: List<Pair<Int, String>> = listOf(
        KeyEvent.KEYCODE_BUTTON_A to "A / Cross",
        KeyEvent.KEYCODE_BUTTON_B to "B / Circle",
        KeyEvent.KEYCODE_BUTTON_X to "X / Square",
        KeyEvent.KEYCODE_BUTTON_Y to "Y / Triangle",
        KeyEvent.KEYCODE_BUTTON_L1 to "L1 / LB",
        KeyEvent.KEYCODE_BUTTON_R1 to "R1 / RB",
        KeyEvent.KEYCODE_BUTTON_L2 to "L2 / LT",
        KeyEvent.KEYCODE_BUTTON_R2 to "R2 / RT",
        KeyEvent.KEYCODE_BUTTON_THUMBL to "L3",
        KeyEvent.KEYCODE_BUTTON_THUMBR to "R3",
        KeyEvent.KEYCODE_BUTTON_START to "Start / Options",
        KeyEvent.KEYCODE_BUTTON_SELECT to "Select / Share",
        KeyEvent.KEYCODE_BUTTON_MODE to "Home / PS / Xbox",
        KeyEvent.KEYCODE_DPAD_UP to "D-Pad Up",
        KeyEvent.KEYCODE_DPAD_DOWN to "D-Pad Down",
        KeyEvent.KEYCODE_DPAD_LEFT to "D-Pad Left",
        KeyEvent.KEYCODE_DPAD_RIGHT to "D-Pad Right"
    )

    val TRACKED_AXES: List<Int> = listOf(
        MotionEvent.AXIS_X,
        MotionEvent.AXIS_Y,
        MotionEvent.AXIS_Z,
        MotionEvent.AXIS_RZ,
        MotionEvent.AXIS_LTRIGGER,
        MotionEvent.AXIS_RTRIGGER,
        MotionEvent.AXIS_BRAKE,
        MotionEvent.AXIS_GAS,
        MotionEvent.AXIS_HAT_X,
        MotionEvent.AXIS_HAT_Y
    )

    fun isGamepadOrJoystick(device: InputDevice): Boolean {
        if (device.isVirtual) return false
        val sources = device.sources
        val hasGamepad = (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
        val hasJoystick = (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
        return hasGamepad || hasJoystick
    }

    fun classifyController(
        vendorId: Int,
        productId: Int,
        name: String,
        sources: Int,
        descriptor: String = ""
    ): ControllerType {
        val lowerName = name.lowercase()
        return when {
            vendorId == SONY_VENDOR_ID && productId in DUALSENSE_PIDS -> ControllerType.DUALSENSE
            vendorId == SONY_VENDOR_ID && productId in DUALSHOCK_4_PIDS -> ControllerType.DUALSHOCK_4
            vendorId == SONY_VENDOR_ID && productId in DUALSHOCK_3_PIDS -> ControllerType.DUALSHOCK_3
            lowerName.contains("dualsense") -> ControllerType.DUALSENSE
            lowerName.contains("dualshock 4") || lowerName.contains("wireless controller") && vendorId == SONY_VENDOR_ID ->
                ControllerType.DUALSHOCK_4
            lowerName.contains("playstation(r)3") || lowerName.contains("dualshock 3") ->
                ControllerType.DUALSHOCK_3
            vendorId == MICROSOFT_VENDOR_ID && (productId in XBOX_PIDS || lowerName.contains("xbox")) ->
                ControllerType.XBOX
            lowerName.contains("xbox") -> ControllerType.XBOX
            descriptor.contains("usb", ignoreCase = true) || lowerName.contains("usb") ->
                ControllerType.USB_CONTROLLER
            (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ->
                ControllerType.GENERIC_GAMEPAD
            else -> ControllerType.UNKNOWN
        }
    }

    fun inferTransport(device: InputDevice): ConnectionTransport {
        val lowerName = device.name.orEmpty().lowercase()
        val lowerDesc = device.descriptor.orEmpty().lowercase()
        val isExternal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            device.isExternal
        } else {
            true
        }
        return when {
            lowerDesc.contains("usb") || lowerName.contains("usb") -> ConnectionTransport.USB_OR_WIRED
            lowerDesc.contains("bt") || lowerDesc.contains("uhid") || lowerName.contains("bluetooth") ||
                lowerName.contains("wireless") || lowerName.contains("dualsense") ||
                lowerName.contains("dualshock") -> ConnectionTransport.BLUETOOTH_OR_WIRELESS
            isExternal -> ConnectionTransport.EXTERNAL
            else -> ConnectionTransport.UNKNOWN
        }
    }

    fun inspectDevice(device: InputDevice): ControllerDeviceInfo {
        val sources = device.sources
        val keyCodes = STANDARD_GAMEPAD_KEYS.map { it.first }.toIntArray()
        val supportedFlags = try {
            device.hasKeys(*keyCodes)
        } catch (_: Throwable) {
            BooleanArray(keyCodes.size) { false }
        }
        val availableKeys = STANDARD_GAMEPAD_KEYS.indices
            .filter { supportedFlags.getOrElse(it) { false } }
            .map { STANDARD_GAMEPAD_KEYS[it].second }

        val axisSpecs = device.motionRanges
            .filter { it.axis in TRACKED_AXES }
            .distinctBy { it.axis }
            .map { range ->
                MotionAxisSpec(
                    axis = range.axis,
                    axisName = MotionEvent.axisToString(range.axis),
                    min = range.min,
                    max = range.max,
                    flat = range.flat,
                    fuzz = range.fuzz
                )
            }

        val axisIds = axisSpecs.map { it.axis }.toSet()

        val hasVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            device.vibratorManager.vibratorIds.isNotEmpty()
        } else {
            @Suppress("DEPRECATION")
            device.vibrator.hasVibrator()
        }

        val batteryInfo = readBatteryInfo(device)

        val isExternal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            device.isExternal
        } else {
            !device.isVirtual
        }

        val capabilities = ControllerCapabilities(
            hasDPad = (sources and InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD ||
                MotionEvent.AXIS_HAT_X in axisIds ||
                supportedFlags.getOrElse(13) { false },
            hasFaceButtons = supportedFlags.take(4).any { it },
            hasShoulderBumpers = supportedFlags.getOrElse(4) { false } || supportedFlags.getOrElse(5) { false },
            hasAnalogTriggers = MotionEvent.AXIS_LTRIGGER in axisIds ||
                MotionEvent.AXIS_RTRIGGER in axisIds ||
                MotionEvent.AXIS_BRAKE in axisIds ||
                MotionEvent.AXIS_GAS in axisIds,
            hasThumbPress = supportedFlags.getOrElse(8) { false } || supportedFlags.getOrElse(9) { false },
            hasLeftStick = MotionEvent.AXIS_X in axisIds && MotionEvent.AXIS_Y in axisIds,
            hasRightStick = MotionEvent.AXIS_Z in axisIds || MotionEvent.AXIS_RZ in axisIds,
            hasVibrator = hasVibrator,
            availableKeyLabels = availableKeys,
            motionAxes = axisSpecs
        )

        return ControllerDeviceInfo(
            deviceId = device.id,
            name = device.name?.takeIf { it.isNotBlank() } ?: "Unnamed Device #${device.id}",
            descriptor = device.descriptor.orEmpty(),
            vendorId = device.vendorId,
            productId = device.productId,
            type = classifyController(
                vendorId = device.vendorId,
                productId = device.productId,
                name = device.name.orEmpty(),
                sources = sources,
                descriptor = device.descriptor.orEmpty()
            ),
            transport = inferTransport(device),
            isExternal = isExternal,
            sourcesDescription = describeSources(sources),
            battery = batteryInfo,
            capabilities = capabilities,
            isGamepadClass = isGamepadOrJoystick(device)
        )
    }

    fun readBatteryInfo(device: InputDevice): ControllerBatteryInfo {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return ControllerBatteryInfo(
                isSupportedOnAndroidVersion = false,
                isPresent = false,
                percentage = null,
                statusLabel = "Requires Android 12+ (API 31)"
            )
        }
        val state = device.batteryState
        if (!state.isPresent) {
            return ControllerBatteryInfo(
                isSupportedOnAndroidVersion = true,
                isPresent = false,
                percentage = null,
                statusLabel = "Not exposed by controller"
            )
        }
        val pct = if (state.capacity in 0f..1f) {
            (state.capacity * 100f).roundToInt().coerceIn(0, 100)
        } else {
            null
        }
        val statusText = when (state.status) {
            BatteryState.STATUS_CHARGING -> "Charging"
            BatteryState.STATUS_DISCHARGING -> "Discharging"
            BatteryState.STATUS_FULL -> "Full"
            BatteryState.STATUS_NOT_CHARGING -> "Not charging"
            else -> "Active"
        }
        val label = if (pct != null) "$pct% · $statusText" else statusText
        return ControllerBatteryInfo(
            isSupportedOnAndroidVersion = true,
            isPresent = true,
            percentage = pct,
            statusLabel = label
        )
    }

    fun describeSources(sources: Int): String {
        val parts = buildList {
            if ((sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) add("GAMEPAD")
            if ((sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) add("JOYSTICK")
            if ((sources and InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) add("DPAD")
            if ((sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD) add("KEYBOARD")
            if ((sources and InputDevice.SOURCE_TOUCHPAD) == InputDevice.SOURCE_TOUCHPAD) add("TOUCHPAD")
            if ((sources and InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE) add("MOUSE")
        }
        return if (parts.isEmpty()) "0x%08X".format(sources) else parts.joinToString(" | ")
    }

    fun formatFriendlyKeyName(keyCode: Int): String {
        val match = STANDARD_GAMEPAD_KEYS.firstOrNull { it.first == keyCode }
        return match?.second ?: KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_")
    }

    fun isGamepadKeyEvent(event: KeyEvent): Boolean {
        val source = event.source
        val fromGamepad = (source and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
            (source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
        return fromGamepad || KeyEvent.isGamepadButton(event.keyCode)
    }

    fun isJoystickMotionEvent(event: MotionEvent): Boolean {
        val source = event.source
        return (source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
            (source and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
    }
}
