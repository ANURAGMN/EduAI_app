package com.ncert7.aitutorandlab.ui.screens.conceptscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.ncert7.aitutorandlab.R
import com.ncert7.aitutorandlab.ui.screens.conceptscreen.viewmodel.ConceptSimulationViewModel
import com.ncert7.aitutorandlab.ui.screens.simulation_agent.components.SimulationWebView
import com.ncert7.aitutorandlab.ui.theme.HeaderGradientStart
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.TextOnPrimary

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
    val dimens = LocalDimensions.current
    // Prevent double-marking if the WebView fires onPageFinished multiple times
    var progressMarked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HeaderGradientStart)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = TextOnPrimary,
                    modifier = Modifier.size(dimens.iconMedium)
                )
            }

            Text(
                text = simulationTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextOnPrimary,
                modifier = Modifier.weight(1f)
            )
        }


        // WebView
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            SimulationWebView(
                url = simulationUrl,
                modifier = Modifier.fillMaxSize(),
                onPageFinished = {
                    // Mark simulation URL completed once when page loads (only if conceptId known)
                    if (conceptId.isNotBlank() && !progressMarked) {
                        progressMarked = true
                        viewModel.markSimulationUrlCompleted(conceptId)
                    }
                }
            )
        }
    }
}
