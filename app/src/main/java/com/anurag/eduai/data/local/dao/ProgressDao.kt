package com.anurag.eduai.data.local.dao

import androidx.room.*
import com.anurag.eduai.data.local.entities.ProgressEntity
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
        "SELECT * FROM progress WHERE studentId = :studentId AND itemType = :itemType AND itemId = :itemId"
    )
    suspend fun getProgress(studentId: String, itemType: String, itemId: String): ProgressEntity?

    @Query(
        "SELECT * FROM progress WHERE studentId = :studentId AND itemType = :itemType AND itemId = :itemId"
    )
    fun getProgressFlow(studentId: String, itemType: String, itemId: String): Flow<ProgressEntity?>

    @Query("SELECT * FROM progress WHERE studentId = :studentId")
    fun getAllProgress(studentId: String): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE studentId = :studentId AND itemType = :itemType")
    suspend fun getAllProgressSync(studentId: String, itemType: String): List<ProgressEntity>

    @Query(
        "SELECT COUNT(*) FROM progress WHERE studentId = :studentId AND itemType = :itemType AND status = 'COMPLETED' AND completedAt >= :weekStartTimestamp"
    )
    suspend fun getWeeklyCompletedCount(
        studentId: String,
        weekStartTimestamp: Long,
        itemType: String
    ): Int

    @Query("SELECT * FROM progress WHERE isSynced = 0")
    suspend fun getUnsyncedProgress(): List<ProgressEntity>

    @Query("UPDATE progress SET isSynced = 1 WHERE progressId IN (:ids)")
    suspend fun markProgressAsSynced(ids: List<Long>)

    @Query(
        "DELETE FROM progress WHERE studentId = :studentId AND itemType = :itemType AND itemId = :itemId"
    )
    suspend fun deleteProgress(studentId: String, itemType: String, itemId: String)

    @Transaction
    suspend fun updateProgressStatus(
        studentId: String,
        itemType: String,
        itemId: String,
        newStatus: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val existing = getProgress(studentId, itemType, itemId)
        if (existing != null) {
            val updated =
                existing.copy(
                    status = newStatus,
                    completedAt =
                        if (newStatus == "COMPLETED") timestamp
                        else existing.completedAt,
                    startedAt = existing.startedAt ?:
                        if (newStatus == "IN_PROGRESS") timestamp else null,
                    openedAt = existing.openedAt ?:
                        if (newStatus in listOf("STARTED", "IN_PROGRESS")) timestamp else null,
                    lastAccessedAt = timestamp,
                    updatedAt = timestamp,
                    isSynced = false
                )
            updateProgress(updated)
        } else {
            insertProgress(
                ProgressEntity(
                    studentId = studentId,
                    itemType = itemType,
                    itemId = itemId,
                    status = newStatus,
                    startedAt = if (newStatus == "IN_PROGRESS") timestamp else null,
                    openedAt = if (newStatus in listOf("STARTED", "IN_PROGRESS")) timestamp else null,
                    completedAt = if (newStatus == "COMPLETED") timestamp else null,
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
        AND p.status != 'COMPLETED'
        ORDER BY 
            CASE WHEN p.status = 'IN_PROGRESS' THEN 0 ELSE 1 END ASC,
            CASE WHEN p.status = 'IN_PROGRESS' THEN p.lastAccessedAt ELSE 0 END DESC,
            c.orderIndex ASC
        LIMIT 4
    """
    )
    fun getHomeScreenConcepts(studentId: String, itemType: String): Flow<List<ProgressEntity>>

    /**
     * Progress for home screen today progress section
     */
    @Query(
        """
    SELECT * FROM progress
    WHERE studentId = :studentId
      AND itemType = 'CONCEPT'
      AND status = 'COMPLETED'
    ORDER BY completedAt DESC
    LIMIT 1
"""
    )
    suspend fun getLastCompletedConcept(studentId: String): ProgressEntity?

    /** Get the total number of completed concepts for a student */
    @Query(
        """
        SELECT COUNT(*) 
        FROM progress 
        WHERE studentId = :studentId 
        AND itemType = 'CONCEPT' 
        AND status = 'COMPLETED'
    """
    )
    suspend fun getTotalCompletedConcepts(studentId: String): Int

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
        AND status = 'COMPLETED'
        AND completedAt >= :sevenDaysAgoTimestamp
        GROUP BY DATE(completedAt / 1000, 'unixepoch', 'localtime')
        ORDER BY date DESC
    """
    )
    suspend fun getConceptsClearedLast7Days(
        studentId: String,
        sevenDaysAgoTimestamp: Long
    ): List<DailyConceptCount>

    @Query(
        """
    SELECT 
        ch.chapterId AS chapterId,
        ch.chapterName AS chapterName,
        COUNT(c.conceptId) AS totalConcepts,
        COUNT(p.progressId) AS completedConcepts,
        (COUNT(p.progressId) * 100.0 / COUNT(c.conceptId)) AS completionPercentage
    FROM chapters ch
    INNER JOIN subjects s 
        ON ch.subjectId = s.subjectId
    INNER JOIN concepts c 
        ON c.chapterId = ch.chapterId
    LEFT JOIN progress p 
        ON p.itemId = c.conceptId
        AND p.itemType = 'CONCEPT'
        AND p.status = 'COMPLETED'
        AND p.studentId = :studentId
    WHERE 
        s.classLevel = :classLevel
        AND s.subjectId = :subjectId
    GROUP BY ch.chapterId
    ORDER BY ch.orderIndex ASC
    """)
    suspend fun getChapterWiseProgress(
        studentId: String,
        classLevel: Int,
        subjectId: String
    ): List<ChapterProgressSummary>


    /** Get count of CONCEPT completed today */
    @Query(
        """
    SELECT COUNT(*) 
    FROM progress
    WHERE studentId = :studentId
      AND itemType = 'CONCEPT'
      AND status = 'COMPLETED'
      AND completedAt BETWEEN :startOfDay AND :endOfDay
    """
    )
    suspend fun getTodayCompletedConceptCount(
        studentId: String,
        startOfDay: Long,
        endOfDay: Long
    ): Int

    /** Get count of SIMULATION completed today */
    @Query(
        """
    SELECT COUNT(*) 
    FROM progress
    WHERE studentId = :studentId
      AND itemType = 'SIMULATION'
      AND status = 'COMPLETED'
      AND completedAt BETWEEN :startOfDay AND :endOfDay
    """
    )
    suspend fun getTodayCompletedSimulationCount(
        studentId: String,
        startOfDay: Long,
        endOfDay: Long
    ): Int
}


/** Data class to hold daily concept completion count */
data class DailyConceptCount(
        val date: String, // Format: YYYY-MM-DD
        val count: Int
)

/**
 * Data class to hold the chapter wise progresss
 */
data class ChapterProgressSummary(
    val chapterId: String,
    val chapterName: String,
    val totalConcepts: Int,
    val completedConcepts: Int,
    val completionPercentage: Float
)
