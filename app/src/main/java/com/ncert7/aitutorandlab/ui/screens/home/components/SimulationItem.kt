package com.ncert7.aitutorandlab.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.ncert7.aitutorandlab.domain.simulation.usecase.SimulationInfo
import com.ncert7.aitutorandlab.ui.theme.BrandPrimary
import com.ncert7.aitutorandlab.ui.theme.ColorHint
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.TextPrimary
import com.ncert7.aitutorandlab.ui.theme.White

@Composable
fun SimulationItem(
    simulation: SimulationInfo,
    modifier: Modifier = Modifier,
    onClick: (SimulationInfo) -> Unit
) {
    val dimes = LocalDimensions.current

    Row(
        modifier =
            modifier.fillMaxWidth()
                .clip(RoundedCornerShape(dimes.cornerRadiusMedium))
                .background(ColorHint.copy(alpha = 0.2f))
                .clickable { onClick(simulation) }
                .padding(dimes.cardPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier.size(dimes.dropdownItemHeight)
                    .clip(RoundedCornerShape(dimes.spaceSmall))
                    .background(BrandPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = "Simulation Icon",
                tint = White
            )
        }
        Spacer(modifier = Modifier.padding(dimes.spaceExtraSmall))
        Text(text = simulation.title, color = TextPrimary)
    }
}