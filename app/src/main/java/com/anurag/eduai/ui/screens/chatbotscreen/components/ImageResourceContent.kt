package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.anurag.eduai.ui.theme.LocalDimensions
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder

/**
 * Stable Image Loader - This composable never recomposes after initial load
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ImageLoader(
    imageUrl: String,
    description: String?,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current
    GlideImage(
        model = imageUrl,
        contentDescription = description ?: "Image",
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        loading = placeholder {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimens.iconMedium),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(dimens.spaceSmall))
                Text(
                    text = "Image is loading...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        failure = placeholder {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimens.spaceMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Failed to load image",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(dimens.spaceSmall))
                Text(
                    text = "Please check your connection",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    )
}

/**
 * Image Content with Loading State and Zoom Controls
 */
@Composable
fun ImageResourceContent(
    imageUrl: String,
    description: String?,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current
    // Convert GitHub blob URL to raw URL for direct image loading
    val rawImageUrl = remember(imageUrl) {
        imageUrl.replace("github.com", "raw.githubusercontent.com")
            .replace("/blob/", "/")
    }

    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Transformable state for pinch-to-zoom and pan gestures
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)

        // Only allow panning when zoomed in
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = state)
        ) {
            // image loader
            ImageLoader(
                imageUrl = rawImageUrl,
                description = description
            )
        }

        // Zoom controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(dimens.spaceMedium),
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
        ) {
            // Zoom Out
            IconButton(
                onClick = {
                    scale = (scale - 0.25f).coerceAtLeast(0.5f)
                    if (scale <= 1f) {
                        offset = Offset.Zero
                    }
                },
                modifier = Modifier
                    .size(dimens.iconLarge)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = "Zoom Out",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Zoom In
            IconButton(
                onClick = {
                    scale = (scale + 0.25f).coerceAtMost(3f)
                },
                modifier = Modifier
                    .size(dimens.iconLarge)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom In",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}