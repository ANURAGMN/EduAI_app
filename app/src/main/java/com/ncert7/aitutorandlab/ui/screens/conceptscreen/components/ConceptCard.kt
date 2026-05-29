package com.ncert7.aitutorandlab.ui.screens.conceptscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import com.ncert7.aitutorandlab.R
import com.ncert7.aitutorandlab.domain.progress.model.ProgressStatus
import com.ncert7.aitutorandlab.ui.models.ConceptUiModel
import com.ncert7.aitutorandlab.ui.theme.AccentBlue
import com.ncert7.aitutorandlab.ui.theme.CardBackground
import com.ncert7.aitutorandlab.ui.theme.CompleteTextColor
import com.ncert7.aitutorandlab.ui.theme.InProgressTextColor
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.NotStartedTextColor
import com.ncert7.aitutorandlab.ui.theme.TextPrimary
import com.ncert7.aitutorandlab.ui.theme.TextSecondary
import com.ncert7.aitutorandlab.ui.theme.White

/**
 * Composable function to display a Concept Card with status badge, title, concept completion status, and action buttons.
 *
 * The buttons displayed depend on the concept type:
 * - STUDY: Single clickable card (navigates to chat screen)
 * - MATH PROBLEM: Shows "Start Problem" button (navigates to math agent with problemId)
 * - SIMULATION: Shows "Agent" button (if simulationId exists) and "Simulation" button (if simulationUrl exists)
 *
 * @param concept The Concept data to display.
 * @param serialNumber The serial number (1, 2, 3, ...) to display in the badge.
 * @param onClick Lambda function to handle main card click (STUDY type).
 * @param onSimulationAgentClick Lambda to handle simulation agent button click.
 * @param onSimulationClick Lambda to handle simulation URL button click.
 */
@Composable
fun ConceptCard(
    concept: ConceptUiModel,
    serialNumber: Int = 1,
    onClick: (conceptId: String, problemId: String, conceptType: String) -> Unit = { _, _, _ -> },
    onSimulationAgentClick: (String, String) -> Unit = { _, _ -> },
    onSimulationClick: (title: String, url: String, conceptId: String) -> Unit = { _, _, _ -> },
) {
    val dimens = LocalDimensions.current

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(
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
                conceptOrder = serialNumber.toString(),
                status = concept.status
            )

            // Content (Title + Status)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = dimens.inputHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
            ) {
                Text(
                    text = concept.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Text(
                    text = getStatus(concept.status),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = getStatusColor(concept.status)
                )

                when {
                    concept.type.equals("STUDY", ignoreCase = true) -> {
                        StudyConceptButtons(
                            conceptId = concept.id,
                            onClick = onClick
                        )
                    }
                    concept.type.equals("MATH PROBLEM", ignoreCase = true) -> {
                        MathProblemButtons(
                            conceptId = concept.id,
                            problemId = concept.problemId,
                            onClick = onClick
                        )
                    }
                    concept.type.equals("SIMULATION", ignoreCase = true) -> {
                        SimulationConceptButtons(
                            concept = concept,
                            onSimulationAgentClick = onSimulationAgentClick,
                            onSimulationClick = onSimulationClick
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.open_concept),
                tint = TextSecondary,
                modifier = Modifier.size(dimens.iconLarge)
            )
        }
    }
}


/**
 * Buttons for STUDY type concepts
 * Single button that navigates to chat/tutoring screen
 */
@Composable
private fun StudyConceptButtons(
    conceptId: String,
    onClick: (conceptId: String, problemId: String, conceptType: String) -> Unit
) {
    val dimens = LocalDimensions.current

    Button(
        onClick = { onClick(conceptId, "", "STUDY") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimens.spaceSmall),
        contentPadding = PaddingValues(horizontal = dimens.spaceExtraSmall),
        shape = MaterialTheme.shapes.small,
        colors = buttonColors(
            containerColor = AccentBlue,
            contentColor = White
        )
    ) {
        Text(
            text = stringResource(R.string.start_learning),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = White
        )
    }
}

