package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.anurag.eduai.ui.screens.chatbotscreen.dataclass.ChatUiState
import com.anurag.eduai.ui.screens.chatbotscreen.components.inputSection.AutoSuggestionChips
import com.anurag.eduai.ui.screens.chatbotscreen.components.inputSection.InputField
import com.anurag.eduai.ui.screens.chatbotscreen.components.inputSection.ListeningOverlay
import com.anurag.eduai.ui.viewModel.SpeechToText

/**
 * input section that handles:
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Auto-suggestions
        val shouldShowAutosuggestions = !sttState.isListening &&
                chatState.showAutosuggestions &&
                chatState.inputText.isEmpty() &&
                !chatState.isLoading

        if (shouldShowAutosuggestions) {
            AutoSuggestionChips(
                suggestions = chatState.autosuggestions,
                visible = true,
                onSuggestionClick = onSuggestionClick
            )
        }
        //input field
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
                statusMessage = sttState.statusMessage,
                onStopClick = onStopListening
            )
        }
    }

}