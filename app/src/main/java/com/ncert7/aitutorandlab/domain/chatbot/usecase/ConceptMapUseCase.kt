package com.ncert7.aitutorandlab.domain.chatbot.usecase

import com.ncert7.aitutorandlab.data.remote.GeminiLLMClient
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.domain.chatbot.model.ConceptMap
import javax.inject.Inject

class ConceptMapUseCase @Inject constructor(
    private val llmClient: GeminiLLMClient
) {

    suspend fun generateConceptMap(aiResponse: String, language: String): ConceptMap {
        return try {
            val generationStartTime = System.currentTimeMillis()
            DebugLogger.debugLog("ConceptMapUseCase", "Starting concept map generation...")

            val response = llmClient.queryLLM(aiResponse, language)
            DebugLogger.debugLog("ConceptMapUseCase", "LLM response received")

            val json = llmClient.extractConceptMapJSON(response)
            val generationTimeMs = System.currentTimeMillis() - generationStartTime

            val isDefault = isDefaultConceptMap(json)

            DebugLogger.debugLog("ConceptMapUseCase", "Concept map JSON extracted in ${generationTimeMs}ms")

            ConceptMap(
                success = true,
                json = json,
                generationTimeMs = generationTimeMs,
                isDefault = isDefault
            )
        } catch (e: Exception) {
            DebugLogger.errorLog("ConceptMapUseCase", "generateConceptMap error: ${e.message}")
            ConceptMap(success = false)
        }
    }

     fun isDefaultConceptMap(json: String): Boolean {
        return llmClient.isDefaultConceptMap(json)
    }
}