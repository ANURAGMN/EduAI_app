package com.ncert7.aitutorandlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ncert7.aitutorandlab.data.local.entities.AppAnalyticsEntity

/**
 * DAO for managing app analytics data in the local database.
 */
@Dao
interface AppAnalyticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: AppAnalyticsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsList(analyticsList: List<AppAnalyticsEntity>)

    @Query("""
        UPDATE app_analytics 
        SET eventType = :eventType, 
            exitTime = :exitTime, 
            durationMillis = :durationMillis 
        WHERE analyticsId = :analyticsId
    """)
    suspend fun updateAnalyticsExit(
        analyticsId: Long,
        eventType: String,
        exitTime: Long,
        durationMillis: Long
    )

    @Query("""
        SELECT * FROM app_analytics 
        WHERE sessionId = :sessionId 
        AND screenName = :screenName 
        AND exitTime IS NULL 
        ORDER BY entryTime DESC 
        LIMIT 1
    """)
    suspend fun getActiveAnalyticsForScreen(sessionId: String, screenName: String): AppAnalyticsEntity?

    @Query("SELECT * FROM app_analytics WHERE screenName = :screenName ORDER BY entryTime DESC")
    suspend fun getAnalyticsForScreen(screenName: String): List<AppAnalyticsEntity>

    @Query("SELECT * FROM app_analytics WHERE sessionId = :sessionId ORDER BY entryTime ASC")
    suspend fun getAnalyticsForSession(sessionId: String): List<AppAnalyticsEntity>

    // screen visit count
    @Query("""
        SELECT COUNT(*) FROM app_analytics 
        WHERE screenName = :screenName 
        AND exitTime IS NOT NULL
    """)
    suspend fun getScreenVisitCount(screenName: String): Int

    @Query("SELECT * FROM app_analytics WHERE isSynced = 0")
    suspend fun getUnsyncedAnalytics(): List<AppAnalyticsEntity>

    @Query("UPDATE app_analytics SET isSynced = 1 WHERE analyticsId = :analyticsId")
    suspend fun markAnalyticsAsSynced(analyticsId: Long)

    @Query("DELETE FROM app_analytics WHERE entryTime < :cutoffTimestamp")
    suspend fun deleteOldAnalytics(cutoffTimestamp: Long)

    // ===== Aggregation Queries for Multiple Visits =====

    /**
     * Get total time spent on a specific screen in a session (sum of all visits)
     */
    @Query("""
        SELECT COALESCE(SUM(durationMillis), 0) 
        FROM app_analytics 
        WHERE sessionId = :sessionId 
        AND screenName = :screenName 
        AND exitTime IS NOT NULL
    """)
    suspend fun getTotalTimeOnScreenInSession(sessionId: String, screenName: String): Long

    /**
     * Get number of visits to a specific screen in a session
     */
    @Query("""
        SELECT COUNT(*) 
        FROM app_analytics 
        WHERE sessionId = :sessionId 
        AND screenName = :screenName 
        AND exitTime IS NOT NULL
    """)
    suspend fun getVisitCountInSession(sessionId: String, screenName: String): Int

    /**
     * Get average time per visit to a specific screen in a session
     */
    @Query("""
        SELECT COALESCE(AVG(durationMillis), 0) 
        FROM app_analytics 
        WHERE sessionId = :sessionId 
        AND screenName = :screenName 
        AND exitTime IS NOT NULL
    """)
    suspend fun getAverageTimeOnScreenInSession(sessionId: String, screenName: String): Long
}