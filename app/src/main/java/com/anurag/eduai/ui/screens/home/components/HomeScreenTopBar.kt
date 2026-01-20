package com.anurag.eduai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import java.time.LocalTime

/** A method to say greeting based on time of day TODO: Move this method to viewmodel */
@Composable
fun getGreeting(): String {
    val hour = remember { LocalTime.now().hour }

    return when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Good Night"
    // TODO: remove hard coded string
    }
}

@Composable
fun HomeScreenTopBar(
    userName: String,
    subject: String,
    streakDays: Int,
    onChangeSubject: () -> Unit = {}
) {
    val dimes = LocalDimensions.current
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    HeaderGradientStart,
                                    HeaderGradientEnd
                                )
                        ),
                    shape =
                        RoundedCornerShape(
                            bottomStart = dimes.cornerRadiusRound,
                            bottomEnd = dimes.cornerRadiusRound
                        )
                )
                .padding(dimes.screenPadding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(dimes.spaceSmall)) {
            Text(
                text = getGreeting(),
                color = TextPrimary,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = userName,
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            HomeScreenSubjectCard(subject, onChangeClick = onChangeSubject)
            StreakCard(streakDays, modifier = Modifier.fillMaxWidth())
        }
    }
}
