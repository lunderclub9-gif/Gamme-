package com.example

import android.content.Context
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.controller.ControllerClassifier
import com.example.controller.ControllerRepository
import com.example.controller.ControllerType
import com.example.controller.InputEventType
import com.example.data.local.ControlProfileEntity
import com.example.data.local.DroidPlayDatabase
import com.example.data.local.DroidPlayLocalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DroidPlayRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `controller classifier identifies PlayStation Xbox and generic controllers`() {
        assertEquals(
            ControllerType.DUALSENSE,
            ControllerClassifier.classifyController(
                vendorId = 0x054C,
                productId = 0x0CE6,
                name = "DualSense Wireless Controller",
                sources = InputDevice.SOURCE_GAMEPAD or InputDevice.SOURCE_JOYSTICK
            )
        )
        assertEquals(
            ControllerType.DUALSHOCK_4,
            ControllerClassifier.classifyController(
                vendorId = 0x054C,
                productId = 0x09CC,
                name = "Wireless Controller",
                sources = InputDevice.SOURCE_GAMEPAD or InputDevice.SOURCE_JOYSTICK
            )
        )
        assertEquals(
            ControllerType.DUALSHOCK_3,
            ControllerClassifier.classifyController(
                vendorId = 0x054C,
                productId = 0x0268,
                name = "PLAYSTATION(R)3 Controller",
                sources = InputDevice.SOURCE_GAMEPAD or InputDevice.SOURCE_JOYSTICK
            )
        )
        assertEquals(
            ControllerType.XBOX,
            ControllerClassifier.classifyController(
                vendorId = 0x045E,
                productId = 0x0B12,
                name = "Xbox Wireless Controller",
                sources = InputDevice.SOURCE_GAMEPAD or InputDevice.SOURCE_JOYSTICK
            )
        )
        assertEquals(
            ControllerType.USB_CONTROLLER,
            ControllerClassifier.classifyController(
                vendorId = 0x1234,
                productId = 0x5678,
                name = "USB Gamepad",
                sources = InputDevice.SOURCE_GAMEPAD,
                descriptor = "usb-0001"
            )
        )
    }

    @Test
    fun `controller repository processes real KeyEvent and MotionEvent packets`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ControllerRepository(context)

        val downEvent = KeyEvent(0L, 0L, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BUTTON_A, 0, 0, 7, 304, 0, InputDevice.SOURCE_GAMEPAD)
        val consumedDown = repo.processKeyEvent(downEvent, captureForLab = true, lockBackKeyInLab = true)
        assertTrue(consumedDown)
        assertTrue(KeyEvent.KEYCODE_BUTTON_A in repo.liveState.value.pressedKeys)
        assertEquals(1, repo.inputLog.value.size)
        assertEquals(InputEventType.KEY_DOWN, repo.inputLog.value.first().eventType)

        val upEvent = KeyEvent(0L, 10L, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BUTTON_A, 0, 0, 7, 304, 0, InputDevice.SOURCE_GAMEPAD)
        repo.processKeyEvent(upEvent, captureForLab = true, lockBackKeyInLab = true)
        assertFalse(KeyEvent.KEYCODE_BUTTON_A in repo.liveState.value.pressedKeys)
        assertEquals(2, repo.inputLog.value.size)

        // Non-gamepad keyboard key should not be captured as controller event
        val textKey = KeyEvent(0L, 20L, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE, 0, 0, 1, 57, 0, InputDevice.SOURCE_KEYBOARD)
        assertFalse(repo.processKeyEvent(textKey, captureForLab = true, lockBackKeyInLab = true))

        // Analog MotionEvent test
        val props = arrayOf(MotionEvent.PointerProperties().apply { id = 0 })
        val coords = arrayOf(
            MotionEvent.PointerCoords().apply {
                setAxisValue(MotionEvent.AXIS_X, 0.85f)
                setAxisValue(MotionEvent.AXIS_RTRIGGER, 0.72f)
            }
        )
        val motion = MotionEvent.obtain(
            0L, 30L, MotionEvent.ACTION_MOVE, 1, props, coords,
            0, 0, 1f, 1f, 7, 0, InputDevice.SOURCE_JOYSTICK, 0
        )
        assertTrue(repo.processMotionEvent(motion, captureForLab = true))
        assertEquals(0.85f, repo.liveState.value.leftStickX, 0.01f)
        assertEquals(0.72f, repo.liveState.value.rightTrigger, 0.01f)
        motion.recycle()
    }

    @Test
    fun `room local repository persists profiles and game launches`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, DroidPlayDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = DroidPlayLocalRepository(db.droidPlayDao())

        repo.ensureDefaultProfileExists()
        val initialProfiles = repo.allProfiles.first()
        assertEquals(1, initialProfiles.size)
        assertTrue(initialProfiles.first().isActive)

        val newId = repo.saveProfile(
            ControlProfileEntity(
                name = "Racing DualSense",
                targetPackageName = "com.example.racer",
                targetGameTitle = "Racer",
                leftStickDeadzone = 0.05f,
                triggerThreshold = 0.1f,
                isActive = false
            )
        )
        repo.activateProfile(newId)
        val updated = repo.allProfiles.first()
        assertEquals(2, updated.size)
        assertEquals("Racing DualSense", updated.first { it.isActive }.name)

        repo.recordGameLaunch("com.example.racer", "Racer")
        val recents = repo.recentGameEntries.first()
        assertEquals(1, recents.size)
        assertEquals(1, recents.first().launchCount)

        db.close()
    }

    @Test
    fun `navigate across all seven DroidPlay screens cleanly`() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_dashboard_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_lab").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("controller_lab_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_games").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("my_games_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_studio").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("control_studio_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_home").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tile_controller_hub").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("controller_hub_screen").assertIsDisplayed()
    }
}
