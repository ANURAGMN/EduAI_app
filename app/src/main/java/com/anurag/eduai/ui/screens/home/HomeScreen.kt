package com.anurag.eduai.ui.screens.home

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.home.components.HomeScreenTopBar
import com.anurag.eduai.ui.screens.home.components.SimulationCard
import com.anurag.eduai.ui.screens.home.components.TodayProgressCard
import com.anurag.eduai.ui.theme.BackgroundSecondary
import com.anurag.eduai.ui.theme.Dimensions
import com.anurag.eduai.ui.viewModel.HomeViewModel
import com.anurag.eduai.ui.viewmodel_factory.HomeViewModelFactory
import com.anurag.eduai.utils.StreakManager

@Composable
fun HomeScreen(
        onNavigateToLearning: () -> Unit = {},
        onNavigateToChapters: (String) -> Unit = {},
        onLessonClick: (String) -> Unit = {}
) {

    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.HOME)

    val scrollState = rememberScrollState()

    val context = LocalContext.current

    val db = remember { EduAiDatabase.getInstance(context) }
    val studentDao = db.studentDao()
    val conceptDao = db.conceptDao()
    val progressDao = db.progressDao()
    val sharedPreferenceUtils = SharedPreferenceUtils(context)
    val streakManager = StreakManager(context)

    val userId = sharedPreferenceUtils.getUserId().toString() ?: error("Userid missing in home screen")

    val selectedSubject = sharedPreferenceUtils.getSubjectSelection()

    val factory = HomeViewModelFactory(conceptDao,progressDao, studentDao, userId, streakManager)
    val viewModel: HomeViewModel = viewModel(factory = factory)

    val progressConcepts by viewModel.progressConcepts.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()
    val todayCompletedConceptCount by viewModel.todayConceptCount.collectAsState()
    val todayCompletedSimulationCount by viewModel.todaySimulationCount.collectAsState()
    val student by viewModel.student.collectAsState()


    // Testing if user is added to LocalDB or not
    LaunchedEffect(Unit) {
        DebugLogger.debugLog("HomeScreen", "CurrentUser:\n $student")
    }

    LaunchedEffect(progressConcepts) {
        DebugLogger.debugLog("HomeScreen", "Concept:\n $progressConcepts")
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .background(BackgroundSecondary)
                                .verticalScroll(scrollState)
        ) {
            //TODO: if student is null then load defult values
            /**
             * if (student == null) {
             *     LoadingHomeHeader()
             * } else {
             *     HomeScreenTopBar(...)
             * }
             */
            HomeScreenTopBar(
                    userName = student?.studentName ?: "John Doe",
                    subject = selectedSubject ?: "Science",
                    streakDays = streakCount,
                    onChangeSubject = { onNavigateToLearning() }
            )

            Column(modifier = Modifier.padding(Dimensions.Compact.screenPadding)) {
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

                Spacer(modifier = Modifier.height(Dimensions.Compact.spaceSmall))

                SimulationCard()
            }
        }
    }
}
