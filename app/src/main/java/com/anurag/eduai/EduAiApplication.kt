package com.anurag.eduai

import android.app.Application
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.SessionManager
import com.anurag.eduai.utils.AppLifecycleObserver

class EduAiApplication : Application() {

    private lateinit var appLifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        DebugLogger.debugLog("EduAiApplication", "Application onCreate")

        // Initialize SessionManager (handles both sessions and analytics)
        SessionManager.initialize(this)

        // Register app lifecycle observer
        appLifecycleObserver = AppLifecycleObserver()
        appLifecycleObserver.register()

        DebugLogger.debugLog("EduAiApplication", "AppLifecycleObserver registered")
    }
}