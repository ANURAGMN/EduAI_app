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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anurag.eduai.ui.theme.CardBackground
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextOnPrimary

@Composable
fun ChapterProgressCardOnHeader(
    completed: Int = 1,
    total: Int = 4
) {
    val dimens = LocalDimensions.current
    val progress = if (total > 0) (completed * 100) / total else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.spaceMedium, vertical = dimens.spaceSmall)
    ) {
        // Main Card Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.cornerRadiusMedium))
                .background(CardBackground)
                .padding(dimens.cardPadding)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Title and Percentage Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimens.spaceSmall),
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
                        text = "$progress%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextOnPrimary
                    )
                }

                // Card Around Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.cornerRadiusMedium))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(dimens.cardPadding)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimens.spaceExtraSmall)
                                .clip(RoundedCornerShape(dimens.cornerRadiusMedium)),
                            color = Color.White,
                            trackColor = HeaderGradientEnd
                        )

                        Spacer(modifier = Modifier.height(dimens.spaceSmall))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$completed of $total concepts completed",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Normal,
                                color = TextOnPrimary
                            )
                            Text(
                                text = "${total - completed} steps",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Normal,
                                color = TextOnPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}