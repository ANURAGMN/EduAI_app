package com.anurag.eduai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anurag.eduai.data.local.entities.AppAnalyticsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing app analytics data in the local database.
 */
@Dao
interface AppAnalyticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: AppAnalyticsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsList(analyticsList: List<AppAnalyticsEntity>)

    @Query("SELECT * FROM app_analytics WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getAnalyticsForStudent(studentId: String): Flow<List<AppAnalyticsEntity>>

    @Query("SELECT * FROM app_analytics WHERE studentId = :studentId ORDER BY timestamp DESC")
    suspend fun getAnalyticsForStudentSync(studentId: String): List<AppAnalyticsEntity>

    @Query("SELECT * FROM app_analytics WHERE studentId = :studentId AND screenName = :screenName ORDER BY timestamp DESC")
    suspend fun getAnalyticsForScreen(studentId: String, screenName: String): List<AppAnalyticsEntity>

    @Query("SELECT * FROM app_analytics WHERE studentId = :studentId AND sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getAnalyticsForSession(studentId: String, sessionId: String): List<AppAnalyticsEntity>

    // screen visit count
    @Query("""
        SELECT COUNT(*) FROM app_analytics 
        WHERE studentId = :studentId 
        AND screenName = :screenName 
        AND eventType = 'ENTRY'
    """)
    suspend fun getScreenVisitCount(studentId: String, screenName: String): Int

    @Query("SELECT * FROM app_analytics WHERE isSynced = 0")
    suspend fun getUnsyncedAnalytics(): List<AppAnalyticsEntity>

    @Query("UPDATE app_analytics SET isSynced = 1 WHERE analyticsId = :analyticsId")
    suspend fun markAnalyticsAsSynced(analyticsId: Long)

    @Query("DELETE FROM app_analytics WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldAnalytics(cutoffTimestamp: Long)
}
