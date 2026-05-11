package com.anurag.eduai.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks chapter progress for each agent (Study, Simulation, Revision)
 * Progress is calculated per language, per student, per app
 *
 * CALCULATION LOGIC (Updated):
 *
 * STUDY (33.33% or 50%): % of STUDY-type concepts completed
 *    - Only counts study concepts loaded on screen
 *    - Calculation: (completed / total) * 100
 *
 * SIMULATION (33.33% or 50%): Average of simulation concepts with proper weighting
 *    - Only counts SIMULATION-type concepts loaded on screen (filtered by language)
 *    - Per concept with Agent + URL: Both must complete = 100% for that concept
 *    - Per concept with only Agent: Agent complete = 100% for that concept
 *    - Per concept with only URL: URL complete = 100% for that concept
 *    - Per concept with neither: Skipped (0%, not included in average)
 *    - Average across all simulation concepts with data
 *
 * REVISION (33.33% or 0%): % of concepts with revision session started
 *    - Only if chapter has revision agent (checked via RevisionUseCase)
 *    - If available: Include in calculation, divisor = 3
 *    - If not available: Skip revision, divisor = 2
 *    - Calculation: (revised concepts / total concepts) * 100
 *
 * OVERALL CALCULATION:
 *    - With revision agent: (STUDY + SIMULATION + REVISION) / 3
 *    - Without revision agent: (STUDY + SIMULATION) / 2
 *
 * Weight distribution example (with revision agent):
 *    - Study Agent: 33.33% weight
 *    - Simulation Agent + URL: 33.33% weight
 *    - Revision Agent: 33.33% weight
 *
 * Weight distribution example (without revision agent):
 *    - Study Agent: 50% weight
 *    - Simulation Agent + URL: 50% weight
 */
@Entity(
    tableName = "chapter_agent_progress",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["chapterId"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["chapterId"]),
        Index(value = ["studentId", "chapterId", "language", "appName"], unique = true),
        Index(value = ["appName"])
    ]
)
data class ChapterAgentProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val progressId: Long = 0,
    val studentId: String,
    val chapterId: String,
    val language: String = "en",    // Language: "en" or "kn" (Kannada) - progress varies per language
    val appName: String = "",       // App name to distinguish between multiple apps on same Firebase project
    // Percentage for each agent (0-100)
    val studyPercentage: Int = 0,        // Study/Chatbot agent
    val simulationPercentage: Int = 0,   // Simulation + Simulation Agent
    val revisionPercentage: Int = 0,     // Revision agent
    // Overall chapter progress (0-100) = average of components based on availability
    val overallPercentage: Int = 0,
    // Status: NOT_STARTED, IN_PROGRESS, COMPLETED
    val status: String = "NOT_STARTED",
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    // Sync status
    val isSynced: Boolean = false
)
