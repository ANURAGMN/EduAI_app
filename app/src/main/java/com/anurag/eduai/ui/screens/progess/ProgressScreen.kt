package com.anurag.eduai.ui.screens.progess

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.progess.component.ProgressScreenTopBar
import com.anurag.eduai.ui.screens.progess.component.SkillsProgressSection
import com.anurag.eduai.ui.screens.progess.component.StatusCardGrid
import com.anurag.eduai.ui.screens.progess.component.WeeklyActivitySection
import com.anurag.eduai.ui.theme.BackgroundSecondary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.ProgressScreenViewModel

/**
 * Progress Screen Composable
 * Pure UI component - NO business logic, NO data manipulation
 * Only observes ViewModel state and triggers ViewModel actions
 */
@Composable
fun ProgressScreen(
    onGoHome: () -> Unit = {},
    onGoSetting: () -> Unit = {}
) {
    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.PROGRESS)

    val dimes = LocalDimensions.current

    // Initialize ViewModel
    val viewModel: ProgressScreenViewModel = hiltViewModel()

    // Collect all state from ViewModel
    val totalCompletedConcept by viewModel.totalCompletedConcept.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()
    val weeklyProgressData by viewModel.weeklyProgressData.collectAsState()
    val chapterProgress by viewModel.chapterProgressSummary.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val student by viewModel.student.collectAsState()
    val chaptersToShow by viewModel.chaptersToShow.collectAsState()
    val showAllChapters by viewModel.showAllChapters.collectAsState()
    val hasMoreChapters by viewModel.hasMoreChapters.collectAsState()

    // Get class level from student (with default)
    val classLevel = student?.classLevel ?: 7

    // Load data when screen launches
    LaunchedEffect(Unit) {
        viewModel.getSevenDayProgress(viewModel.getSevenDaysAgoInMillis())
    }

    // Load subjects when class level is available
    LaunchedEffect(classLevel) {
        viewModel.loadSubjects(classLevel)
    }

    // Load chapter progress when subject is selected
    LaunchedEffect(selectedSubject) {
        selectedSubject?.let { subject ->
            viewModel.getChapterProgressSummary(
                classLevel = classLevel,
                subject = subject.subjectId
            )
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSecondary)
            .verticalScroll(scrollState)
    ) {
        ProgressScreenTopBar(
            onGoHome = onGoHome,
            onGoSetting = onGoSetting
        )

        Spacer(modifier = Modifier.padding(dimes.screenPadding))

        Column(
            modifier = Modifier
                .background(BackgroundSecondary)
                .padding(dimes.screenPadding)
        ) {
            StatusCardGrid(
                streakCount = streakCount,
                completedConceptCount = totalCompletedConcept,
                completedSimulationCount = 0, // TODO: Get from ViewModel when available
                score = 78 // TODO: Get from ViewModel when available
            )

            Spacer(modifier = Modifier.height(dimes.sectionSpacing))

            WeeklyActivitySection(
                weeklyProgressData = weeklyProgressData,
                maxValue = viewModel.maxWeeklyValue.collectAsState().value,
                getBarHeight = { count -> viewModel.calculateBarHeight(count) }
            )

            Spacer(modifier = Modifier.height(dimes.sectionSpacing))

            SkillsProgressSection(
                subjects = subjects,
                selectedSubject = selectedSubject,
                chaptersToShow = chaptersToShow,
                showAllChapters = showAllChapters,
                hasMoreChapters = hasMoreChapters,
                hiddenChaptersCount = viewModel.getHiddenChaptersCount(),
                onSubjectSelected = { subject -> viewModel.selectSubject(subject) },
                onToggleShowAll = { viewModel.toggleShowAllChapters() },
                getProgressColor = { percentage -> viewModel.getProgressColor(percentage) },
                capitalizeSubjectName = { name -> viewModel.capitalizeFirstLetter(name) }
            )

            Spacer(modifier = Modifier.height(dimes.spaceMedium))
        }
    }
}