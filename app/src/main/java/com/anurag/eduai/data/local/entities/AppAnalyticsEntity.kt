package com.anurag.eduai.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * App Analytics Entity - Tracks screen navigation and app events
 */
@Entity(
    tableName = "app_analytics",
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
        Index(value = ["screenName"]),
        Index(value = ["eventType"])
    ]
)
data class AppAnalyticsEntity(
    @PrimaryKey(autoGenerate = true)
    val analyticsId: Long = 0,
    val studentId: String,
    val sessionId: String,
    val screenName: String, // "LOGIN", "HOME", "SUBJECT", "CONCEPT", "SIMULATION"
    val eventType: String, // "ENTRY", "EXIT", "APP_OPEN", "APP_CRASH", "SESSION_START", "SESSION_END"
    val timestamp: Long = System.currentTimeMillis(),
    val additionalData: String? = null, // JSON string for extra data
    val isSynced: Boolean = false
)
