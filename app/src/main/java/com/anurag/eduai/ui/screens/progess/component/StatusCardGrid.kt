package com.anurag.eduai.ui.screens.progess.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R

@Composable
fun StatusCardGrid(
    streakCount: String,
    completedConceptCount: String,
    completedSimulationCount: String,
    score: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            StatusCardItem(
                icon = "\uD83D\uDD25", // icon value "🔥"
                value = streakCount,
                title = "Day Streak",
                modifier = Modifier.weight(0.5f)
            )
            Spacer(modifier = Modifier.padding(10.dp))
            StatusCardItem(
                icon = "\uD83D\uDCDA", // icon value "📚"
                value = completedConceptCount,
                title = stringResource(R.string.concept),
                modifier = Modifier.weight(0.5f)
            )
        }
        Spacer(modifier = Modifier.padding(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            StatusCardItem(
                icon = "▶\uFE0F", // icon value "▶️
                value = completedSimulationCount,
                title = "Simulation",
                modifier = Modifier.weight(0.5f)
            )
            Spacer(modifier = Modifier.padding(15.dp))
            StatusCardItem(
                icon = "\uD83D\uDCC8", // icon value "📈"
                value = score,
                title = "Average Score",
                modifier = Modifier.weight(0.5f)
            )
        }
    }
}