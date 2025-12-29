package com.anurag.eduai.repository

import com.anurag.eduai.data.local.dao.SessionDao
import com.anurag.eduai.data.local.entities.SessionEntity
import com.anurag.eduai.debug.DebugLogger
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SessionRepository(
    private val sessionDao: SessionDao
) {

    suspend fun createSession(): SessionEntity {
        val session = SessionEntity(
            sessionId = UUID.randomUUID().toString(),
            sessionDate = getCurrentDate(),
            sessionStartTime = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )

        sessionDao.insertSession(session)
        DebugLogger.debugLog("SessionRepository", "Session created: ${session.sessionId}")
        return session
    }

    suspend fun endSession(sessionId: String) {
        val session = sessionDao.getSession(sessionId) ?: return

        val endTime = System.currentTimeMillis()
        val updatedSession = session.copy(
            sessionEndTime = endTime,
            durationMillis = endTime - session.sessionStartTime
        )

        sessionDao.updateSession(updatedSession)
        DebugLogger.debugLog("SessionRepository", "Session ended: $sessionId, Duration: ${updatedSession.durationMillis}ms")
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}