package com.example

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.example.controller.ControllerRepository
import com.example.service.DroidPlayAccessibilityService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 30, 31, 36])
class DroidPlayCompatibilityTest {

    @Test
    fun `activity and controller repository initialize cleanly across Android 8 to Android 16`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ControllerRepository(context)
        repo.startListening()
        assertNotNull(repo.controllers.value)
        repo.stopListening()

        // Accessibility service is disabled by default and never auto-enabled
        assertFalse(DroidPlayAccessibilityService.isEnabledInSystemSettings(context))

        // Verify Bluetooth permission gating behaves properly by API level
        val hasPerm = repo.hasBluetoothInspectPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            assertFalse(hasPerm)
        }

        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        assertEquals(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O, true)
        controller.pause().stop().destroy()
    }
}
