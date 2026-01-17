package com.anurag.eduai.ui.screens.chapterscreen.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.anurag.eduai.R
import com.anurag.eduai.ui.screens.chapterscreen.ChapterStatus
import com.anurag.eduai.ui.theme.ColorError
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.ColorSuccess
import com.anurag.eduai.ui.theme.ColorWarning
import com.anurag.eduai.ui.theme.LocalDimensions

/**
 * A badge component to display the status of a chapter.
 * 1. Completed - Check Icon (Green)
 * 2. In Progress - Play Icon (Yellow)
 * 3. Not Started - Lock Icon (Red)
 *
 * @param status The status of the chapter.
 */
@Composable
fun StatusBadge(status: ChapterStatus) {
    val dimens = LocalDimensions.current
    when (status) {
        ChapterStatus.COMPLETED -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.completed),
                tint = ColorSuccess,
                modifier = Modifier.size(dimens.iconLarge)
            )
        }
        ChapterStatus.IN_PROGRESS -> {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.in_progress),
                tint = ColorWarning,
                modifier = Modifier.size(dimens.iconLarge)
            )
        }
        ChapterStatus.NOT_STARTED -> {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = stringResource(R.string.not_started),
                tint = ColorHint,
                modifier = Modifier.size(dimens.iconLarge)
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