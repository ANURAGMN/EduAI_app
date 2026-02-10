package com.anurag.eduai.domain.chatbot.usecase

import com.anurag.eduai.domain.chatbot.model.HighlightResult
import javax.inject.Inject

class TextHighlightUseCase @Inject constructor(
    private val textProcessingUseCase: TextProcessingUseCase = TextProcessingUseCase()
) {

    fun build(
        text: String,
        fullText: String,
        isTyping: Boolean,
        currentWordIndex: Int,
        shouldHighlight: Boolean
    ): HighlightResult {
        val processedFull = textProcessingUseCase.process(fullText)
        val displayProcessed = if (isTyping) textProcessingUseCase.process(text) else processedFull

        val highlightRange = if (shouldHighlight && currentWordIndex in processedFull.wordPositions.indices) {
            val word = processedFull.wordPositions[currentWordIndex]
            word.start..word.end
        } else null

        return HighlightResult(
            displayText = displayProcessed.cleanText,
            boldRanges = displayProcessed.boldRanges,
            highlightRange = highlightRange
        )
    }
}
