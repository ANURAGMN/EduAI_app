package com.ncert7.aitutorandlab.domain.chatbot.usecase

import com.ncert7.aitutorandlab.ui.screens.chatbotscreen.components.text.ProcessedText
import com.ncert7.aitutorandlab.ui.screens.chatbotscreen.components.text.TextProcessor
import javax.inject.Inject

class TextProcessingUseCase @Inject constructor(
    private val processor: TextProcessor = TextProcessor()
) {
    fun process(text: String): ProcessedText = processor.process(text)
}
