package com.anurag.eduai.ui.screens.subjectscreen.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.anurag.eduai.R
import com.anurag.eduai.ui.models.SubjectUiModel
import com.anurag.eduai.ui.theme.CardBackground
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextOnAccent
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

/**
 * Composable function to display a Subject Card with subject details and a start learning button.
 * 1. Subject Initial in a colored box
 * 2. Subject Name
 * 3. Chapter Count
 * 4. Start Learning Button

 * @param subject The Subject data to display.
 * @param onClick Lambda function to handle card click events.
 * @param modifier Optional modifier for styling the card.
 */

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SubjectCard(
    subject: SubjectUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(dimens.cornerRadiusMedium),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = dimens.cardElevation
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = CardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(dimens.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(dimens.boxSizeSmall)
                    .background(
                        color = subject.color,
                        shape = RoundedCornerShape(dimens.cornerRadiusMedium)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Subject Initial
                // Load image from URL or show fallback emoji
                if (!subject.iconUrl.isNullOrEmpty()) {
                    GlideImage(
                        model = subject.iconUrl,
                        contentDescription = "${subject.name} icon",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimens.cornerRadiusMedium)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = stringResource(R.string.science_emoji),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextOnAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.spaceMedium))

            // Subject Name
            Text(
                text = subject.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(dimens.spaceSmall))

            // Total chapters in subject
            Text(
                text = pluralStringResource(
                    R.plurals.chapter_count,
                    subject.totalChapters,
                    subject.totalChapters
                ),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(dimens.spaceMedium))

            // Start Learning Button
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = subject.color),
                shape = RoundedCornerShape(dimens.cornerRadiusLarge)
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
