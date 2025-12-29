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
    indices = [
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
    val additionalData: String? = null, // JSON string for extra data
    val isSynced: Boolean = false
)
