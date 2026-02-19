package com.anurag.eduai.ui.screens.conceptdetailscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.conceptdetailscreen.components.ConceptDetailScreenHeader
import com.anurag.eduai.ui.theme.AccentGreen
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.viewModel.ConceptDetailViewModel
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.ColorWarning
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.utils.getLocalizedName
import com.anurag.eduai.R

@Composable
fun ConceptDetailScreen(
    conceptId: String,
    onBackClick: () -> Unit = {},
    onGoHome: () -> Unit = {},
    onGoSetting: () -> Unit = {},
    viewModel: ConceptDetailViewModel = hiltViewModel()
) {
    TrackScreenEvent(screenName = ScreenName.CONCEPT_DETAIL)
    val dimens = LocalDimensions.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(conceptId) {
        viewModel.loadConcept(conceptId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        ConceptDetailScreenHeader(
            title = state.concept?.getLocalizedName() ?: stringResource(R.string.concept),
            subtitle = "Concepts",
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimens.spaceLarge),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${state.error}", color = TextPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimens.spaceLarge)
                    .verticalScroll(rememberScrollState())
            ) {
                state.concept?.let { concept ->
                    Text(
                        text = concept.getLocalizedName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom =dimens.spaceMedium),
                        color = TextPrimary
                    )
                    Text(
                        text = concept.description ?: "No description available",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = dimens.spaceLarge),
                        color = TextSecondary
                    )

                    // Progress Tracking Section with 3 checkboxes
                    ProgressTrackingSection(
                        progressStatus = state.progressStatus,
                        onMarkStarted = {
                            viewModel.updateProgressStatus("STARTED")
                        },
                        onMarkInProgress = {
                            viewModel.updateProgressStatus("IN_PROGRESS")
                        },
                        onMarkCompleted = {
                            viewModel.updateProgressStatus("COMPLETED")
                        }
                    )

                    Spacer(modifier = Modifier.height(dimens.spaceExtraLarge))

                    // Placeholder for Future Simulation Section
                    if (concept.hasSimulation) {
                        Text(
                            text = "Simulations",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = dimens.spaceMedium ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Simulation content will be displayed here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

//progress tracking with checkboxes for testing
@Composable
private fun ProgressTrackingSection(
    progressStatus: String,
    onMarkStarted: () -> Unit,
    onMarkInProgress: () -> Unit,
    onMarkCompleted: () -> Unit
) {
    val dimens = LocalDimensions.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.spaceMedium)
    ) {
        Text(
            text = "Learning Progress",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = dimens.spaceLarge)
        )

        // Started Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = progressStatus in listOf("STARTED", "IN_PROGRESS", "COMPLETED"),
                onCheckedChange = { if (it) onMarkStarted() },
                enabled = progressStatus !in listOf("IN_PROGRESS", "COMPLETED")
            )
            Spacer(modifier = Modifier.width(dimens.spaceLarge))
            Column {
                Text(
                    text = " Started Learning",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "I've opened and read this concept",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // In Progress Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = progressStatus in listOf("IN_PROGRESS", "COMPLETED"),
                onCheckedChange = { if (it) onMarkInProgress() },
                enabled = progressStatus in listOf("STARTED", "IN_PROGRESS") && progressStatus != "COMPLETED"
            )
            Spacer(modifier = Modifier.width(dimens.spaceLarge))
            Column {
                Text(
                    text = " In Progress",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "I'm actively studying this concept",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Completed Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = progressStatus == "COMPLETED",
                onCheckedChange = { if (it) onMarkCompleted() },
                enabled = progressStatus in listOf("IN_PROGRESS", "COMPLETED")
            )
            Spacer(modifier = Modifier.width(dimens.spaceLarge))
            Column {
                Text(
                    text = " Completed",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "I've mastered this concept",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(dimens.spaceLarge))

        // Status Display with better visuals
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Current Status: ",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = when (progressStatus) {
                    "STARTED" -> " Started"
                    "IN_PROGRESS" -> " In Progress"
                    "COMPLETED" -> " Completed"
                    else -> " Not Started"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (progressStatus) {
                    "COMPLETED" -> AccentGreen
                    "IN_PROGRESS" -> ColorWarning
                    "STARTED" -> AccentBlue
                    else -> TextSecondary
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}