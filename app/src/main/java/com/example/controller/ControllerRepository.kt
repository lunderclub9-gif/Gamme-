package com.example.controller

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.input.InputManager
import android.os.Build
import android.os.CombinedVibration
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs

class ControllerRepository(context: Context) {

    private val appContext = context.applicationContext
    private val inputManager = appContext.getSystemService(Context.INPUT_SERVICE) as InputManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val logSequence = AtomicLong(1L)

    private val _controllers = MutableStateFlow<List<ControllerDeviceInfo>>(emptyList())
    val controllers: StateFlow<List<ControllerDeviceInfo>> = _controllers.asStateFlow()

    private val _allExternalInputDevices = MutableStateFlow<List<ControllerDeviceInfo>>(emptyList())
    val allExternalInputDevices: StateFlow<List<ControllerDeviceInfo>> = _allExternalInputDevices.asStateFlow()

    private val _liveState = MutableStateFlow(LiveGamepadState())
    val liveState: StateFlow<LiveGamepadState> = _liveState.asStateFlow()

    private val _inputLog = MutableStateFlow<List<InputLogEntry>>(emptyList())
    val inputLog: StateFlow<List<InputLogEntry>> = _inputLog.asStateFlow()

    private val _bluetoothScanState = MutableStateFlow<BluetoothScanState>(BluetoothScanState.Idle)
    val bluetoothScanState: StateFlow<BluetoothScanState> = _bluetoothScanState.asStateFlow()

    private var isRegistered = false

    private val inputDeviceListener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(deviceId: Int) {
            refreshDevices()
        }

        override fun onInputDeviceRemoved(deviceId: Int) {
            refreshDevices()
            _liveState.update { current ->
                if (current.activeDeviceId == deviceId) LiveGamepadState() else current
            }
        }

