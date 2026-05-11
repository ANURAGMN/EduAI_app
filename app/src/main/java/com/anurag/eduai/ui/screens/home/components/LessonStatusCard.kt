package com.anurag.eduai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.anurag.eduai.R
import com.anurag.eduai.domain.progress.model.ProgressStatus
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.Black
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.ColorSuccess
import com.anurag.eduai.ui.theme.LocalDimensions

@Composable
fun LessonStatusCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    iconColor: Color,
    progressPercentage: Int = 0,
    backgroundColor: Color,
    progressStatus: ProgressStatus,
    icon: @Composable () -> Unit,
    onClick: () -> Unit = {},
) {
    val dimens = LocalDimensions.current
    val isCompleted = progressStatus == ProgressStatus.COMPLETED
    val isInProgress = progressStatus == ProgressStatus.IN_PROGRESS

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cornerRadiusRound))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(dimens.screenPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(dimens.iconExtraLarge)
                .clip(RoundedCornerShape(dimens.cornerRadiusRound))
                .background(iconColor),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(dimens.spaceMedium))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Black
            )
            // Show progress percentage for in-progress concepts
            if (isInProgress && progressPercentage > 0) {
                Spacer(modifier = Modifier.height(dimens.spaceSmall))
                Text(
                    text = "$progressPercentage%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentBlue
                )
            }
            Text(
                text = if (isCompleted) subtitle else stringResource(R.string.pending),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCompleted) ColorSuccess else AccentBlue
            )
        }

        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ColorHint
            )
        }
    }
}