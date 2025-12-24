package com.anurag.eduai.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Concept Activity Entity - Tracks Concept events
 * concept completed/started by students
 */
@Entity(
    tableName = "concept_activities",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["conceptId"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["conceptId"]),
        Index(value = ["eventType"]),
        Index(value = ["timestamp"])
    ]
)
data class ConceptActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val activityId: Long = 0,
    val studentId: String,
    val conceptId: String,
    val sessionId: String,
    val eventType: String, // "Concept_STARTED", "Concept_COMPLETED"
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

