package com.anurag.eduai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.anurag.eduai.data.model.LessonStatus
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
    backgroundColor: Color,
    lessonStatus: LessonStatus,
    icon: @Composable () -> Unit,
    onClick: () -> Unit = {},
) {
    val dimes = LocalDimensions.current
    val isCompleted = lessonStatus == LessonStatus.COMPLETED

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimes.cornerRadiusRound))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(dimes.screenPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(dimes.iconExtraLarge)
                .clip(RoundedCornerShape(dimes.cornerRadiusRound))
                .background(iconColor),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(dimes.spaceMedium))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Black
            )
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