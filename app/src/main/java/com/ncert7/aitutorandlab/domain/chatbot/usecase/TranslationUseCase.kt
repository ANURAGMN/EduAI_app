package com.ncert7.aitutorandlab.domain.chatbot.usecase

import com.ncert7.aitutorandlab.data.remote.AgenticAIClient
import com.ncert7.aitutorandlab.debug.DebugLogger
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
            DebugLogger.debugLog("TranslationUseCase", "Translating to Kannada: $text")
            val result = agenticAIClient.translateToKannada(text)

            if (result.isSuccess) {
                val response = result.getOrNull()
                DebugLogger.debugLog("TranslationUseCase", "API Response - success: ${response?.success}, original: ${response?.original?.take(30)}, translated: ${response?.translated?.take(30)}, error: ${response?.error}")

                if (response?.success == true && response.translated.isNotBlank()) {
                    DebugLogger.debugLog("TranslationUseCase", "Translation to Kannada successful: ${response.translated}")
                    response.translated
                } else {
                    DebugLogger.errorLog("TranslationUseCase", "Translation to Kannada failed: ${response?.error}")
                    text // Return original text if translation failed
                }
            } else {
                DebugLogger.errorLog("TranslationUseCase", "Translation to Kannada request failed: ${result.exceptionOrNull()?.message}")
                text // Return original text if request failed
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("TranslationUseCase", "Exception during translation to Kannada: ${e.message}")
            text // Return original text on exception
        }
    }

    /**
     * Translates a single text to English using the backend translation endpoint
     * @param text The text to translate (can be Kannada or any other language)
     * @return Translated text or original text if translation fails
     */
    suspend fun translateToEnglish(text: String): String {
        return try {
            DebugLogger.debugLog("TranslationUseCase", "Translating to English: $text")
            val result = agenticAIClient.translateToEnglish(text)

            if (result.isSuccess) {
                val response = result.getOrNull()
                DebugLogger.debugLog("TranslationUseCase", "API Response - success: ${response?.success}, original: ${response?.original?.take(30)}, translated: ${response?.translated?.take(30)}, error: ${response?.error}")

                if (response?.success == true && response.translated.isNotBlank()) {
                    DebugLogger.debugLog("TranslationUseCase", "Translation to English successful: ${response.translated}")
                    response.translated
                } else {
                    DebugLogger.errorLog("TranslationUseCase", "Translation to English failed: ${response?.error}")
                    text // Return original text if translation failed
                }
            } else {
                DebugLogger.errorLog("TranslationUseCase", "Translation to English request failed: ${result.exceptionOrNull()?.message}")
                text // Return original text if request failed
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("TranslationUseCase", "Exception during translation to English: ${e.message}")
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

    /**
     * Translates a list of texts to English
     * @param texts List of texts to translate
     * @return List of translated texts (or original if translation fails)
     */
    suspend fun translateListToEnglish(texts: List<String>): List<String> {
        return texts.map { text ->
            translateToEnglish(text)
        }
    }
}