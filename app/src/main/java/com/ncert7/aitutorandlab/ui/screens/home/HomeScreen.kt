package com.ncert7.aitutorandlab.ui.screens.home

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ncert7.aitutorandlab.R
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.service.analytics.ScreenName
import com.ncert7.aitutorandlab.service.analytics.TrackScreenEvent
import com.ncert7.aitutorandlab.ui.screens.home.components.HomeScreenTopBar
import com.ncert7.aitutorandlab.ui.screens.home.components.LoadingHomeHeader
import com.ncert7.aitutorandlab.ui.screens.home.components.PracticeSimulationCard
import com.ncert7.aitutorandlab.ui.screens.home.components.TodayProgressCard
import com.ncert7.aitutorandlab.ui.theme.BackgroundSecondary
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.screens.home.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onNavigateToLearning: () -> Unit = {},
    onNavigateToChapters: (String) -> Unit = {},
    onLessonClick: (String) -> Unit = {},
    onSimulationClick: (String, String) -> Unit = { _, _ -> },
    onSimulationUrlClick: (String, String, String) -> Unit = { _, _, _ -> }
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
    val totalCompletedConceptCount by viewModel.totalCompletedConcept.collectAsState()
    val totalCompletedSimulationCount by viewModel.totalCompletedSimulation.collectAsState()
    val student by viewModel.student.collectAsState()
    val greeting by viewModel.greeting.collectAsState()

    // Testing if user is added to LocalDB or not
    LaunchedEffect(Unit) { DebugLogger.debugLog("HomeScreen", "CurrentUser:\n $student") }

    LaunchedEffect(progressConcepts) {
        DebugLogger.debugLog("HomeScreen", "Concept:\n $progressConcepts")
    }

    // Use LocalConfiguration.current so Compose re-reads this on config changes (locale, dark mode, etc.)
    // This is reactive - it triggers recomposition + LaunchedEffect when language changes
    val configuration = LocalConfiguration.current
    val currentLanguage = configuration.locales[0]?.language ?: "en"

    LaunchedEffect(currentLanguage) {
        viewModel.setLanguage(currentLanguage)
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
                    subject = selectedSubject ?: stringResource(R.string.select_subject),
                    onChangeSubject = { onNavigateToLearning() }
                )
            } else {
                HomeScreenTopBar(
                    userName = student?.studentName ?: "",
                    subject = selectedSubject ?: stringResource(R.string.select_subject),
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
                        val subjectId = if (selectedSubject?.contains("Math", ignoreCase = true) == true ||
                            selectedSubject?.contains("ಗಣಿತ", ignoreCase = true) == true) {
                            "5c0a6b6d-7c6b-4f35-9d5b-9fd0fd8e8a01"  // Math
                        } else {
                            "9a7d0d20-7b8d-4b8c-8c12-5a1a8a55f002"  // Science
                        }
                        onNavigateToChapters(subjectId)
                    }
                )
                Spacer(modifier = Modifier.height(dimens.spaceSmall))
                PracticeSimulationCard(
                    progressSimulations = progressSimulations,
                    onSimulationClick = { simulationId, conceptId ->
                        onSimulationClick(simulationId, conceptId)
                    },
                    onSimulationUrlClick = { title, url, conceptId ->
                        onSimulationUrlClick(title, url, conceptId)
                    }
                )
            }
        }
    }
}