package com.example.ui

import android.app.Application
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.controller.BluetoothScanState
import com.example.controller.ControllerDeviceInfo
import com.example.controller.ControllerRepository
import com.example.controller.InputLogEntry
import com.example.controller.LiveGamepadState
import com.example.data.games.InstalledGameInfo
import com.example.data.games.InstalledGamesRepository
import com.example.data.local.ControlProfileEntity
import com.example.data.local.DroidPlayDatabase
import com.example.data.local.DroidPlayLocalRepository
import com.example.data.local.GameLibraryEntryEntity
import com.example.data.preferences.DroidPlayPreferences
import com.example.data.preferences.UserPreferencesState
import com.example.service.DroidPlayAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GamesUiState(
    val isLoading: Boolean = true,
    val allApps: List<InstalledGameInfo> = emptyList(),
    val recentGames: List<InstalledGameInfo> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

data class DeviceCompatibilitySummary(
    val manufacturer: String,
    val model: String,
    val androidVersionLabel: String,
    val apiLevel: Int,
    val supportsBatteryApi: Boolean,
    val supportsBluetoothConnectPermission: Boolean,
    val supportsVibratorManager: Boolean
)

class DroidPlayViewModel(application: Application) : AndroidViewModel(application) {

    val controllerRepository = ControllerRepository(application)
    private val preferences = DroidPlayPreferences(application)
    private val localRepo = DroidPlayLocalRepository(
        DroidPlayDatabase.getInstance(application).droidPlayDao()
    )
    private val gamesRepo = InstalledGamesRepository(application)

    val controllers: StateFlow<List<ControllerDeviceInfo>> = controllerRepository.controllers
    val allExternalDevices: StateFlow<List<ControllerDeviceInfo>> = controllerRepository.allExternalInputDevices
    val liveGamepadState: StateFlow<LiveGamepadState> = controllerRepository.liveState
    val inputLog: StateFlow<List<InputLogEntry>> = controllerRepository.inputLog
    val bluetoothScanState: StateFlow<BluetoothScanState> = controllerRepository.bluetoothScanState

    val userPreferences: StateFlow<UserPreferencesState> = preferences.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferencesState()
        )

    val profiles: StateFlow<List<ControlProfileEntity>> = localRepo.allProfiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _rawInstalledApps = MutableStateFlow<List<InstalledGameInfo>>(emptyList())
    private val _gamesLoading = MutableStateFlow(true)
    private val _gamesError = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    val gamesUiState: StateFlow<GamesUiState> = combine(
        _gamesLoading,
        _rawInstalledApps,
        localRepo.allGameEntries,
        _searchQuery,
        _gamesError
    ) { loading, apps, roomEntries, query, error ->
        val entryMap = roomEntries.associateBy { it.packageName }
        val merged = apps.map { app ->
            val saved = entryMap[app.packageName]
            if (saved != null) {
                app.copy(
                    isPinned = saved.isPinnedGame,
                    lastLaunchedAt = saved.lastLaunchedAt,
                    launchCount = saved.launchCount,
                    assignedProfileId = saved.activeProfileId
                )
            } else {
                app
            }
        }
        val recents = merged
            .filter { it.lastLaunchedAt > 0L }
            .sortedByDescending { it.lastLaunchedAt }
            .take(8)

        GamesUiState(
            isLoading = loading,
            allApps = merged,
            recentGames = recents,
            searchQuery = query,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GamesUiState()
    )

    private val _accessibilityEnabled = MutableStateFlow(false)
    val accessibilityEnabled: StateFlow<Boolean> = _accessibilityEnabled.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    val compatibilitySummary = DeviceCompatibilitySummary(
        manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
        model = Build.MODEL,
        androidVersionLabel = "Android ${Build.VERSION.RELEASE}",
        apiLevel = Build.VERSION.SDK_INT,
        supportsBatteryApi = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
        supportsBluetoothConnectPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
        supportsVibratorManager = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    )

    init {
        viewModelScope.launch {
            localRepo.ensureDefaultProfileExists()
        }
        refreshInstalledGames()
        refreshAccessibilityStatus()
    }

    fun refreshControllers() {
        controllerRepository.refreshDevices()
    }

    fun refreshInstalledGames() {
        viewModelScope.launch {
            _gamesLoading.value = true
            _gamesError.value = null
            runCatching {
                gamesRepo.loadLaunchableApps()
            }.onSuccess { list ->
                _rawInstalledApps.value = list
                _gamesLoading.value = false
            }.onFailure { err ->
                _gamesError.value = err.message ?: "Unable to query installed apps."
                _gamesLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun launchGame(game: InstalledGameInfo) {
        val launched = gamesRepo.launchPackage(game.packageName)
        if (launched) {
            viewModelScope.launch {
                localRepo.recordGameLaunch(game.packageName, game.title)
            }
        } else {
            _statusBanner.value = "Could not launch ${game.title}."
        }
    }

    fun togglePinnedGame(game: InstalledGameInfo) {
        viewModelScope.launch {
            localRepo.togglePinnedGame(game.packageName, game.title)
        }
    }

    fun assignProfileToGame(game: InstalledGameInfo, profileId: Long?) {
        viewModelScope.launch {
            localRepo.assignProfileToGame(game.packageName, game.title, profileId)
            _statusBanner.value = "Profile updated for ${game.title}."
        }
    }

    fun saveProfile(profile: ControlProfileEntity, activateImmediately: Boolean = true) {
        viewModelScope.launch {
            val id = localRepo.saveProfile(profile)
            if (activateImmediately) {
                localRepo.activateProfile(if (profile.id != 0L) profile.id else id)
            }
            _statusBanner.value = "Saved profile \"${profile.name}\"."
        }
    }

    fun activateProfile(profileId: Long) {
        viewModelScope.launch {
            localRepo.activateProfile(profileId)
        }
    }

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            localRepo.deleteProfile(profileId)
            _statusBanner.value = "Profile deleted."
        }
    }

    fun setConsoleModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setConsoleModeEnabled(enabled)
        }
    }

    fun setShowAllLaunchableApps(showAll: Boolean) {
        viewModelScope.launch {
            preferences.setShowAllLaunchableApps(showAll)
        }
    }

    fun setLabLockBackKey(lock: Boolean) {
        viewModelScope.launch {
            preferences.setLabLockBackKey(lock)
        }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setHapticFeedbackEnabled(enabled)
        }
    }

    fun clearInputLog() {
        controllerRepository.clearLog()
    }

    fun testControllerRumble(deviceId: Int) {
        val ok = controllerRepository.triggerRumbleTest(deviceId)
        _statusBanner.value = if (ok) {
            "Rumble pulse sent to controller #$deviceId."
        } else {
            "Controller #$deviceId does not expose a controllable vibrator."
        }
    }

    fun inspectPairedBluetooth() {
        controllerRepository.inspectPairedBluetoothControllers()
    }

    fun markBluetoothPermissionDenied() {
        controllerRepository.markBluetoothPermissionDenied()
    }

    fun refreshAccessibilityStatus() {
        _accessibilityEnabled.value = DroidPlayAccessibilityService.isEnabledInSystemSettings(getApplication())
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun createExitToAndroidHomeIntent(): Intent {
        return Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun createBluetoothSettingsIntent(): Intent {
        return Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun createAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
