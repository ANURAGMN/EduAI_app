package com.ncert7.aitutorandlab.service.ads

import android.content.Context
import com.ncert7.aitutorandlab.config.AppConfig
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.data.local.database.EduAiDatabase
import com.ncert7.aitutorandlab.debug.DebugLogger
import java.time.LocalDate
import java.time.ZoneId

object ClickAdGate {

    private const val TAG = "ClickAdGate"

    private lateinit var database: EduAiDatabase
    private lateinit var sharedPrefs: SharedPreferenceUtils

    fun initialize(context: Context) {
        database = EduAiDatabase.getInstance(context)
        sharedPrefs = SharedPreferenceUtils(context)
    }

    suspend fun getTodayClickCount(): Int {
        val studentId = sharedPrefs.getUserId().orEmpty()
        if (studentId.isEmpty()) return 0
        val (startOfDay, endOfDay) = todayBounds()
        return database.appAnalyticsDao().getTodayClickCount(
            studentId = studentId,
            startOfDay = startOfDay,
            endOfDay = endOfDay,
            appName = AppConfig.APP_NAME
        )
    }

    suspend fun shouldShowAdBeforeNextClick(): Boolean {
        val count = getTodayClickCount()
        val show = ClickAdPolicy.shouldShowAd(count)
        DebugLogger.debugLog(TAG, "Clicks today: $count, showAd=$show")
        return show
    }

    private fun todayBounds(): Pair<Long, Long> {
        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val endOfDay = LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() - 1
        return startOfDay to endOfDay
    }
}
