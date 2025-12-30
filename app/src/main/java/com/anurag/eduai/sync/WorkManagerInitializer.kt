package com.anurag.eduai.sync

import android.content.Context
import androidx.startup.Initializer
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * App Startup Initializer that schedules the Weekly Sync Worker.
 * Ensures the worker is registered once when the app starts.
 */
class WorkManagerInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val weeklyRequest =
            PeriodicWorkRequestBuilder<WeeklySyncWorker>(7, TimeUnit.DAYS)
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WEEKLY_SYNC_WORK",
            ExistingPeriodicWorkPolicy.KEEP,
            weeklyRequest
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
