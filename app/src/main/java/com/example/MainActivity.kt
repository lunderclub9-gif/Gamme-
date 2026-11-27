package com.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.DroidPlayViewModel
import com.example.ui.navigation.DroidPlayApp
import com.example.ui.theme.DroidPlayTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DroidPlayViewModel by viewModels()
    private var isLabScreenActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            DroidPlayTheme {
                DroidPlayApp(
                    viewModel = viewModel,
                    onLabRouteActiveChanged = { active ->
                        isLabScreenActive = active
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.controllerRepository.startListening()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshControllers()
        viewModel.refreshAccessibilityStatus()
    }

    override fun onStop() {
        super.onStop()
        viewModel.controllerRepository.stopListening()
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val lockBackInLab = viewModel.userPreferences.value.labLockBackKey
        val consumedByLab = viewModel.controllerRepository.processKeyEvent(
            event = event,
            captureForLab = isLabScreenActive,
            lockBackKeyInLab = lockBackInLab
        )
        if (consumedByLab && event.keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
            event.keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
            event.keyCode != KeyEvent.KEYCODE_POWER
        ) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(ev: MotionEvent): Boolean {
        val consumedByLab = viewModel.controllerRepository.processMotionEvent(
            event = ev,
            captureForLab = isLabScreenActive
        )
        if (consumedByLab) {
            return true
        }
        return super.dispatchGenericMotionEvent(ev)
    }
}
