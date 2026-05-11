package com.anurag.eduai

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.SessionManager
import com.anurag.eduai.service.sync.DataSyncService
import com.anurag.eduai.service.sync.WeeklySyncWorker
import com.anurag.eduai.utils.AppLifecycleObserver
import com.anurag.eduai.utils.LanguageHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class EduAiApplication : Application(), Configuration.Provider {

    private lateinit var appLifecycleObserver: AppLifecycleObserver
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        DebugLogger.debugLog("EduAiApplication", "Application onCreate")

        // Initialize language preference from SharedPreferences
        initializeLanguage()

        // Initialize DataSyncService for real-time and offline sync
        DataSyncService.initialize(this)

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

        scheduleDailySync()
    }

    private fun initializeLanguage() {
        try {
            val sharedPref = SharedPreferenceUtils(this)
            val savedLanguage = sharedPref.getLanguagePreference() ?: "en"
            LanguageHelper.setLanguage(savedLanguage)
            DebugLogger.debugLog("EduAiApplication", "Language initialized to: $savedLanguage")
        } catch (e: Exception) {
            DebugLogger.debugLog("EduAiApplication", "Error initializing language: ${e.message}")
        }
    }

    private fun scheduleDailySync() {
        val request =
            PeriodicWorkRequestBuilder<WeeklySyncWorker>(
                1, TimeUnit.DAYS
            )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, // Initial delay
                    TimeUnit.MINUTES
                )
                .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "DAILY_SYNC_WORK",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )

        DebugLogger.debugLog("EduAiApplication", "Daily sync worker scheduled with exponential backoff retry ")
    }

    /**
     * Cleanup resources when app is terminated
     */
    override fun onTerminate() {
        super.onTerminate()
        try {
            // Shutdown DataSyncService
            // DataSyncService.shutdown() // Not strictly needed for object, but following structure
            // Unregister app lifecycle observer
            // appLifecycleObserver.unregister() // AppLifecycleObserver.kt currently doesn't have unregister, only register
            DebugLogger.debugLog("EduAiApplication", " Application terminated and cleaned up")
        } catch (e: Exception) {
            DebugLogger.errorLog("EduAiApplication", " Error during cleanup: ${e.message}")
        }
    }
}