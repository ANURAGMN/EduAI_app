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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.progess.component.ProgressScreenTopBar
import com.anurag.eduai.ui.screens.progess.component.ShareButton
import com.anurag.eduai.ui.screens.progess.component.SkillsProgressSection
import com.anurag.eduai.ui.screens.progess.component.StatusCardGrid
import com.anurag.eduai.ui.screens.progess.component.WeeklyActivitySection
import com.anurag.eduai.ui.theme.BackgroundSecondary
import com.anurag.eduai.ui.viewModel.ProgressScreenVIewModel
import com.anurag.eduai.utils.StreakManager
import com.anurag.eduai.utils.WeeklyProgressUtils

@Composable
fun ProgressScreen()
{
    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.PROGRESS)

    val context = LocalContext.current
    // Object of util class
    val weeklyProgressUtil = WeeklyProgressUtils()

    val sharedPref = SharedPreferenceUtils(context)
    val userId = sharedPref.getUserId().toString()

    val streakManager = StreakManager(context)

    val db = remember { EduAiDatabase.getInstance(context) }
    val progressDao = db.progressDao()
    val studentDao = db.studentDao()
    var student by remember { mutableStateOf<StudentEntity?>(null) }

    val viewModel = remember { ProgressScreenVIewModel(progressDao, streakManager) }

    // collecting all the values as state
    val totalCompletedConcept by viewModel.totalCompletedConcept.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()
    val sevenDayProgress by viewModel.sevenDayProgress.collectAsState()
    val skillProgress by viewModel.chapterProgressSummary.collectAsState()

    LaunchedEffect(userId) {
        student = studentDao.getStudentSync(userId)
    }
    // loading all the value to their state through method call of viewmodel
    LaunchedEffect(Unit) {
        viewModel.getTotalCompletedConcept(userId)
        viewModel.getStreak()
        viewModel.getSevenDayProgress(userId, weeklyProgressUtil.getSevenDaysAgoInMillis())
        viewModel.getChapterProgressSummary(userId, student?.classLevel ?: 7, "science")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSecondary)
            .verticalScroll(rememberScrollState())
    ) {
        ProgressScreenTopBar()

        Spacer(modifier = Modifier.padding(15.dp))
        Column(
            modifier = Modifier
                .background(BackgroundSecondary)
                .padding(15.dp)
        ) {
            StatusCardGrid(
                streakCount = streakCount,
                completedConceptCount = totalCompletedConcept,
                completedSimulationCount = "0",
                score = "78%"
            )
            Spacer(modifier = Modifier.height(25.dp))
            WeeklyActivitySection(
                weeklyProgressList = sevenDayProgress
            )

            Spacer(modifier = Modifier.height(25.dp))

            SkillsProgressSection()

            Spacer(modifier = Modifier.height(20.dp))

//            ShareButton()
//            Spacer(modifier = Modifier.height(20.dp))
        }
    }

}