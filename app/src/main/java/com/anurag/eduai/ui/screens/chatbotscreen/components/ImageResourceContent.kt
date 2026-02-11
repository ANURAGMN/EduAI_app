package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.IconPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.White
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideSubcomposition
import com.bumptech.glide.integration.compose.RequestState

/**
 * Image Content with Loading State and Zoom Controls
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageResourceContent(
    imageUrl: String,
    description: String?,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RectangleShape)
            .background(White)
            .pointerInput(Unit) {
                // Handle Double Tap to Reset
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2.5f
                        offset = Offset.Zero
                    }
                )
            }
            .pointerInput(Unit) {
                // Handle Pinch and Pan
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)

                    if (scale > 1f) {
                        offset += pan
                    } else {
                        offset = Offset.Zero
                    }
                }
            }
    ) {
        GlideSubcomposition(
            model = imageUrl,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        ) {
            when (state) {
                is RequestState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = IconPrimary)
                    }
                }
                is RequestState.Failure -> {
                    Text("Failed to load", Modifier.align(Alignment.Center))
                }
                is RequestState.Success -> {
                    Image(
                        painter = painter,
                        contentDescription = description,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        ZoomControlsOverlay(
            scale = scale,
            onScaleChange = { newScale ->
                scale = newScale
                if (scale <= 1f) offset = Offset.Zero
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(dimens.spaceMedium)
        )
    }
}

@Composable
private fun ZoomControlsOverlay(
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
            modifier = Modifier.size(dimens.iconLarge).background(BackgroundPrimary)

        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = stringResource(R.string.zoom_out),
                tint = IconPrimary
            )
        }
        Spacer(modifier = Modifier.width(dimens.spaceSmall))

        // Zoom In Button
        IconButton(
            onClick = { onScaleChange((scale + 0.5f).coerceAtMost(4f)) },
            modifier = Modifier
                .size(dimens.iconLarge)
                .background(BackgroundPrimary),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.zoom_in),
                tint = IconPrimary
            )
        }
    }
}