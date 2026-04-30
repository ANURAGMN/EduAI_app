package com.anurag.eduai.ui.screens.home

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.home.components.HomeScreenTopBar
import com.anurag.eduai.ui.screens.home.components.LoadingHomeHeader
import com.anurag.eduai.ui.screens.home.components.PracticeSimulationCard
import com.anurag.eduai.ui.screens.home.components.TodayProgressCard
import com.anurag.eduai.ui.theme.BackgroundSecondary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.screens.home.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onNavigateToLearning: () -> Unit = {},
    onNavigateToChapters: (String) -> Unit = {},
    onLessonClick: (String) -> Unit = {},
    onSimulationClick: (String) -> Unit = {},
    onSimulationUrlClick: (String, String) -> Unit = { _, _ -> }
) {
    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.HOME)

    val dimens = LocalDimensions.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val sharedPreferenceUtils = SharedPreferenceUtils(context)
    val selectedSubject = sharedPreferenceUtils.getSubjectSelection()

    val viewModel: HomeViewModel = hiltViewModel()

    val progressConcepts by viewModel.progressConcepts.collectAsState()
    val progressSimulations by viewModel.progressSimulations.collectAsState()

    val streakCount by viewModel.streakCount.collectAsState()
    val todayCompletedConceptCount by viewModel.todayConceptCount.collectAsState()
    val todayCompletedSimulationCount by viewModel.todaySimulationCount.collectAsState()
    val student by viewModel.student.collectAsState()
    val greeting by viewModel.greeting.collectAsState()

    // Observe language change trigger to force recomposition
    val languageChangeTrigger by viewModel.languageChangeTrigger.collectAsState()

    // Testing if user is added to LocalDB or not
    LaunchedEffect(Unit) { DebugLogger.debugLog("HomeScreen", "CurrentUser:\n $student") }

    LaunchedEffect(progressConcepts) {
        DebugLogger.debugLog("HomeScreen", "Concept:\n $progressConcepts")
    }

    // Detect language changes by observing configuration changes
    val currentLanguage = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "en"

    LaunchedEffect(currentLanguage, languageChangeTrigger) {
        // Trigger refresh when configuration locale or app language changes
        viewModel.onLanguageChanged()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(BackgroundSecondary)
                    .verticalScroll(scrollState)
        ) {
            // Show loading state if student is null
            if (student == null) {
                LoadingHomeHeader(
                    subject = selectedSubject ?: "Science",
                    onChangeSubject = { onNavigateToLearning() }
                )
            } else {
                HomeScreenTopBar(
                    userName = student?.studentName ?: "John Doe",
                    subject = selectedSubject ?: "Science",
                    streakDays = streakCount,
                    greeting = greeting,
                    onChangeSubject = { onNavigateToLearning() }
                )
            }

            Column(modifier = Modifier.padding(dimens.screenPadding)) {
                TodayProgressCard(
                    progressConcepts = progressConcepts,
                    onLessonClick = onLessonClick,
                    todayCompletedConcept = todayCompletedConceptCount,
                    todayCompletedSimulation = todayCompletedSimulationCount,
                    onShowAllChapters = {
                        val subjectId = sharedPreferenceUtils.getSubjectSelection() ?: "science"
                        onNavigateToChapters(subjectId)
                    }
                )
                Spacer(modifier = Modifier.height(dimens.spaceSmall))
                PracticeSimulationCard(
                    progressSimulations = progressSimulations,
                    onSimulationClick = { simulationId ->
                        onSimulationClick(simulationId)
                    },
                    onSimulationUrlClick = { title, url ->
                        onSimulationUrlClick(title, url)
                    }
                )
            }
        }
    }
}