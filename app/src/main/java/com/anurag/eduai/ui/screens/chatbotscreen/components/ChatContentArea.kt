package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.TextToSpeech

/**
 * Main content area showing either resource card, loading, or agent message
 */
@Composable
fun ChatContentArea(
    showResourceCard: Boolean,
    currentResource: ResourceContent?,
    resourceDisplayMode: ResourceDisplayMode,
    isLoading: Boolean,
    loadingResourceMessage: String?,
    lastAIMessage: ChatMessageModel?,
    isTyping: Boolean,
    typingText: String,
    ttsController: TextToSpeech,
    onDismissResource: () -> Unit,
    onResourceTimerComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens= LocalDimensions.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        when {
            // Resource Card
            showResourceCard && currentResource != null -> {
                ResourcesCard(
                    content = currentResource,
                    displayMode = resourceDisplayMode,
                    onDismiss = onDismissResource,
                    onTimerComplete = onResourceTimerComplete,
                    timerDurationSeconds = 150,//timeout for resource card
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Loading State
            isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.spaceMedium),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(dimens.iconLarge),
                            strokeWidth =dimens.inputBorderWidth,
                            color = BrandPrimary
                        )
                        Spacer(modifier = Modifier.width(dimens.spaceMedium))
                        Text(
                            text = loadingResourceMessage ?: stringResource(R.string.thinking),
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Agent Message
            else -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (lastAIMessage != null) {
                        AgentMessage(
                            text = if (isTyping) typingText else lastAIMessage.content,
                            isTyping = isTyping,
                            typingText = typingText,
                            fullText = lastAIMessage.content,
                            ttsController = ttsController,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Top fade overlay
                    Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.95f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Bottom fade overlay
                    Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}
