package com.ncert7.aitutorandlab.domain.chatbot.usecase

import com.ncert7.aitutorandlab.ui.screens.chatbotscreen.components.dataclass.ChatUiState
import javax.inject.Inject

class AutoSuggestionUseCase @Inject constructor() {

    fun shouldShowAutosuggestions(state: ChatUiState): Boolean {
        return state.autosuggestions.isNotEmpty() &&
                !state.isUserActive &&
                state.inputText.isEmpty() &&
                !state.isLoading &&
                !state.isTyping &&
                !state.waitingForTTSToComplete
    }
}