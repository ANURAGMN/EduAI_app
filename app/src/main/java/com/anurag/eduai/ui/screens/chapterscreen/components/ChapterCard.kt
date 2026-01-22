package com.anurag.eduai.ui.screens.chapterscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.anurag.eduai.ui.screens.chapterscreen.Chapter
import com.anurag.eduai.ui.screens.chapterscreen.ChapterStatus
import com.anurag.eduai.ui.theme.CardBackground
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.CompleteTextColor
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.InProgressTextColor
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary


/**
 * A card component to display chapter information
 * 1. Chapter ID and Name
 * 2. Status Badge (Completed, In Progress)
 * 3. Progress Bar showing completion percentage
 * 4. Action Buttons: Study, Simulations
 *
 * @param chapter The chapter data to display.
 * @param completedConcepts Number of completed concepts in this chapter
 * @param totalConcepts Total number of concepts in this chapter
 * @param status The current status of the chapter
 * @param onStudyClick Callback when the "Study" button is clicked.
 */
@Composable
fun ChapterCard(
    chapter: Chapter,
    completedConcepts: Int = 0,
    totalConcepts: Int = 0,
    status: ChapterStatus = ChapterStatus.NOT_STARTED,
    onStudyClick: () -> Unit = {},
) {
    val dimens = LocalDimensions.current
    val progress = if (totalConcepts > 0) completedConcepts.toFloat() / totalConcepts.toFloat() else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.cardPadding)
    ) {
        // Status Badge - positioned above the card
        if (status != ChapterStatus.NOT_STARTED) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-16).dp, y = (-8).dp)
                    .zIndex(1f)
                    .background(
                        color = getChapterStatusColor(status),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = getChapterStatusText(status),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }

        // Main Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimens.cornerRadiusLarge),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = dimens.cardElevation
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Chapter Order + name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Chapter number
                    Text(
                        text = chapter.id,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary.copy(alpha = 0.5f)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = chapter.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Concept count
                        Text(
                            text = "$totalConcepts main concepts",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(dimens.spaceMedium))

                        // Progress label and percentage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(dimens.spaceSmall))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(dimens.cornerRadiusRound)),
                            color = HeaderGradientStart,
                            trackColor = ColorHint,
                        )

                        Spacer(modifier = Modifier.height(dimens.spaceMedium))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
                        ) {
                            // First row of buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ChapterActionButton(
                                    label = "Study",
                                    icon = "📚",
                                    modifier = Modifier.weight(1f),
                                    onClick = onStudyClick
                                )
                                ChapterActionButton(
                                    label = "Simulation",
                                    icon = "🧪",
                                    modifier = Modifier.weight(1f),
                                    onClick = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper functions for status badge
private fun getChapterStatusText(status: ChapterStatus): String = when (status) {
    ChapterStatus.COMPLETED -> "Completed"
    ChapterStatus.IN_PROGRESS -> "In Progress"
    ChapterStatus.NOT_STARTED -> ""
}

private fun getChapterStatusColor(status: ChapterStatus): Color = when (status) {
    ChapterStatus.COMPLETED -> CompleteTextColor
    ChapterStatus.IN_PROGRESS -> InProgressTextColor
    ChapterStatus.NOT_STARTED -> Color.Gray
}

@Preview
@Composable
fun ChapterCardPreview() {
    ChapterCard(
        chapter = Chapter(
            id = "1",
            name = "Number Systems",
            conceptCount = "8",
        ),
        completedConcepts = 5,
        totalConcepts = 8,
        status = ChapterStatus.IN_PROGRESS
    )
}