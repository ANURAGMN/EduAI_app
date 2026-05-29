package com.ncert7.aitutorandlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ncert7.aitutorandlab.data.local.entities.ProgressEntity
import com.ncert7.aitutorandlab.domain.progress.model.ProgressStatus
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
        "SELECT * FROM progress WHERE studentId = :studentId AND itemType = :itemType AND itemId = :itemId AND language = :language AND appName = :appName"
    )
    suspend fun getProgress(studentId: String, itemType: String, itemId: String, language: String, appName: String): ProgressEntity?

    @Query(
        "SELECT * FROM progress WHERE studentId = :studentId AND itemType = :itemType AND itemId = :itemId AND language = :language AND appName = :appName"
    )
    fun getProgressFlow(studentId: String, itemType: String, itemId: String, language: String, appName: String): Flow<ProgressEntity?>

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

    @Query("DELETE FROM progress")
    suspend fun clearAllProgress()

    @Transaction
    suspend fun updateProgressStatus(
        studentId: String,
        itemType: String,
        itemId: String,
        appName: String,
        language: String,
        newStatus: String,
        progressPercentage: Int,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val existing = getProgress(studentId, itemType, itemId, language, appName)
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
                    language = language,
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
     * Get the total number of simulations completed today
     * Only counts simulation concepts with valid URLs
     * Uses local date to determine "today"
     */
    @Query(
        """
        SELECT COUNT(*) 
        FROM progress p
        INNER JOIN concepts c ON p.itemId = c.conceptId
        WHERE p.studentId = :studentId 
        AND p.itemType = 'CONCEPT' 
        AND p.status = 'COMPLETED'
        AND c.type = 'SIMULATION'
        AND DATE(p.completedAt / 1000, 'unixepoch', 'localtime') = DATE('now', 'localtime')
        AND (
            (c.simulationUrl IS NOT NULL AND c.simulationUrl != '' AND c.simulationUrl != 'Not found')
            OR
            (c.simulationUrlKannada IS NOT NULL AND c.simulationUrlKannada != '' AND c.simulationUrlKannada != 'Not found')
        )
    """
    )
    suspend fun getTodayCompletedSimulations(studentId: String): Int

    /**
     *
     * we calculate progress directly from concepts and progress tables.
     *
     * Returns Flow<List<ChapterProgressSummary>> that emits updates whenever
     * the progress table changes (real-time updates).
     *
     * Logic:
     * - totalConcepts: Count of STUDY + SIMULATION + MATH PROBLEM concepts in chapter
     * - completedConcepts: Count of those concepts with status = COMPLETED in progress table
     * - completionPercentage: (completedConcepts / totalConcepts) * 100
     */
    @Query(
        """
        SELECT 
            ch.chapterId,
            ch.chapterName,
            ch.chapterNameKannada,
            COALESCE((
                SELECT COUNT(*) FROM concepts c 
                WHERE c.chapterId = ch.chapterId 
                AND c.type IN ('STUDY', 'SIMULATION', 'MATH PROBLEM')
                AND (
                    c.type != 'SIMULATION' OR
                    (CASE 
                        WHEN :language = 'en' THEN
                            (c.simulationId IS NOT NULL AND c.simulationId != '' AND c.simulationId != 'null') 
                            OR (c.simulationUrl IS NOT NULL AND c.simulationUrl != '' AND c.simulationUrl != 'null')
                        WHEN :language = 'kn' THEN
                            (c.simulationIdKannada IS NOT NULL AND c.simulationIdKannada != '' AND c.simulationIdKannada != 'null') 
                            OR (c.simulationUrlKannada IS NOT NULL AND c.simulationUrlKannada != '' AND c.simulationUrlKannada != 'null')
                        ELSE 1
                    END)
                )
            ), 0) AS totalConcepts,
            COALESCE((
                SELECT COUNT(*) FROM concepts c 
                WHERE c.chapterId = ch.chapterId 
                AND c.type IN ('STUDY', 'SIMULATION', 'MATH PROBLEM')
                AND (
                    -- If STUDY or MATH PROBLEM
                    (c.type IN ('STUDY', 'MATH PROBLEM') AND EXISTS (
                        SELECT 1 FROM progress p 
                        WHERE p.itemId = c.conceptId 
                        AND p.studentId = :studentId 
                        AND (p.itemType = 'CONCEPT' OR p.itemType = 'MATH_AGENT')
                        AND p.status = :completedStatus 
                        AND p.language = :language
                        AND p.appName = :appName
                    ))
                    OR
                    -- If SIMULATION
                    (c.type = 'SIMULATION' AND 
                        -- Check if simulation is valid for the language
                        (
                            CASE 
                                WHEN :language = 'en' THEN
                                    (c.simulationId IS NOT NULL AND c.simulationId != '' AND c.simulationId != 'null') 
                                    OR (c.simulationUrl IS NOT NULL AND c.simulationUrl != '' AND c.simulationUrl != 'null')
                                WHEN :language = 'kn' THEN
                                    (c.simulationIdKannada IS NOT NULL AND c.simulationIdKannada != '' AND c.simulationIdKannada != 'null') 
                                    OR (c.simulationUrlKannada IS NOT NULL AND c.simulationUrlKannada != '' AND c.simulationUrlKannada != 'null')
                                ELSE 1
                            END
                        ) AND
                        -- Check agent component if it exists
                        (
                            CASE
                                WHEN :language = 'en' THEN
                                    (c.simulationId IS NULL OR c.simulationId = '' OR c.simulationId = 'null' OR EXISTS (
                                        SELECT 1 FROM progress p 
                                        WHERE p.itemId = c.conceptId 
                                        AND p.studentId = :studentId 
                                        AND p.itemType = 'SIMULATION_AGENT' 
                                        AND p.status = :completedStatus 
                                        AND p.language = :language
                                        AND p.appName = :appName
                                    ))
                                WHEN :language = 'kn' THEN
                                    (c.simulationIdKannada IS NULL OR c.simulationIdKannada = '' OR c.simulationIdKannada = 'null' OR EXISTS (
                                        SELECT 1 FROM progress p 
                                        WHERE p.itemId = c.conceptId 
                                        AND p.studentId = :studentId 
                                        AND p.itemType = 'SIMULATION_AGENT' 
                                        AND p.status = :completedStatus 
                                        AND p.language = :language
                                        AND p.appName = :appName
                                    ))
                                ELSE 1
                            END
                        ) AND
                        -- Check URL component if it exists
                        (
                            CASE
                                WHEN :language = 'en' THEN
                                    (c.simulationUrl IS NULL OR c.simulationUrl = '' OR c.simulationUrl = 'null' OR EXISTS (
                                        SELECT 1 FROM progress p 
                                        WHERE p.itemId = c.conceptId 
                                        AND p.studentId = :studentId 
                                        AND p.itemType = 'SIMULATION' 
                                        AND p.status = :completedStatus 
                                        AND p.language = :language
                                        AND p.appName = :appName
                                    ))
                                WHEN :language = 'kn' THEN
                                    (c.simulationUrlKannada IS NULL OR c.simulationUrlKannada = '' OR c.simulationUrlKannada = 'null' OR EXISTS (
                                        SELECT 1 FROM progress p 
                                        WHERE p.itemId = c.conceptId 
                                        AND p.studentId = :studentId 
                                        AND p.itemType = 'SIMULATION' 
                                        AND p.status = :completedStatus 
                                        AND p.language = :language
                                        AND p.appName = :appName
                                    ))
                                ELSE 1
                            END
                        )
                    )
                )
            ), 0) AS completedConcepts,
            CAST(ROUND(COALESCE((
                SELECT COUNT(*) FROM concepts c 
                WHERE c.chapterId = ch.chapterId 
                AND c.type IN ('STUDY', 'SIMULATION', 'MATH PROBLEM')
                AND (
                    -- If STUDY or MATH PROBLEM
                    (c.type IN ('STUDY', 'MATH PROBLEM') AND EXISTS (
                        SELECT 1 FROM progress p 
                        WHERE p.itemId = c.conceptId 
                        AND p.studentId = :studentId 
                        AND (p.itemType = 'CONCEPT' OR p.itemType = 'MATH_AGENT')
                        AND p.status = :completedStatus 
                        AND p.language = :language
                        AND p.appName = :appName
                    ))
                    OR
                    -- If SIMULATION
                    (c.type = 'SIMULATION' AND 
                        -- Check if simulation is valid for the language
                        (
                            CASE 
                                WHEN :language = 'en' THEN
                                    (c.simulationId IS NOT NULL AND c.simulationId != '' AND c.simulationId != 'null') 
                                    OR (c.simulationUrl IS NOT NULL AND c.simulationUrl != '' AND c.simulationUrl != 'null')
                                WHEN :language = 'kn' THEN
                                    (c.simulationIdKannada IS NOT NULL AND c.simulationIdKannada != '' AND c.simulationIdKannada != 'null') 
                                    OR (c.simulationUrlKannada IS NOT NULL AND c.simulationUrlKannada != '' AND c.simulationUrlKannada != 'null')
                                ELSE 1
                            END
                        ) AND
                        -- Check agent component if it exists
                        (
                            CASE
                                WHEN :language = 'en' THEN
                                    (c.simulationId IS NULL OR c.simulationId = '' OR c.simulationId = 'null' OR EXISTS (
                                        SELECT 1 FROM progress p 
                                        WHERE p.itemId = c.conceptId 
                                        AND p.studentId = :studentId 
                                        AND p.itemType = 'SIMULATION_AGENT' 
                                        AND p.status = :completedStatus 
                                        AND p.language = :language
                                        AND p.appName = :appName
                                    ))
                                WHEN :language = 'kn' THEN
                                    (c.simulationIdKannada IS NULL OR c.simulationIdKannada = '' OR c.simulationIdKannada = 'null' OR EXISTS (
                                        SELECT 1 FROM progress p 
                                        WHERE p.itemId = c.conceptId 
                                        AND p.studentId = :studentId 
                                        AND p.itemType = 'SIMULATION_AGENT' 
                                        AND p.status = :completedStatus 
                                        AND p.language = :language
                                        AND p.appName = :appName
                                    ))
                                ELSE 1
                            END
                        ) AND
                        -- Check URL component if it exists
                        (
                            CASE
                                WHEN :language = 'en' THEN
                                    (c.simulationUrl IS NULL OR c.simulationUrl = '' OR c.simulationUrl = 'null' OR EXISTS (
                                        SELECT 1 FROM progress p 
                                        WHERE p.itemId = c.conceptId 
                                        AND p.studentId = :studentId 
                                        AND p.itemType = 'SIMULATION' 
                                        AND p.status = :completedStatus 
                                        AND p.language = :language
                                        AND p.appName = :appName
                                    ))
                                WHEN :language = 'kn' THEN
                                    (c.simulationUrlKannada IS NULL OR c.simulationUrlKannada = '' OR c.simulationUrlKannada = 'null' OR EXISTS (
                                        SELECT 1 FROM progress p 
                                        WHERE p.itemId = c.conceptId 
                                        AND p.studentId = :studentId 
                                        AND p.itemType = 'SIMULATION' 
                                        AND p.status = :completedStatus 
                                        AND p.language = :language
                                        AND p.appName = :appName
                                    ))
                                ELSE 1
                            END
                        )
                    )
                )
            ), 0) * 100.0 / 
            CASE WHEN COALESCE((
                SELECT COUNT(*) FROM concepts c 
                WHERE c.chapterId = ch.chapterId 
                AND c.type IN ('STUDY', 'SIMULATION', 'MATH PROBLEM')
                AND (
                    c.type != 'SIMULATION' OR
                    (CASE 
                        WHEN :language = 'en' THEN
                            (c.simulationId IS NOT NULL AND c.simulationId != '' AND c.simulationId != 'null') 
                            OR (c.simulationUrl IS NOT NULL AND c.simulationUrl != '' AND c.simulationUrl != 'null')
                        WHEN :language = 'kn' THEN
                            (c.simulationIdKannada IS NOT NULL AND c.simulationIdKannada != '' AND c.simulationIdKannada != 'null') 
                            OR (c.simulationUrlKannada IS NOT NULL AND c.simulationUrlKannada != '' AND c.simulationUrlKannada != 'null')
                        ELSE 1
                    END)
                )
            ), 0) = 0 THEN 1 
            ELSE COALESCE((
                SELECT COUNT(*) FROM concepts c 
                WHERE c.chapterId = ch.chapterId 
                AND c.type IN ('STUDY', 'SIMULATION', 'MATH PROBLEM')
                AND (
                    c.type != 'SIMULATION' OR
                    (CASE 
                        WHEN :language = 'en' THEN
                            (c.simulationId IS NOT NULL AND c.simulationId != '' AND c.simulationId != 'null') 
                            OR (c.simulationUrl IS NOT NULL AND c.simulationUrl != '' AND c.simulationUrl != 'null')
                        WHEN :language = 'kn' THEN
                            (c.simulationIdKannada IS NOT NULL AND c.simulationIdKannada != '' AND c.simulationIdKannada != 'null') 
                            OR (c.simulationUrlKannada IS NOT NULL AND c.simulationUrlKannada != '' AND c.simulationUrlKannada != 'null')
                        ELSE 1
                    END)
                )
            ), 1) END) AS INTEGER) AS completionPercentage
        FROM chapters ch
        LEFT JOIN progress p_dummy ON p_dummy.studentId = :studentId AND 1=0
        WHERE ch.subjectId = :subjectId
        ORDER BY ch.orderIndex ASC
        """
    )
    fun getChapterWiseProgressFlow(
        studentId: String,
        subjectId: String,
        language: String,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): Flow<List<ChapterProgressSummary>>

    @Query(
        """
        SELECT COUNT(*) 
        FROM progress 
        WHERE studentId = :studentId 
        AND itemType = 'CONCEPT' 
        AND status = :completedStatus
        AND language = :language
        AND appName = :appName
    """
    )
    fun getTotalCompletedConceptsFlow(
        studentId: String,
        language: String,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): Flow<Int>

    /**
     * Get the total number of fully completed simulation-type concepts for a specific language.
     * A simulation concept is COMPLETED only if all its required components (Agent and/or URL) are COMPLETED.
     */
    @Query(
        """
        SELECT COUNT(*) FROM concepts c
        WHERE c.type = 'SIMULATION'
        -- Ensure it has at least one valid component to be counted for this language
        AND (
            CASE 
                WHEN :language = 'en' THEN
                    (c.simulationId IS NOT NULL AND c.simulationId != '' AND c.simulationId != 'null') 
                    OR (c.simulationUrl IS NOT NULL AND c.simulationUrl != '' AND c.simulationUrl != 'null')
                WHEN :language = 'kn' THEN
                    (c.simulationIdKannada IS NOT NULL AND c.simulationIdKannada != '' AND c.simulationIdKannada != 'null') 
                    OR (c.simulationUrlKannada IS NOT NULL AND c.simulationUrlKannada != '' AND c.simulationUrlKannada != 'null')
                ELSE 1
            END
        )
        AND (
            -- Check agent completion
            CASE
                WHEN :language = 'en' THEN
                    (c.simulationId IS NULL OR c.simulationId = '' OR c.simulationId = 'null' OR 
                     EXISTS (SELECT 1 FROM progress p WHERE p.itemId = c.conceptId AND p.studentId = :studentId AND p.itemType = 'SIMULATION_AGENT' AND p.status = :completedStatus AND p.language = :language AND p.appName = :appName))
                WHEN :language = 'kn' THEN
                    (c.simulationIdKannada IS NULL OR c.simulationIdKannada = '' OR c.simulationIdKannada = 'null' OR 
                     EXISTS (SELECT 1 FROM progress p WHERE p.itemId = c.conceptId AND p.studentId = :studentId AND p.itemType = 'SIMULATION_AGENT' AND p.status = :completedStatus AND p.language = :language AND p.appName = :appName))
                ELSE 1
            END
        )
        AND (
            -- Check URL completion
            CASE
                WHEN :language = 'en' THEN
                    (c.simulationUrl IS NULL OR c.simulationUrl = '' OR c.simulationUrl = 'null' OR 
                     EXISTS (SELECT 1 FROM progress p WHERE p.itemId = c.conceptId AND p.studentId = :studentId AND p.itemType = 'SIMULATION' AND p.status = :completedStatus AND p.language = :language AND p.appName = :appName))
                WHEN :language = 'kn' THEN
                    (c.simulationUrlKannada IS NULL OR c.simulationUrlKannada = '' OR c.simulationUrlKannada = 'null' OR 
                     EXISTS (SELECT 1 FROM progress p WHERE p.itemId = c.conceptId AND p.studentId = :studentId AND p.itemType = 'SIMULATION' AND p.status = :completedStatus AND p.language = :language AND p.appName = :appName))
                ELSE 1
            END
        )
    """
    )
    fun getTotalCompletedSimulationsFlow(
        studentId: String,
        language: String,
        appName: String,
        completedStatus: String = ProgressStatus.COMPLETED.value
    ): Flow<Int>

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
    ): Flow<Int>

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
    ): Flow<Int>

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
 *  Data class to hold chapter-wise progress
 * This is used by ProgressScreenViewModel to display chapter progress
 * Updated in real-time via Flow from getChapterWiseProgressFlow()
 */
data class ChapterProgressSummary(
    val chapterId: String,
    val chapterName: String,
    val chapterNameKannada: String = "",
    val totalConcepts: Int,
    val completedConcepts: Int,
    val completionPercentage: Int  // Changed from Float to Int for consistency
)