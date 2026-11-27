package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "control_profiles")
data class ControlProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetPackageName: String? = null,
    val targetGameTitle: String? = null,
    val controllerTypeHint: String = "Universal Gamepad",
    val leftStickDeadzone: Float = 0.08f,
    val rightStickDeadzone: Float = 0.08f,
    val leftStickSensitivity: Float = 1.0f,
    val rightStickSensitivity: Float = 1.0f,
    val triggerThreshold: Float = 0.15f,
    val invertRightY: Boolean = false,
    val swapFaceButtonsAB: Boolean = false,
    val swapFaceButtonsXY: Boolean = false,
    val swapBumpersTriggers: Boolean = false,
    val touchMappingNotes: String = "",
    val isActive: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
