package com.ncert7.aitutorandlab.domain.chatbot.controller

import com.ncert7.aitutorandlab.debug.DebugLogger
import kotlinx.coroutines.*
import javax.inject.Inject

class IdleTimerController @Inject constructor() {
    private var idleJob: Job? = null
    private var userActivityJob: Job? = null

    fun startIdleTimer(
        scope: CoroutineScope,
        delayMs: Long = 5000L,
        onIdle: () -> Unit
    ) {
        idleJob?.cancel()
        DebugLogger.debugLog("IdleTimerController", " Starting idle timer for ${delayMs}ms")
        idleJob = scope.launch {
            delay(delayMs)
            DebugLogger.debugLog("IdleTimerController", "Idle timer completed - triggering autosuggestions")
            onIdle()
        }
    }

    fun cancelIdleTimer() {
        DebugLogger.debugLog("IdleTimerController", " Cancelling idle timer")
        idleJob?.cancel()
    }

    fun markUserActive(
        scope: CoroutineScope,
        onActive: () -> Unit,
        onInactive: () -> Unit,
        inactivityDelayMs: Long = 2000L
    ) {
        onActive()
        userActivityJob?.cancel()
        userActivityJob = scope.launch {
            delay(inactivityDelayMs)
            onInactive()
        }
    }

    fun markUserInactive(
        scope: CoroutineScope,
        onInactive: () -> Unit,
        delayMs: Long = 500L
    ) {
        userActivityJob?.cancel()
        scope.launch {
            delay(delayMs)
            onInactive()
        }
    }

    fun cancel() {
        idleJob?.cancel()
        userActivityJob?.cancel()
    }
}