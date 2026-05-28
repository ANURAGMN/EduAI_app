package com.ncert7.aitutorandlab.service.sync

import com.ncert7.aitutorandlab.config.AppConfig
import com.ncert7.aitutorandlab.data.local.dao.AppAnalyticsDao
import com.ncert7.aitutorandlab.data.local.dao.ChapterAgentProgressDao
import com.ncert7.aitutorandlab.data.local.dao.ProgressDao
import com.ncert7.aitutorandlab.data.local.dao.SessionDao
import com.ncert7.aitutorandlab.data.local.dao.StreakDao
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Manages real-time and offline sync of Progress, Analytics, Sessions, Streaks, 
 * and ChapterAgentProgress data to Firestore.
 * Ensures data isolation between different apps using the same Firebase project via AppConfig.APP_NAME.
 */
class ProgressAnalyticsSessionSyncManager(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val progressDao: ProgressDao,
    private val analyticsDao: AppAnalyticsDao,
    private val sessionDao: SessionDao,
    private val streakDao: StreakDao,
    private val chapterProgressDao: ChapterAgentProgressDao,
    private val studentId: String
) {
    companion object {
        private const val TAG = "ProgressAnalyticsSync"
        private const val PROGRESS_COLLECTION = "progress"
        private const val ANALYTICS_COLLECTION = "analytics"
        private const val SESSIONS_COLLECTION = "sessions"
        private const val STREAK_COLLECTION = "streak"
        private const val CHAPTER_PROGRESS_COLLECTION = "chapterprogress"
        private const val BATCH_SIZE = 100
    }

    // Unique document ID for the student in this specific app
    private val studentAppDocId = "${AppConfig.APP_NAME}_$studentId"

    /**
     * Syncs all unsynced data to Firestore
     */
    suspend fun syncAllUnsyncedData(): SyncResult {
        return try {
            DebugLogger.debugLog(TAG, "Starting full sync for student: $studentId (App: ${AppConfig.APP_NAME})")

            val progressResult = syncUnsyncedProgress()
            val analyticsResult = syncUnsyncedAnalytics()
            val sessionsResult = syncUnsyncedSessions()
            val streakResult = syncUnsyncedStreak()
            val chapterProgressResult = syncUnsyncedChapterProgress()

            val allSuccess = progressResult.success && analyticsResult.success && 
                             sessionsResult.success && streakResult.success && 
                             chapterProgressResult.success
            
            val message = """
                Progress: ${progressResult.message}
                Analytics: ${analyticsResult.message}
                Sessions: ${sessionsResult.message}
                Streak: ${streakResult.message}
                Chapter Progress: ${chapterProgressResult.message}
            """.trimIndent()

            SyncResult(success = allSuccess, message = message)
        } catch (e: Exception) {
            val errorMsg = "Full sync failed: ${e.message}"
            DebugLogger.errorLog(TAG, errorMsg)
            SyncResult(success = false, message = errorMsg)
        }
    }

    /**
     * Syncs unsynced progress records
     */
    private suspend fun syncUnsyncedProgress(): SyncResult {
        return try {
            val unsynced = progressDao.getUnsyncedProgress()
            if (unsynced.isEmpty()) return SyncResult(true, "No unsynced progress")

            unsynced.chunked(BATCH_SIZE).forEach { batch ->
                val firestoreBatch = firestore.batch()
                batch.forEach { progress ->
                    val docRef = firestore.collection(PROGRESS_COLLECTION)
                        .document(studentAppDocId)
                        .collection("records")
                        .document("${progress.itemType}_${progress.itemId}")
                    
                    val data = mapOf(
                        "progressId" to progress.progressId,
                        "studentId" to progress.studentId,
                        "itemType" to progress.itemType,
                        "itemId" to progress.itemId,
                        "status" to progress.status,
                        "progressPercentage" to progress.progressPercentage,
                        "startedAt" to progress.startedAt,
                        "completedAt" to progress.completedAt,
                        "lastAccessedAt" to progress.lastAccessedAt,
                        "updatedAt" to progress.updatedAt,
                        "appName" to AppConfig.APP_NAME,
                        "syncedAt" to System.currentTimeMillis()
                    )
                    firestoreBatch.set(docRef, data, SetOptions.merge())
                }
                firestoreBatch.commit().await()
            }

            progressDao.markProgressAsSynced(unsynced.map { it.progressId })
            SyncResult(true, "Synced ${unsynced.size} progress records")
        } catch (e: Exception) {
            SyncResult(false, "Progress sync error: ${e.message}")
        }
    }

    /**
     * Syncs unsynced analytics records
     */
    private suspend fun syncUnsyncedAnalytics(): SyncResult {
        return try {
            val unsynced = analyticsDao.getUnsyncedAnalytics()
            if (unsynced.isEmpty()) return SyncResult(true, "No unsynced analytics")

            unsynced.chunked(BATCH_SIZE).forEach { batch ->
                val firestoreBatch = firestore.batch()
                batch.forEach { analytics ->
                    val docRef = firestore.collection(ANALYTICS_COLLECTION)
                        .document(studentAppDocId)
                        .collection("events")
                        .document(analytics.analyticsId.toString())
                    
                    val data = mapOf(
                        "analyticsId" to analytics.analyticsId,
                        "studentId" to studentId,
                        "sessionId" to analytics.sessionId,
                        "screenName" to analytics.screenName,
                        "eventType" to analytics.eventType,
                        "entryTime" to analytics.entryTime,
                        "exitTime" to analytics.exitTime,
                        "durationMillis" to analytics.durationMillis,
                        "appName" to AppConfig.APP_NAME,
                        "syncedAt" to System.currentTimeMillis()
                    )
                    firestoreBatch.set(docRef, data, SetOptions.merge())
                }
                firestoreBatch.commit().await()
            }

            unsynced.forEach { analyticsDao.markAnalyticsAsSynced(it.analyticsId) }
            SyncResult(true, "Synced ${unsynced.size} analytics records")
        } catch (e: Exception) {
            SyncResult(false, "Analytics sync error: ${e.message}")
        }
    }

    /**
     * Syncs unsynced sessions
     */
    private suspend fun syncUnsyncedSessions(): SyncResult {
        return try {
            val unsynced = sessionDao.getUnsyncedSessions()
            if (unsynced.isEmpty()) return SyncResult(true, "No unsynced sessions")

            unsynced.chunked(BATCH_SIZE).forEach { batch ->
                val firestoreBatch = firestore.batch()
                batch.forEach { session ->
                    val docRef = firestore.collection(SESSIONS_COLLECTION)
                        .document(studentAppDocId)
                        .collection("records")
                        .document(session.sessionId)
                    
                    val data = mapOf(
                        "sessionId" to session.sessionId,
                        "studentId" to studentId,
                        "sessionDate" to session.sessionDate,
                        "sessionStartTime" to session.sessionStartTime,
                        "sessionEndTime" to session.sessionEndTime,
                        "durationMillis" to session.durationMillis,
                        "appName" to AppConfig.APP_NAME,
                        "syncedAt" to System.currentTimeMillis()
                    )
                    firestoreBatch.set(docRef, data, SetOptions.merge())
                }
                firestoreBatch.commit().await()
            }

            unsynced.forEach { sessionDao.markSessionAsSynced(it.sessionId) }
            SyncResult(true, "Synced ${unsynced.size} sessions")
        } catch (e: Exception) {
            SyncResult(false, "Session sync error: ${e.message}")
        }
    }

    /**
     * Syncs unsynced streak data
     */
    private suspend fun syncUnsyncedStreak(): SyncResult {
        return try {
            val streak = streakDao.getUnsyncedStreak() ?: return SyncResult(true, "No unsynced streak")

            val docRef = firestore.collection(STREAK_COLLECTION)
                .document(studentAppDocId)
                .collection("data")
                .document("current")
            
            val data = mapOf(
                "studentId" to studentId,
                "streakCount" to streak.streakCount,
                "lastStreakDate" to streak.lastStreakDate,
                "createdAt" to streak.createdAt,
                "updatedAt" to streak.updatedAt,
                "appName" to AppConfig.APP_NAME,
                "syncedAt" to System.currentTimeMillis()
            )
            
            docRef.set(data, SetOptions.merge()).await()
            streakDao.markStreakAsSynced(studentId)
            
            SyncResult(true, "Synced streak data")
        } catch (e: Exception) {
            SyncResult(false, "Streak sync error: ${e.message}")
        }
    }

    private suspend fun syncUnsyncedChapterProgress(): SyncResult {
        return try {
            val unsynced = chapterProgressDao.getUnsyncedProgress()
            DebugLogger.debugLog(TAG, "Found ${unsynced.size} unsynced chapter progress records for student: $studentId")
            if (unsynced.isEmpty()) return SyncResult(true, "No unsynced chapter progress")

            unsynced.chunked(BATCH_SIZE).forEach { batch ->
                val firestoreBatch = firestore.batch()
                batch.forEach { cp ->
                    val docRef = firestore.collection(CHAPTER_PROGRESS_COLLECTION)
                        .document(studentAppDocId)
                        .collection("records")
                        .document("${cp.chapterId}_${cp.language}")
                    
                    val data = mapOf(
                        "progressId" to cp.progressId,
                        "chapterId" to cp.chapterId,
                        "studentId" to studentId,
                        "language" to cp.language,
                        "studyPercentage" to cp.studyPercentage,
                        "simulationPercentage" to cp.simulationPercentage,
                        "revisionPercentage" to cp.revisionPercentage,
                        "overallPercentage" to cp.overallPercentage,
                        "status" to cp.status,
                        "createdAt" to cp.createdAt,
                        "updatedAt" to cp.updatedAt,
                        "completedAt" to cp.completedAt,
                        "appName" to AppConfig.APP_NAME,
                        "syncedAt" to System.currentTimeMillis()
                    )
                    firestoreBatch.set(docRef, data, SetOptions.merge())
                }
                firestoreBatch.commit().await()
            }

            chapterProgressDao.markAsSynced(unsynced.map { it.progressId })
            DebugLogger.debugLog(TAG, "Successfully uploaded ${unsynced.size} chapter progress records to Firestore")
            SyncResult(true, "Synced ${unsynced.size} chapter progress records")
        } catch (e: Exception) {
            SyncResult(false, "Chapter progress sync error: ${e.message}")
        }
    }



    /**
     * Syncs a single progress update to Firestore in real-time
     */
    @Throws(Exception::class)
    suspend fun syncProgressUpdate(progressId: Long, studentId: String): Boolean {
        return try {
            DebugLogger.debugLog (TAG, "Real-time sync triggered for progress: $progressId")

            // Try fetching from database directly by ID
            val allUnsyncedProgress = progressDao.getUnsyncedProgress()
            val progress = allUnsyncedProgress.find { it.progressId == progressId }

            if (progress != null) {
                val docRef = firestore
                    .collection(PROGRESS_COLLECTION)
                    .document(studentId)
                    .collection("records")
                    .document("${progress.itemType}_${progress.itemId}")

                val data = mapOf(
                    "progressId" to progress.progressId,
                    "studentId" to progress.studentId,
                    "itemType" to progress.itemType,
                    "itemId" to progress.itemId,
                    "status" to progress.status,
                    "progressPercentage" to progress.progressPercentage,
                    "startedAt" to progress.startedAt,
                    "completedAt" to progress.completedAt,
                    "lastAccessedAt" to progress.lastAccessedAt,
                    "updatedAt" to progress.updatedAt,
                    "appName" to progress.appName,
                    "syncedAt" to System.currentTimeMillis()
                )

                docRef.set(data).await()
                progressDao.markProgressAsSynced(listOf(progressId))
                DebugLogger.debugLog(TAG, " Progress synced to Firestore: $progressId, Type: ${progress.itemType}")
                true
            } else {
                DebugLogger.debugLog(TAG, " Progress not found or already synced: $progressId")
                false
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, " Progress real-time sync failed: ${e.message}")
            throw e
        }
    }

    /**
     * Syncs a single analytics update to Firestore in real-time
     */
    @Throws(Exception::class)
    suspend fun syncAnalyticsUpdate(analyticsId: Long): Boolean {
        return try {
            DebugLogger.debugLog(TAG, "Real-time sync triggered for analytics: $analyticsId")

            val allAnalytics = analyticsDao.getUnsyncedAnalytics()
            val analytics = allAnalytics.find { it.analyticsId == analyticsId }

            if (analytics != null) {
                val docRef = firestore
                    .collection(ANALYTICS_COLLECTION)
                    .document(studentId)
                    .collection("events")
                    .document(analytics.analyticsId.toString())

                val data = mapOf(
                    "analyticsId" to analytics.analyticsId,
                    "studentId" to studentId,
                    "sessionId" to analytics.sessionId,
                    "screenName" to analytics.screenName,
                    "eventType" to analytics.eventType,
                    "entryTime" to analytics.entryTime,
                    "exitTime" to analytics.exitTime,
                    "durationMillis" to analytics.durationMillis,
                    "appName" to analytics.appName,
                    "syncedAt" to System.currentTimeMillis()
                )

                docRef.set(data).await()
                analyticsDao.markAnalyticsAsSynced(analytics.analyticsId)
                DebugLogger.debugLog(TAG, " Analytics synced to Firestore: $analyticsId, Screen: ${analytics.screenName}")
                true
            } else {
                DebugLogger.debugLog(TAG, " Analytics not found or already synced: $analyticsId")
                false
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, " Analytics real-time sync failed: ${e.message}")
            throw e
        }
    }

    /**
     * Syncs a single session update to Firestore in real-time
     */
    @Throws(Exception::class)
    suspend fun syncSessionUpdate(sessionId: String): Boolean {
        return try {
            DebugLogger.debugLog(TAG, " Syncing session to Firebase: $sessionId")

            // IMPORTANT: Always read fresh data from database
            val session = sessionDao.getSession(sessionId)

            if (session != null) {
                val docRef = firestore
                    .collection(SESSIONS_COLLECTION)
                    .document(session.studentId)  // Use session's studentId, not sync manager's
                    .collection("records")
                    .document(session.sessionId)

                val data = mapOf(
                    "sessionId" to session.sessionId,
                    "studentId" to session.studentId,
                    "sessionDate" to session.sessionDate,
                    "sessionStartTime" to session.sessionStartTime,
                    "sessionEndTime" to session.sessionEndTime,
                    "durationMillis" to session.durationMillis,
                    "appName" to session.appName,
                    "syncedAt" to System.currentTimeMillis()
                )

                DebugLogger.debugLog(TAG, " Writing to Firebase: endTime=${session.sessionEndTime}, duration=${session.durationMillis}ms")

                // Use update() with merge semantics to properly update fields
                // If document doesn't exist, set it; if it exists, update only the specified fields
                try {
                    docRef.update(data).await()
                } catch (e: Exception) {
                    // If document doesn't exist yet (first sync), use set instead
                    if (e.message?.contains("No document to update") == true) {
                        DebugLogger.debugLog(TAG, " Document doesn't exist yet, creating with set()")
                        docRef.set(data).await()
                    } else {
                        throw e
                    }
                }

                // Only mark as synced AFTER successful Firebase write
                sessionDao.markSessionAsSynced(session.sessionId)

                DebugLogger.debugLog(TAG, " Session synced to Firebase: $sessionId, Duration: ${session.durationMillis}ms, EndTime: ${session.sessionEndTime}")
                true
            } else {
                DebugLogger.debugLog(TAG, " Session not found in database: $sessionId")
                false
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, " Session sync failed: ${e.message}")
            throw e
        }
    }

}


