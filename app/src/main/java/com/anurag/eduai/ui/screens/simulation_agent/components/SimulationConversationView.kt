package com.anurag.eduai.ui.screens.simulation_agent.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.anurag.eduai.R
import com.anurag.eduai.ui.screens.chatbotscreen.components.AgentMessage
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.TextToSpeech

/** Conversation view with avatar and content area (matches chatbot ConversationView) */
@Composable
fun SimulationConversationView(
    avatarSize: Dp,
    currentMessage: String,
    isLoading: Boolean,
    ttsController: TextToSpeech,
    modifier: Modifier = Modifier,
    showWebView: Boolean = false,
    simulationUrls: List<String> = emptyList(),
    onCloseWebView: () -> Unit = {},
    errorCardHeight: Dp = 0.dp // height to match when reducing message container
) {
    val dimens = LocalDimensions.current

    // Animate avatar visibility based on whether simulation is showing
    val avatarHeightFraction by animateDpAsState(
        targetValue = if (showWebView && simulationUrls.isNotEmpty()) 0.dp else dimens.avatarSizeLarge * 2.5f,
        label = "avatarHeightFraction"
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Avatar at top - Hidden when simulation shows, visible otherwise
        if (avatarHeightFraction > dimens.spaceSmall) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(avatarHeightFraction + dimens.spaceMedium),
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
        }

        // Content area - avoid nested scrolls: AgentMessage already handles its own scrolling
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
                        text = stringResource(R.string.sim_teacher_thinking),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (showWebView && simulationUrls.isNotEmpty()) {
                // WebView content displayed inline with auto-scrolling text above
                // Column with proper weight distribution: message container constrained to errorCardHeight when provided
                Column(modifier = Modifier.fillMaxSize()) {
                    // Auto-scrolling message text section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (errorCardHeight > 0.dp) Modifier.height(errorCardHeight) else Modifier.weight(1f))
                    ) {
                        AgentMessage(
                            text = currentMessage,
                            isTyping = false,
                            fullText = currentMessage,
                            ttsController = ttsController,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(dimens.spaceMedium),
                            reduceTextSize = true
                        )
                    }

                    // Simulation content section (fixed height)
                    Box(modifier = Modifier
                        .fillMaxWidth()
                    ) {
                        when (simulationUrls.size) {
                            1 -> {
                                // Single simulation view
                                InlineSimulationWebView(
                                    url = simulationUrls[0],
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(dimens.spaceLarge * 15)
                                )
                            }
                            2 -> {
                                // Before/After comparison
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = stringResource(R.string.sim_before_label),
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(dimens.spaceMedium)
                                    )
                                    Box(modifier = Modifier
                                        .fillMaxWidth()
                                        .height(dimens.spaceLarge * 15)) {
                                        InlineSimulationWebView(url = simulationUrls[0])
                                    }
                                    Text(
                                        text = stringResource(R.string.sim_after_label),
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(dimens.spaceMedium)
                                    )
                                    Box(modifier = Modifier
                                        .fillMaxWidth()
                                        .height(dimens.spaceLarge * 15)) {
                                        InlineSimulationWebView(url = simulationUrls[1])
                                    }
                                }
                            }
                        }

                        // Close button
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(dimens.spaceMedium),
                            shape = MaterialTheme.shapes.small,
                            elevation = CardDefaults.cardElevation(defaultElevation = dimens.cardElevation),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            IconButton(onClick = onCloseWebView) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.sim_close_simulation),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            } else {
                // AgentMessage content (no simulation)
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

/** WebView component for rendering simulation HTML inline */
@Composable
fun InlineSimulationWebView(url: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
