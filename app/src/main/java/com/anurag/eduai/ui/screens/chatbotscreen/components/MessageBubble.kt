package com.anurag.eduai.ui.screens.chatbotscreen.components

import ChatMessageModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
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
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.AiMessageBackground
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.IconPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary


/**
 * A composable representing a message bubble from the AI agent.
 */

@Composable
fun MessageBubble(
    message: ChatMessageModel,
    onListenClick: (String) -> Unit = {}
) {
    // Check sender field - "ai" or "user"
    when (message.sender.lowercase()) {
        "ai" -> AgentMessageBubble(
            text = message.content,
            isError = message.isError,
            onListenClick = { onListenClick(message.content) }
        )
        "user" -> UserMessageBubble(text = message.content)
        else -> UserMessageBubble(text = message.content)
    }
}

@Composable
fun AgentMessageBubble(
    modifier: Modifier = Modifier,
    text : String,
    isError: Boolean = false,
    onListenClick: () -> Unit = {},
) {
    val dimens = LocalDimensions.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimens.messageHorizontalPadding,
                vertical = dimens.messageVerticalPadding),
        horizontalArrangement = Arrangement.Start
    ) {
        // Agent avatar
        Box(
            modifier = Modifier
                .size(dimens.avatarSize)
                .clip(RoundedCornerShape(dimens.cornerRadiusRound))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            HeaderGradientStart,
                            HeaderGradientEnd
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.SmartToy,
                contentDescription = stringResource(R.string.agent),
                tint = Color.White,
                modifier = Modifier.size(dimens.avatarIconSize)
            )
        }

        Spacer(modifier = Modifier.width(dimens.spaceMedium))

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
                Text(
                    text = text,
                    fontSize = typography.bodyMedium.fontSize,
                    color = TextPrimary
                )

                if (!isError) {
                    Spacer(modifier = Modifier.width(dimens.spaceMedium))

                    TextButton(
                        onClick = onListenClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = IconPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(dimens.spaceExtraSmall))
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
    val dimens = LocalDimensions.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimens.spaceMedium,
                vertical = dimens.messageVerticalPadding
            ),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = dimens.userMessageMaxWidth)
                .clip(
                    RoundedCornerShape(
                        topStart = dimens.cornerRadiusLarge,
                        topEnd = dimens.cornerRadiusLarge,
                        bottomEnd = dimens.cornerRadiusSmall,
                        bottomStart = dimens.cornerRadiusLarge
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
                fontSize = typography.bodyMedium.fontSize,
                color = Color.White
            )
        }
    }
}

@Preview(
    name = "Phone",
    widthDp = 360,
    showBackground = true
)
@Preview(
    name = "Tablet",
    widthDp = 840,
    showBackground = true
)
@Composable
fun AgentMessageBubblePreview() {
    AgentMessageBubble(
        text = "Hello! How can I assist you today?",
    )
}

@Preview(
    name = "Phone",
    widthDp = 360,
    showBackground = true
)
@Preview(
    name = "Tablet",
    widthDp = 840,
    showBackground = true
)
@Composable
fun UserMessageBubblePreview() {
    UserMessageBubble(
        text = "I need help with my homework."
    )
}