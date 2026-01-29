package com.anurag.eduai.ui.screens.simulationscreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.anurag.eduai.R
import com.anurag.eduai.ui.models.SimulationUiModel
import com.anurag.eduai.ui.theme.CardBackground
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextOnPrimary
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary

/**
 * Card component to display simulation information
 */
@Composable
fun SimulationCard(
    modifier: Modifier = Modifier,
    simulation: SimulationUiModel,
    onLaunchClick: () -> Unit = {}
) {
    val dimens = LocalDimensions.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimens.cornerRadiusLarge),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimens.cardElevation
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.screenPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulation number
            Text(
                text = simulation.id.substringAfterLast("_"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextSecondary.copy(alpha = 0.5f)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = dimens.spaceSmall),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = simulation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(dimens.spaceMedium))

                Button(
                    onClick = onLaunchClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HeaderGradientStart
                    ),
                    shape = RoundedCornerShape(dimens.cornerRadiusMedium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.launch_simulation),
                        tint = TextOnPrimary
                    )
                    Text(
                        text = stringResource(R.string.launch_simulation),
                        color = TextOnPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = dimens.spaceSmall)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SimulationCardPreview() {
    SimulationCard(
        simulation = SimulationUiModel(
            id = "unit_8_1",
            title = "unit_8_1.html",
            htmlFileName = "unit_8_1.html",
            chapterId = "8"
        )
    )
}