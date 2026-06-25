package com.ncert7.aitutorandlab.service.analytics

import android.content.Context
import com.ncert7.aitutorandlab.debug.DebugLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Tracks onboarding funnel steps to local DB, Firestore, and GA4.
 * Pre-login events are backfilled with studentId on [DataSyncService.onUserAuthenticated].
 */
object FunnelAnalyticsTracker {

    private const val TAG = "FunnelAnalytics"
    private const val SCREEN_NAME = "FUNNEL"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize(context: Context) {
        AnalyticsEventRecorder.initialize(context)
        FirebaseAnalyticsHelper.initialize(context)
    }

    fun track(step: FunnelStep) {
        scope.launch {
            trackAndWait(step)
        }
    }

    suspend fun trackAndWait(step: FunnelStep) {
        AnalyticsEventRecorder.recordFunnelStep(
            screenName = SCREEN_NAME,
            step = step
        )
        FirebaseAnalyticsHelper.logFunnelStep(step)
        DebugLogger.debugLog(TAG, "Funnel: ${step.value}")
    }
}
