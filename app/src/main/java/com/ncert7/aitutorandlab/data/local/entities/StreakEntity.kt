package com.ncert7.aitutorandlab.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching user streak data locally
 * Synced from Firestore for offline access and performance
 */
@Entity(tableName = "streak")
data class StreakEntity(
    @PrimaryKey
    val userId: String = "",           // User ID (primary key)
    val streakCount: Int = 0,          // Current streak count
    val lastStreakDate: Long = 0L,     // Last day streak was updated (start of day timestamp)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val appName: String = "",          // App name to distinguish between multiple apps on same Firebase project
    val isSynced: Boolean = false      // Whether this is synced with Firestore
)