/**
 * Buttons for MATH PROBLEM type concepts
 * Single button labeled "Start Problem" that opens math agent with problemId
 */
@Composable
private fun MathProblemButtons(
    conceptId: String,
    problemId: String,
    onClick: (conceptId: String, problemId: String, conceptType: String) -> Unit
) {
    val dimens = LocalDimensions.current

    Button(
        onClick = { onClick(conceptId, problemId, "MATH PROBLEM") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimens.spaceSmall),
        contentPadding = PaddingValues(horizontal = dimens.spaceExtraSmall),
        shape = MaterialTheme.shapes.small,
        colors = buttonColors(
            containerColor = AccentBlue,
            contentColor = White
        )
    ) {
        Text(
            text = stringResource(R.string.problem_to_solve),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = White
        )
    }
}

/**
 * Buttons for SIMULATION type concepts
 * Shows "Agent" button (if simulationId exists) and "Simulation" button (if simulationUrl exists)
 */
@Composable
private fun SimulationConceptButtons(
    concept: ConceptUiModel,
    onSimulationAgentClick: (String, String) -> Unit,
    onSimulationClick: (title: String, url: String, conceptId: String) -> Unit
) {
    val dimens = LocalDimensions.current

    val hasAgent = concept.simulationId?.isNotBlank() == true && concept.simulationId != "null"
    val hasUrl = concept.simulationUrl?.isNotBlank() == true && concept.simulationUrl != "null"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimens.spaceSmall),
        verticalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
    ) {
        // Row for Agent and Simulation buttons if both exist
        if (hasAgent && hasUrl) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
            ) {
                // Agent button
                Button(
                    onClick = { onSimulationAgentClick(concept.simulationId ?: "", concept.id) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = dimens.spaceExtraSmall),
                    shape = MaterialTheme.shapes.small,
                    colors = buttonColors(
                        containerColor = AccentBlue,
                        contentColor = White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.agent),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = White
                    )
                }

                // Simulation button
                OutlinedButton(
                    onClick = { onSimulationClick(concept.name, concept.simulationUrl, concept.id) },
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
                    )
                }
            }
        } else if (hasAgent && concept.simulationId.isNotEmpty()) {
            // Only Agent button
            Button(
                onClick = { onSimulationAgentClick(concept.simulationId, concept.id) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = dimens.spaceExtraSmall),
                shape = MaterialTheme.shapes.small,
                colors = buttonColors(
                    containerColor = AccentBlue,
                    contentColor = White
                )
            ) {
                Text(
                    text = stringResource(R.string.agent),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = White
                )
            }
        } else if (hasUrl && concept.simulationUrl.isNotEmpty()) {
            // Only Simulation button
            OutlinedButton(
                onClick = { onSimulationClick(concept.name, concept.simulationUrl, concept.id) },
                modifier = Modifier.fillMaxWidth(),
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
                )
            }
        }
    }
}

// Helper Functions for Status Texts and Colors
@Composable
private fun getStatus(status: ProgressStatus): String = when (status) {
    ProgressStatus.COMPLETED -> stringResource(R.string.completed)
    ProgressStatus.IN_PROGRESS -> stringResource(R.string.in_progress_continue_learning)
    ProgressStatus.NOT_STARTED -> stringResource(R.string.complete_previous_concepts)
    ProgressStatus.LOCKED -> stringResource(R.string.locked)
}

private fun getStatusColor(status: ProgressStatus): Color = when (status) {
    ProgressStatus.COMPLETED -> CompleteTextColor
    ProgressStatus.IN_PROGRESS -> InProgressTextColor
    ProgressStatus.NOT_STARTED -> NotStartedTextColor
    ProgressStatus.LOCKED -> NotStartedTextColor
}