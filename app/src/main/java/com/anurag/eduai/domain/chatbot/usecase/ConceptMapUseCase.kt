package com.anurag.eduai.domain.chatbot.usecase

import com.anurag.eduai.data.remote.GeminiLLMClient
import com.anurag.eduai.debug.DebugLogger
import javax.inject.Inject

data class ConceptMapResult(
    val success: Boolean,
    val json: String? = null,
    val generationTimeMs: Long = 0,
    val isDefault: Boolean = false
)

class ConceptMapUseCase @Inject constructor(
    private val llmClient: GeminiLLMClient
) {

    suspend fun generateConceptMap(aiResponse: String, language: String): ConceptMapResult {
        return try {
            val generationStartTime = System.currentTimeMillis()
            DebugLogger.debugLog("ConceptMapUseCase", "Starting concept map generation...")

            val response = llmClient.queryLLM(aiResponse, language)
            DebugLogger.debugLog("ConceptMapUseCase", "LLM response received")

            val json = llmClient.extractConceptMapJSON(response)
            val generationTimeMs = System.currentTimeMillis() - generationStartTime

            val isDefault = isDefaultConceptMap(json)

            DebugLogger.debugLog("ConceptMapUseCase", "Concept map JSON extracted in ${generationTimeMs}ms")

            ConceptMapResult(
                success = true,
                json = json,
                generationTimeMs = generationTimeMs,
                isDefault = isDefault
            )
        } catch (e: Exception) {
            DebugLogger.errorLog("ConceptMapUseCase", "generateConceptMap error: ${e.message}")
            ConceptMapResult(success = false)
        }
    }


     fun isDefaultConceptMap(json: String): Boolean {
        return llmClient.isDefaultConceptMap(json)
    }
}