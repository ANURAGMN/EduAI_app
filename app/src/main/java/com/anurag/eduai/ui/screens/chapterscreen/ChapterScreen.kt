package com.anurag.eduai.ui.screens.chapterscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.anurag.eduai.ui.components.Header
import com.anurag.eduai.ui.screens.chapterscreen.components.ConceptsSection
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
fun ChapterScreen(
    chapterName: String = "Polynomials",
    className: String = "Class 7 - Mathematics",
) {
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
        // Concepts Section
        ConceptsSection(
            concepts = concepts
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChapterScreenPreview() {
    ChapterScreen()
}