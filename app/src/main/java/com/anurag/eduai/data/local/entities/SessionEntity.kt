package com.anurag.eduai.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Session Entity - Tracks user sessions
 */
@Entity(
    tableName = "sessions",
    indices = [
        Index(value = ["sessionDate"])
    ]
)
data class SessionEntity(
    @PrimaryKey
    val sessionId: String,
    val sessionDate: String, // Format: "yyyy-MM-dd"
    val sessionStartTime: Long,
    val sessionEndTime: Long? = null,
    val durationMillis: Long = 0,
    val syncAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
