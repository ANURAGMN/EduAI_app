package com.anurag.eduai.domain.chatbot.usecase

import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
) {

    fun createUserMessage(content: String): ChatMessageModel {
        return ChatMessageModel(
            sender = "user",
            content = content,
        )
    }

    fun createAIMessage(content: String): ChatMessageModel {
        return ChatMessageModel(
            sender = "ai",
            content = content,
        )
    }
}
