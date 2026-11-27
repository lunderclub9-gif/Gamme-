# DroidPlay

A native Android gaming companion and console-style launcher built with Kotlin and Jetpack Compose.

## Compatibility target
- Minimum Android: 8.0 (API 26)
- Target Android SDK: 36
- Designed for native Android gamepad input and an optional console-style in-app experience.

## Build
The project uses Gradle 9.3.1. Install JDK 17, Android SDK platform 36.1 and Build Tools 36.0.0, then run:

```bash
gradle --no-daemon testDebugUnitTest
gradle --no-daemon assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Current limitations
See [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md) for the distinction between implemented code, unfinished features, and hardware validation still required.

DroidPlay does not promise that every controller model or every third-party game supports touch mapping. Android OS restrictions and game anti-cheat protections apply.
