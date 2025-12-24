package com.anurag.eduai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anurag.eduai.data.local.entities.SessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing study sessions in the local database.
 */
@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<SessionEntity>)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    suspend fun getSession(sessionId: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    fun getSessionFlow(sessionId: String): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE studentId = :studentId AND sessionDate = :date")
    suspend fun getSessionsForDate(studentId: String, date: String): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE studentId = :studentId AND sessionDate = :date")
    fun getSessionsForDateFlow(studentId: String, date: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE studentId = :studentId ORDER BY sessionStartTime DESC LIMIT 1")
    suspend fun getLatestSession(studentId: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE studentId = :studentId ORDER BY sessionStartTime DESC")
    fun getAllSessionsForStudent(studentId: String): Flow<List<SessionEntity>>

    // Get average session duration
    @Query("SELECT AVG(durationMillis) FROM sessions WHERE studentId = :studentId AND durationMillis > 0")
    suspend fun getAverageDuration(studentId: String): Long?

    // Get all durations for percentile calculation (p50, p90)
    @Query("SELECT durationMillis FROM sessions WHERE studentId = :studentId AND durationMillis > 0 ORDER BY durationMillis ASC")
    suspend fun getAllDurationsForPercentile(studentId: String): List<Long>

    @Query("SELECT COUNT(*) FROM sessions WHERE studentId = :studentId AND sessionDate = :date")
    suspend fun getSessionCountForDate(studentId: String, date: String): Int

    @Query("SELECT * FROM sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<SessionEntity>

    @Query("UPDATE sessions SET isSynced = 1 WHERE sessionId = :sessionId")
    suspend fun markSessionAsSynced(sessionId: String)

    @Query("DELETE FROM sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

}