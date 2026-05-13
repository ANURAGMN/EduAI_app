package com.ncert7.aitutorandlab.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ncert7.aitutorandlab.R
import com.ncert7.aitutorandlab.ui.theme.BrandPrimary
import com.ncert7.aitutorandlab.ui.theme.HeaderGradientEnd
import com.ncert7.aitutorandlab.ui.theme.HeaderGradientStart
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.TextPrimary

@Composable
fun LoadingHomeHeader(
    subject: String,
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
                text = stringResource(R.string.good_morning),
                color = TextPrimary,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.loading),
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            HomeScreenSubjectCard(subject, onChangeClick = onChangeSubject)

            // Loading placeholder for streak card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                HeaderGradientStart.copy(alpha = 0.5f),
                                HeaderGradientEnd.copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(dimes.cornerRadiusRound)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        }
    }
}