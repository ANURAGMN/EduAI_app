package com.anurag.eduai.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppLifecycleObserver(
    private val sessionRepository: SessionRepository
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentSessionId: String? = null

    fun register() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        DebugLogger.debugLog("AppLifecycleObserver", "Registered")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        DebugLogger.debugLog("AppLifecycleObserver", "App → Foreground")

        // Start new session if none exists
        if (currentSessionId == null) {
            scope.launch {
                try {
                    val session = sessionRepository.createSession()
                    currentSessionId = session.sessionId
                    DebugLogger.debugLog("AppLifecycleObserver", " Session started: ${session.sessionId}")
                } catch (e: Exception) {
                    DebugLogger.errorLog("AppLifecycleObserver", "Error starting session: ${e.message}")
                }
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        DebugLogger.debugLog("AppLifecycleObserver", "App → Background")

        // End current session if exists
        currentSessionId?.let { sessionId ->
            scope.launch {
                try {
                    sessionRepository.endSession(sessionId)
                    DebugLogger.debugLog("AppLifecycleObserver", " Session ended: $sessionId")
                    currentSessionId = null
                } catch (e: Exception) {
                    DebugLogger.errorLog("AppLifecycleObserver", "Error ending session: ${e.message}")
                }
            }
        }
    }
}