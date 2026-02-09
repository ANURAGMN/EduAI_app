package com.anurag.eduai.domain.chatbot.usecase

import com.anurag.eduai.ui.screens.chatbotscreen.components.text.ProcessedText
import com.anurag.eduai.ui.screens.chatbotscreen.components.text.TextProcessor
import javax.inject.Inject

class TextProcessingUseCase @Inject constructor(
    private val processor: TextProcessor = TextProcessor()
) {
    fun process(text: String): ProcessedText = processor.process(text)
}
