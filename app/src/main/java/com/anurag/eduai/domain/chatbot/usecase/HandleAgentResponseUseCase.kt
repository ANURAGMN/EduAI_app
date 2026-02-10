package com.anurag.eduai.domain.chatbot.usecase

import javax.inject.Inject

class HandleAgentResponseUseCase @Inject constructor(
) {
    /**
     * Processes the agent response text to clean it up for display
     */
    fun processAgentResponse(text: String): String {
        return text.trim()
    }
}