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
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.ChapterViewModel


/**
 * ChapterScreen displays a list of chapters for a given subject.
 *
 * @param subjectId The ID of the subject whose chapters are to be displayed.
 * @param onBackClick Callback function to be invoked when the back button is clicked.
 * @param onChapterClick Callback function to be invoked when a chapter is clicked, passing the chapter ID.
 * @param onSimulationClick Callback function to be invoked when simulation button is clicked, passing chapter info.
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
    onGoHome: () -> Unit = {},
    onGoSetting: () -> Unit = {},
    onProgressClick: () -> Unit = {},
    viewModel: ChapterViewModel = hiltViewModel()
) {
    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.CHAPTER)

    val dimens = LocalDimensions.current
    val state by viewModel.state.collectAsState()

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
                        onStudyClick = { onStudyClick(chapterUiModel.id, "STUDY") },
                        onSimulationClick = { onSimulationClick(chapterUiModel.id, "SIMULATION") }
                    )
                }
            }
        }
    }
}