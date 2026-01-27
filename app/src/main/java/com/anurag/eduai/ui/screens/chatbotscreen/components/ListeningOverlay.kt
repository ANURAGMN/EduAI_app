package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.IconPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.White

/**
 * A composable overlay that indicates the app is listening for voice input
 * with voice-responsive animation with amplitude, transcribed text display,
 */
@Composable
fun ListeningOverlay(
    text: String,
    amplitude: Float = 0f,
    statusMessage: String = stringResource(R.string.listening),
    onStopClick: () -> Unit
) {
    val dimens = LocalDimensions.current
    val scrollState = rememberScrollState()
    // Auto-scroll to bottom when new text arrives
    LaunchedEffect(text) {
        if (text.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = White),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            // Smooth curved line animation at the very top (acts as the top edge)
            VoiceWaveAnimation(
                amplitude = amplitude,
                isListening = true,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimens.spaceMedium))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.spaceSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
                IconButton(
                    onClick = onStopClick,
                    modifier = Modifier.size(dimens.iconMedium)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.stop_listening),
                        tint = IconPrimary,
                    )
                }
            }

            // Text display area
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .padding(dimens.spaceMedium)
            ) {
                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary,
                    )
                }
            }
        }

}

@Preview
@Composable
fun ListeningOverlayPreview() {
    ListeningOverlay(
        text = "This is a sample transcribed text from speech recognition. It can be quite long to demonstrate scrolling behavior in the overlay.",
        amplitude = 0.5f,
        statusMessage = "Listening...",
        onStopClick = {}
    )
}