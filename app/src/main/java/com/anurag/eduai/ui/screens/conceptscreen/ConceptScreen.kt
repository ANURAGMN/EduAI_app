package com.anurag.eduai.ui.screens.conceptscreen

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.components.ScreenWithHeader
import com.anurag.eduai.ui.screens.conceptscreen.components.Concept
import com.anurag.eduai.ui.screens.conceptscreen.components.ConceptCard
import com.anurag.eduai.ui.screens.conceptscreen.components.ConceptStatus
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.viewModel.ConceptViewModel
import com.anurag.eduai.utils.StreakManager

@Composable
fun ConceptScreen(
    chapterId: String,
    onBackClick: () -> Unit = {},
    onConceptClick: (String) -> Unit = {}
) {
    TrackScreenEvent(screenName = ScreenName.CONCEPT)

    val context = LocalContext.current
    val db = remember { EduAiDatabase.getInstance(context) }
    val conceptDao = db.conceptDao()
    val chapterDao = db.chapterDao()
    val progressDao = db.progressDao()
    val sharedPrefs = remember { SharedPreferenceUtils(context) }


    // streak update
    val streakManager = StreakManager(context)

    val viewModel = remember {
        ConceptViewModel(conceptDao, chapterDao, progressDao, sharedPrefs)
    }
    val state by viewModel.state.collectAsState()

    // updating streak on concept opening
    LaunchedEffect(Unit) {
        streakManager.onConceptOpened()
    }
    LaunchedEffect(chapterId) {
        viewModel.loadConcepts(chapterId)
    }

    ScreenWithHeader(
        title = state.chapter?.chapterName ?: "Concepts",
        onBackClick = onBackClick
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${state.error}", color = TextPrimary)
            }
        } else {
            ConceptCard(
                concepts = state.concepts.map { conceptWithProgress ->
                    Concept(
                        id = conceptWithProgress.concept.conceptId,
                        order = conceptWithProgress.concept.orderIndex,
                        name = conceptWithProgress.concept.conceptName,
                        status = when (conceptWithProgress.status) {
                            "COMPLETED" -> ConceptStatus.COMPLETED
                            "IN_PROGRESS", "STARTED" -> ConceptStatus.IN_PROGRESS // Treat STARTED and IN_PROGRESS the same in list
                            else -> ConceptStatus.NOT_STARTED
                        }
                    )
                },
                onConceptClick = { conceptId ->
                    DebugLogger.debugLog("ConceptScreen", "Concept clicked: $conceptId")
                    onConceptClick(conceptId)
                }
            )
        }
    }
}