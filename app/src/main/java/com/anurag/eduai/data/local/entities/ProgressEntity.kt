package com.anurag.eduai.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Generic Progress Entity - combines concept and simulation progress
 *
 * Tracks fine-grained progress for individual learning items with multi-app support
 * Updated on basis of language (en/kn) for each student
 *
 * itemType: CONCEPT, SIMULATION, SIMULATION_AGENT, MATH_AGENT, SCIENCE_AGENT, REVISION_AGENT
 * itemId: conceptId, simulationId, or problemId depending on itemType
 */
@Entity(
    tableName = "progress",
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
        Index(value = ["itemType", "itemId"]),
        Index(value = ["studentId", "itemType", "itemId", "appName"], unique = true),
        Index(value = ["studentId", "language", "appName"]),
        Index(value = ["appName"])
    ]
)

data class ProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val progressId: Long = 0,
    val studentId: String,
    val itemType: String, // "CONCEPT", "SIMULATION", "SIMULATION_AGENT", "MATH_AGENT", "SCIENCE_AGENT", "REVISION_AGENT"
    val itemId: String,
    val status: String, // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    val progressPercentage: Int, //percentage
    val language: String = "en", // Language code: "en" or "kn" (Kannada)
    val appName: String = "", // App name to distinguish between multiple apps on same Firebase project
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

