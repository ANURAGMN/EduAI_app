package com.anurag.eduai.ui.screens.subjectscreen

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
import com.anurag.eduai.ui.screens.subjectscreen.components.ConceptCard
import com.anurag.eduai.ui.components.Header
import com.anurag.eduai.ui.theme.BackgroundPrimary


enum class ConceptStatus {
    COMPLETED,
    IN_PROGRESS,
    NOT_STARTED
}

data class Concept(
    val id: String,
    val name: String,
    val conceptCount: String,
    val status: ConceptStatus,
)

@Composable
fun SubjectScreen() {
    val concepts = listOf(
        Concept(
            id = "1",
            name = "Number Systems",
            conceptCount = "8 main concepts",
            status = ConceptStatus.COMPLETED
        ),
        Concept(
            id = "2",
            name = "Polynomials",
            conceptCount = "4 main concepts",
            status = ConceptStatus.IN_PROGRESS
        ),
        Concept(
            id = "3",
            name = "Linear Equations",
            conceptCount = "6 main concepts",
            status = ConceptStatus.NOT_STARTED
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
            items(concepts) { concept ->
                ConceptCard(
                    concept = concept,
                    onStudyClick = {},
                )
            }
        }
    }
}

@Preview
@Composable
fun SubjectScreenPreview() {
    SubjectScreen()
}