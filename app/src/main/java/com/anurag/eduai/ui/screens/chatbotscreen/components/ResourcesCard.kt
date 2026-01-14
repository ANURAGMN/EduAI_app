package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Resource type for different content types
 */
sealed class ResourceContent {
    data class Image(
        val url: String,
        val description: String?
    ) : ResourceContent()

    data class ConceptMap(
        val json: String,
        val description: String?,
        val currentAudioTime: Float = 0f,
        val isAudioPlaying: Boolean = false
    ) : ResourceContent()
}
enum class ResourceDisplayMode {
    IMAGE,           // APK -> CI (Image display)
    CONCEPT_MAP,     // CI -> SIM_CC (Concept Map)
}


/**
 * Main ResourcesCard - Template container with progress timer and close option
 */
@Composable
fun ResourcesCard(
    modifier: Modifier = Modifier,
    content: ResourceContent?,
    displayMode: ResourceDisplayMode,
    onDismiss: () -> Unit,
    onTimerComplete: () -> Unit = {},
    timerDurationSeconds: Int = 6,
) {
    var isVisible by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableIntStateOf(timerDurationSeconds) }

    // Timer countdown
    LaunchedEffect(Unit) {
        isVisible = true
//        launch {
//            while (timeRemaining > 0) {
//                delay(1000)
//                timeRemaining--
//            }
//            // Auto-close when timer reaches 0
//            onTimerComplete()
//            delay(300) // Allow exit animation
//            onDismiss()
//        }
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val cardHeight = (screenHeight * 0.60f) // 60% of screen height

    AnimatedVisibility(
        visible = isVisible && content != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 })
    ) {
        Card(
            modifier = modifier
                .padding(16.dp)
                .heightIn(max = cardHeight)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()
            ) {
                // Content based on type
                when (displayMode) {
                    ResourceDisplayMode.IMAGE -> {
                        if (content is ResourceContent.Image) {
                            ImageResourceContent(
                                imageUrl = content.url,
                                description = content.description
                            )
                        }
                    }
                    ResourceDisplayMode.CONCEPT_MAP -> {
                        if (content is ResourceContent.ConceptMap) {
                            ConceptMapResourceContent(
                                json = content.json,
                                currentAudioTime = content.currentAudioTime,
                                isAudioPlaying = content.isAudioPlaying
                            )
                        }
                    }
                }
//
//                // Progress Timer Overlay
//                ResourceCardCloseTimer(
//                    timeRemaining = timeRemaining,
//                    totalDuration = timerDurationSeconds,
//                    modifier = Modifier
//                        .align(Alignment.TopStart)
//                        .padding(16.dp)
//                )

                // Close Button
                IconButton(
                    onClick = {
                        isVisible = false
                        onDismiss()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}