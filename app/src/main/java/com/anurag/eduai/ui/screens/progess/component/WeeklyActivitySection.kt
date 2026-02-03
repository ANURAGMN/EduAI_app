package com.anurag.eduai.ui.screens.progess.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.CardBackground
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.WeeklyActivityBarColor
import com.anurag.eduai.ui.viewModel.DayProgress

/**
 * Weekly Activity Section Component
 * Pure UI component - displays weekly activity bar chart
 * NO business logic, NO data processing, NO hardcoded values
 * All data and calculations come from ViewModel
 */
@Composable
fun WeeklyActivitySection(
    weeklyProgressData: List<DayProgress>,
    maxValue: Int,
    getBarHeight: (Int) -> Float
) {
    val dimes = LocalDimensions.current

    Column {
        Text(
            text = stringResource(R.string.weekly_activity),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = dimes.spaceMedium)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = dimes.cardElevation),
            shape = RoundedCornerShape(dimes.cornerRadiusMedium)
        ) {
            Column(modifier = Modifier.padding(dimes.screenPadding)) {
                // Bar Chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimes.weeklyActivityCardHeight),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyProgressData.forEach { day ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val barHeight = getBarHeight(day.count)

                            // Bar
                            Box(
                                modifier = Modifier
                                    .width(dimes.spaceLarge)
                                    .height(barHeight.dp)
                                    .background(
                                        color = WeeklyActivityBarColor,
                                        shape = RoundedCornerShape(
                                            topStart = dimes.cornerRadiusSmall,
                                            topEnd = dimes.cornerRadiusSmall
                                        )
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimes.spaceSmall))

                // Day Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyProgressData.forEach { day ->
                        Text(
                            text = day.dayLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = ColorHint,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}