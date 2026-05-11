package com.anurag.eduai.service.sync

import com.anurag.eduai.config.AppConfig
import com.anurag.eduai.data.local.dao.AppAnalyticsDao
import com.anurag.eduai.data.local.dao.ChapterAgentProgressDao
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.dao.SessionDao
import com.anurag.eduai.data.local.dao.StreakDao
import com.anurag.eduai.debug.DebugLogger
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Manages real-time and offline sync of Progress, Analytics, Sessions, Streaks, 
 * and ChapterAgentProgress data to Firestore.
 * Ensures data isolation between different apps using the same Firebase project via AppConfig.APP_NAME.
 */
class ProgressAnalyticsSyncManager(
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
}


