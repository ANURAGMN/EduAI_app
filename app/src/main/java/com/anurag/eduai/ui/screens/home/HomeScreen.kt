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
import com.anurag.eduai.repository.ConceptRepository
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.home.components.HomeScreenTopBar
import com.anurag.eduai.ui.screens.home.components.SimulationCard
import com.anurag.eduai.ui.screens.home.components.TodayProgressCard
import com.anurag.eduai.ui.theme.BackgroundSecondary

@Composable
fun HomeScreen() {

    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.HOME)

    val context = LocalContext.current

    val db = remember { EduAiDatabase.getInstance(context) }
    val studentDao = db.studentDao()
    val conceptDao = db.conceptDao()
    val sharedPreferenceUtils = SharedPreferenceUtils(context)

    val userId = sharedPreferenceUtils.getUserId().toString()
    var student by remember { mutableStateOf<StudentEntity?>(null) }

    // Testing if user is added to LocalDB or not
    LaunchedEffect (Unit) {
        student = studentDao.getStudentSync(userId)
        DebugLogger.debugLog("HomeScreen", "CurrentUser:\n $student")
    }
    // Log all concepts once
    LaunchedEffect(Unit) {
        val allConcepts = conceptDao.getAllConceptsSync()
        DebugLogger.debugLog(
            "HomeScreen",
            "Concepts in LocalDB:\n${allConcepts.joinToString("\n")}"
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .verticalScroll(rememberScrollState())
        ) {
            HomeScreenTopBar(
                userName = student?.studentName ?: "John Doe"
            )

            Column(
                modifier = Modifier
                    .padding(10.dp)
            ) {

                TodayProgressCard()
                Spacer(modifier = Modifier.height(15.dp))
                SimulationCard()
            }
        }
    }
}