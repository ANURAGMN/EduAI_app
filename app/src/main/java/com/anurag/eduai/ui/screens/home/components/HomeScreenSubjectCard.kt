package com.anurag.eduai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.Dimensions
import com.anurag.eduai.ui.theme.White
import java.util.Locale
import java.util.Locale.getDefault

@Composable
fun HomeScreenSubjectCard(
    subject: String,
    onChangeClick: () -> Unit = {}
) {
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
            .clickable(onClick = onChangeClick)
            .padding(Dimensions.Compact.screenPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "\uD83D\uDCD6", // 📖 icon
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.width(Dimensions.Compact.spaceSmall))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.current_subject),
                style = MaterialTheme.typography.titleSmall,
                color = White.copy(alpha = 0.7f)
            )
            Text(
                text = subject.replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() },
                style = MaterialTheme.typography.titleLarge,
                color = White
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.change), color = White.copy(alpha = 0.7f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = White.copy(alpha = 0.7f)
            )
        }
    }
}
