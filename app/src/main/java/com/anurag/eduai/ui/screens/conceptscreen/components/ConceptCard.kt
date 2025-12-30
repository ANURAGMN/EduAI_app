package com.anurag.eduai.ui.screens.conceptscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.ui.screens.conceptscreen.Concept
import com.anurag.eduai.ui.screens.conceptscreen.ConceptStatus
import com.anurag.eduai.ui.theme.AccentGreen
import com.anurag.eduai.ui.theme.CompleteIconBackground
import com.anurag.eduai.ui.theme.CompleteTextColor
import com.anurag.eduai.ui.theme.InProgressIconBackground
import com.anurag.eduai.ui.theme.InProgressTextColor
import com.anurag.eduai.ui.theme.NotStartedIconBackground
import com.anurag.eduai.ui.theme.NotStartedTextColor
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary

@Composable
fun ConceptCard(
    concepts: List<Concept>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Concepts to Master",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(concepts) { concept ->
                ConceptItemCard(
                    concept = concept,
                )
            }
        }
    }
}

@Composable
fun ConceptItemCard(
    concept: Concept,
) {
    val isEnabled = concept.status != ConceptStatus.NOT_STARTED

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
           .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Badge + Content
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status badge (Circle with icon/number)
                ConceptStatusBadge(
                    conceptNumber = concept.id,
                    status = concept.status
                )

                // Content (Title + Subtitle)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title
                    Text(
                        text = concept.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    // Subtitle based on status
                    Text(
                        text = getSubtitle(concept.status),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = getSubtitleColor(concept.status)
                    )
                }
            }

            // Right side: Chevron or Lock icon
            Icon(
                imageVector = if (isEnabled) Icons.Default.ChevronRight else Icons.Default.Lock,
                contentDescription = if (isEnabled) "Open concept" else "Locked",
                tint = if (isEnabled) TextSecondary else NotStartedTextColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ConceptStatusBadge(
    conceptNumber: String,
    status: ConceptStatus
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = when (status) {
                    ConceptStatus.COMPLETED -> CompleteIconBackground
                    ConceptStatus.IN_PROGRESS ->InProgressIconBackground
                    ConceptStatus.NOT_STARTED ->NotStartedIconBackground
                },
                shape = RoundedCornerShape(50)
            ),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            ConceptStatus.COMPLETED -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = AccentGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            ConceptStatus.IN_PROGRESS -> {
                Text(
                    text = conceptNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            ConceptStatus.NOT_STARTED -> {
                Text(
                    text = conceptNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NotStartedTextColor
                )
            }
        }
    }
}

// Helper Functions

private fun getSubtitle(status: ConceptStatus): String = when (status) {
    ConceptStatus.COMPLETED -> "Completed"
    ConceptStatus.IN_PROGRESS -> "Continue Learning"
    ConceptStatus.NOT_STARTED -> "Complete previous concepts"
}

private fun getSubtitleColor(status: ConceptStatus): Color = when (status) {
    ConceptStatus.COMPLETED -> CompleteTextColor
    ConceptStatus.IN_PROGRESS -> InProgressTextColor
    ConceptStatus.NOT_STARTED -> NotStartedTextColor
}

// Previews

@Preview
@Composable
fun ConceptItemCardCompletedPreview() {
    ConceptItemCard(
        concept = Concept(
            id = "1",
            name = "Introduction to Polynomials",
            status = ConceptStatus.COMPLETED
        )
    )
}

@Preview
@Composable
fun ConceptItemCardInProgressPreview() {
    ConceptItemCard(
        concept = Concept(
            id = "2",
            name = "Types of Polynomials",
            status = ConceptStatus.IN_PROGRESS
        )
    )
}

@Preview
@Composable
fun ConceptItemCardNotStartedPreview() {
    ConceptItemCard(
        concept = Concept(
            id = "3",
            name = "Operations on Polynomials",
            status = ConceptStatus.NOT_STARTED
        )
    )
}

@Preview
@Composable
fun ConceptStatusBadgeCompletedPreview() {
    ConceptStatusBadge(
        conceptNumber = "1",
        status = ConceptStatus.COMPLETED
    )
}

@Preview
@Composable
fun ConceptStatusBadgeInProgressPreview() {
    ConceptStatusBadge(
        conceptNumber = "2",
        status = ConceptStatus.IN_PROGRESS
    )
}

@Preview
@Composable
fun ConceptStatusBadgeNotStartedPreview() {
    ConceptStatusBadge(
        conceptNumber = "3",
        status = ConceptStatus.NOT_STARTED
    )
}