package com.anurag.eduai.ui.screens.chapterscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.ui.screens.chapterscreen.Chapter
import com.anurag.eduai.ui.screens.chapterscreen.ChapterStatus
import com.anurag.eduai.ui.theme.ChipBackground
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary

@Composable
fun ChapterCard(
    chapter: Chapter,
    onStudyClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        // Chapter title and status
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = chapter.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            StatusBadge(status = chapter.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = chapter.chapterCount,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }


        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

@Composable
fun ChapterActionButton(
    label: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .border(1.dp, ChipBackground, RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$icon $label",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Preview
@Composable
fun ChapterCardPreview() {
    ChapterCard(
        chapter = Chapter(
            id = "1",
            name = "Number Systems",
            chapterCount = "8 main chapters",
            status = ChapterStatus.COMPLETED
        )
    )
}

@Preview
@Composable
fun ChapterActionButtonPreview() {
    ChapterActionButton(
        label = "Study",
        icon = "📚"
    )
}