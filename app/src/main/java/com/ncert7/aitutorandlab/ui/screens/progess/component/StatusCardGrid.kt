package com.ncert7.aitutorandlab.ui.screens.progess.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.ncert7.aitutorandlab.R
import com.ncert7.aitutorandlab.ui.theme.IconColorUnspecified
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions

/**
 * Status Card Grid Component
 * Pure UI component - displays status cards in a 2x2 grid
 * NO business logic, NO hardcoded strings/dimensions/colors
 */
@Composable
fun StatusCardGrid(
    streakCount: Int,
    completedConceptCount: Int,
    completedSimulationCount: Int,
    totalScore: Int = 0,
) {
    val dimes = LocalDimensions.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            StatusCardItem(
                icon = painterResource(R.drawable.ic_fire),
                value = streakCount,
                title = stringResource(R.string.day_streak),
                iconColor = IconColorUnspecified,
                iconContentDescription = stringResource(R.string.fire_icon),
                modifier = Modifier.weight(0.5f)
            )
            Spacer(modifier = Modifier.padding(dimes.spaceSmall))
            StatusCardItem(
                icon = painterResource(R.drawable.ic_book),
                value = completedConceptCount,
                title = stringResource(R.string.concept),
                iconColor = IconColorUnspecified,
                iconContentDescription = stringResource(R.string.book_icon),
                modifier = Modifier.weight(0.5f)
            )
        }

        Spacer(modifier = Modifier.padding(dimes.spaceSmall))

        Row(modifier = Modifier.fillMaxWidth()) {
            StatusCardItem(
                icon = painterResource(R.drawable.ic_simulation),
                value = completedSimulationCount,
                title = stringResource(R.string.simulation),
                iconColor = IconColorUnspecified,
                iconContentDescription = stringResource(R.string.simulation_icon),
                modifier = Modifier.weight(0.5f)
            )
        }
    }
}