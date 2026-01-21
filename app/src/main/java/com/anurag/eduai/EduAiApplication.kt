package com.anurag.eduai

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.SessionManager
import com.anurag.eduai.sync.WeeklySyncWorker
import com.anurag.eduai.utils.AppLifecycleObserver
import java.util.concurrent.TimeUnit

class EduAiApplication : Application(), Configuration.Provider{

    private lateinit var appLifecycleObserver: AppLifecycleObserver

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        DebugLogger.debugLog("EduAiApplication", "Application onCreate")

        // Initialize SessionManager (handles both sessions and analytics)
        SessionManager.initialize(this)

        // Start initial session (before any screen renders)
        SessionManager.startSession()

        // Register app lifecycle observer
        appLifecycleObserver = AppLifecycleObserver()
        appLifecycleObserver.register()

        DebugLogger.debugLog("EduAiApplication", "AppLifecycleObserver registered")
        scheduleWeeklySync()
    }

    private fun scheduleWeeklySync() {
        val request =
            PeriodicWorkRequestBuilder<WeeklySyncWorker>(
                1, TimeUnit.DAYS // testing
            ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "WEEKLY_SYNC_WORK",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )

        DebugLogger.debugLog("EduAiApplication", "Weekly sync worker scheduled")
    }

}