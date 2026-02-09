package com.anurag.eduai.domain.chatbot.usecase

import javax.inject.Inject

class HandleAgentResponseUseCase @Inject constructor() {

    fun processAgentResponse(text: String): String {
        return text.trim()
    }
}