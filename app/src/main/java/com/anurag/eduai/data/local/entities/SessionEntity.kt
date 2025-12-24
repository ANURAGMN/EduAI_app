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
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["sessionDate"])
    ]
)
data class SessionEntity(
    @PrimaryKey
    val sessionId: String,
    val studentId: String,
    val sessionDate: String, // Format: "yyyy-MM-dd"
    val sessionStartTime: Long,
    val sessionEndTime: Long? = null,
    val durationMillis: Long = 0,
    val conceptsCompletedCount: Int = 0,
    val simulationsCompletedCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
