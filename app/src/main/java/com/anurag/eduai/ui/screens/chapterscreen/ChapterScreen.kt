package com.anurag.eduai.ui.screens.chapterscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anurag.eduai.ui.components.Header
import com.anurag.eduai.ui.screens.chapterscreen.components.ChapterCard
import com.anurag.eduai.ui.theme.BackgroundPrimary


enum class ChapterStatus {
    COMPLETED,
    IN_PROGRESS,
    NOT_STARTED
}

data class Chapter(
    val id: String,
    val name: String,
    val chapterCount: String,
    val status: ChapterStatus,
)

@Composable
fun ChapterScreen(
) {
    val chapters = listOf(
        Chapter(
            id = "1",
            name = "Number Systems",
            chapterCount = "8 main chapters",
            status = ChapterStatus.COMPLETED
        ),
        Chapter(
            id = "2",
            name = "Polynomials",
            chapterCount = "4 main chapters",
            status = ChapterStatus.IN_PROGRESS
        ),
        Chapter(
            id = "3",
            name = "Linear Equations",
            chapterCount = "6 main chapters",
            status = ChapterStatus.NOT_STARTED
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        Header(title="Class 7- Mathematics", subtitle="NCERT Curriculum")

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chapters) { chapter ->
                ChapterCard(
                    chapter = chapter,
                    onStudyClick = {},
                )
            }
        }
    }
}

@Preview
@Composable
fun ChapterScreenPreview() {
    ChapterScreen()
}