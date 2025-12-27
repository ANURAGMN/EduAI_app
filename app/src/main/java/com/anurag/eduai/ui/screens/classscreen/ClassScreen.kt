package com.anurag.eduai.ui.screens.classscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anurag.eduai.ui.components.Header
import com.anurag.eduai.ui.screens.classscreen.components.SubjectCard
import com.anurag.eduai.ui.theme.*

data class Subject(
    val id: String,
    val name: String,
    val color: Color,
    val conceptCount: String
)
@Composable
fun ClassScreen(
    onBackClick: () -> Unit = {},
    onSubjectClick: (Subject) -> Unit = {}
) {
    val subjects = listOf(
        Subject("1", "Mathematics", Color(0xFF3B82F6), "12 Concepts"),
        Subject("2", "English", Color(0xFF22C55E), "10 Concepts"),
        Subject("3", "Hindi", Color(0xFFF97316), "15 Concepts"),
        Subject("4", "Science", Color(0xFF8B5CF6), "20 Concepts"),
        Subject("5", "Physics", Color(0xFF06B6D4), "18 Concepts"),
        Subject("6", "Chemistry", Color(0xFFEC4899), "16 Concepts"),
        Subject("7", "Geography", Color(0xFF14B8A6), "14 Concepts"),
        Subject("8", "Electronics", Color(0xFF6366F1), "12 Concepts")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        Header(
            title = "Class 7",
            subtitle = "NCERT Curriculum"
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(subjects) { subject ->
                SubjectCard(
                    subject = subject,
                    onClick = { onSubjectClick(subject) }
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ClassScreenPreview() {
    ClassScreen()
}