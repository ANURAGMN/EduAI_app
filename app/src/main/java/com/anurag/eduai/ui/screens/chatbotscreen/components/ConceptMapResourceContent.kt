package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.GraphData
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.util.Locale

/**
 * ConceptMapResourceContent - Wrapper that handles progressive rendering
 *
 * If isAudioPlaying=true but no actual TTS is playing, this component
 * will simulate audio timing to enable progressive node/edge reveal
 */
@Composable
fun ConceptMapResourceContent(
    json: String,
    currentAudioTime: Float,
    isAudioPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val TAG = "ConceptMapResourceContent"

    // Track loading state
    var isLoading by remember(json) { mutableStateOf(true) }

    // Parse to get total duration from audioSegments
    val totalDuration = remember(json) {
        try {
            val graphData = Json.decodeFromString<GraphData>(json)

            // Calculate total duration by summing all estimatedDuration values
            val duration = graphData.audioSegments.sumOf { it.estimatedDuration.toDouble() }.toFloat()

            DebugLogger.debugLog(TAG, "Parsed concept map with ${graphData.audioSegments.size} segments")
            DebugLogger.debugLog(TAG, "Total duration: ${duration}s (calculated from estimatedDuration)")
            isLoading = false
            duration
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Failed to parse audioSegments: ${e.message}")
            isLoading = false
            0f
        }
    }

    // Simulated audio time for progressive rendering (without actual TTS audio)
    var simulatedAudioTime by remember(json) { mutableFloatStateOf(0f) }

    // Auto-start simulated playback when concept map loads
    LaunchedEffect(json, isAudioPlaying) {
        if (isAudioPlaying && totalDuration > 0f) {
            simulatedAudioTime = 0f

            DebugLogger.debugLog(TAG, "═══════════════════════════════════════════════════════")
            DebugLogger.debugLog(TAG, "Starting progressive rendering animation")
            DebugLogger.debugLog(TAG, "Total duration: ${totalDuration}s")
            DebugLogger.debugLog(TAG, "Initial time: ${simulatedAudioTime}s")
            DebugLogger.debugLog(TAG, "═══════════════════════════════════════════════════════")

            val startTime = System.currentTimeMillis()

            while (simulatedAudioTime < totalDuration) {
                delay(50) // Update every 50ms for smooth animation
                val elapsed = (System.currentTimeMillis() - startTime) / 1000f
                simulatedAudioTime = elapsed.coerceAtMost(totalDuration)

                // Log every second for debugging
                if (elapsed.toInt() != (elapsed - 0.05f).toInt()) {
                    DebugLogger.debugLog(TAG, "Animation progress: ${String.format(Locale.US, "%.2f", simulatedAudioTime)}s / ${totalDuration}s")
                }
            }

            DebugLogger.debugLog(TAG, "═══════════════════════════════════════════════════════")
            DebugLogger.debugLog(TAG, "Progressive rendering completed!")
            DebugLogger.debugLog(TAG, "Final time: ${simulatedAudioTime}s")
            DebugLogger.debugLog(TAG, "═══════════════════════════════════════════════════════")
        } else {
            DebugLogger.debugLog(TAG, "Progressive rendering disabled (isAudioPlaying=$isAudioPlaying, totalDuration=${totalDuration}s)")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            // Show loading indicator with message while parsing
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
                Text(
                    text = stringResource(R.string.loading_concept_map),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // Render the concept map with timing
            ConceptMapModel(
                json = json,
                currentAudioTime = if (isAudioPlaying && totalDuration > 0f) simulatedAudioTime else currentAudioTime,
                isAudioPlaying = isAudioPlaying && totalDuration > 0f
            )
        }
    }
}