package com.anurag.eduai.data.local.dao

import androidx.room.*
import com.anurag.eduai.data.local.entities.ProgressEntity
import com.anurag.eduai.domain.progress.model.ProgressStatus
import kotlinx.coroutines.flow.Flow

/** Data Access Object for managing student progress in learning items. */
@Dao
interface ProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressList(progressList: List<ProgressEntity>)

    @Update
    suspend fun updateProgress(progress: ProgressEntity)

    @Query(
        "SELECT * FROM progress WHERE studentId = :studentId AND itemType = :itemType AND itemId = :itemId AND appName = :appName"
    )
    suspend fun getProgress(studentId: String, itemType: String, itemId: String, appName: String): ProgressEntity?

    @Query(
        "SELECT * FROM progress WHERE studentId = :studentId AND itemType = :itemType AND itemId = :itemId AND appName = :appName"
    )
    fun getProgressFlow(studentId: String, itemType: String, itemId: String, appName: String): Flow<ProgressEntity?>

    @Query("SELECT * FROM progress WHERE studentId = :studentId AND appName = :appName")
    fun getAllProgress(studentId: String, appName: String): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE studentId = :studentId AND itemType = :itemType AND appName = :appName")
    suspend fun getAllProgressSync(studentId: String, itemType: String, appName: String): List<ProgressEntity>

    @Query(
        "SELECT COUNT(*) FROM progress WHERE studentId = :studentId AND itemType = :itemType AND status = :completedStatus AND completedAt >= :weekStartTimestamp AND appName = :appName"
    )
    suspend fun getWeeklyCompletedCount(
        studentId: String,
        weekStartTimestamp: Long,
        itemType: String,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): Int

    @Query("SELECT * FROM progress WHERE isSynced = 0")
    suspend fun getUnsyncedProgress(): List<ProgressEntity>

    @Query("UPDATE progress SET isSynced = 1 WHERE progressId IN (:ids)")
    suspend fun markProgressAsSynced(ids: List<Long>)

    @Query(
        "DELETE FROM progress WHERE studentId = :studentId AND itemType = :itemType AND itemId = :itemId AND appName = :appName"
    )
    suspend fun deleteProgress(studentId: String, itemType: String, itemId: String, appName: String)

    @Transaction
    suspend fun updateProgressStatus(
        studentId: String,
        itemType: String,
        itemId: String,
        appName: String,
        newStatus: String,
        progressPercentage: Int,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val existing = getProgress(studentId, itemType, itemId, appName)
        if (existing != null) {
            val updated =
                existing.copy(
                    status = newStatus,
                    completedAt =
                        if (newStatus == ProgressStatus.COMPLETED.value) timestamp
                        else existing.completedAt,
                    startedAt = existing.startedAt ?:
                        if (newStatus == ProgressStatus.IN_PROGRESS.value) timestamp else null,
                    lastAccessedAt = timestamp,
                    updatedAt = timestamp,
                    progressPercentage = progressPercentage.coerceIn(0, 100),
                    isSynced = false
                )
            updateProgress(updated)
        } else {
            insertProgress(
                ProgressEntity(
                    studentId = studentId,
                    itemType = itemType,
                    itemId = itemId,
                    appName = appName,
                    status = newStatus,
                    progressPercentage = progressPercentage.coerceIn(0, 100),
                    startedAt = if (newStatus == ProgressStatus.IN_PROGRESS.value) timestamp else null,
                    completedAt = if (newStatus == ProgressStatus.COMPLETED.value) timestamp else null,
                    lastAccessedAt = timestamp,
                    updatedAt = timestamp
                )
            )
        }
    }

    /**
     * Get home screen concepts with real-time updates: 1st item - most recently updated IN_PROGRESS
     * concept Next 3 items - NOT_STARTED concepts ordered by ConceptEntity.orderIndex Limit to 4
     * total items
     *
     * Automatically emits new list whenever progress changes
     */
    @Query(
        """
        SELECT p.* FROM progress p
        INNER JOIN concepts c ON p.itemId = c.conceptId
        WHERE p.studentId = :studentId 
        AND p.itemType = :itemType 
        AND p.appName = :appName
        AND p.status != :completedStatus
        ORDER BY 
            CASE WHEN p.status = :inProgressStatus THEN 0 ELSE 1 END ASC,
            CASE WHEN p.status = :inProgressStatus THEN p.lastAccessedAt ELSE 0 END DESC,
            c.orderIndex ASC
        LIMIT 4
    """
    )
    fun getHomeScreenConcepts(
        studentId: String,
        itemType: String,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value,
        inProgressStatus: String = ProgressStatus.IN_PROGRESS.value
    ): Flow<List<ProgressEntity>>

    /**
     * Progress for home screen today progress section
     */
    @Query(
        """
    SELECT * FROM progress
    WHERE studentId = :studentId
      AND itemType = 'CONCEPT'
      AND status = :completedStatus
      AND appName = :appName
    ORDER BY completedAt DESC
    LIMIT 1
"""
    )
    suspend fun getLastCompletedConcept(
        studentId: String,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): ProgressEntity?

    // ===== FLOW-BASED QUERIES FOR REAL-TIME UPDATES =====


    /**
     * Get concepts cleared last 7 days as Flow for real-time updates
     * Emits updated list whenever progress changes
     */
    @Query(
        """
        SELECT 
            DATE(completedAt / 1000, 'unixepoch', 'localtime') as date,
            COUNT(*) as count
        FROM progress
        WHERE studentId = :studentId
        AND itemType = 'CONCEPT'
        AND status = :completedStatus
        AND completedAt >= :sevenDaysAgoTimestamp
        AND appName = :appName
        GROUP BY DATE(completedAt / 1000, 'unixepoch', 'localtime')
        ORDER BY date DESC
    """
    )
    fun getConceptsClearedLast7DaysFlow(
        studentId: String,
        sevenDaysAgoTimestamp: Long,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): Flow<List<DailyConceptCount>>

    /**
     * Get chapter-wise progress as Flow for real-time updates.
     * Calculates totalConcepts and completedConcepts dynamically from the concepts and progress tables,
     * using chapter_agent_progress for the overall precomputed percentage.
     */
    @Query(
        """
        SELECT 
            ch.chapterId AS chapterId,
            ch.chapterName AS chapterName,
            ch.chapterNameKannada AS chapterNameKannada,
            (SELECT COUNT(*) FROM concepts c WHERE c.chapterId = ch.chapterId AND c.type = 'STUDY') AS totalConcepts,
            CAST(ROUND((COALESCE(cap.overallPercentage, 0) / 100.0) * (SELECT COUNT(*) FROM concepts c WHERE c.chapterId = ch.chapterId AND c.type = 'STUDY')) AS INTEGER) AS completedConcepts,
            COALESCE(cap.overallPercentage, 0) AS completionPercentage
        FROM chapters ch
        LEFT JOIN chapter_agent_progress cap 
            ON cap.chapterId = ch.chapterId 
            AND cap.studentId = :studentId
            AND cap.language = :language
            AND cap.appName = :appName
        WHERE 
            ch.subjectId = :subjectId
        ORDER BY ch.orderIndex ASC
        """
    )
    fun getChapterWiseProgressFlow(
        studentId: String,
        subjectId: String,
        language: String,
        appName: String
    ): kotlinx.coroutines.flow.Flow<List<ChapterProgressSummary>>

    @Query(
        """
        SELECT COUNT(*) 
        FROM progress 
        WHERE studentId = :studentId 
        AND itemType = 'CONCEPT' 
        AND status = :completedStatus
        AND appName = :appName
    """
    )
    fun getTotalCompletedConceptsFlow(
        studentId: String,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): kotlinx.coroutines.flow.Flow<Int>

    /**
     * Get the total number of fully completed simulation-type concepts.
     * A simulation concept is COMPLETED only if all its required components (Agent and/or URL) are COMPLETED.
     */
    @Query(
        """
        SELECT COUNT(*) FROM concepts c
        WHERE c.type = 'SIMULATION'
        -- Ensure it has at least one valid component to be counted
        AND ( (c.simulationId IS NOT NULL AND c.simulationId != '' AND c.simulationId != 'null') 
              OR (c.simulationUrl IS NOT NULL AND c.simulationUrl != '' AND c.simulationUrl != 'null') )
        AND (
            (c.simulationId IS NULL OR c.simulationId = '' OR c.simulationId = 'null' OR 
             EXISTS (SELECT 1 FROM progress p WHERE p.itemId = c.conceptId AND p.studentId = :studentId AND p.itemType = 'SIMULATION_AGENT' AND p.status = :completedStatus AND p.appName = :appName))
            AND
            (c.simulationUrl IS NULL OR c.simulationUrl = '' OR c.simulationUrl = 'null' OR 
             EXISTS (SELECT 1 FROM progress p WHERE p.itemId = c.conceptId AND p.studentId = :studentId AND p.itemType = 'SIMULATION' AND p.status = :completedStatus AND p.appName = :appName))
        )
    """
    )
    fun getTotalCompletedSimulationsFlow(
        studentId: String,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): kotlinx.coroutines.flow.Flow<Int>

    /**
     * Get the number of concepts cleared in the last 7 days, day-wise Returns a list of
     * DailyConceptCount with date and count Ordered from most recent (today) to 7 days ago
     */
    @Query(
        """
        SELECT 
            DATE(completedAt / 1000, 'unixepoch', 'localtime') as date,
            COUNT(*) as count
        FROM progress
        WHERE studentId = :studentId
        AND itemType = 'CONCEPT'
        AND status = :completedStatus
        AND completedAt >= :sevenDaysAgoTimestamp
        AND appName = :appName
        GROUP BY DATE(completedAt / 1000, 'unixepoch', 'localtime')
        ORDER BY date DESC
    """
    )
    suspend fun getConceptsClearedLast7Days(
        studentId: String,
        sevenDaysAgoTimestamp: Long,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): List<DailyConceptCount>

    /**
     * Get count of CONCEPT completed today (Reactive Flow) */
    @Query(
        """
    SELECT COUNT(*) 
    FROM progress
    WHERE studentId = :studentId
      AND itemType = 'CONCEPT'
      AND status = :completedStatus
      AND completedAt BETWEEN :startOfDay AND :endOfDay
      AND appName = :appName
    """
    )
    fun getTodayCompletedConceptCountFlow(
        studentId: String,
        startOfDay: Long,
        endOfDay: Long,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): kotlinx.coroutines.flow.Flow<Int>

    /**
     * Get count of CONCEPT completed today (Synchronous) */
    @Query(
        """
    SELECT COUNT(*) 
    FROM progress
    WHERE studentId = :studentId
      AND itemType = 'CONCEPT'
      AND status = :completedStatus
      AND completedAt BETWEEN :startOfDay AND :endOfDay
      AND appName = :appName
    """
    )
    suspend fun getTodayCompletedConceptCount(
        studentId: String,
        startOfDay: Long,
        endOfDay: Long,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): Int

    /**
     * Get count of simulation concepts fully completed today (Reactive Flow).
     * A simulation concept is COMPLETED only if all its required components (Agent and/or URL) are COMPLETED.
     * At least one component must have been completed within the today's time range.
     */
    @Query(
        """
    SELECT COUNT(*) FROM concepts c
    WHERE c.type = 'SIMULATION'
    -- Ensure it has at least one valid component
    AND ( (c.simulationId IS NOT NULL AND c.simulationId != '' AND c.simulationId != 'null') 
          OR (c.simulationUrl IS NOT NULL AND c.simulationUrl != '' AND c.simulationUrl != 'null') )
    -- ALL existing components must be completed
    AND (
        (c.simulationId IS NULL OR c.simulationId = '' OR c.simulationId = 'null' OR 
         EXISTS (SELECT 1 FROM progress p WHERE p.itemId = c.conceptId AND p.studentId = :studentId AND p.itemType = 'SIMULATION_AGENT' AND p.status = :completedStatus AND p.appName = :appName))
        AND
        (c.simulationUrl IS NULL OR c.simulationUrl = '' OR c.simulationUrl = 'null' OR 
         EXISTS (SELECT 1 FROM progress p WHERE p.itemId = c.conceptId AND p.studentId = :studentId AND p.itemType = 'SIMULATION' AND p.status = :completedStatus AND p.appName = :appName))
    )
    -- AT LEAST ONE component must have been completed TODAY
    AND EXISTS (
        SELECT 1 FROM progress p 
        WHERE p.itemId = c.conceptId 
        AND p.studentId = :studentId 
        AND p.status = :completedStatus 
        AND p.appName = :appName 
        AND p.completedAt BETWEEN :startOfDay AND :endOfDay
    )
    """
    )
    fun getTodayCompletedSimulationCountFlow(
        studentId: String,
        startOfDay: Long,
        endOfDay: Long,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): kotlinx.coroutines.flow.Flow<Int>

    /**
     * Get count of SIMULATION completed today (Synchronous) */
    @Query(
        """
    SELECT COUNT(*) 
    FROM progress
    WHERE studentId = :studentId
      AND (itemType = 'SIMULATION' OR itemType = 'SIMULATION_AGENT')
      AND status = :completedStatus
      AND completedAt BETWEEN :startOfDay AND :endOfDay
      AND appName = :appName
      AND itemId IS NOT NULL AND itemId != ''
    """
    )
    suspend fun getTodayCompletedSimulationCount(
        studentId: String,
        startOfDay: Long,
        endOfDay: Long,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): Int

    /**
     * Get daily activity: count any completed activity (concept/simulation/revision) on a specific date
     * This is used for streak calculation - ANY activity counts toward the streak
     * Returns count of activities per day
     */
    @Query(
        """
        SELECT 
            DATE(completedAt / 1000, 'unixepoch', 'localtime') as date,
            COUNT(*) as count
        FROM progress
        WHERE studentId = :studentId
          AND status = :completedStatus
          AND completedAt >= :sevenDaysAgoTimestamp
          AND appName = :appName
          AND itemType IN ('CONCEPT', 'SIMULATION', 'SIMULATION_AGENT', 'REVISION_AGENT', 'MATH_AGENT', 'SCIENCE_AGENT')
        GROUP BY DATE(completedAt / 1000, 'unixepoch', 'localtime')
        ORDER BY date DESC
        """
    )
    suspend fun getDailyCompletedActivityLast7Days(
        studentId: String,
        sevenDaysAgoTimestamp: Long,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): List<DailyConceptCount>

    /**
     * Get count of completed activities today (any activity: concept/simulation/revision)
     * Used for streak tracking - ANY completed activity counts
     */
    @Query(
        """
        SELECT COUNT(*)
        FROM progress
        WHERE studentId = :studentId
          AND status = :completedStatus
          AND completedAt BETWEEN :startOfDay AND :endOfDay
          AND appName = :appName
          AND itemType IN ('CONCEPT', 'SIMULATION', 'SIMULATION_AGENT', 'REVISION_AGENT', 'MATH_AGENT', 'SCIENCE_AGENT')
        """
    )
    suspend fun getTodayFullyCompletedActivityCount(
        studentId: String,
        startOfDay: Long,
        endOfDay: Long,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): Int
}


/** Data class to hold daily concept completion count */
data class DailyConceptCount(
        val date: String, // Format: YYYY-MM-DD
        val count: Int
)

/**
 * Data class to hold the chapter wise progress
 */
data class ChapterProgressSummary(
    val chapterId: String,
    val chapterName: String,
    val chapterNameKannada: String = "",
    val totalConcepts: Int,
    val completedConcepts: Int,
    val completionPercentage: Float
)
