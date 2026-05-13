package com.ncert7.aitutorandlab.ui.screens.home.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.ncert7.aitutorandlab.ui.theme.HeaderGradientEnd
import com.ncert7.aitutorandlab.ui.theme.HeaderGradientStart
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.TextPrimary

@Composable
fun HomeScreenTopBar(
    userName: String,
    subject: String,
    streakDays: Int,
    greeting: String,
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
                text = greeting,
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