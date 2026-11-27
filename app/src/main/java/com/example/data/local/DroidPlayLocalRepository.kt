package com.example.data.local

import kotlinx.coroutines.flow.Flow

class DroidPlayLocalRepository(private val dao: DroidPlayDao) {

    val allProfiles: Flow<List<ControlProfileEntity>> = dao.observeAllProfiles()
    val allGameEntries: Flow<List<GameLibraryEntryEntity>> = dao.observeAllGameEntries()
    val recentGameEntries: Flow<List<GameLibraryEntryEntity>> = dao.observeRecentGameEntries()

    suspend fun ensureDefaultProfileExists() {
        if (dao.getProfileCount() == 0) {
            dao.upsertProfile(
                ControlProfileEntity(
                    name = "Console Standard (Global)",
                    targetPackageName = null,
                    targetGameTitle = "All Native Games",
                    controllerTypeHint = "DualSense / DualShock / Xbox / HID",
                    leftStickDeadzone = 0.08f,
                    rightStickDeadzone = 0.08f,
                    leftStickSensitivity = 1.0f,
                    rightStickSensitivity = 1.0f,
                    triggerThreshold = 0.15f,
                    isActive = true
                )
            )
        }
    }

    suspend fun saveProfile(profile: ControlProfileEntity): Long {
        val updated = profile.copy(updatedAt = System.currentTimeMillis())
        return dao.upsertProfile(updated)
    }

    suspend fun activateProfile(profileId: Long) {
        dao.clearActiveFlags()
        dao.markProfileActive(profileId)
    }

    suspend fun deleteProfile(profileId: Long) {
        dao.deleteProfileById(profileId)
        ensureDefaultProfileExists()
    }

    suspend fun recordGameLaunch(packageName: String, displayName: String) {
        val existing = dao.getGameEntry(packageName)
        val next = existing?.copy(
            displayName = displayName,
            lastLaunchedAt = System.currentTimeMillis(),
            launchCount = existing.launchCount + 1
        ) ?: GameLibraryEntryEntity(
            packageName = packageName,
            displayName = displayName,
            isPinnedGame = true,
            lastLaunchedAt = System.currentTimeMillis(),
            launchCount = 1
        )
        dao.upsertGameEntry(next)
    }

    suspend fun togglePinnedGame(packageName: String, displayName: String) {
        val existing = dao.getGameEntry(packageName)
        val next = existing?.copy(
            displayName = displayName,
            isPinnedGame = !existing.isPinnedGame
        ) ?: GameLibraryEntryEntity(
            packageName = packageName,
            displayName = displayName,
            isPinnedGame = true
        )
        dao.upsertGameEntry(next)
    }

    suspend fun assignProfileToGame(packageName: String, displayName: String, profileId: Long?) {
        val existing = dao.getGameEntry(packageName)
        val next = existing?.copy(
            displayName = displayName,
            activeProfileId = profileId
        ) ?: GameLibraryEntryEntity(
            packageName = packageName,
            displayName = displayName,
            isPinnedGame = true,
            activeProfileId = profileId
        )
        dao.upsertGameEntry(next)
    }
}
