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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.R
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.ChapterRepository
import com.anurag.eduai.repository.StudentLocalRepository
import com.anurag.eduai.repository.SubjectRepository
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.chapterscreen.components.ChapterScreenHeader
import com.anurag.eduai.ui.screens.chapterscreen.components.ChapterCard
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.ChapterViewModel
import com.anurag.eduai.ui.viewmodel_factory.ChapterViewModelFactory


/**
 * ChapterScreen displays a list of chapters for a given subject.
 * 1. It shows a loading indicator while data is being fetched.
 * 2. It displays the list of chapters using ChapterCard components.
 * 3. It includes a header with the subject name and a back button.
 *
 * @param subjectId The ID of the subject whose chapters are to be displayed.
 * @param onBackClick Callback function to be invoked when the back button is clicked.
 * @param onChapterClick Callback function to be invoked when a chapter is clicked, passing the chapter ID.
 * @param onGoHome Callback function to navigate to the home screen.
 * @param onGoSetting Callback function to navigate to the settings screen.
 * @param onProgressClick Callback function to navigate to the progress screen.
 */
@Composable
fun ChapterScreen(
    subjectId: String,
    onBackClick: () -> Unit = {},
    onChapterClick: (String) -> Unit = {},
    onGoHome: () -> Unit = {},
    onGoSetting: () -> Unit = {},
    onProgressClick: () -> Unit = {}
) {
    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.CHAPTER)

    val dimens = LocalDimensions.current
    val context = LocalContext.current

    val db = remember { EduAiDatabase.getInstance(context) }
    val sharedPrefs = remember { SharedPreferenceUtils(context) }

    // Create repositories
    val chapterRepository = remember { ChapterRepository(db.chapterDao(), db.progressDao()) }
    val subjectRepository = remember { SubjectRepository(db.subjectDao()) }
    val studentRepository = remember { StudentLocalRepository(db.studentDao()) }

    // Create factory and ViewModel
    val factory = remember {
        ChapterViewModelFactory(chapterRepository, subjectRepository, studentRepository, sharedPrefs)
    }
    val viewModel: ChapterViewModel = viewModel(factory = factory)
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
                items(state.chapters,{it.id}) { chapterUiModel ->
                    ChapterCard(
                        chapter = chapterUiModel,
                        onStudyClick = { onChapterClick(chapterUiModel.id) }
                    )
                }
            }
        }
    }
}