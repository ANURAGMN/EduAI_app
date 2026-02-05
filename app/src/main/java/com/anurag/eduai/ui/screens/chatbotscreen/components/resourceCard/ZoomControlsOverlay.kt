package com.anurag.eduai.ui.screens.chatbotscreen.components.resourceCard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.White

@Composable
fun ZoomControlsOverlay(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current

    // Unified container for a cleaner "Row" look
    Row(
        modifier = modifier
            .clip(RectangleShape) // Creates the pill shape
            .padding(horizontal = dimens.spaceSmall, vertical = dimens.spaceSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceExtraSmall)
    ) {
        // Zoom Out Button
        IconButton(
            onClick = { onScaleChange((scale - 0.5f).coerceAtLeast(1f)) },
            modifier = Modifier.size(dimens.iconLarge).background(White)

        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = stringResource(R.string.zoom_out),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(dimens.spaceSmall))

        // Zoom In Button
        IconButton(
            onClick = { onScaleChange((scale + 0.5f).coerceAtMost(4f)) },
            modifier = Modifier.size(dimens.iconLarge).background(White)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.zoom_in),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}