package com.anurag.eduai.ui.screens.conceptscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.ButtonDefaults.outlinedButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.anurag.eduai.R
import com.anurag.eduai.ui.models.ConceptStatus
import com.anurag.eduai.ui.models.ConceptUiModel
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.CardBackground
import com.anurag.eduai.ui.theme.CompleteTextColor
import com.anurag.eduai.ui.theme.InProgressTextColor
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.NotStartedTextColor
import com.anurag.eduai.ui.theme.TextOnPrimary
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.theme.White

/**
 * Composable function to display a Concept Card with status badge, title, concept completion status, and an icon.
 *
 * @param concept The Concept data to display.
 * @param onClick Lambda function to handle card click events.
 */
@Composable
fun ConceptCard(
    concept: ConceptUiModel,
    onClick: () -> Unit = {},
    onSimulationAgentClick: (String) -> Unit = {},
    onSimulationClick: (String,String) -> Unit = { _,_ -> }
) {
    val dimens = LocalDimensions.current
    val isEnabled = concept.status != ConceptStatus.NOT_STARTED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick),
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
                    text = getStatus(concept.status),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = getStatusColor(concept.status)
                )

                // Simulation Buttons
                if (concept.type.equals("SIMULATION", ignoreCase = true)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = dimens.spaceSmall),
                        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
                    ) {
                        Button(
                            onClick = { onSimulationAgentClick("simple_pendulum") },
                            enabled = isEnabled,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = dimens.spaceExtraSmall),
                            shape = MaterialTheme.shapes.small,
                            colors = buttonColors(
                                containerColor = if (isEnabled) AccentBlue else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                contentColor = TextPrimary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.agent),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        val hasValidUrl = !concept.simulationUrl.isNullOrBlank() &&
                                concept.simulationUrl != "Not found"

                        if (hasValidUrl) {
                        OutlinedButton(
                            onClick = {
                                onSimulationClick(concept.name, concept.simulationUrl)
                            },
                            enabled = isEnabled,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = dimens.spaceExtraSmall),
                            shape = MaterialTheme.shapes.small,
                            colors = outlinedButtonColors(
                                containerColor = White,
                                contentColor = TextPrimary,
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.simulation),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }}
                    }
                }
            }


            // Right side: Chevron or Lock icon
            Icon(
                imageVector = if (isEnabled) Icons.Default.ChevronRight else Icons.Default.Lock,
                contentDescription =
                    if (isEnabled) stringResource(R.string.open_concept)
                    else stringResource(R.string.locked),
                tint = if (isEnabled) TextSecondary else NotStartedTextColor,
                modifier = Modifier.size(dimens.iconLarge)
            )
        }
    }
}

// Helper Functions for Status Texts and Colors
@Composable
private fun getStatus(status: ConceptStatus): String = when (status) {
    ConceptStatus.COMPLETED -> stringResource(R.string.completed)
    ConceptStatus.IN_PROGRESS -> stringResource(R.string.in_progress_continue_learning)
    ConceptStatus.NOT_STARTED -> stringResource(R.string.complete_previous_concepts)
}

private fun getStatusColor(status: ConceptStatus): Color = when (status) {
    ConceptStatus.COMPLETED -> CompleteTextColor
    ConceptStatus.IN_PROGRESS -> InProgressTextColor
    ConceptStatus.NOT_STARTED -> NotStartedTextColor
}