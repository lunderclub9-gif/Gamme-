package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Optional AccessibilityService bridge for future/voluntary gesture-based touch mapping
 * in Android games without native controller support.
 *
 * Design & Policy Guarantees:
 * - Never requested automatically. Users must explicitly enable it in System Accessibility Settings.
 * - Does not monitor typed text, retrieve window contents, or inject fake background events.
 * - Native controller-supported games do NOT require this service.
 */
class DroidPlayAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceConnected.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally minimal: only tracks active window state if user enables touch mapping.
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        _isServiceConnected.value = false
        return super.onUnbind(intent)
    }

    companion object {
        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        fun isEnabledInSystemSettings(context: Context): Boolean {
            if (_isServiceConnected.value) return true
            val expectedComponent = ComponentName(context, DroidPlayAccessibilityService::class.java)
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            return enabledServices
                .split(':')
                .mapNotNull { ComponentName.unflattenFromString(it) }
                .any { it == expectedComponent }
        }
    }
}
