package com.anurag.eduai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.TextPrimary

/**
 * A method to say greeting based on time of day
 */
fun getGreeting(): String {
    val hour = java.time.LocalTime.now().hour

    return when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Good Night"
    }
}

@Composable
fun HomeScreenTopBar(
    userName: String = "John Doe",
    subject: String = "Science",
    streakDays: Int = 7,
    onChangeSubject: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HeaderGradientStart,
                        HeaderGradientEnd
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = getGreeting(),
                color = TextPrimary,
                fontSize = 14.sp
            )

            Text(
                text = userName,
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            SubjectCard(
                subject,
                onChangeClick = onChangeSubject
            )
            StreakCard(streakDays)
        }
    }
}