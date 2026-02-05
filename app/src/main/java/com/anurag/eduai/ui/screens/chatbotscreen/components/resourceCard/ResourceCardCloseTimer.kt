package com.anurag.eduai.ui.screens.chatbotscreen.components.resourceCard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.IconSecondary
import com.anurag.eduai.ui.theme.LocalDimensions


/**
 * Progress Timer with visual indicator
 */
@Composable
fun ResourceCardCloseTimer(
    timeRemaining: Int,
    totalDuration: Int,
    modifier: Modifier = Modifier
) {
    val progress = timeRemaining.toFloat() / totalDuration.toFloat()
    val dimens= LocalDimensions.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimens.cornerRadiusMedium),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal =dimens.messageHorizontalPadding, vertical = dimens.messageVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceMedium)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .height(dimens.progressIndicatorStrokeWidth)
                    .width(dimens.timerLength)
                    .clip(RoundedCornerShape(dimens.cornerRadiusSmall)),
                color = AccentBlue,
                trackColor = IconSecondary,
            )

            // Timer text
            Text(
                text = "${timeRemaining}s",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}