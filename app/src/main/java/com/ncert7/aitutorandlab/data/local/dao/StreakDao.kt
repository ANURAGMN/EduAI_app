package com.ncert7.aitutorandlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ncert7.aitutorandlab.data.local.entities.StreakEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Streak entity
 * Handles all local database operations for streak data
 */
@Dao
interface StreakDao {

    /**
     * Get streak for a specific user
     */
    @Query("SELECT * FROM streak WHERE userId = :userId")
    suspend fun getStreakByUserId(userId: String): StreakEntity?

    /**
     * Get streak as a reactive Flow for real-time updates
     * This will automatically emit whenever the streak data changes
     */
    @Query("SELECT * FROM streak WHERE userId = :userId")
    fun getStreakByUserIdFlow(userId: String): Flow<StreakEntity?>

    /**
     * Insert or replace a streak record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: StreakEntity)

    /**
     * Update an existing streak record
     */
    @Update
    suspend fun updateStreak(streak: StreakEntity)

    /**
     * Delete a streak record
     */
    @Query("DELETE FROM streak WHERE userId = :userId")
    suspend fun deleteStreak(userId: String)

    /**
     * Get all unsynced streak records
     * Used for syncing with Firestore
     */
    @Query("SELECT * FROM streak WHERE isSynced = 0")
    suspend fun getUnsyncedStreak(): StreakEntity?

    /**
     * Mark streak as synced
     */
    @Query("UPDATE streak SET isSynced = 1 WHERE userId = :userId")
    suspend fun markStreakAsSynced(userId: String)

    /**
     * Clear all streak data (for logout)
     */
    @Query("DELETE FROM streak")
    suspend fun clearAll()
}
