package com.anurag.eduai.ui.screens.conceptscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.components.Header
import com.anurag.eduai.ui.screens.conceptscreen.components.ConceptCard
import com.anurag.eduai.ui.theme.BackgroundPrimary

enum class ConceptStatus {
    COMPLETED,
    IN_PROGRESS,
    NOT_STARTED
}

data class Concept(
    val id: String,
    val name: String,
    val status: ConceptStatus,
)

@Composable
fun ConceptScreen(
    chapterName: String = "Polynomials",
    className: String = "Class 7 - Mathematics",
) {

    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.CONCEPT)

    val concepts = listOf(
        Concept(
            id = "1",
            name = "Introduction to Polynomials",
            status = ConceptStatus.COMPLETED
        ),
        Concept(
            id = "2",
            name = "Types of Polynomials",
            status = ConceptStatus.IN_PROGRESS
        ),
        Concept(
            id = "3",
            name = "Operations on Polynomials",
            status = ConceptStatus.NOT_STARTED
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        // Top Header
        Header(
           className,
           chapterName,

        )
        // Concepts Card
        ConceptCard(
            concepts = concepts
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChapterScreenPreview() {
    ConceptScreen()
}