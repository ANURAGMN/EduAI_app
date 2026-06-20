package com.ncert7.aitutorandlab.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * App Analytics Entity - Tracks screen events
 */
@Entity(
    tableName = "app_analytics",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE, // Delete analytics when session is deleted
        )
    ],
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["screenName"]),
        Index(value = ["eventType"]),
        Index(value = ["sessionId", "screenName", "exitTime"])
    ]
)
data class AppAnalyticsEntity(
    @PrimaryKey(autoGenerate = true)
    val analyticsId: Long = 0,
    val studentId: String = "",
    val sessionId: String,
    val screenName: String, // "LOGIN", "HOME", "SUBJECT", "CONCEPT", "SIMULATION","PROGRESS", "SETTINGS","PROFILE"
    val eventType: String?, // "ENTRY" when entered, updates to "EXIT" when exited
    val entryTime: Long, // Timestamp when screen was entered
    val exitTime: Long? = null, // Timestamp when screen was exited (null if still active)
    val durationMillis: Long = 0, // Time spent on screen (exitTime - entryTime)
    val appName: String = "",
    val isSynced: Boolean = false,
    val conceptId: String? = null,
    val source: String? = null,
    val interactionType: String? = null
)
