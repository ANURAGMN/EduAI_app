package com.ncert7.aitutorandlab.domain.revisionagent.usecase

import com.ncert7.aitutorandlab.data.remote.AgenticAIClient
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.domain.chatbot.model.SessionResult
import com.ncert7.aitutorandlab.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel
import com.ncert7.aitutorandlab.utils.getCurrentLanguageCode
import com.ncert7.aitutorandlab.utils.isKannada
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for handling revision session operations.
 * Similar to SessionUseCase but for revision endpoints.
 */
class RevisionUseCase @Inject constructor(
    private val agenticAIClient: AgenticAIClient,
) {
    private val revisionThreadMap = mutableMapOf<String, String>()
    private val revisionSessionMap = mutableMapOf<String, String>()

    /**
     * Get current app language using LocalizationUtils
     * Returns "en" or "kn" based on the app's locale setting
     */
    fun getAppLanguage(): String = getCurrentLanguageCode()

    /**
     * Check if app is currently in Kannada using LocalizationUtils
     */
    fun isAppInKannada(): Boolean = isKannada()

    /**
     * Fetch available chapters from backend /revision/chapters endpoint
     */
    suspend fun getAvailableChapters(): List<String> {
        return try {
            val result = agenticAIClient.getRevisionChapters()
            if (result.isSuccess) {
                val response = result.getOrNull()
                response?.chapters ?: emptyList()
            } else {
                DebugLogger.errorLog("RevisionUseCase", "Failed to fetch revision chapters")
                emptyList()
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "RevisionUseCase",
                "Error fetching revision chapters: ${e.message}"
            )
            emptyList()
        }
    }

    fun hasExistingRevisionSession(chapter: String): Boolean {
        return revisionThreadMap.containsKey(chapter)
    }

    fun getRevisionThreadId(chapter: String): String? {
        return revisionThreadMap[chapter]
    }

    private fun saveRevisionThreadMapping(chapter: String, threadId: String, sessionId: String) {
        revisionThreadMap[chapter] = threadId
        revisionSessionMap[chapter] = sessionId
        DebugLogger.debugLog("RevisionUseCase", "Saved revision mapping: chapter=$chapter, threadId=$threadId, sessionId=$sessionId")
    }

    fun deleteRevisionSessionMapping(chapter: String) {
        revisionThreadMap.remove(chapter)
        revisionSessionMap.remove(chapter)
        DebugLogger.debugLog("RevisionUseCase", "Deleted revision session mapping for chapter: $chapter")
    }

    suspend fun startRevisionSession(
        chapter: String,
        userId: String,
        isKannada: Boolean
    ): SessionResult {
        return try {
            val result = agenticAIClient.startRevisionSession(
                chapter = chapter,
                studentId = userId,
                isKannada = isKannada
            )

            if (result.isSuccess) {
                val response = result.getOrNull() ?: return SessionResult(false)
                if (!response.success) return SessionResult(false)

                saveRevisionThreadMapping(chapter, response.threadId, response.sessionId)
                agenticAIClient.setCurrentThreadAndSession(response.threadId, response.sessionId)

                SessionResult(
                    success = true,
                    autosuggestions = emptyList(), // Revision doesn't have autosuggestions
                    agentResponse = response.agentResponse,
                    metadata = null, // Revision doesn't use metadata
                    currentState = response.currentState
                )
            } else {
                SessionResult(false)
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("RevisionUseCase", "startRevisionSession error: ${e.message}")
            SessionResult(false)
        }
    }

    suspend fun resumeRevisionSession(threadId: String, sessionId: String?): SessionResult {
        return try {
            DebugLogger.debugLog("RevisionUseCase", "Resuming revision session - thread=$threadId")
            agenticAIClient.setCurrentThreadAndSession(threadId, sessionId)

            val histResult = agenticAIClient.getRevisionSessionHistory(threadId)
            if (histResult.isSuccess) {
                val response = histResult.getOrNull()
                val messages = response?.messages ?: emptyList()

                // Translate messages based on current app language
                val translatedMessages = translateMessagesForCurrentLanguage(messages)

                SessionResult(
                    success = true,
                    messages = translatedMessages,
                    currentState = null // Revision history doesn't include current state
                )
            } else {
                SessionResult(false)
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("RevisionUseCase", "resumeRevisionSession error: ${e.message}")
            SessionResult(false)
        }
    }

    suspend fun continueRevisionSession(
        chapter: String,
        userMessage: String,
        isKannada: Boolean
    ): SessionResult = withContext(Dispatchers.IO) {
        try {
            val threadId = revisionThreadMap[chapter]
                ?: return@withContext SessionResult(
                    false,
                    agentResponse = "No active revision session"
                )

            val result = agenticAIClient.continueRevisionSession(
                threadId = threadId,
                userMessage = userMessage,
                isKannada = isKannada
            )

            if (result.isSuccess) {
                val response = result.getOrNull() ?: return@withContext SessionResult(false)
                if (!response.success) return@withContext SessionResult(false)

                SessionResult(
                    success = true,
                    agentResponse = response.agentResponse,
                    autosuggestions = emptyList(), // Revision doesn't have autosuggestions
                    currentState = response.currentState
                )
            } else {
                SessionResult(false)
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "RevisionUseCase",
                "continueRevisionSession error: ${e.message}"
            )
            SessionResult(false)
        }
    }

    /**
     * Translate messages based on current app language.
     * If app is in Kannada and message is in English, translate to Kannada.
     * If app is in English and message is in Kannada, translate to English.
     */
    private suspend fun translateMessagesForCurrentLanguage(messages: List<Map<String, Any>>): List<ChatMessageModel> {
        val currentIsKannada = isAppInKannada()

        return messages.mapNotNull { msg ->
            val role = (msg["role"] as? String)?.lowercase() ?: return@mapNotNull null
            val content = msg["content"] as? String ?: return@mapNotNull null
            val sender = when (role) {
                "assistant", "ai" -> "ai"
                "user" -> "user"
                else -> return@mapNotNull null
            }

            // Only translate AI messages to match current app language
            val translatedContent = if (sender == "ai") {
                translateIfNeeded(content, currentIsKannada)
            } else {
                content
            }

            ChatMessageModel(
                sender = sender,
                content = translatedContent,
            )
        }
    }

    /**
     * Translate content if it doesn't match the current language requirement.
     * @param content The text to potentially translate
     * @param needsKannada True if app is in Kannada, false if in English
     * @return Translated text or original if translation not needed/failed
     */
    private suspend fun translateIfNeeded(content: String, needsKannada: Boolean): String {
        return try {
            if (needsKannada) {
                // App is in Kannada - check if content needs translation to Kannada
                if (isTextInKannada(content)) {
                    // Already in Kannada, no translation needed
                    content
                } else {
                    // Translate English to Kannada
                    val result = agenticAIClient.translateToKannada(content)
                    if (result.isSuccess) {
                        val response = result.getOrNull()
                        if (response?.success == true && response.translated.isNotBlank()) {
                            DebugLogger.debugLog("RevisionUseCase", "Translated to Kannada: ${content.take(30)}... -> ${response.translated.take(30)}...")
                            response.translated
                        } else {
                            content
                        }
                    } else {
                        content
                    }
                }
            } else {
                // App is in English - check if content needs translation to English
                if (isTextInKannada(content)) {
                    // Translate Kannada to English
                    val result = agenticAIClient.translateToEnglish(content)
                    if (result.isSuccess) {
                        val response = result.getOrNull()
                        if (response?.success == true && response.translated.isNotBlank()) {
                            DebugLogger.debugLog("RevisionUseCase", "Translated to English: ${content.take(30)}... -> ${response.translated.take(30)}...")
                            response.translated
                        } else {
                            content
                        }
                    } else {
                        content
                    }
                } else {
                    // Already in English, no translation needed
                    content
                }
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("RevisionUseCase", "Translation error: ${e.message}")
            content // Return original on error
        }
    }

    /**
     * Simple heuristic to detect if text contains Kannada characters
     */
    private fun isTextInKannada(text: String): Boolean {
        // Kannada Unicode range: \u0C80-\u0CFF
        return text.any { it in '\u0C80'..'\u0CFF' }
    }
}