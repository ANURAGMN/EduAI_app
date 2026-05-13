package com.ncert7.aitutorandlab.domain.mathagent.usecase

import com.ncert7.aitutorandlab.ui.screens.mathagentscreen.dataclass.MathMessageModel
import javax.inject.Inject

/**
 * Use case for creating math messages
 * Mirrors SendMessageUseCase from chatbot domain
 */
class MathSendMessageUseCase @Inject constructor() {

    /**
     * Creates a user message
     */
    fun createUserMessage(
        content: String,
        imageBase64: String? = null
    ): MathMessageModel {
        val imageUrl = if (imageBase64 != null) {
            "data:image/jpeg;base64,$imageBase64"
        } else {
            null
        }

        return MathMessageModel(
            role = "user",
            content = content,
            imageUrl = imageUrl
        )
    }

    /**
     * Creates an assistant message
     */
    fun createAssistantMessage(
        content: String,
        node: String? = null
    ): MathMessageModel {
        return MathMessageModel(
            role = "assistant",
            content = content,
            node = node
        )
    }

    /**
     * Creates an error message
     */
    fun createErrorMessage(content: String): MathMessageModel {
        return MathMessageModel(
            role = "assistant",
            content = content,
            isError = true,
            canRetry = true
        )
    }
}
