package com.anurag.eduai.ui.screens.chatbotscreen.components

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.anurag.eduai.ui.screens.chatbotscreen.components.chat.ChatContentArea
import com.anurag.eduai.ui.screens.chatbotscreen.dataclass.ChatMessageModel
import com.anurag.eduai.ui.screens.chatbotscreen.dataclass.ChatUiState
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.TextToSpeech

/**
 * Initial avatar view shown before conversation starts
 */
@Composable
fun InitialAvatarView(
    avatarSize: Dp,
    ttsController: TextToSpeech,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
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

/**
 * Conversation view with avatar and content area
 */
@Composable
fun ConversationView(
    avatarSize: Dp,
    chatState: ChatUiState,
    lastAIMessage: ChatMessageModel?,
    ttsController: TextToSpeech,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current
    Column(modifier = modifier.fillMaxSize()) {
        // Avatar at top -
        Box(
            modifier = Modifier
                .fillMaxWidth(),
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

        // Content area - This scrolls
        ChatContentArea(
            isLoading = chatState.isLoading,
            loadingResourceMessage = chatState.loadingResourceMessage,
            lastAIMessage = lastAIMessage,
            isTyping = chatState.isTyping,
            typingText = chatState.typingText,
            ttsController = ttsController,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}