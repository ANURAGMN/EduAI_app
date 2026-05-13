package com.ncert7.aitutorandlab.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import com.ncert7.aitutorandlab.R
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.SubjectCardGradientCenter
import com.ncert7.aitutorandlab.ui.theme.SubjectCardGradientEnd
import com.ncert7.aitutorandlab.ui.theme.SubjectCardGradientStart
import com.ncert7.aitutorandlab.ui.theme.White

@Composable
fun StreakCard(days: Int, modifier: Modifier = Modifier) {
    val dimes = LocalDimensions.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        SubjectCardGradientStart,
                        SubjectCardGradientCenter,
                        SubjectCardGradientEnd
                    )
                ),
                shape = RoundedCornerShape(dimes.cornerRadiusRound)
            )
            .padding(dimes.screenPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.LocalFireDepartment,
            contentDescription = "Fire Icon",
            tint = White,
            modifier = Modifier.size(dimes.iconLarge)
        )

        Spacer(modifier = Modifier.width(dimes.spaceSmall))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = "$days", style = MaterialTheme.typography.titleLarge, color = White)
            Text(
                text = stringResource(R.string.day_streak),
                style = MaterialTheme.typography.titleSmall,
                color = White.copy(alpha = 0.7f)
            )
        }

        Icon(
            imageVector = Icons.Outlined.EmojiEvents,
            contentDescription = "Trophy Icon",
            tint = White,
            modifier = Modifier.size(dimes.iconLarge)
        )
    }
}