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
import androidx.compose.ui.res.stringResource
import com.anurag.eduai.R
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.components.ScreenWithHeader
import com.anurag.eduai.ui.screens.chapterscreen.components.ChapterCard
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.ChapterViewModel

enum class ChapterStatus {
    COMPLETED,
    IN_PROGRESS,
    NOT_STARTED
}

data class Chapter(
    val id: String,
    val name: String,
    val conceptCount: String,
)

/**
 * ChapterScreen displays a list of chapters for a given subject.
 * 1. It shows a loading indicator while data is being fetched.
 * 2. It displays the list of chapters using ChapterCard components.
 * 2. It includes a header with the subject name and a back button.
 *
 *
 * @param subjectId The ID of the subject whose chapters are to be displayed.
 * @param onBackClick Callback function to be invoked when the back button is clicked.
 * @param onChapterClick Callback function to be invoked when a chapter is clicked, passing the chapter ID.
 */
@Composable
fun ChapterScreen(
    subjectId: String,
    onBackClick: () -> Unit = {},
    onChapterClick: (String) -> Unit = {}
) {

    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.CHAPTER)
    val dimens = LocalDimensions.current
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
        onBackClick = onBackClick,
        subtitle = stringResource(R.string.ncert_curriculum),
        extraContent = {}
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
                    .padding(dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
            ) {
                items(state.chapters) { chapter ->
                    ChapterCard(
                        chapter = Chapter(
                            id = chapter.orderIndex.toString(),
                            name = chapter.chapterName,
                            conceptCount = "${chapter.totalConcepts} concepts"
                        ),
                        onStudyClick = { onChapterClick(chapter.chapterId) }
                    )
                }
            }
        }
    }
}