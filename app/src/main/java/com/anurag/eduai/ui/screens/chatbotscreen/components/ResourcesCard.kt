package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ResourceCardUiState
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.White

/**
 * ResourcesCard
 */
@Composable
fun ResourcesCard(
    state: ResourceCardUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val cardHeight = (screenHeight * 0.60f) // 60% of screen height

    AnimatedVisibility(
        visible = state !is ResourceCardUiState.Hidden,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 })
    ) {
        Card(
            modifier = modifier
                .padding(dimens.spaceMedium)
                .heightIn(max = cardHeight)
                .fillMaxWidth(),
            shape = RoundedCornerShape(dimens.cornerRadiusMedium),
            elevation = CardDefaults.cardElevation(defaultElevation =dimens.cardElevation),
            colors = CardDefaults.cardColors(
                containerColor = White
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    is ResourceCardUiState.Image -> {
                        ImageResourceContent(
                            imageUrl = state.imageUrl,
                            description = state.description
                        )
                    }
                    is ResourceCardUiState.ConceptMap -> {
                        ConceptMapResourceContent(
                            json = state.json,
                            currentAudioTime = state.audioProgress,
                            isAudioPlaying = state.isAudioPlaying
                        )
                    }
                    ResourceCardUiState.Hidden -> Unit
                }

                // Progress Timer
                if (state !is ResourceCardUiState.Hidden) {
                    ResourceCardCloseTimer(
                        timeRemaining = when (state) {
                            is ResourceCardUiState.Image -> state.remainingSeconds
                            is ResourceCardUiState.ConceptMap -> state.remainingSeconds
                            else -> 0
                        },
                        totalDuration = when (state) {
                            is ResourceCardUiState.Image -> state.totalSeconds
                            is ResourceCardUiState.ConceptMap -> state.totalSeconds
                            else -> 1
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(dimens.spaceMedium)
                    )
                }

                // Close Button
                FilledIconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(dimens.spaceMedium)
                        .size(dimens.iconLarge),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}