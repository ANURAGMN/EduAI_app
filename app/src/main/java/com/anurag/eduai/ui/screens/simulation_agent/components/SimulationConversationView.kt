package com.anurag.eduai.ui.screens.simulation_agent.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.ui.screens.chatbotscreen.components.AgentMessage
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.TextToSpeech

/** Conversation view with agent message (25%) and single simulation (75%) */
@Composable
fun SimulationConversationView(
    avatarSize: Dp,
    currentMessage: String,
    isLoading: Boolean,
    ttsController: TextToSpeech,
    modifier: Modifier = Modifier,
    simulationUrl: String? = null,
    onParamsChanged: (Map<String, Any>) -> Unit = {}
) {
    val dimens = LocalDimensions.current

    Column(modifier = modifier.fillMaxSize()) {
        // TOP SECTION (25%): Agent Message
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f)
                .background(White)
                .padding(dimens.spaceMedium)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(dimens.iconLarge),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = dimens.inputBorderWidth
                    )
                    Spacer(modifier = Modifier.height(dimens.spaceSmall))
                    Text(
                        text = stringResource(R.string.sim_teacher_thinking),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                AgentMessage(
                    text = currentMessage,
                    isTyping = false,
                    fullText = currentMessage,
                    ttsController = ttsController,
                    modifier = Modifier.fillMaxSize(),
                    reduceTextSize = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.spaceLarge)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(White, White.copy(alpha = 0f))
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.spaceLarge)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(White.copy(alpha = 0f), White)
                            )
                        )
                )
            }
        }

        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)

        // BOTTOM SECTION (75%): Single Simulation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.75f)
                .background(White)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(dimens.iconLarge),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = dimens.inputBorderWidth
                    )
                }
            } else if (simulationUrl == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimens.spaceMedium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.sim_no_simulation),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                SimulationWebView(
                    url = simulationUrl,
                    onParamsChanged = onParamsChanged,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
