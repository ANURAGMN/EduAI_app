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
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.data.local.dao.DailyConceptCount
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.AccentGreen
import com.anurag.eduai.ui.theme.Black
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.utils.WeeklyProgressUtils
import java.time.LocalDate

// helper model
data class DayProgress(
    val dayLabel: String,
    val count: Int
)


@Composable
fun WeeklyActivitySection(
    weeklyProgressList: List<DailyConceptCount>
) {
    val util = WeeklyProgressUtils()

    val today = LocalDate.now()
    val last7day = (6 downTo 0).map { today.minusDays(it.toLong()).toString() }

    // converting the list to a map for easy use
    val progressMap = weeklyProgressList.associateBy { it.date }

    // Building full 7-day dataset :
    // missing day = 0 (or if days not found)
    val weeklyData = last7day.map { date ->
        DayProgress(
            dayLabel = util.getDayOfWeek(date),
            count = progressMap[date]?.count ?: 0 // if not found then 0
        )
    }

    val maxValue = (weeklyData.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)

    // UI layout
    Column {
        Text(
            text = "Weekly Activity",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Black,
            modifier = Modifier.padding(bottom = 15.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Bar Chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyData.forEach { day ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val barHeight =
                                (day.count.toFloat() / maxValue * 100)
                                    .coerceAtLeast(4f)
                            // Bar
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(barHeight.dp)
                                    .background(
                                        color = AccentGreen,
                                        shape = RoundedCornerShape(
                                            topStart = 6.dp,
                                            topEnd = 6.dp
                                        )
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyData.forEach { day ->
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
