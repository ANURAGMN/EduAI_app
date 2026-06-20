package com.ncert7.aitutorandlab.ui.screens.conceptscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.service.analytics.ScreenName
import com.ncert7.aitutorandlab.service.analytics.TrackScreenEvent
import com.ncert7.aitutorandlab.ui.screens.conceptscreen.viewmodel.ConceptSimulationViewModel
import com.ncert7.aitutorandlab.ui.screens.simulation_agent.components.SimulationWebView
import java.net.URLDecoder

/**
 * ConceptSimulationViewer displays a simulation in a WebView for the concept screen.
 *
 * When the WebView finishes loading, it marks the simulation URL as completed
 * via [ConceptSimulationViewModel] so that chapter progress is updated.
 *
 * @param simulationUrl The HTML file url to load
 * @param simulationTitle The title of the simulation
 * @param conceptId The concept whose progress should be updated when page loads (empty = skip tracking)
 * @param onBackClick Callback function to be invoked when the back button is clicked
 */
@Composable
fun ConceptSimulationViewer(
    simulationUrl: String,
    simulationTitle: String,
    conceptId: String = "",
    onBackClick: () -> Unit = {},
    viewModel: ConceptSimulationViewModel = hiltViewModel()
) {
    val decodedConceptId = remember(conceptId) {
        try {
            URLDecoder.decode(conceptId, "UTF-8")
        } catch (e: Exception) {
            conceptId
        }
    }

    TrackScreenEvent(
        screenName = ScreenName.SIMULATIONVIEWER,
        conceptId = decodedConceptId
    )

    val isInitPending by viewModel.isAdCheckPending.collectAsState()

    // Decode URL-encoded title to show original name (URL stays encoded for web requests)
    val decodedTitle = try {
        URLDecoder.decode(simulationTitle, "UTF-8")
    } catch (e: Exception) {
        simulationTitle
    }

    val decodedUrl = try {
        URLDecoder.decode(simulationUrl, "UTF-8")
    } catch (e: Exception) {
        simulationUrl
    }

    // Prevent double-marking if the WebView fires onPageFinished multiple times
    var progressMarked by remember { mutableStateOf(false) }

    LaunchedEffect(decodedConceptId, decodedUrl, decodedTitle) {
        if (decodedConceptId.isNotEmpty() && decodedUrl.isNotEmpty() && decodedTitle.isNotEmpty()) {
            DebugLogger.debugLog(
                "ConceptSimulationViewer",
                "LaunchedEffect: Initializing viewer for conceptId=$decodedConceptId"
            )
            viewModel.initializeSimulationWithAdCheck(
                conceptId = decodedConceptId,
                simulationUrl = decodedUrl,
                simulationTitle = decodedTitle
            )
        }
    }
    // Handle page loaded
    val handlePageLoaded = {
        if (decodedConceptId.isNotEmpty() && decodedConceptId != "empty" && !progressMarked) {
            progressMarked = true
            viewModel.markSimulationCompleted(decodedConceptId)
            DebugLogger.debugLog(
                "ConceptSimulationViewer",
                "Simulation page loaded for concept: $decodedConceptId"
            )
        }
    }

    val handleBackClick = {
        onBackClick()
    }

    when {
        isInitPending -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            SimulationHeader(
                title = decodedTitle,
                onBackClick = handleBackClick
            )

            // WebView
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                SimulationWebView(
                    url = decodedUrl,
                    onPageFinished = handlePageLoaded
                )
            }
        }
    }
}
