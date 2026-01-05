package com.anurag.eduai.ui.screens.chatbotscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.ui.screens.chatbotscreen.components.InputSection
import com.anurag.eduai.ui.screens.chatbotscreen.components.ListeningOverlay
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.HeaderGradientStart
import androidx.compose.runtime.setValue
import com.anurag.eduai.ui.screens.chatbotscreen.components.AgentMessageBubble
import com.anurag.eduai.ui.screens.chatbotscreen.components.UserMessageBubble
import com.anurag.eduai.ui.theme.White


@Composable
fun ChatbotScreen() {
    var isListening by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        // Section 1: Agent Response Section
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Robot Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
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
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Robot",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = "Let's explore together!",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D3748)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AgentMessageBubble(text = "Hi there! How can I assist you today?", onListenClick = {  })
            UserMessageBubble(text="I want to learn about pendulums.")
        }

        // Section 2: User Interaction Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isListening,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InputSection(
                        onSpeakClick = { isListening = true }
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = isListening,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListeningOverlay(
                        text = "Help me to learn pendulum",
                        onStopClick = { isListening = false }
                    )
                }
            }
        }
    }
}
@Preview
@Composable
fun ChatbotScreenPreview() {
    ChatbotScreen()
}