        override fun onInputDeviceChanged(deviceId: Int) {
            refreshDevices()
        }
    }

    fun startListening() {
        refreshDevices()
        if (!isRegistered) {
            inputManager.registerInputDeviceListener(inputDeviceListener, mainHandler)
            isRegistered = true
        }
    }

    fun stopListening() {
        if (isRegistered) {
            inputManager.unregisterInputDeviceListener(inputDeviceListener)
            isRegistered = false
        }
    }

    fun refreshDevices() {
        val ids = inputManager.inputDeviceIds
        val inspectedAll = ids.toList().mapNotNull { id ->
            val device = inputManager.getInputDevice(id) ?: return@mapNotNull null
            if (device.isVirtual) return@mapNotNull null
            ControllerClassifier.inspectDevice(device)
        }
        val gamepads = inspectedAll
            .filter { it.isGamepadClass }
            .sortedBy { it.deviceId }
        val externals = inspectedAll
            .filter { it.isExternal || it.isGamepadClass }
            .sortedBy { it.deviceId }

        _controllers.value = gamepads
        _allExternalInputDevices.value = externals
    }

    fun processKeyEvent(event: KeyEvent, captureForLab: Boolean, lockBackKeyInLab: Boolean): Boolean {
        if (!ControllerClassifier.isGamepadKeyEvent(event)) return false
        val device = inputManager.getInputDevice(event.deviceId)
        val deviceName = device?.name ?: "Device #${event.deviceId}"

        val isDown = event.action == KeyEvent.ACTION_DOWN
        val isUp = event.action == KeyEvent.ACTION_UP
        if (!isDown && !isUp) return false

        // Ignore repeated hold events in log spam, still keep pressed set accurate
        val shouldLog = event.repeatCount == 0
        val friendlyLabel = ControllerClassifier.formatFriendlyKeyName(event.keyCode)

        _liveState.update { state ->
            val nextKeys = if (isDown) {
                state.pressedKeys + event.keyCode
            } else {
                state.pressedKeys - event.keyCode
            }
            state.copy(
                activeDeviceId = event.deviceId,
                activeDeviceName = deviceName,
                pressedKeys = nextKeys,
                lastKeyLabel = "$friendlyLabel (${KeyEvent.keyCodeToString(event.keyCode)})",
                lastKeyAction = if (isDown) "PRESSED" else "RELEASED",
                lastScanCode = event.scanCode
            )
        }

        if (shouldLog) {
            appendLog(
                InputLogEntry(
                    id = logSequence.getAndIncrement(),
                    timestampMs = System.currentTimeMillis(),
                    deviceId = event.deviceId,
                    deviceName = deviceName,
                    eventType = if (isDown) InputEventType.KEY_DOWN else InputEventType.KEY_UP,
                    codeLabel = friendlyLabel,
                    rawCode = event.keyCode,
                    scanCode = event.scanCode,
                    valueSummary = KeyEvent.keyCodeToString(event.keyCode)
                )
            )
        }

        // In Controller Lab with Back-key lock enabled, prevent B/Back from exiting screen
        return captureForLab && lockBackKeyInLab
    }

    fun processMotionEvent(event: MotionEvent, captureForLab: Boolean): Boolean {
        if (!ControllerClassifier.isJoystickMotionEvent(event)) return false
        if (event.action != MotionEvent.ACTION_MOVE) return false

        val device = inputManager.getInputDevice(event.deviceId)
        val deviceName = device?.name ?: "Device #${event.deviceId}"

        val lx = readCenteredAxis(event, device, MotionEvent.AXIS_X)
        val ly = readCenteredAxis(event, device, MotionEvent.AXIS_Y)
        val rx = readCenteredAxis(event, device, MotionEvent.AXIS_Z)
        val ry = readCenteredAxis(event, device, MotionEvent.AXIS_RZ)
        val hatX = readCenteredAxis(event, device, MotionEvent.AXIS_HAT_X)
        val hatY = readCenteredAxis(event, device, MotionEvent.AXIS_HAT_Y)

        val lTrigger = maxOf(
            event.getAxisValue(MotionEvent.AXIS_LTRIGGER),
            event.getAxisValue(MotionEvent.AXIS_BRAKE)
        ).coerceIn(0f, 1f)

        val rTrigger = maxOf(
            event.getAxisValue(MotionEvent.AXIS_RTRIGGER),
            event.getAxisValue(MotionEvent.AXIS_GAS)
        ).coerceIn(0f, 1f)

        // Determine dominant changed axis for clear live identification
        val candidates = listOf(
            "AXIS_X (Left Stick X)" to lx,
            "AXIS_Y (Left Stick Y)" to ly,
            "AXIS_Z (Right Stick X)" to rx,
            "AXIS_RZ (Right Stick Y)" to ry,
            "AXIS_LTRIGGER (L2)" to lTrigger,
            "AXIS_RTRIGGER (R2)" to rTrigger,
            "AXIS_HAT_X (D-Pad X)" to hatX,
            "AXIS_HAT_Y (D-Pad Y)" to hatY
        )
        val dominant = candidates.maxByOrNull { abs(it.second) }
        val activeAxis = dominant?.takeIf { abs(it.second) > 0.08f }

        _liveState.update { state ->
            state.copy(
                activeDeviceId = event.deviceId,
                activeDeviceName = deviceName,
                leftStickX = lx,
                leftStickY = ly,
                rightStickX = rx,
                rightStickY = ry,
                leftTrigger = lTrigger,
                rightTrigger = rTrigger,
                dpadX = hatX,
                dpadY = hatY,
                lastAxisLabel = activeAxis?.first ?: state.lastAxisLabel,
                lastAxisValue = activeAxis?.second ?: state.lastAxisValue
            )
        }

        if (activeAxis != null) {
            appendLog(
                InputLogEntry(
                    id = logSequence.getAndIncrement(),
                    timestampMs = System.currentTimeMillis(),
                    deviceId = event.deviceId,
                    deviceName = deviceName,
                    eventType = InputEventType.AXIS_MOTION,
                    codeLabel = activeAxis.first,
                    rawCode = 0,
                    scanCode = 0,
                    valueSummary = "%.3f".format(activeAxis.second)
                )
            )
        }

        return captureForLab
    }

    private fun readCenteredAxis(
        event: MotionEvent,
        device: InputDevice?,
        axis: Int
    ): Float {
        val value = event.getAxisValue(axis)
        val range = device?.getMotionRange(axis, event.source)
        val flat = range?.flat ?: 0.04f
        return if (abs(value) <= flat) 0f else value.coerceIn(-1f, 1f)
    }

    private fun appendLog(entry: InputLogEntry) {
        _inputLog.update { existing ->
            (listOf(entry) + existing).take(MAX_LOG_ENTRIES)
        }
    }

    fun clearLog() {
        _inputLog.value = emptyList()
        _liveState.update {
            it.copy(
                lastKeyLabel = null,
                lastKeyAction = null,
                lastScanCode = null,
                lastAxisLabel = null,
                lastAxisValue = null
            )
        }
    }

    fun triggerRumbleTest(deviceId: Int): Boolean {
        val device = inputManager.getInputDevice(deviceId) ?: return false
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = device.vibratorManager
                if (manager.vibratorIds.isEmpty()) return false
                val effect = VibrationEffect.createOneShot(180L, 180)
                manager.vibrate(CombinedVibration.createParallel(effect))
                true
            } else {
                @Suppress("DEPRECATION")
                val vibrator = device.vibrator
                if (!vibrator.hasVibrator()) return false
                vibrator.vibrate(VibrationEffect.createOneShot(180L, 180))
                true
            }
        } catch (_: Throwable) {
            false
        }
    }

    fun hasBluetoothInspectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun inspectPairedBluetoothControllers() {
        if (!hasBluetoothInspectPermission()) {
            _bluetoothScanState.value = BluetoothScanState.PermissionRequired
            return
        }
        val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter
        if (adapter == null || !adapter.isEnabled) {
            _bluetoothScanState.value = BluetoothScanState.BluetoothUnavailableOrOff
            return
        }
        try {
            val bonded = adapter.bondedDevices.orEmpty().map { btDevice ->
                val name = btDevice.name?.takeIf { it.isNotBlank() } ?: "Bluetooth Device"
                val majorClass = btDevice.bluetoothClass?.majorDeviceClass
                val deviceClass = btDevice.bluetoothClass?.deviceClass
                val looksLikeGamepad = majorClass == BluetoothClass.Device.Major.PERIPHERAL ||
                    deviceClass == 0x0508 || // Peripheral Gamepad
                    deviceClass == 0x0504 || // Peripheral Joystick
                    name.contains("controller", ignoreCase = true) ||
                    name.contains("dualsense", ignoreCase = true) ||
                    name.contains("dualshock", ignoreCase = true) ||
                    name.contains("xbox", ignoreCase = true) ||
                    name.contains("gamepad", ignoreCase = true)
                val suffix = btDevice.address?.takeLast(5) ?: "XX:XX"
                PairedBluetoothDeviceInfo(
                    name = name,
                    addressSuffix = "••:••:••:••:$suffix",
                    looksLikeController = looksLikeGamepad
                )
            }.sortedByDescending { it.looksLikeController }
            _bluetoothScanState.value = BluetoothScanState.Loaded(bonded)
        } catch (se: SecurityException) {
            _bluetoothScanState.value = BluetoothScanState.PermissionDenied
        } catch (t: Throwable) {
            _bluetoothScanState.value = BluetoothScanState.Error(t.message ?: "Bluetooth query failed")
        }
    }

    fun markBluetoothPermissionDenied() {
        _bluetoothScanState.value = BluetoothScanState.PermissionDenied
    }

    companion object {
        private const val MAX_LOG_ENTRIES = 80
    }
}
