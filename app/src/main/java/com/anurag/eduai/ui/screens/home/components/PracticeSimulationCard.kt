package com.anurag.eduai.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.data.local.entities.ProgressEntity
import com.anurag.eduai.domain.progress.model.ProgressStatus
import com.anurag.eduai.ui.models.ConceptUiModel
import com.anurag.eduai.ui.screens.conceptscreen.components.ConceptCard
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.utils.getLocalizedName
import com.anurag.eduai.utils.isKannada

@Composable
fun PracticeSimulationCard(
    progressSimulations: List<Pair<ProgressEntity?, ConceptEntity?>>,
    onSimulationClick: (String) -> Unit, // Click agent button - navigates to simulation agent
    onSimulationUrlClick: (String, String, String) -> Unit = { _, _, _ -> } // Click simulation button - opens URL viewer
) {
    val dimes = LocalDimensions.current

    // Don't show card if no simulations
    if (progressSimulations.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundPrimary),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = dimes.cardElevation)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(dimes.screenPadding)) {
            Text(
                text = stringResource(R.string.practice_simulation),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(0.dp, dimes.spaceExtraSmall)
            )
            Spacer(modifier = Modifier.height(dimes.screenPadding))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimes.spaceSmall)
            ) {
                progressSimulations.forEachIndexed { index, (progress, concept) ->
                    concept?.let { sim ->
                        val conceptUiModel = ConceptUiModel(
                            id = sim.conceptId,
                            name = sim.getLocalizedName(),
                            order = sim.orderIndex,
                            status = when (progress?.status) {
                                "COMPLETED" -> ProgressStatus.COMPLETED
                                "IN_PROGRESS" -> ProgressStatus.IN_PROGRESS
                                else -> ProgressStatus.NOT_STARTED
                            },
                            type = sim.type,
                            // Pass only the correct language-based URL and ID
                            simulationUrl = if (isKannada()) sim.simulationUrlKannada else sim.simulationUrl,
                            simulationId = if (isKannada()) sim.simulationIdKannada else sim.simulationId
                        )

                        ConceptCard(
                            concept = conceptUiModel,
                            serialNumber = index + 1,
                            onClick = {
                                // Clicking the card itself opens simulation agent
                                onSimulationClick(sim.conceptId)
                            },
                            onSimulationAgentClick = { simulationId ->
                                // Clicking "Agent" button opens simulation agent
                                onSimulationClick(simulationId)
                            },
                            onSimulationClick = { title, url, conceptId ->
                                // Clicking "Simulation" button opens URL viewer
                                onSimulationUrlClick(title, url, conceptId)
                            }
                        )
                    }
                }
            }
        }
    }
}