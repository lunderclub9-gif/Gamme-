# DroidPlay — implementation status and compatibility

This document separates implemented code from features that still require engineering or device validation.

## Build configuration
- Android application module, Kotlin and Jetpack Compose.
- Minimum SDK: 26 (Android 8.0).
- Target SDK: 36.
- Compile SDK declaration: Android API 36, minor API level 1.
- Gradle distribution: 9.3.1.
- Gradle memory defaults are tuned for low-memory developer machines; CI uses a hosted runner.

## Implemented in the current codebase
- Android InputManager device discovery and hot-plug listener.
- Gamepad/joystick classification using Android input sources and available vendor/product identifiers.
- KeyEvent and MotionEvent inspection while DroidPlay is in the foreground.
- Controller Lab live state and bounded input log.
- Installed launchable-app discovery and launch intents.
- Room-backed local game/profile data and DataStore preferences.
- A user-initiated AccessibilityService declaration and settings entry point.
- Console-style in-app dashboard and navigation.

## Not yet implemented or not verified
- Global controller key filtering while another game is foregrounded.
- A working touch-mapping pipeline from controller events to AccessibilityService gestures.
- A visual coordinate editor for per-game touch zones.
- Applying saved button swaps/sensitivity settings to events delivered to third-party games. Android apps cannot generally rewrite input events delivered to other apps.
- Full HOME-role/default-launcher integration; the current console mode is an in-app experience with an intent to return to Android Home.
- Reliable PS3 Bluetooth pairing across devices. Android input discovery only reports a controller after the OS has successfully connected it.
- Physical controller validation on HONOR X9d / Android 16.
- Verified Android 8–16 device matrix and successful APK build until CI reports a passing run.

## Controller compatibility notes
DroidPlay uses Android's standard input APIs. Actual compatibility depends on the controller exposing supported Android gamepad/joystick input sources and on the phone's Bluetooth/USB host implementation. Vendor/product IDs are hints, not proof of complete protocol support. Native-gamepad games receive input through Android directly; DroidPlay does not need to run as a background input injector for those games.

## Build verification
GitHub Actions runs unit tests and assembles a debug APK using Gradle 9.3.1 and Android SDK 36.1. A green workflow run is required before treating the repository build as verified. Emulator tests do not replace physical Bluetooth/USB controller tests.
