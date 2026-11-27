package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_library_entries")
data class GameLibraryEntryEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val isPinnedGame: Boolean = false,
    val lastLaunchedAt: Long = 0L,
    val launchCount: Int = 0,
    val activeProfileId: Long? = null
)
