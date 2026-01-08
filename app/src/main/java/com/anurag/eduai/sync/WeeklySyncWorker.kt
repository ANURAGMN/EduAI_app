package com.anurag.eduai.sync

import android.content.Context
import android.os.Build
import androidx.compose.runtime.rememberCoroutineScope
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.ConceptRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

/**
 * Weekly background worker responsible for syncing new Firebase data
 * into the local Room database.
 * Runs once a week using WorkManager periodic request.
 */
class WeeklySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {


    override suspend fun doWork(): Result {
        return try {

            val database = EduAiDatabase.getInstance(applicationContext)
            val syncManager = FirebaseSyncManager(
                subjectDao = database.subjectDao(),
                chapterDao = database.chapterDao(),
                conceptDao = database.conceptDao()
            )

            val result = syncManager.syncAllContent()
            if (result.success) {
                DebugLogger.debugLog("WeeklySync", "Successfully sync with firebase")
            }
            val now = Timestamp.now()

            FirebaseFirestore.getInstance()
                .collection("worker_test")
                .add(
                    mapOf(
                        "time" to now,
                        "device" to Build.MODEL
                    )
                )
                .await()

            DebugLogger.debugLog("WorkerTest", "Worker executed at $now")
            return Result.success()
        } catch (e: Exception) {
            DebugLogger.debugLog("WeeklySyncWorker", "Error: \n $e")
            Result.retry()
        }
    }
}
