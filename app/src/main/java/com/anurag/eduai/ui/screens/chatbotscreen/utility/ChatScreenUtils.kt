package com.anurag.eduai.ui.screens.chatbotscreen.utility

import com.anurag.eduai.ui.screens.chatbotscreen.dataclass.ChatMessageModel
import com.anurag.eduai.ui.screens.chatbotscreen.dataclass.ChatUiState
import com.anurag.eduai.ui.screens.chatbotscreen.dataclass.ResourceCardUiState
import com.anurag.eduai.ui.viewModel.ChatViewModel
import com.anurag.eduai.ui.viewModel.TextToSpeech

/**
 * Helper functions for ChatbotScreen
 */
object ChatScreenUtils {

    /**
     * Handles volume/TTS button click logic
     *
     * Logic:
     * 1. If resource card is showing and TTS was paused for resource, resume TTS
     * 2. If TTS is currently speaking, stop it
     * 3. Else, speak the last AI message
     *
     * @param chatState Current chat UI state
     * @param ttsState Current TTS state
     * @param lastAIMessage Last AI message to speak
     * @param chatViewModel ViewModel to manage chat state
     * @param ttsController Controller for text-to-speech
     */
    fun handleVolumeClick(
        chatState: ChatUiState,
        ttsState: TextToSpeech.TTSState,
        lastAIMessage: ChatMessageModel?,
        chatViewModel: ChatViewModel,
        ttsController: TextToSpeech
    ) {
        when {
            chatState.resourceCardState !is ResourceCardUiState.Hidden && chatState.ttsPausedForResource -> {
                chatViewModel.resumeTTSForResource()
                lastAIMessage?.let { ttsController.speak(it.content) }
            }
            ttsState.isSpeaking -> ttsController.stop()
            else -> lastAIMessage?.let { ttsController.speak(it.content) }
        }
    }

    /**
     * Handles voice selection change from settings
     *
     * Logic:
     * 1. Find the selected voice by display name
     * 2. Apply the voice change
     * 3. If TTS is currently speaking, restart with new voice
     *
     * @param selectedDisplayName Display name of the selected voice
     * @param ttsState Current TTS state
     * @param ttsController Controller for text-to-speech
     * @param aiMessageOutput Current AI message being displayed
     */
    fun handleVoiceChange(
        selectedDisplayName: String,
        ttsState: TextToSpeech.TTSState,
        ttsController: TextToSpeech,
        aiMessageOutput: String
    ) {
        ttsState.availableVoices.find {
            ttsController.formatVoiceName(it) == selectedDisplayName
        }?.let { voice ->
            ttsController.setVoice(voice)
            if (ttsState.isSpeaking) {
                ttsController.stop()
                ttsController.speak(aiMessageOutput)
            }
        }
    }

    /**
     * Handles speech rate/speed change from settings
     *
     * Logic:
     * 1. Convert speed label to actual speed value
     * 2. Apply the speed change
     * 3. If TTS is currently speaking, restart with new speed
     *
     * @param label Speed label (e.g., "0.75x", "1.0x", "1.25x", "1.5x")
     * @param ttsController Controller for text-to-speech
     * @param ttsState Current TTS state
     * @param aiMessageOutput Current AI message being displayed
     */
    fun handleSpeedChange(
        label: String,
        ttsController: TextToSpeech,
        ttsState: TextToSpeech.TTSState,
        aiMessageOutput: String
    ) {
        val speed = when (label) {
            "0.75x" -> 0.75f
            "1.0x" -> 1.0f
            "1.25x" -> 1.25f
            "1.5x" -> 1.5f
            else -> 0.75f  // Default speed
        }
        ttsController.setSpeechRate(speed)
        if (ttsState.isSpeaking) {
            ttsController.stop()
            ttsController.speak(aiMessageOutput)
        }
    }
}
