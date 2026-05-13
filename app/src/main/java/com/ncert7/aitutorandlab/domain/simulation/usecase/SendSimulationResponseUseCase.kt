package com.ncert7.aitutorandlab.domain.simulation.usecase

import com.ncert7.aitutorandlab.data.remote.AgenticAIClient
import com.ncert7.aitutorandlab.data.remote.SimSessionResponse
import com.ncert7.aitutorandlab.debug.DebugLogger
import javax.inject.Inject

/**
 * Use case for sending student responses during simulation
 */
class SendSimulationResponseUseCase @Inject constructor(
    private val agenticAIClient: AgenticAIClient
) {
    private val tag = "SendSimulationResponseUseCase"

    /**
     * Send student response to the simulation
     */
    suspend fun sendResponse(
        sessionId: String,
        studentResponse: String,
        changedParams: Map<String, Any>? = null
    ): Result<SimSessionResponse> {
        return try {
            DebugLogger.debugLog(tag, "Sending student response: $studentResponse")
            if (changedParams != null) {
                DebugLogger.debugLog(tag, "With changed parameters: $changedParams")
            }

            val result = agenticAIClient.sendSimulationResponse(
                sessionId = sessionId,
                studentResponse = studentResponse,
                studentChangedParams = changedParams
            )

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                DebugLogger.debugLog(tag, "Response received successfully")
                DebugLogger.debugLog(tag, "Teacher Message: ${response.teacherMessage.text}")
                Result.success(response)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to send response"))
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(tag, "Error sending response: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Submit quiz answer for the simulation
     */
    suspend fun submitQuizAnswer(
        sessionId: String,
        answer: String
    ): Result<SimSessionResponse> {
        return try {
            DebugLogger.debugLog(tag, "Submitting quiz answer: $answer")

            val result = agenticAIClient.submitSimulationQuiz(
                sessionId = sessionId,
                answer = answer
            )

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                DebugLogger.debugLog(tag, "Quiz answer submitted successfully")
                DebugLogger.debugLog(tag, "Teacher Message: ${response.teacherMessage.text}")
                Result.success(response)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to submit quiz answer"))
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(tag, "Error submitting quiz answer: ${e.message}")
            Result.failure(e)
        }
    }
}
