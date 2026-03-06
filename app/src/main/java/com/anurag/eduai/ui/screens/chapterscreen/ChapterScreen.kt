package com.anurag.eduai.ui.screens.chapterscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.anurag.eduai.R
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.chapterscreen.components.ChapterScreenHeader
import com.anurag.eduai.ui.screens.chapterscreen.components.ChapterCard
import com.anurag.eduai.ui.screens.chatbotscreen.components.AppDialog
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.ChapterViewModel
import com.anurag.eduai.ui.viewModel.RevisionViewModel


/**
 * ChapterScreen displays a list of chapters for a given subject.
 *
 * @param subjectId The ID of the subject whose chapters are to be displayed.
 * @param onBackClick Callback function to be invoked when the back button is clicked.
 * @param onChapterClick Callback function to be invoked when a chapter is clicked, passing the chapter ID.
 * @param onSimulationClick Callback function to be invoked when simulation button is clicked, passing chapter info.
 * @param onRevisionClick Callback function to be invoked when revision button is clicked, passing chapter name.
 * @param onGoHome Callback function to navigate to the home screen.
 * @param onGoSetting Callback function to navigate to the settings screen.
 * @param onProgressClick Callback function to navigate to the progress screen.
 * @param viewModel ChapterViewModel injected by Hilt
 */
@Composable
fun ChapterScreen(
    subjectId: String,
    onBackClick: () -> Unit = {},
    onStudyClick: (String, String) -> Unit = {_, _ -> },
    onSimulationClick: (String, String) -> Unit = {_, _ -> },
    onRevisionClick: (String) -> Unit = {},
    onGoHome: () -> Unit = {},
    onGoSetting: () -> Unit = {},
    onProgressClick: () -> Unit = {},
    viewModel: ChapterViewModel = hiltViewModel(),
    revisionViewModel: RevisionViewModel = hiltViewModel()
) {
    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.CHAPTER)

    val dimens = LocalDimensions.current
    val state by viewModel.state.collectAsState()

    // State for revision dialog
    var showRevisionDialog by remember { mutableStateOf(false) }
    var pendingRevisionChapter by remember { mutableStateOf<String?>(null) }

    // Load chapters when subjectId changes
    LaunchedEffect(subjectId) {
        viewModel.loadChapters(subjectId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        ChapterScreenHeader(
            classLevel = state.classLevel,
            subjectName = state.subjectName,
            onBackClick = onBackClick,
            onGoHome = onGoHome,
            onGoSetting = onGoSetting,
            onProgressClick = onProgressClick
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            DebugLogger.errorLog("ChapterScreen", "Error loading chapters: ${state.error}")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.unable_to_load_chapters))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
            ) {
                items(state.chapters, { it.id }) { chapterUiModel ->
                    ChapterCard(
                        chapter = chapterUiModel,
                        subjectName = state.subjectName, // Pass subject name for conditional rendering
                        onStudyClick = { onStudyClick(chapterUiModel.id, "STUDY") },
                        onSimulationClick = { onSimulationClick(chapterUiModel.id, "SIMULATION") },
                        onRevisionClick = {
                            // Hardcoded for testing - will be replaced with dynamic chapter mapping
                            val chapterName = "Measurement Of Time And Motion"
                            DebugLogger.debugLog("ChapterScreen", "Revision button clicked for chapter: ${chapterUiModel.name}, using: $chapterName")

                            // Check if session exists
                            if (revisionViewModel.hasExistingSession(chapterName)) {
                                DebugLogger.debugLog("ChapterScreen", "Existing revision session found, showing dialog")
                                pendingRevisionChapter = chapterName
                                showRevisionDialog = true
                            } else {
                                DebugLogger.debugLog("ChapterScreen", "No existing revision session, navigating directly")
                                onRevisionClick(chapterName)
                            }
                        }
                    )
                }
            }
        }

        // Revision Session Resume Dialog
        AppDialog(
            show = showRevisionDialog,
            title = stringResource(R.string.existing_session_found),
            message = stringResource(R.string.resume_or_start_fresh),
            confirmText = stringResource(R.string.continue_session),
            dismissText = stringResource(R.string.start_new),
            onConfirm = {
                // Resume existing session
                pendingRevisionChapter?.let { chapterName ->
                    DebugLogger.debugLog("ChapterScreen", "User chose to resume revision session")
                    onRevisionClick(chapterName)
                }
                showRevisionDialog = false
                pendingRevisionChapter = null
            },
            onDismiss = {
                // Start fresh session
                pendingRevisionChapter?.let { chapterName ->
                    DebugLogger.debugLog("ChapterScreen", "User chose to start fresh revision session")
                    revisionViewModel.startFreshSession()
                    onRevisionClick(chapterName)
                }
                showRevisionDialog = false
                pendingRevisionChapter = null
            }
        )
    }
}