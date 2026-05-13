package com.ncert7.aitutorandlab.ui.screens.conceptscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.ncert7.aitutorandlab.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.domain.chatbot.usecase.ChatIntent
import com.ncert7.aitutorandlab.service.analytics.ScreenName
import com.ncert7.aitutorandlab.service.analytics.TrackScreenEvent
import com.ncert7.aitutorandlab.ui.screens.chatbotscreen.components.AppDialog
import com.ncert7.aitutorandlab.ui.screens.chatbotscreen.viewmodel.ChatViewModel
import com.ncert7.aitutorandlab.ui.screens.conceptscreen.components.ConceptCard
import com.ncert7.aitutorandlab.ui.screens.conceptscreen.components.ConceptScreenHeader
import com.ncert7.aitutorandlab.ui.screens.conceptscreen.viewmodel.ConceptViewModel
import com.ncert7.aitutorandlab.ui.screens.simulation_agent.viewmodel.SimulationAgentViewModel
import com.ncert7.aitutorandlab.ui.theme.BackgroundPrimary
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.TextPrimary
import com.ncert7.aitutorandlab.utils.StreakManager

@Composable
fun ConceptScreen(
    chapterId: String,
    type: String,
    onBackClick: () -> Unit = {},
    onConceptClick: (String) -> Unit = {},
    onSimulationAgentClick: (String) -> Unit = {},
    onSimulationClick: (title: String, url: String, conceptId: String) -> Unit = { _, _, _ -> },
    onGoHome:() -> Unit = {},
    onGoSetting:() -> Unit = {},
    viewModel: ConceptViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
) {
    TrackScreenEvent(screenName = ScreenName.CONCEPT)

    val dimes = LocalDimensions.current
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val chatState by chatViewModel.uiState.collectAsState()
    val pendingNavigation by viewModel.pendingNavigation.collectAsState()

    // Handle Ad-Interception Navigation
    LaunchedEffect(pendingNavigation) {
        pendingNavigation?.let { nav ->
            if (nav.isDirect) {
                DebugLogger.debugLog("ConceptScreen", "Performing Direct Navigation: ${nav.route}")
                when (nav.route) {
                    "simulation_agent" -> {
                        nav.conceptId?.let { onSimulationAgentClick(it) }
                    }
                    "concept_sim_view" -> {
                        if (nav.simulationTitle != null && nav.simulationUrl != null && nav.conceptId != null) {
                            onSimulationClick(nav.simulationTitle, nav.simulationUrl, nav.conceptId)
                        }
                    }
                }
                viewModel.clearPendingNavigation()
            }
        }
    }

    val simulationViewModel: SimulationAgentViewModel = hiltViewModel()

    // streak update
    val streakManager = remember { StreakManager(context) }

    LaunchedEffect(Unit) {
        streakManager.onConceptOpened()
        simulationViewModel.loadAvailableSimulations()
    }
    LaunchedEffect(chapterId, type) {
        viewModel.loadConcepts(chapterId, type)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPrimary)
        ) {
            ConceptScreenHeader(
                classLevel = state.classLevel,
                subjectName = state.subjectName,
                chapterName = state.chapterName,
                progress = state.progressUiModel,
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(R.string.unable_to_load_concepts), color = TextPrimary)
                }
            } else {
                Column(
                    modifier = Modifier.padding(dimes.spaceMedium),
                ) {
                    Text(
                        text = if (state.type.equals("SIMULATION", ignoreCase = true))
                            stringResource(R.string.simulations_to_explore)
                        else
                            stringResource(R.string.lessons_to_master),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = dimes.spaceSmall)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(dimes.spaceMedium)
                    ) {
                        itemsIndexed(state.concepts, key = { _, it -> it.id }) { index, conceptUiModel ->
                            ConceptCard(
                                concept = conceptUiModel,
                                serialNumber = index + 1,
                                onClick = {
                                    chatViewModel.selectConceptWithDialog(conceptUiModel.name)
                                    if (!chatViewModel.hasExistingSession(conceptUiModel.name)) {
                                        onConceptClick(conceptUiModel.id)
                                    }
                                },
                                onSimulationAgentClick = { simId ->
                                    viewModel.onSimulationOpened(simId)
                                },
                                onSimulationClick = { title, url, conceptId ->
                                    viewModel.onSimulationUrlOpened(title, url, conceptId)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Ad Banner Dialog - Shown after 5 free simulations
        if (pendingNavigation != null && !pendingNavigation!!.isDirect) {
            com.ncert7.aitutorandlab.ui.components.AdDialog(
                context = context,
                onDismiss = {
                    viewModel.markAdShown()
                }
            )
        }

        // Session Resume Dialog
        AppDialog(
            show = chatState.pendingConceptForDialog != null,
            title = stringResource(R.string.existing_session_found),
            message = stringResource(R.string.resume_or_start_fresh),
            confirmText = stringResource(R.string.continue_session),
            dismissText = stringResource(R.string.start_new),
            onConfirm = {
                chatState.pendingConceptForDialog?.let { conceptName ->
                    chatViewModel.onIntent(ChatIntent.SelectConcept(conceptName))
                    chatViewModel.dismissSessionDialog()
                    state.concepts.find { it.name == conceptName }?.let { concept ->
                        onConceptClick(concept.id)
                    }
                }
            },
            onDismiss = {
                chatState.pendingConceptForDialog?.let { conceptName ->
                    chatViewModel.onIntent(ChatIntent.StartFreshSession(conceptName))
                    chatViewModel.dismissSessionDialog()
                    state.concepts.find { it.name == conceptName }?.let { concept ->
                        onConceptClick(concept.id)
                    }
                }
            }
        )
    }
}