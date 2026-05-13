package com.ncert7.aitutorandlab.service.sync

import android.content.Context
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.data.local.database.EduAiDatabase
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.utils.NetworkConnectivityObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Service to manage data synchronization between local Room database and Firestore.
 * Handles real-time triggers and monitors network for offline-to-online sync.
 */
object DataSyncService {
    private const val TAG = "DataSyncService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isInitialized = false

    private lateinit var database: EduAiDatabase
    private lateinit var sharedPrefs: SharedPreferenceUtils
    private lateinit var connectivityObserver: NetworkConnectivityObserver

    /**
     * Initialize the sync service
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        database = EduAiDatabase.getInstance(context)
        sharedPrefs = SharedPreferenceUtils(context)
        connectivityObserver = NetworkConnectivityObserver.getInstance(context)
        
        isInitialized = true
        
        // Start observing connectivity to trigger sync when coming online
        observeConnectivity(context)
        
        DebugLogger.debugLog(TAG, "DataSyncService initialized")
    }

    /**
     * Monitors network status and triggers sync when internet becomes available
     */
    private fun observeConnectivity(context: Context) {
        serviceScope.launch {
            connectivityObserver.isOnline.collectLatest { isOnline ->
                if (isOnline) {
                    DebugLogger.debugLog(TAG, "Device is online, triggering background sync")
                    triggerFullSync()
                }
            }
        }
    }

    /**
     * Trigger a background sync immediately
     */
    fun triggerFullSync() {
        val userId = sharedPrefs.getUserId() ?: return
        
        serviceScope.launch {
            try {
                DebugLogger.debugLog(TAG, "Triggering real-time sync for user: $userId")
                
                val syncManager = ProgressAnalyticsSyncManager(
                    progressDao = database.progressDao(),
                    analyticsDao = database.appAnalyticsDao(),
                    sessionDao = database.sessionDao(),
                    streakDao = database.streakDao(),
                    chapterProgressDao = database.chapterAgentProgressDao(),
                    studentId = userId
                )
                
                val result = syncManager.syncAllUnsyncedData()
                DebugLogger.debugLog(TAG, "Real-time sync result: ${result.message}")
            } catch (e: Exception) {
                DebugLogger.errorLog(TAG, "Real-time sync error: ${e.message}")
            }
        }
    }

    /**
     * Manually trigger a restoration of user data from Firestore.
     * Useful after login.
     */
    suspend fun restoreUserData(context: Context, studentId: String) {
        try {
            DebugLogger.debugLog(TAG, "Starting user data restoration for: $studentId")
            
            val contentSyncManager = FirebaseSyncManager(
                subjectDao = database.subjectDao(),
                chapterDao = database.chapterDao(),
                conceptDao = database.conceptDao(),
                progressDao = database.progressDao(),
                streakDao = database.streakDao(),
                chapterProgressDao = database.chapterAgentProgressDao(),
                context = context
            )
            
            contentSyncManager.syncUserProgress(studentId)
            contentSyncManager.syncUserStreak(studentId)
            contentSyncManager.syncChapterAgentProgress(studentId)
            
            DebugLogger.debugLog(TAG, "User data restoration complete")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Data restoration failed: ${e.message}")
        }
    }

    /**
     * Shutdown service and cleanup
     */
    fun shutdown() {
        // No-op for now as it's an object, but kept for future use
    }
}
