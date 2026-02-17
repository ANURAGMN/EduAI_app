package com.anurag.eduai.domain.chatbot.usecase

import com.anurag.eduai.data.remote.AgenticAIClient
import com.anurag.eduai.debug.DebugLogger
import javax.inject.Inject

class TranslationUseCase @Inject constructor(
    private val agenticAIClient: AgenticAIClient
) {
    /**
     * Translates a single text to Kannada using the backend translation endpoint
     * @param text The text to translate
     * @return Translated text or original text if translation fails
     */
    suspend fun translateToKannada(text: String): String {
        return try {
            DebugLogger.debugLog("TranslationUseCase", "Translating text: $text")
            val result = agenticAIClient.getTranslatedText(text)

            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response?.success == true && response.translated.isNotBlank()) {
                    DebugLogger.debugLog("TranslationUseCase", "Translation successful: ${response.translated}")
                    response.translated
                } else {
                    DebugLogger.errorLog("TranslationUseCase", "Translation failed: ${response?.error}")
                    text // Return original text if translation failed
                }
            } else {
                DebugLogger.errorLog("TranslationUseCase", "Translation request failed: ${result.exceptionOrNull()?.message}")
                text // Return original text if request failed
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("TranslationUseCase", "Exception during translation: ${e.message}")
            text // Return original text on exception
        }
    }

    /**
     * Translates a list of texts to Kannada
     * @param texts List of texts to translate
     * @return List of translated texts (or original if translation fails)
     */
    suspend fun translateListToKannada(texts: List<String>): List<String> {
        return texts.map { text ->
            translateToKannada(text)
        }
    }
}