package com.anurag.eduai

import android.app.Application
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.SessionRepository
import com.anurag.eduai.utils.AppLifecycleObserver

class EduAiApplication : Application() {

    private lateinit var appLifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        DebugLogger.debugLog("EduAiApplication", "Application onCreate")

        // Initialize database and repository
        val database = EduAiDatabase.getInstance(this)
        val sessionRepository = SessionRepository(database.sessionDao())

        // Register app lifecycle observer
        appLifecycleObserver = AppLifecycleObserver(sessionRepository)
        appLifecycleObserver.register()

        DebugLogger.debugLog("EduAiApplication", "AppLifecycleObserver registered")
    }
}