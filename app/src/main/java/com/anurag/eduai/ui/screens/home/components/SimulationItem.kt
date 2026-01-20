package com.anurag.eduai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.White

@Composable
fun SimulationItem(
    title: String,
    modifier: Modifier = Modifier,
    status: String = "locked" // default all simulation are locked

) {
    val dimes = LocalDimensions.current
    Row(
        modifier =
            modifier.fillMaxWidth()
                .clip(RoundedCornerShape(dimes.cornerRadiusMedium))
                .background(ColorHint.copy(alpha = 0.2f))
                .padding(dimes.cardPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier.size(dimes.dropdownItemHeight)
                    .clip(RoundedCornerShape(dimes.spaceSmall))
                    .background(if (status == "locked") ColorHint else BrandPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector =
                    if (status != "locked") Icons.Outlined.PlayArrow else Icons.Filled.Lock,
                contentDescription = "Simulation Icon",
                tint = White
            )
        }
        Spacer(modifier = Modifier.padding(dimes.spaceExtraSmall))
        Text(text = title, color = TextPrimary)
    }
}
