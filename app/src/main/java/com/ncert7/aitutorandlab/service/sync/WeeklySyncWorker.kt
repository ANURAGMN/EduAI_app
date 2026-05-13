package com.ncert7.aitutorandlab.service.sync

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.data.local.database.EduAiDatabase
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Weekly background worker responsible for syncing new Firebase data
 * into the local Room database and uploading unsynced user data.
 */
class WeeklySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = EduAiDatabase.getInstance(applicationContext)
            val sharedPrefs = SharedPreferenceUtils(applicationContext)
            val studentId = sharedPrefs.getUserId()

            // 1. Initialize Content Sync Manager
            val contentSyncManager = FirebaseSyncManager(
                subjectDao = database.subjectDao(),
                chapterDao = database.chapterDao(),
                conceptDao = database.conceptDao(),
                progressDao = database.progressDao(),
                streakDao = database.streakDao(),
                chapterProgressDao = database.chapterAgentProgressDao(),
                context = applicationContext
            )

            // 2. Sync all content (Subjects, Chapters, Concepts)
            val contentResult = contentSyncManager.syncAllContent()
            if (contentResult.success) {
                DebugLogger.debugLog("WeeklySync", "Content sync successful: ${contentResult.message}")
            }

            // 3. If user is logged in, sync/upload their progress data
            if (!studentId.isNullOrBlank()) {
                // A. Upload unsynced local data to Cloud
                val uploadManager = ProgressAnalyticsSyncManager(
                    progressDao = database.progressDao(),
                    analyticsDao = database.appAnalyticsDao(),
                    sessionDao = database.sessionDao(),
                    streakDao = database.streakDao(),
                    chapterProgressDao = database.chapterAgentProgressDao(),
                    studentId = studentId
                )
                val uploadResult = uploadManager.syncAllUnsyncedData()
                DebugLogger.debugLog("WeeklySync", "Data upload result: ${uploadResult.message}")

                // B. Restore any progress from Cloud (for cross-device sync)
                contentSyncManager.syncUserProgress(studentId)
                contentSyncManager.syncUserStreak(studentId)
                contentSyncManager.syncChapterAgentProgress(studentId)
            }

            // 4. Log worker execution for debugging
            val now = Timestamp.now()
            FirebaseFirestore.getInstance()
                .collection("worker_test")
                .add(
                    mapOf(
                        "time" to now,
                        "device" to Build.MODEL,
                        "studentId" to (studentId ?: "guest"),
                        "status" to "success"
                    )
                )
                .await()

            DebugLogger.debugLog("WeeklySync", "Worker executed successfully at $now")
            return Result.success()
        } catch (e: Exception) {
            DebugLogger.errorLog("WeeklySyncWorker", "Sync Error: ${e.message}")
            Result.retry()
        }
    }
}
