package com.anurag.eduai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anurag.eduai.data.local.entities.ConceptActivityEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing concept activity data in the local database.
 */
@Dao
interface ConceptActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ConceptActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ConceptActivityEntity>)

    @Query("SELECT * FROM concept_activities WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getActivitiesForStudent(studentId: String): Flow<List<ConceptActivityEntity>>

    @Query("SELECT * FROM concept_activities WHERE studentId = :studentId ORDER BY timestamp DESC")
    suspend fun getActivitiesForStudentSync(studentId: String): List<ConceptActivityEntity>

    @Query("SELECT * FROM concept_activities WHERE studentId = :studentId AND conceptId = :conceptId ORDER BY timestamp DESC")
    suspend fun getActivitiesForConceptSync(studentId: String, conceptId: String): List<ConceptActivityEntity>

    @Query("SELECT * FROM concept_activities WHERE studentId = :studentId AND sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getActivitiesForSession(studentId: String, sessionId: String): List<ConceptActivityEntity>

    // Get weekly concept completed count
    @Query("""
        SELECT COUNT(*) FROM concept_activities 
        WHERE studentId = :studentId 
        AND eventType = 'CONCEPT_COMPLETED'
        AND timestamp >= :weekStartTimestamp
    """)
    suspend fun getWeeklyConceptsCompleted(studentId: String, weekStartTimestamp: Long): Int

    @Query("SELECT * FROM concept_activities WHERE isSynced = 0")
    suspend fun getUnsyncedActivities(): List<ConceptActivityEntity>

    @Query("UPDATE concept_activities SET isSynced = 1 WHERE activityId = :activityId")
    suspend fun markActivityAsSynced(activityId: Long)

    @Query("DELETE FROM concept_activities WHERE activityId = :activityId")
    suspend fun deleteActivity(activityId: Long)

    @Query("DELETE FROM concept_activities WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldActivities(cutoffTimestamp: Long)
}