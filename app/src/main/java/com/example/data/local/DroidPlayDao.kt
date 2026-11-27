package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DroidPlayDao {

    @Query("SELECT * FROM control_profiles ORDER BY isActive DESC, updatedAt DESC")
    fun observeAllProfiles(): Flow<List<ControlProfileEntity>>

    @Query("SELECT * FROM control_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): ControlProfileEntity?

    @Query("SELECT COUNT(*) FROM control_profiles")
    suspend fun getProfileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ControlProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ControlProfileEntity)

    @Query("UPDATE control_profiles SET isActive = 0")
    suspend fun clearActiveFlags()

    @Query("UPDATE control_profiles SET isActive = 1 WHERE id = :profileId")
    suspend fun markProfileActive(profileId: Long)

    @Query("DELETE FROM control_profiles WHERE id = :profileId")
    suspend fun deleteProfileById(profileId: Long)

    @Query("SELECT * FROM game_library_entries ORDER BY lastLaunchedAt DESC")
    fun observeAllGameEntries(): Flow<List<GameLibraryEntryEntity>>

    @Query("SELECT * FROM game_library_entries WHERE lastLaunchedAt > 0 ORDER BY lastLaunchedAt DESC LIMIT 10")
    fun observeRecentGameEntries(): Flow<List<GameLibraryEntryEntity>>

    @Query("SELECT * FROM game_library_entries WHERE packageName = :packageName LIMIT 1")
    suspend fun getGameEntry(packageName: String): GameLibraryEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGameEntry(entry: GameLibraryEntryEntity)
}
