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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.progess.component.ProgressScreenTopBar
import com.anurag.eduai.ui.screens.progess.component.SkillsProgressSection
import com.anurag.eduai.ui.screens.progess.component.StatusCardGrid
import com.anurag.eduai.ui.screens.progess.component.WeeklyActivitySection
import com.anurag.eduai.ui.theme.BackgroundSecondary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.ProgressScreenVIewModel
import com.anurag.eduai.ui.viewmodel_factory.ProgressViewModelFactory
import com.anurag.eduai.utils.StreakManager
import com.anurag.eduai.utils.WeeklyProgressUtils

@Composable
fun ProgressScreen(onGoHome: () -> Unit = {}, onGoSetting: () -> Unit = {}) {
    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.PROGRESS)

    val dimes = LocalDimensions.current
    val context = LocalContext.current
    // Object of util class
    val weeklyProgressUtil = WeeklyProgressUtils()

    val sharedPref = SharedPreferenceUtils(context)
    val userId = sharedPref.getUserId().toString()
    var classLevel = 7

    val streakManager = StreakManager(context)

    val db = remember { EduAiDatabase.getInstance(context) }
    val progressDao = db.progressDao()
    val studentDao = db.studentDao()
    val subjectDao = db.subjectDao()

    val viewModel: ProgressScreenVIewModel =
        viewModel(
            factory =
                ProgressViewModelFactory(
                    progressDao,
                    subjectDao,
                    streakManager,
                    studentDao,
                    userId
                )
        )

    // collecting all the values as state
    val totalCompletedConcept by viewModel.totalCompletedConcept.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()
    val sevenDayProgress by viewModel.sevenDayProgress.collectAsState()

    val chapterProgress by viewModel.chapterProgressSummary.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val student by viewModel.student.collectAsState()

    LaunchedEffect(userId) {
        classLevel = student?.classLevel ?: 7 // default value as class 7
    }
    // loading all the value to their state through method call of viewmodel
    LaunchedEffect(Unit) {
        viewModel.getSevenDayProgress(userId, weeklyProgressUtil.getSevenDaysAgoInMillis())
    }

    // Load subjects when screen launches
    LaunchedEffect(classLevel) { viewModel.loadSubjects(classLevel) }
    // Load chapter progress when subject is selected
    LaunchedEffect(selectedSubject) {
        selectedSubject?.let { subject ->
            viewModel.getChapterProgressSummary(
                userId = userId,
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
        ProgressScreenTopBar(onGoHome, onGoSetting)

        Spacer(modifier = Modifier.padding(dimes.screenPadding))
        Column(modifier = Modifier.background(BackgroundSecondary).padding(dimes.screenPadding)) {
            StatusCardGrid(
                streakCount = streakCount,
                completedConceptCount = totalCompletedConcept,
                completedSimulationCount = 0,
                score = 78
            )
            Spacer(modifier = Modifier.height(dimes.sectionSpacing))
            WeeklyActivitySection(weeklyProgressList = sevenDayProgress)

            Spacer(modifier = Modifier.height(dimes.sectionSpacing))

            SkillsProgressSection(
                subjects = subjects,
                selectedSubject = selectedSubject,
                chapterProgress = chapterProgress,
                onSubjectSelected = { subject -> viewModel.selectSubject(subject) }
            )

            Spacer(modifier = Modifier.height(dimes.spaceMedium))
        }
    }
}
