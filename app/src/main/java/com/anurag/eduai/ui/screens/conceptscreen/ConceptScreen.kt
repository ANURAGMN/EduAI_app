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
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.components.ScreenWithHeader
import com.anurag.eduai.ui.screens.conceptscreen.components.Concept
import com.anurag.eduai.ui.screens.conceptscreen.components.ConceptCard
import com.anurag.eduai.ui.screens.conceptscreen.components.ConceptStatus
import com.anurag.eduai.ui.viewModel.ConceptViewModel

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

    val viewModel = remember { ConceptViewModel(conceptDao, chapterDao) }
    val state by viewModel.state.collectAsState()

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
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${state.error}")
            }
        } else {
            ConceptCard(
                concepts = state.concepts.map {
                    Concept(
                        id = it.conceptId,
                        order=it.orderIndex,
                        name = it.conceptName,
                    )
                },
                onConceptClick = onConceptClick
            )
        }
    }
}