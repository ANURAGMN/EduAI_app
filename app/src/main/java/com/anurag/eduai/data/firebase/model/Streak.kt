package com.anurag.eduai.data.firebase.model

/**
 * Firestore model for user streak
 * Stored in a separate "streak" collection with userId as document ID
 */
data class Streak(
    val userId: String = "",           // User ID (document ID in Firestore)
    val streakCount: Int = 0,          // Current streak count
    val lastStreakDate: Long = 0L,     // Last day streak was updated (start of day timestamp)
    val createdAt: Long = System.currentTimeMillis(),  // When streak was first created
    val updatedAt: Long = System.currentTimeMillis(),   // Last time streak was updated
    val appName: String = ""           // App name to distinguish between multiple apps on same Firebase project
)
