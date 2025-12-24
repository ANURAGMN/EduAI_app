package com.anurag.eduai.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Generic Progress Entity - combines concept and simulation progress
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
        Index(value = ["studentId", "itemType", "itemId"], unique = true)
    ]
)

data class ProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val progressId: Long = 0,
    val studentId: String,
    val itemType: String, // "CONCEPT" or "SIMULATION"
    val itemId: String,
    val status: String, // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    val startedAt: Long? = null,
    val openedAt: Long? = null,
    val completedAt: Long? = null,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

