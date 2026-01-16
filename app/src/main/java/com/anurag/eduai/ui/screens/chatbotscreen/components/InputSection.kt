package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.IconPrimary
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.viewModel.ChatUiState
import com.anurag.eduai.ui.viewModel.SpeechToText

/**
 * Comprehensive input section that handles:
 * - Auto-suggestions display
 * - Text input field
 * - Listening overlay for speech-to-text
 */
@Composable
fun InputSection(
    chatState: ChatUiState,
    sttState: SpeechToText.STTState,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onSpeakClick: () -> Unit,
    onStopListening: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onSizeChanged: (IntSize) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
        color = Color.White,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged(onSizeChanged)
        ) {
            // Auto-suggestions
            val shouldShowAutosuggestions = !sttState.isListening &&
                    chatState.showAutosuggestions &&
                    chatState.inputText.isEmpty() &&
                    !chatState.isLoading

            // Debug logging - only when state changes
            LaunchedEffect(shouldShowAutosuggestions, chatState.autosuggestions.size) {
                if (chatState.autosuggestions.isNotEmpty()) {
                    DebugLogger.debugLog("InputSection", """
                        AUTO-SUGGESTION CHIPS: ${if (shouldShowAutosuggestions) "VISIBLE " else "HIDDEN "}
                        - showAutosuggestions: ${chatState.showAutosuggestions}
                        - suggestions.size: ${chatState.autosuggestions.size}
                        - inputText.isEmpty: ${chatState.inputText.isEmpty()}
                        - !isLoading: ${!chatState.isLoading}
                        - !isListening: ${!sttState.isListening}
                    """.trimIndent())
                }
            }

            if (shouldShowAutosuggestions) {
                AutoSuggestionChips(
                    suggestions = chatState.autosuggestions,
                    visible = true,
                    onSuggestionClick = onSuggestionClick
                )
            }

            // Input or listening overlay
            if (!sttState.isListening) {
                InputField(
                    textValue = chatState.inputText,
                    onTextChange = onTextChange,
                    onSpeakClick = onSpeakClick,
                    onSendClick = onSendClick
                )
            } else {
                ListeningOverlay(
                    text = sttState.resultText,
                    amplitude = sttState.audioAmplitude,
                    onStopClick = onStopListening
                )
            }
        }
    }
}

/**
 * Input field component (internal)
 */
@Composable
private fun InputField(
    textValue: String,
    onTextChange: (String) -> Unit,
    onSpeakClick: () -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hasText = textValue.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(
                    shape = RoundedCornerShape(24.dp),
                    width = 1.dp,
                    color = AccentBlue
                )
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Text Input Field
            TextField(
                value = textValue,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                placeholder = {
                    Text(
                        text = "Type or speak...",
                        color = TextPrimary
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = HeaderGradientStart
                ),
                interactionSource = interactionSource
            )

            // Animated Icon Button - Switches between Mic and Send
            AnimatedContent(
                targetState = hasText,
                transitionSpec = {
                    fadeIn(animationSpec = tween(150)) togetherWith
                            fadeOut(animationSpec = tween(150))
                },
                modifier = Modifier.padding(end = 8.dp)
            ) { showSend ->
                if (showSend) {
                    // Send Icon
                    IconButton(
                        onClick = onSendClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            tint = HeaderGradientStart,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Mic Icon
                    IconButton(
                        onClick = onSpeakClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Start listening",
                            tint = IconPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun InputSectionPreview() {
    InputField(
        textValue = "",
        onTextChange = {},
        onSpeakClick = {},
        onSendClick = {}
    )
}