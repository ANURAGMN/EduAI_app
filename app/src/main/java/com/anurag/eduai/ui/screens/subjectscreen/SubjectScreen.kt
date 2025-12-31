package com.anurag.eduai.ui.screens.subjectscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.components.ScreenWithHeader
import com.anurag.eduai.ui.screens.subjectscreen.components.SubjectCard
import com.anurag.eduai.ui.viewModel.SubjectViewModel

data class Subject(
    val id: String,
    val name: String,
    val color: Color,
    val chapterCount: String
)

@Composable
fun SubjectScreen(
    onBackClick: () -> Unit = {},
    onSubjectClick: (Subject) -> Unit = {}
) {
    TrackScreenEvent(screenName = ScreenName.SUBJECT)

    val context = LocalContext.current
    val db = remember { EduAiDatabase.getInstance(context) }
    val subjectDao = db.subjectDao()

    val viewModel = remember { SubjectViewModel(subjectDao) }
    val state by viewModel.state.collectAsState()

    ScreenWithHeader(
        title = "Class ${state.classLevel}",
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.subjects) { subject ->
                    SubjectCard(
                        subject = Subject(
                            id = subject.subjectId,
                            name = subject.subjectName,
                            color = Color(0xFF3B82F6),
                            chapterCount = "Chapters"
                        ),
                        onClick = onSubjectClick
                    )
                }
            }
        }
    }
}
