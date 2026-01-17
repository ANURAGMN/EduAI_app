package com.anurag.eduai.ui.screens.chapterscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anurag.eduai.ui.screens.chapterscreen.ChapterStatus
import com.anurag.eduai.ui.theme.ColorError
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.ColorSuccess
import com.anurag.eduai.ui.theme.ColorWarning

/**
 * A chip component to display the status of a chapter as a colored badge
 * checking using instead of status badge
 *
 * @param status The status of the chapter
 */
@Composable
fun StatusChip(status: ChapterStatus) {
    val (backgroundColor, textColor, statusText) = when (status) {
        ChapterStatus.COMPLETED -> Triple(
            ColorSuccess.copy(alpha = 0.2f),
            ColorSuccess,
            "Completed"
        )
        ChapterStatus.IN_PROGRESS -> Triple(
            ColorWarning.copy(alpha = 0.2f),
            ColorWarning,
            "In Progress"
        )
        ChapterStatus.NOT_STARTED -> Triple(
            ColorError.copy(alpha = 0.2f),
            ColorHint,
            "Not Started"
        )
    }

    Text(
        text = statusText,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Preview
@Composable
fun StatusChipCompletedPreview() {
    StatusChip(status = ChapterStatus.COMPLETED)
}

@Preview
@Composable
fun StatusChipInProgressPreview() {
    StatusChip(status = ChapterStatus.IN_PROGRESS)
}

@Preview
@Composable
fun StatusChipNotStartedPreview() {
    StatusChip(status = ChapterStatus.NOT_STARTED)
}

