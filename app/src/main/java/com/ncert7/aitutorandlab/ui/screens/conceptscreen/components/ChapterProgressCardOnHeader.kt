package com.ncert7.aitutorandlab.ui.screens.conceptscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ncert7.aitutorandlab.R
import com.ncert7.aitutorandlab.ui.models.ChapterProgressUiModel
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.TextOnPrimary

/**
 * Progress card displayed in the header of ConceptScreen
 * Shows chapter completion progress
 *
 * @param progress ChapterProgressUiModel containing all calculated progress data
 */
@Composable
fun ChapterProgressCardOnHeader(
    progress: ChapterProgressUiModel
) {
    val dimens = LocalDimensions.current

    // Main Card Container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cornerRadiusMedium))
            .background(color = TextOnPrimary.copy(alpha = 0.12f))
            .padding(dimens.cardPadding)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Title and Percentage Row - compact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.chapter_progress),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextOnPrimary
                )
                Text(
                    text = "${progress.progressPercentage}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary
                )
            }

            Spacer(modifier = Modifier.height(dimens.spaceSmall))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(dimens.cornerRadiusSmall)),
                color = TextOnPrimary,
                trackColor = TextOnPrimary.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(dimens.spaceSmall))

            Text(
                text = stringResource(
                    R.string.progress_status,
                    progress.completed,
                    progress.total,
                    progress.remaining
                ),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
                color = TextOnPrimary.copy(alpha = 0.9f)
            )
        }
    }
}