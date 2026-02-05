package com.anurag.eduai.ui.screens.simulation_agent.components

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import com.anurag.eduai.ui.screens.chatbotscreen.components.chat.AgentMessage
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.TextToSpeech

/** Conversation view with avatar and content area (matches chatbot ConversationView) */
@Composable
fun SimulationConversationView(
    avatarSize: Dp,
    currentMessage: String,
    isLoading: Boolean,
    ttsController: TextToSpeech,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current
    Column(modifier = modifier.fillMaxSize()) {
        // Avatar at top (same as chatbot)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = dimens.cardElevation),
                shape = CircleShape,
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
            ) {
                AndroidView(
                    factory = {
                        WebView(it).apply {
                            setBackgroundColor(0)
                            ttsController.setupWebView(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.height(dimens.spaceMedium))

        // Content area with fade effect - This scrolls
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Show loading spinner or message content
            if (isLoading) {
                // Loading state with spinner and text
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimens.spaceMedium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(dimens.iconExtraLarge),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = dimens.inputBorderWidth
                    )

                    Spacer(modifier = Modifier.height(dimens.spaceMedium))

                    Text(
                        text = "Teacher is thinking...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // AgentMessage content
                AgentMessage(
                    text = currentMessage,
                    isTyping = false,
                    fullText = currentMessage,
                    ttsController = ttsController,
                    modifier = Modifier.fillMaxSize()
                )

                // Top gradient fade
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.spaceLarge * 2) // Adjust for more/less fade
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color.White.copy(alpha = 0f)
                                )
                            )
                        )
                )

                // Bottom gradient fade
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.spaceLarge * 2) // Adjust for more/less fade
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0f),
                                    Color.White
                                )
                            )
                        )
                )
            }
        }
    }
}