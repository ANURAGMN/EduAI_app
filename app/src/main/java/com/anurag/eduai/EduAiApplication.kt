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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class EduAiApplication : Application(), Configuration.Provider{

    private lateinit var appLifecycleObserver: AppLifecycleObserver
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        DebugLogger.debugLog("EduAiApplication", "Application onCreate")

        // Initialize SessionManager (handles both sessions and analytics)
        SessionManager.initialize(this)

        // Register app lifecycle observer (this will handle session start/end)
        appLifecycleObserver = AppLifecycleObserver()
        appLifecycleObserver.register()

        // Start initial session
        applicationScope.launch {
            SessionManager.startSession()
            DebugLogger.debugLog("EduAiApplication", "AppLifecycleObserver registered and initial session started")
        }

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