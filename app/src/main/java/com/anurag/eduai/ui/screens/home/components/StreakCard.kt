package com.anurag.eduai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.Dimensions
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.White

@Composable
fun StreakCard(
    days: String,
    modifier: Modifier = Modifier
) {
    val dimes = LocalDimensions.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF7F63FF),
                        Color(0xFF9B4DFF),
                        Color(0xFFB03BFE)
                    )
                ),
                shape = RoundedCornerShape(Dimensions.Compact.cornerRadiusRound)
            )
            .padding(dimes.screenPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "\uD83D\uDD25", // fire emoji instead of icon
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.width(Dimensions.Compact.spaceSmall))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = days,
                style = MaterialTheme.typography.titleLarge,
                color = White
            )
            Text(
                text = stringResource(R.string.day_streak),
                style = MaterialTheme.typography.titleSmall,
                color = White.copy(alpha = 0.7f)
            )
        }
        
        Text(
            text = "\uD83C\uDFC6", // Trophy emoji instead of icon
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
