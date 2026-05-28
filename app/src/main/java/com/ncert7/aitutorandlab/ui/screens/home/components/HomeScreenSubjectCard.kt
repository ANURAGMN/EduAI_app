package com.ncert7.aitutorandlab.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MenuBook
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
fun HomeScreenSubjectCard(
    subject: String,
    onChangeClick: () -> Unit = {}
) {
    val dimes = LocalDimensions.current
    Row(
        modifier = Modifier
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
            .clickable(onClick = onChangeClick)
            .padding(dimes.screenPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.MenuBook,
            contentDescription = "Book Icon",
            tint = White,
            modifier = Modifier.size(dimes.iconLarge)
        )

        Spacer(modifier = Modifier.width(dimes.spaceSmall))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.current_subject),
                style = MaterialTheme.typography.titleSmall,
                color = White.copy(alpha = 0.7f)
            )
            Text(
                text = subject,
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