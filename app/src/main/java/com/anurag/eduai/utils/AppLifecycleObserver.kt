package com.anurag.eduai.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppLifecycleObserver(
) : DefaultLifecycleObserver {

    fun register() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        DebugLogger.debugLog("AppLifecycleObserver", "Registered")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        DebugLogger.debugLog("AppLifecycleObserver", "App → Foreground")
        SessionManager.startSession()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        DebugLogger.debugLog("AppLifecycleObserver", "App → Background")
        SessionManager.endSession()
    }
}