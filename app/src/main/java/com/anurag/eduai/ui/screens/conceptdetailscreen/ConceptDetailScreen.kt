package com.anurag.eduai.ui.screens.conceptdetailscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.components.ScreenWithHeader
import com.anurag.eduai.ui.viewModel.ConceptDetailViewModel

@Composable
fun ConceptDetailScreen(
    conceptId: String,
    onBackClick: () -> Unit = {}
) {
    TrackScreenEvent(screenName = ScreenName.CONCEPT_DETAIL)

    val context = LocalContext.current
    val db = remember { EduAiDatabase.getInstance(context) }
    val conceptDao = db.conceptDao()

    val viewModel = remember { ConceptDetailViewModel(conceptDao) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(conceptId) {
        viewModel.loadConcept(conceptId)
    }

    ScreenWithHeader(
        title = state.concept?.conceptName ?: "Concept",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                state.concept?.let { concept ->
                    Text(
                        text = concept.conceptName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = concept.description ?: "No description available",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}