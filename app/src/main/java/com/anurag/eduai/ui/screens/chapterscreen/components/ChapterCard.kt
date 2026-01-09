package com.anurag.eduai.ui.screens.chapterscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anurag.eduai.ui.screens.chapterscreen.Chapter
import com.anurag.eduai.ui.screens.chapterscreen.ChapterStatus
import com.anurag.eduai.ui.theme.CardBackground
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary


/**
 * A card component to display chapter information
 * 1. Chapter ID and Name
 * 2. Chapter Count
 * 3. Status Badge(Completed, In Progress, Not Started)
 * 4. Action Buttons: Study, Videos, Simulations...
 *
 * @param chapter The chapter data to display.
 * @param onStudyClick Callback when the "Study" button is clicked.
 *
 */
@Composable
fun ChapterCard(
    chapter: Chapter,
    onStudyClick: () -> Unit = {},
) {
    val dimens = LocalDimensions.current
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = CardDefaults.shape,
        colors =CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimens.cardElevation
        ),

    ) {
        // Chapter title and status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.spaceSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = chapter.id,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ){
                    Text(
                        text = chapter.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

            }
            // Status Badge
            StatusBadge(status = chapter.status)
        }

        Spacer(modifier = Modifier.height(dimens.spaceSmall))
        // total  concepts  in chapter
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
        ) {
            Spacer(modifier = Modifier.width(dimens.spaceSmall))
            Text(
                text = chapter.chapterCount,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(dimens.spaceSmall))

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.spaceSmall),
            horizontalArrangement = Arrangement.Absolute.SpaceEvenly
        ) {
            ChapterActionButton(
                label = "Study",
                icon = "📚",
                modifier = Modifier.weight(1f),
                onClick = onStudyClick
            )
            ChapterActionButton(
                label = "Videos",
                icon = "🎬",
                modifier = Modifier.weight(1f),
                onClick = {}
            )
            ChapterActionButton(
                label = "Simulations",
                icon = "🧪",
                modifier = Modifier.weight(1f),
                onClick = {}
            )
        }

    }
}

@Preview
@Composable
fun ChapterCardPreview() {
    ChapterCard(
        chapter = Chapter(
            id = "1",
            name = "Number Systems",
            chapterCount = "8 main concepts",
            status = ChapterStatus.NOT_STARTED
        )
    )
}

