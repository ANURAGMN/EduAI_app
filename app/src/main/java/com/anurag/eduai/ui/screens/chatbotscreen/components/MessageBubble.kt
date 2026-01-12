package com.anurag.eduai.ui.screens.chatbotscreen.components

import ChatMessageModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.R
import com.anurag.eduai.ui.screens.chatbotscreen.components.text.TextWithHighlights
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.AiMessageBackground
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.IconPrimary


/**
 * A composable representing a message bubble from the AI agent.
 */

@Composable
fun MessageBubble(
    message: ChatMessageModel,
    isTyping: Boolean = false,
    typingText: String = "",
    fullText: String = message.content,
    isSpeaking: Boolean = false,
    onListenClick: (String) -> Unit = {}
) {
    // Check sender field - "ai" or "user"
    when (message.sender.lowercase()) {
        "ai" -> AgentMessageBubble(
        text = if (isTyping) typingText else message.content,
        isTyping = isTyping,
        fullText = fullText,
        isError = message.isError,
        onListenClick = { onListenClick(fullText) }
        )
        "user" -> UserMessageBubble(text = message.content)
        else -> UserMessageBubble(text = message.content)
    }
}

@Composable
fun AgentMessageBubble(
    modifier: Modifier = Modifier,
    text : String,
    isTyping: Boolean = false,
    fullText: String = text,
    isError: Boolean = false,
    isSpeaking: Boolean = false,
    onListenClick: () -> Unit = {},

) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        // Agent avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AccentBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.SmartToy,
                contentDescription = stringResource(R.string.agent),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Message bubble
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(
                containerColor = AiMessageBackground
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                TextWithHighlights(
                    text = text,
                    isTyping = isTyping,
                    fullText = fullText
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!isError && text.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = onListenClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = IconPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isSpeaking) "Stop" else "Listen",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isSpeaking) "Stop" else "Listen")
                    }
                }
            }
        }
    }
}
/**
 * A composable representing a message bubble from the user.
 */

@Composable
fun UserMessageBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomEnd = 4.dp,
                        bottomStart = 16.dp
                    )
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            HeaderGradientStart,
                            HeaderGradientEnd
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = text,
                fontSize = androidx.compose.material3.MaterialTheme.typography.bodyMedium.fontSize,
                color = Color.White
            )
        }
    }
}


@Preview
@Composable
fun AgentMessageBubblePreview() {
    AgentMessageBubble(
        text = "Hello! How can I assist you today?",
    )
}
@Composable
@Preview
fun UserMessageBubblePreview() {
    UserMessageBubble(
        text = "I need help with my homework."
    )
}