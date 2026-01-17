package com.anurag.eduai.ui.screens.conceptscreen.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anurag.eduai.ui.theme.CardBackground
import com.anurag.eduai.ui.theme.ColorSuccess
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextOnPrimary
import com.anurag.eduai.ui.theme.TextSecondary

/**
 * Progress card displayed in the header of ConceptScreen
 * Shows chapter completion progress
 *
 * @param completed Number of completed concepts
 * @param total Total number of concepts in the chapter
 */
@Composable
fun ChapterProgressCardOnHeader(
    completed: Int = 0,
    total: Int = 0
) {
    val dimens = LocalDimensions.current

    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
    val progressPercentage = (progress * 100).toInt()

// Main Card Container
 Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cornerRadiusMedium))
            .background(    color = TextOnPrimary.copy(alpha = 0.12f))
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
                    text = "Chapter Progress",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextOnPrimary
                )
                Text(
                    text = "$progressPercentage%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary
                )
            }

            Spacer(modifier = Modifier.height(dimens.spaceSmall))

            // Thinner Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(dimens.cornerRadiusSmall)),
                color = TextOnPrimary,
                trackColor = TextOnPrimary.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(dimens.spaceSmall))

            Text(
                text = "$completed of $total • ${total - completed} left",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
                color = TextOnPrimary.copy(alpha = 0.9f)
            )
        }
    }
}