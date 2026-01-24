package com.anurag.eduai.ui.screens.home.components

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.viewModel.SimulationAgentViewModel
import com.anurag.eduai.ui.viewModel.SimulationInfo
import com.anurag.eduai.ui.viewmodel_factory.SimulationAgentViewmodelFactory


@Composable
fun PracticeSimulationCard(
    onSimulationClick: (SimulationInfo) -> Unit
) {

    val viewModel: SimulationAgentViewModel = viewModel(
        factory = SimulationAgentViewmodelFactory()
    )
    val availableSimulation by viewModel.availableSimulations.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAvailableSimulations()
    }
    val dimes = LocalDimensions.current
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

            availableSimulation.forEach { sims ->
                SimulationItem(
                    simulation = sims,
                    onClick = { onSimulationClick(it) }
                    )
                Spacer(modifier = Modifier.height(dimes.spaceMedium))
            }
        }
    }
}
