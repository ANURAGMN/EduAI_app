package com.anurag.eduai.ui.screens.progess.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.LocalDimensions

@Composable
fun StatusCardGrid(
    streakCount: Int,
    completedConceptCount: Int,
    completedSimulationCount: Int,
    score: Int
) {
    val dimes = LocalDimensions.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            StatusCardItem(
                icon = painterResource(R.drawable.ic_fire),
                value = streakCount,
                title = "Day Streak",
                iconColor = Color.Unspecified,
                modifier = Modifier.weight(0.5f)
            )
            Spacer(modifier = Modifier.padding(dimes.spaceSmall))
            StatusCardItem(
                icon = painterResource(R.drawable.ic_book), // icon value "📚"
                value = completedConceptCount,
                title = stringResource(R.string.concept),
                iconColor = Color.Unspecified,
                modifier = Modifier.weight(0.5f)
            )
        }
        Spacer(modifier = Modifier.padding(dimes.spaceSmall))
        Row(modifier = Modifier.fillMaxWidth()) {
            StatusCardItem(
                icon = painterResource(R.drawable.ic_simulation),
                value = completedSimulationCount,
                title = "Simulation",
                iconColor = Color.Unspecified,
                modifier = Modifier.weight(0.5f)
            )
            Spacer(modifier = Modifier.padding(dimes.spaceSmall))
            StatusCardItem(
                icon = painterResource(R.drawable.ic_graph), // icon value "📈"
                value = score,
                title = "Average Score",
                iconColor = Color.Unspecified,
                modifier = Modifier.weight(0.5f)
            )
        }
    }
}
