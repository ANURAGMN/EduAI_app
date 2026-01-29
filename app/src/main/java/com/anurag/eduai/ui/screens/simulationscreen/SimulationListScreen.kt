package com.anurag.eduai.ui.screens.simulationscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.R
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.SimulationRepository
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.simulationscreen.component.SimulationCard
import com.anurag.eduai.ui.screens.simulationscreen.component.SimulationScreenHeader
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.SimulationViewModel
import com.anurag.eduai.ui.viewmodel_factory.SimulationViewModelFactory

/**
 * SimulationListScreen displays a list of simulations for a given chapter.
 *
 * @param chapterId The ID of the chapter whose simulations are to be displayed
 * @param classLevel The class level (e.g., 7, 8, 9)
 * @param subjectName The name of the subject
 * @param chapterName The name of the chapter
 * @param onBackClick Callback function to be invoked when the back button is clicked
 * @param onSimulationClick Callback function to be invoked when a simulation is clicked (simulationId, htmlFileName, simulationTitle)
 * @param onGoHome Callback function to navigate to the home screen
 * @param onGoSetting Callback function to navigate to the settings screen
 */
@Composable
fun SimulationListScreen(
    chapterId: String,
    classLevel: Int,
    subjectName: String,
    chapterName: String,
    onBackClick: () -> Unit = {},
    onSimulationClick: (String, String, String) -> Unit = { _, _, _ -> },
    onGoHome: () -> Unit = {},
    onGoSetting: () -> Unit = {}
) {
    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.SIMULATIONLIST)

    val dimens = LocalDimensions.current

    // Create repository and ViewModel
    val simulationRepository = remember { SimulationRepository() }
    val factory = remember { SimulationViewModelFactory(simulationRepository) }
    val viewModel: SimulationViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    // Load simulations when chapterId changes
    LaunchedEffect(chapterId) {
        viewModel.loadSimulations(chapterId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        SimulationScreenHeader(
            classLevel = classLevel,
            subjectName = subjectName,
            chapterName = chapterName,
            onBackClick = onBackClick,
            onGoHome = onGoHome,
            onGoSetting = onGoSetting
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            DebugLogger.errorLog("SimulationListScreen", "Error loading simulations: ${state.error}")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.unable_to_load_simulations),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else if (state.simulations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_simulations_available),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
            ) {
                items(state.simulations, { it.id }) { simulation ->
                    SimulationCard(
                        simulation = simulation,
                        onLaunchClick = {
                            onSimulationClick(
                                simulation.id,
                                simulation.htmlFileName,
                                simulation.title
                            )
                        }
                    )
                }
            }
        }
    }
}