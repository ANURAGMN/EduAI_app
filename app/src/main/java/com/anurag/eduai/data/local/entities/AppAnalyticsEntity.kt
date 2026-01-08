package com.anurag.eduai.data.local.entities

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
        Index(value = ["eventType"])
    ]
)
data class AppAnalyticsEntity(
    @PrimaryKey(autoGenerate = true)
    val analyticsId: Long = 0,
    val sessionId: String,
    val screenName: String, // "LOGIN", "HOME", "SUBJECT", "CONCEPT", "SIMULATION","PROGRESS", "SETTINGS","PROFILE"
    val eventType: String, // "ENTRY", "EXIT",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
