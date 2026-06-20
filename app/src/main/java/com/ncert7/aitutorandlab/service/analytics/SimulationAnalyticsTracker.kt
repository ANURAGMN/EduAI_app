package com.ncert7.aitutorandlab.service.analytics

import android.content.Context
import com.ncert7.aitutorandlab.debug.DebugLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Tracks per-simulation engagement (clicks, completions) to local DB, Firestore, and GA4.
 */
object SimulationAnalyticsTracker {

    private const val TAG = "SimulationAnalytics"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize(context: Context) {
        AnalyticsEventRecorder.initialize(context)
        FirebaseAnalyticsHelper.initialize(context)
    }

    fun trackSimulationClick(
        conceptId: String,
        interaction: SimulationInteraction,
        source: ClickSource
    ) {
        if (conceptId.isBlank() || conceptId == "empty") return
        scope.launch {
            trackSimulationClickAndWait(conceptId, interaction, source)
        }
    }

    suspend fun trackSimulationClickAndWait(
        conceptId: String,
        interaction: SimulationInteraction,
        source: ClickSource
    ) {
        if (conceptId.isBlank() || conceptId == "empty") return
        AnalyticsEventRecorder.recordClick(
            screenName = "SIMULATION",
            itemId = conceptId,
            source = source.value,
            interactionType = interaction.value
        )
        FirebaseAnalyticsHelper.logSimulationClick(conceptId, source, interaction)
        DebugLogger.debugLog(
            TAG,
            "Click: conceptId=$conceptId, interaction=${interaction.value}, source=${source.value}"
        )
    }

    fun trackSimulationComplete(
        conceptId: String,
        interaction: SimulationInteraction
    ) {
        if (conceptId.isBlank() || conceptId == "empty") return
        scope.launch {
            AnalyticsEventRecorder.recordClick(
                screenName = "SIMULATION",
                itemId = conceptId,
                source = null,
                interactionType = interaction.value,
                eventType = EventType.COMPLETE.type
            )
            FirebaseAnalyticsHelper.logSimulationComplete(conceptId, interaction)
            DebugLogger.debugLog(
                TAG,
                "Complete: conceptId=$conceptId, interaction=${interaction.value}"
            )
        }
    }
}
