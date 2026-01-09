package com.anurag.eduai.ui.screens.subjectscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.anurag.eduai.R
import com.anurag.eduai.ui.screens.subjectscreen.Subject
import com.anurag.eduai.ui.theme.CardBackground
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextOnAccent
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary

/**
 * Composable function to display a Subject Card with subject details and a start learning button.
 * 1. Subject Initial in a colored box
 * 2. Subject Name
 * 3. Chapter Count
 * 4. Start Learning Button

 * @param subject The Subject data to display.
 * @param onClick Lambda function to handle card click events.
 */
@Composable
fun SubjectCard(
    subject: Subject,
    onClick: (Subject) -> Unit = {}
) {
    val dimens = LocalDimensions.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{ onClick(subject) },
        shape=RoundedCornerShape(dimens.cornerRadiusMedium),
        elevation =CardDefaults.elevatedCardElevation(
            defaultElevation = dimens.cardElevation
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = CardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(dimens.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Box(
                modifier = Modifier
                    .size(dimens.boxSizeSmall)
                    .background(
                        color = subject.color,
                        shape = RoundedCornerShape(dimens.cornerRadiusMedium)
                    ),
                contentAlignment = Alignment.Center
            ) {// Subject Initial
                Text(
                    text = subject.name.first().toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextOnAccent
                )
            }

            Spacer(modifier = Modifier.height(dimens.spaceMedium))

            // Subject Name
            Text(
                text = subject.name,
                style= MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(dimens.spaceSmall))

            // total chapters in subject
            Text(
                text = "${subject.chapterCount} Chapters",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(dimens.spaceMedium))

            // Start Learning Button
            Button(
                onClick = { onClick(subject) },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = subject.color),
                shape = RoundedCornerShape(dimens.cornerRadiusMedium)
            ) {
                Text(
                    text = stringResource(R.string.start_learning),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextOnAccent,
                )
            }
        }
    }
}