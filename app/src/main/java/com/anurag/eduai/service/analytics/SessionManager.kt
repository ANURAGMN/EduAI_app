package com.anurag.eduai.service.analytics

import android.content.Context
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.entities.AppAnalyticsEntity
import com.anurag.eduai.data.local.entities.SessionEntity
import com.anurag.eduai.debug.DebugLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Singleton manager for handling sessions and analytics tracking.
 * Provides a clean, centralized way to track app lifecycle and screen events.
 */
object SessionManager {

    private var database: EduAiDatabase? = null

    private var sharedPrefs: SharedPreferenceUtils? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var currentSessionId: String? = null

    /**
     * Initialize the SessionManager
     */
    fun initialize(context: Context) {
        database = EduAiDatabase.getInstance(context)
        sharedPrefs = SharedPreferenceUtils(context)
        DebugLogger.debugLog("SessionManager", "Initialized")
    }

    /**
     * Start a new session when app comes to foreground
     */
    fun startSession() {
        scope.launch {
            try {
                val sessionId = UUID.randomUUID().toString()
                val session = SessionEntity(
                    sessionId = sessionId,
                    sessionDate = getCurrentDate(),
                    sessionStartTime = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis()
                )

                database?.sessionDao()?.insertSession(session)
                currentSessionId = sessionId

                // Store session id in SharedPreferences
                sharedPrefs?.setCurrentSession(sessionId)

                DebugLogger.debugLog("SessionManager", "Session started: $sessionId")
            } catch (e: Exception) {
                DebugLogger.debugLog("SessionManager", "Error starting session: ${e.message}")
            }
        }
    }

    /**
     * End the current session when app goes to background
     */
    fun endSession() {
        scope.launch {
            try {
                val sessionId = currentSessionId ?: return@launch
                val session = database?.sessionDao()?.getSession(sessionId) ?: return@launch

                val endTime = System.currentTimeMillis()
                val updatedSession = session.copy(
                    sessionEndTime = endTime,
                    durationMillis = endTime - session.sessionStartTime
                )

                database?.sessionDao()?.updateSession(updatedSession)
                DebugLogger.debugLog("SessionManager", "Session ended: $sessionId, Duration: ${updatedSession.durationMillis}ms")

                currentSessionId = null
                // Clear session id from SharedPreferences
                sharedPrefs?.clearCurrentSession()
            } catch (e: Exception) {
                DebugLogger.debugLog("SessionManager", "Error ending session: ${e.message}")
            }
        }
    }

    /**
     * Track screen entry event
     */
    fun trackScreenEntry(screenName: ScreenName) {
        trackEvent(screenName, EventType.ENTRY)
    }

    /**
     * Track screen exit event
     */
    fun trackScreenExit(screenName: ScreenName) {
        trackEvent(screenName, EventType.EXIT)
    }

    /**
     * Internal method to track analytics events
     */
    private fun trackEvent(screenName: ScreenName, eventType: EventType) {
        scope.launch {
            try {
                val sessionId = currentSessionId ?: sharedPrefs?.getCurrentSession() ?: run {
                    DebugLogger.debugLog("SessionManager", "No active session for tracking event")
                    return@launch
                }

                val analytics = AppAnalyticsEntity(
                    sessionId = sessionId,
                    screenName = screenName.displayName,
                    eventType = eventType.type,
                    timestamp = System.currentTimeMillis(),
                    isSynced = false
                )

                database?.appAnalyticsDao()?.insertAnalytics(analytics)
                DebugLogger.debugLog("SessionManager", "Event tracked: ${screenName.displayName} - ${eventType.type}")
            } catch (e: Exception) {
                DebugLogger.debugLog("SessionManager", "Error tracking event: ${e.message}")
            }
        }
    }

    /**
     * Get the current active session ID
     */
    fun getCurrentSessionId(): String? {
        return currentSessionId ?: sharedPrefs?.getCurrentSession()
    }

    /**
     * Get current date in "yyyy-MM-dd" format
     */
    private fun getCurrentDate(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd",Locale.getDefault()).format(Date())
    }
}