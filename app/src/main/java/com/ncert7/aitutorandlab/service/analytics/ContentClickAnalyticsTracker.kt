package com.ncert7.aitutorandlab.service.analytics

import android.content.Context
import com.ncert7.aitutorandlab.debug.DebugLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Tracks non-simulation content taps (lessons, chapters, subjects, revision, math).
 */
object ContentClickAnalyticsTracker {

    private const val TAG = "ContentAnalytics"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize(context: Context) {
        AnalyticsEventRecorder.initialize(context)
        FirebaseAnalyticsHelper.initialize(context)
    }

    fun trackClick(
        itemId: String,
        contentType: ContentClickType,
        source: ClickSource
    ) {
        if (itemId.isBlank()) return
        scope.launch {
            trackClickAndWait(itemId, contentType, source)
        }
    }

    suspend fun trackClickAndWait(
        itemId: String,
        contentType: ContentClickType,
        source: ClickSource
    ) {
        if (itemId.isBlank()) return
        AnalyticsEventRecorder.recordClick(
            screenName = "CONTENT",
            itemId = itemId,
            source = source.value,
            interactionType = contentType.value
        )
        FirebaseAnalyticsHelper.logContentClick(itemId, contentType, source)
        DebugLogger.debugLog(
            TAG,
            "Click: itemId=$itemId, type=${contentType.value}, source=${source.value}"
        )
    }
}
