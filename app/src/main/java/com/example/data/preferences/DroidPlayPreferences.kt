package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.droidPlayDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "droidplay_preferences"
)

data class UserPreferencesState(
    val consoleModeEnabled: Boolean = true,
    val showAllLaunchableApps: Boolean = false,
    val labLockBackKey: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true
)

class DroidPlayPreferences(context: Context) {

    private val dataStore = context.applicationContext.droidPlayDataStore

    val preferencesFlow: Flow<UserPreferencesState> = dataStore.data.map { prefs ->
        UserPreferencesState(
            consoleModeEnabled = prefs[KEY_CONSOLE_MODE] ?: true,
            showAllLaunchableApps = prefs[KEY_SHOW_ALL_APPS] ?: false,
            labLockBackKey = prefs[KEY_LAB_LOCK_BACK] ?: true,
            hapticFeedbackEnabled = prefs[KEY_HAPTIC_FEEDBACK] ?: true
        )
    }

    suspend fun setConsoleModeEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_CONSOLE_MODE] = enabled }
    }

    suspend fun setShowAllLaunchableApps(showAll: Boolean) {
        dataStore.edit { it[KEY_SHOW_ALL_APPS] = showAll }
    }

    suspend fun setLabLockBackKey(lock: Boolean) {
        dataStore.edit { it[KEY_LAB_LOCK_BACK] = lock }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_HAPTIC_FEEDBACK] = enabled }
    }

    companion object {
        private val KEY_CONSOLE_MODE = booleanPreferencesKey("console_mode_enabled")
        private val KEY_SHOW_ALL_APPS = booleanPreferencesKey("show_all_launchable_apps")
        private val KEY_LAB_LOCK_BACK = booleanPreferencesKey("lab_lock_back_key")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback_enabled")
    }
}
