package com.anurag.eduai.ui.screens.progess.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.ProgressGradientEnd
import com.anurag.eduai.ui.theme.ProgressGradientStart
import com.anurag.eduai.ui.theme.TopBarIconColor
import com.anurag.eduai.ui.theme.TopBarSubtextColor
import com.anurag.eduai.ui.theme.TopBarTextColor

/**
 * Progress Screen Top Bar Component
 * Pure UI component - displays top bar with navigation icons
 * NO hardcoded strings/dimensions/colors
 */
@Composable
fun ProgressScreenTopBar(
    onGoHome: () -> Unit = {},
    onGoSetting: () -> Unit = {}
) {
    val dimes = LocalDimensions.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        ProgressGradientStart,
                        ProgressGradientEnd
                    )
                ),
                shape = RoundedCornerShape(
                    bottomStart = dimes.cornerRadiusRound,
                    bottomEnd = dimes.cornerRadiusRound
                )
            )
            .padding(dimes.screenPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, dimes.spaceSmall)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.your_progress),
                    color = TopBarTextColor,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                // Navigate to home
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = stringResource(R.string.home_icon),
                    modifier = Modifier
                        .size(dimes.iconMedium)
                        .clickable(enabled = true, onClick = onGoHome),
                    tint = TopBarIconColor
                )

                Spacer(modifier = Modifier.width(dimes.spaceSmall))

                // Navigate to setting
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.setting_icon),
                    modifier = Modifier
                        .size(dimes.iconMedium)
                        .clickable(enabled = true, onClick = onGoSetting),
                    tint = TopBarIconColor
                )
            }

            Spacer(modifier = Modifier.height(dimes.spaceMedium))

            Text(
                text = stringResource(R.string.last_seven_days),
                color = TopBarSubtextColor,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}