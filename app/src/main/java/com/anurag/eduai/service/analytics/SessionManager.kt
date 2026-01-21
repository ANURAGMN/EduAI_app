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
import kotlinx.coroutines.runBlocking
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
     * Start a new session - always creates a fresh session
     * If an old session exists in SharedPrefs, it ends it first before creating new one
     */
    fun startSession() {
        // Check if we already have an active session
        val existingActiveSessionId = currentSessionId
        if (existingActiveSessionId != null) {
            DebugLogger.debugLog("SessionManager", "Session already active: $existingActiveSessionId, skipping duplicate creation")
            return
        }

        // Use runBlocking to ensure old session is properly closed before starting new one
        runBlocking {
            try {
                // Check if there's an old session that wasn't properly ended
                val existingSessionId = sharedPrefs?.getCurrentSession()
                if (existingSessionId != null) {
                    DebugLogger.debugLog("SessionManager", "Found old session: $existingSessionId, ending it before starting new one")

                    val oldSession = database?.sessionDao()?.getSession(existingSessionId)
                    if (oldSession != null && oldSession.sessionEndTime == null) {
                        // Close all active analytics for the old session
                        val activeAnalytics = database?.appAnalyticsDao()?.getAnalyticsForSession(existingSessionId)
                            ?.filter { it.exitTime == null }

                        activeAnalytics?.forEach { analytics ->
                            val exitTime = System.currentTimeMillis()
                            val duration = exitTime - analytics.entryTime
                            database?.appAnalyticsDao()?.updateAnalyticsExit(
                                analyticsId = analytics.analyticsId,
                                eventType = EventType.EXIT.type,
                                exitTime = exitTime,
                                durationMillis = duration
                            )
                            DebugLogger.debugLog("SessionManager", "Auto-closed old session screen: ${analytics.screenName}")
                        }

                        // Session wasn't properly ended, close it now
                        val endTime = System.currentTimeMillis()
                        val updatedSession = oldSession.copy(
                            sessionEndTime = endTime,
                            durationMillis = endTime - oldSession.sessionStartTime
                        )
                        database?.sessionDao()?.updateSession(updatedSession)
                        DebugLogger.debugLog("SessionManager", "Old session ended: $existingSessionId, Duration: ${updatedSession.durationMillis}ms")
                    }
                }
            } catch (e: Exception) {
                DebugLogger.debugLog("SessionManager", "Error ending old session: ${e.message}")
            }
        }

        // Generate NEW session ID (synchronously) to avoid race conditions
        val sessionId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()
        currentSessionId = sessionId
        sharedPrefs?.setCurrentSession(sessionId)

        DebugLogger.debugLog("SessionManager", "New session started: $sessionId at $startTime")

        // Save to database asynchronously
        scope.launch {
            try {
                val session = SessionEntity(
                    sessionId = sessionId,
                    sessionDate = getCurrentDate(),
                    sessionStartTime = startTime,
                    syncAt = System.currentTimeMillis()
                )

                database?.sessionDao()?.insertSession(session)
                DebugLogger.debugLog("SessionManager", "Session saved to DB: $sessionId")
            } catch (e: Exception) {
                DebugLogger.debugLog("SessionManager", "Error saving session to DB: ${e.message}")
            }
        }
    }

    /**
     * End the current session when app goes to background
     * Uses runBlocking to ensure all operations complete before app is killed
     */
    fun endSession() {
        runBlocking {
            try {
                val sessionId = currentSessionId ?: sharedPrefs?.getCurrentSession()

                if (sessionId == null) {
                    DebugLogger.debugLog("SessionManager", "No active session to end")
                    return@runBlocking
                }

                DebugLogger.debugLog("SessionManager", "Ending session: $sessionId")

                // First, close all active (non-exited) screen analytics
                val activeAnalytics = database?.appAnalyticsDao()?.getAnalyticsForSession(sessionId)
                    ?.filter { it.exitTime == null }

                if (!activeAnalytics.isNullOrEmpty()) {
                    val exitTime = System.currentTimeMillis()
                    activeAnalytics.forEach { analytics ->
                        val duration = exitTime - analytics.entryTime
                        database?.appAnalyticsDao()?.updateAnalyticsExit(
                            analyticsId = analytics.analyticsId,
                            eventType = EventType.EXIT.type,
                            exitTime = exitTime,
                            durationMillis = duration
                        )
                        DebugLogger.debugLog("SessionManager", "Auto-closed screen: ${analytics.screenName}, Duration: ${duration}ms")
                    }
                    DebugLogger.debugLog("SessionManager", "Closed ${activeAnalytics.size} active screen(s)")
                }

                // Then end the session
                val session = database?.sessionDao()?.getSession(sessionId)

                if (session != null) {
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - session.sessionStartTime
                    val updatedSession = session.copy(
                        sessionEndTime = endTime,
                        durationMillis = duration
                    )

                    database?.sessionDao()?.updateSession(updatedSession)
                    DebugLogger.debugLog("SessionManager", "Session ended: $sessionId, Duration: ${duration}ms (${duration/1000}s)")
                } else {
                    DebugLogger.debugLog("SessionManager", "Session not found in database: $sessionId")
                }

                // Clear session references
                currentSessionId = null
                sharedPrefs?.clearCurrentSession()

                DebugLogger.debugLog("SessionManager", "Session cleanup completed")
            } catch (e: Exception) {
                DebugLogger.debugLog("SessionManager", "Error ending session: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Track screen entry event - Creates a new analytics record
     */
    fun trackScreenEntry(screenName: ScreenName) {
        scope.launch {
            try {
                val sessionId = currentSessionId ?: sharedPrefs?.getCurrentSession() ?: run {
                    DebugLogger.debugLog("SessionManager", "No active session for tracking entry")
                    return@launch
                }

                val analytics = AppAnalyticsEntity(
                    sessionId = sessionId,
                    screenName = screenName.displayName,
                    eventType = EventType.ENTRY.type,
                    entryTime = System.currentTimeMillis(),
                    exitTime = null,
                    durationMillis = 0,
                    isSynced = false
                )

                database?.appAnalyticsDao()?.insertAnalytics(analytics)
                DebugLogger.debugLog("SessionManager", "Screen Entry: ${screenName.displayName}")
            } catch (e: Exception) {
                DebugLogger.debugLog("SessionManager", "Error tracking entry: ${e.message}")
            }
        }
    }

    /**
     * Track screen exit event - Updates the existing analytics record
     */
    fun trackScreenExit(screenName: ScreenName) {
        scope.launch {
            try {
                val sessionId = currentSessionId ?: sharedPrefs?.getCurrentSession() ?: run {
                    DebugLogger.debugLog("SessionManager", "No active session for tracking exit")
                    return@launch
                }

                // Find the active (non-exited) analytics record for this screen in current session
                val activeAnalytics = database?.appAnalyticsDao()
                    ?.getActiveAnalyticsForScreen(sessionId, screenName.displayName)

                if (activeAnalytics != null) {
                    val exitTime = System.currentTimeMillis()
                    val duration = exitTime - activeAnalytics.entryTime

                    database?.appAnalyticsDao()?.updateAnalyticsExit(
                        analyticsId = activeAnalytics.analyticsId,
                        eventType = EventType.EXIT.type,
                        exitTime = exitTime,
                        durationMillis = duration
                    )
                    DebugLogger.debugLog("SessionManager", "Screen Exit: ${screenName.displayName}, Duration: ${duration}ms")
                } else {
                    DebugLogger.debugLog("SessionManager", "No active analytics found for exit: ${screenName.displayName}")
                }
            } catch (e: Exception) {
                DebugLogger.debugLog("SessionManager", "Error tracking exit: ${e.message}")
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