package com.anurag.eduai.ui.screens.chatbotscreen.components

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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.HeaderGradientStart

/**
 * A composable representing a message bubble from the AI agent.
 */
@Composable
fun AgentMessageBubble(
    text: String,
    onListenClick: () -> Unit,
    modifier: Modifier = Modifier
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
                imageVector =
                    Icons.Default.SmartToy,
                contentDescription = "Agent",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Message bubble
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0F4FF)
            ),
            shape = RoundedCornerShape(12.dp)
        )
        {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Think about this:",
                        fontSize = 14.sp,
                        color = Color(0xFF5A6C7D),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Can you count all the numbers between 1 and 2?",
                    fontSize = 16.sp,
                    color = Color(0xFF2D3748),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { /* Listen action */ },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF5B8EF8)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Listen")
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
                fontSize = 15.sp,
                color = Color.White,
                lineHeight = 20.sp
            )
        }
    }
}
@Preview
@Composable
fun AgentMessageBubblePreview() {
    AgentMessageBubble(
        text = "Hello! How can I assist you today?",
        onListenClick = {}
    )
}
@Composable
@Preview
fun UserMessageBubblePreview() {
    UserMessageBubble(
        text = "I need help with my homework."
    )
}