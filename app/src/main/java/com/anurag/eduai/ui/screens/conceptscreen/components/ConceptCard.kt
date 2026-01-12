package com.anurag.eduai.ui.screens.conceptscreen.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.anurag.eduai.R
import com.anurag.eduai.ui.screens.conceptscreen.Concept
import com.anurag.eduai.ui.screens.conceptscreen.ConceptStatus
import com.anurag.eduai.ui.theme.CardBackground
import com.anurag.eduai.ui.theme.CompleteTextColor
import com.anurag.eduai.ui.theme.InProgressTextColor
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.NotStartedTextColor
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary

/**
 * Composable function to display a Concept Card with status badge, title, concept completion status, and an icon.
 *
 * @param concept The Concept data to display.
 * @param onClick Lambda function to handle card click events.
 */
@Composable
fun ConceptCard(
    concept: Concept,
    onClick: () -> Unit = {}
) {
    val dimens = LocalDimensions.current
    val isEnabled = concept.status != ConceptStatus.NOT_STARTED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardDefaults.shape,
        colors =CardDefaults.cardColors(
            containerColor = CardBackground,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimens.cardElevation,
        )
    ){
        // Left side: Badge + Content
        Row(
            modifier = Modifier
                .padding(dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
        ) {
                // Status badge (Circle with icon/order)
                ConceptStatusBadge(
                    conceptOrder = concept.order.toString(),
                    status = concept.status
                )

                // Content (Title + Status)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal =dimens.inputHorizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
                ) {
                    // Title
                    Text(
                        text = concept.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) TextPrimary else TextSecondary
                    )

                    // Concept Completion status
                    Text(
                        text = getStatus(
                            concept.status,
                            context = LocalContext.current
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = getStatusColor(concept.status)
                    )
                }


            // Right side: Chevron or Lock icon
//            Icon(
//                imageVector = if (isEnabled) Icons.Default.ChevronRight else Icons.Default.Lock,
//                contentDescription =
//                    if (isEnabled) stringResource(R.string.open_concept)
//                    else stringResource(R.string.locked),
//                tint = if (isEnabled) TextSecondary else NotStartedTextColor,
//                modifier = Modifier.size(dimens.iconLarge)
//            )
        }
    }
}

// Helper Functions for Status Texts and Colors
private fun getStatus(status: ConceptStatus,context: Context): String = when (status) {
    ConceptStatus.COMPLETED -> context.getString(R.string.completed)
    ConceptStatus.IN_PROGRESS -> context.getString(R.string.in_progress_continue_learning)
    ConceptStatus.NOT_STARTED -> context.getString(R.string.complete_previous_concepts)
}

private fun getStatusColor(status: ConceptStatus): Color = when (status) {
    ConceptStatus.COMPLETED -> CompleteTextColor
    ConceptStatus.IN_PROGRESS -> InProgressTextColor
    ConceptStatus.NOT_STARTED -> NotStartedTextColor
}