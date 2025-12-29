package com.anurag.eduai.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.ConceptRepository
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Weekly background worker responsible for syncing new Firebase data
 * into the local Room database.
 *
 * Runs once a week using WorkManager periodic request.
 */
class WeeklySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = EduAiDatabase.getInstance(applicationContext)
            val conceptDao = db.conceptDao()

            val repo = ConceptRepository(
                firestore = FirebaseFirestore.getInstance(),
                conceptDao = conceptDao,
                sharedPreferenceUtils = SharedPreferenceUtils(applicationContext)
            )

            repo.syncWeekly()

            Result.success()
        } catch (e: Exception) {
            DebugLogger.debugLog("WeeklySyncWorker", "Error: \n $e")
            Result.retry()
        }
    }
}
