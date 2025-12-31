package com.anurag.eduai.ui.screens.chapterscreen.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anurag.eduai.ui.theme.ColorError
import com.anurag.eduai.ui.theme.ColorSuccess
import com.anurag.eduai.ui.theme.ColorWarning

@Composable
fun StatusBadge(status: ChapterStatus) {
    when (status) {
        ChapterStatus.COMPLETED -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = ColorSuccess,
                modifier = Modifier.size(24.dp)
            )
        }
        ChapterStatus.IN_PROGRESS -> {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "In Progress",
                tint = ColorWarning,
                modifier = Modifier.size(24.dp)
            )
        }
        ChapterStatus.NOT_STARTED -> {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Not Started",
                tint = ColorError,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview
@Composable
fun StatusBadgeCompletePreview() {
    StatusBadge(status = ChapterStatus.COMPLETED)
}
@Preview
@Composable
fun StatusBadgeInProgressPreview() {
    StatusBadge(status = ChapterStatus.IN_PROGRESS)
}
@Preview
@Composable
fun StatusBadgeNotStartedPreview() {
    StatusBadge(status = ChapterStatus.NOT_STARTED)
}