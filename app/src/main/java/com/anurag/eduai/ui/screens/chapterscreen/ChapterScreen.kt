package com.anurag.eduai.ui.screens.chapterscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.components.ScreenWithHeader
import com.anurag.eduai.ui.screens.chapterscreen.components.Chapter
import com.anurag.eduai.ui.screens.chapterscreen.components.ChapterCard
import com.anurag.eduai.ui.viewModel.ChapterViewModel

@Composable
fun ChapterScreen(
    subjectId: String,
    onBackClick: () -> Unit = {},
    onChapterClick: (String) -> Unit = {}
) {

    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.CHAPTER)

    val context = LocalContext.current
    val db = remember { EduAiDatabase.getInstance(context) }
    val chapterDao = db.chapterDao()
    val subjectDao = db.subjectDao()

    val viewModel = remember { ChapterViewModel(chapterDao, subjectDao) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(subjectId) {
        viewModel.loadChapters(subjectId)
    }

    ScreenWithHeader(
        title = state.subject?.subjectName ?: "Chapters",
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.chapters) { chapter ->
                    ChapterCard(
                        chapter = Chapter(
                            id = chapter.orderIndex.toString(),
                            name = chapter.chapterName,
                            chapterCount = "${chapter.totalConcepts} concepts"
                        ),
                        onStudyClick = { onChapterClick(chapter.chapterId) }
                    )
                }
            }
        }
    }
}