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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.repository.ConceptRepository
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.conceptdetailscreen.components.ConceptDetailScreenHeader
import com.anurag.eduai.ui.theme.AccentGreen
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.viewModel.ConceptDetailViewModel
import com.anurag.eduai.ui.viewmodel_factory.ConceptDetailViewModelFactory
import androidx.compose.foundation.background

@Composable
fun ConceptDetailScreen(
    conceptId: String,
    onBackClick: () -> Unit = {},
    onGoHome:() -> Unit = {},
    onGoSetting:() -> Unit = {},
) {
    TrackScreenEvent(screenName = ScreenName.CONCEPT_DETAIL)

    val context = LocalContext.current
    val db = remember { EduAiDatabase.getInstance(context) }
    val sharedPrefs = remember { SharedPreferenceUtils(context) }

    // Create repository
    val repository = remember { ConceptRepository(db.conceptDao(), db.progressDao()) }

    // Create factory and ViewModel
    val factory = remember { ConceptDetailViewModelFactory(repository, sharedPrefs) }
    val viewModel: ConceptDetailViewModel = viewModel(factory = factory)
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
            title = state.concept?.conceptName ?: "Concept",
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
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${state.error}", color = TextPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                state.concept?.let { concept ->
                    Text(
                        text = concept.conceptName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = TextPrimary
                    )
                    Text(
                        text = concept.description ?: "No description available",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 24.dp),
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Placeholder for Future Simulation Section
                    if (concept.hasSimulation) {
                        Text(
                            text = "Simulations",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp),
                            color = TextPrimary
                        )
                        Text(
                            text = "Simulation content will be displayed here",
                            fontSize = 14.sp,
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            text = "Learning Progress",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Started Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = progressStatus in listOf("STARTED", "IN_PROGRESS", "COMPLETED"),
                onCheckedChange = { if (it) onMarkStarted() },
                enabled = progressStatus !in listOf("IN_PROGRESS", "COMPLETED")
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = " Started Learning",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "I've opened and read this concept",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // In Progress Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = progressStatus in listOf("IN_PROGRESS", "COMPLETED"),
                onCheckedChange = { if (it) onMarkInProgress() },
                enabled = progressStatus in listOf("STARTED", "IN_PROGRESS") && progressStatus != "COMPLETED"
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = " In Progress",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "I'm actively studying this concept",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Completed Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = progressStatus == "COMPLETED",
                onCheckedChange = { if (it) onMarkCompleted() },
                enabled = progressStatus in listOf("IN_PROGRESS", "COMPLETED")
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = " Completed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "I've mastered this concept",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Display with better visuals
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Current Status: ",
                fontSize = 12.sp,
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
                fontSize = 12.sp,
                color = when (progressStatus) {
                    "COMPLETED" -> AccentGreen
                    "IN_PROGRESS" -> Color(0xFFFFA500)
                    "STARTED" -> Color(0xFF2196F3)
                    else -> TextSecondary
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}