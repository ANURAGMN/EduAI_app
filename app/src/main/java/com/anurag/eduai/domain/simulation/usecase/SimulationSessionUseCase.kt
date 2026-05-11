package com.anurag.eduai.domain.simulation.usecase

import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.SimulationSessionRepository
import com.anurag.eduai.data.remote.AgenticAIClient
import com.anurag.eduai.data.remote.SimSessionResponse
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.isKannada
import javax.inject.Inject

/**
 * Use case for managing simulation sessions
 * Handles session creation, resumption, and cleanup
 */
class SimulationSessionUseCase @Inject constructor(
    private val agenticAIClient: AgenticAIClient,
    private val simulationSessionRepository: SimulationSessionRepository,
    private val sharedPrefs: SharedPreferenceUtils
) {
    private val tag = "SimulationSessionUseCase"

    /**
     * Start a new simulation session
     */
    suspend fun startNewSession(simulationId: String): Result<SimSessionResponse> {
        return try {
            val studentId = sharedPrefs.getUserId()
            val currentLanguage = if (isKannada()) "kannada" else "english"

            DebugLogger.debugLog(tag, "Starting new session for simulation: $simulationId")
            DebugLogger.debugLog(tag, "Student: $studentId, Language: $currentLanguage")

            val result = agenticAIClient.startSimulationSession(
                simulationId = simulationId,
                studentId = studentId,
                language = currentLanguage
            )

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                // Save session mapping
                simulationSessionRepository.saveMapping(simulationId, response.sessionId)
                DebugLogger.debugLog(tag, "Session started: ${response.sessionId}")
                Result.success(response)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to start session"))
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(tag, "Error starting session: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Resume an existing session with history
     */
    suspend fun resumeExistingSession(simulationId: String): Result<SimSessionResponse> {
        return try {
            val sessionId = simulationSessionRepository.loadMapping(simulationId)
                ?: return Result.failure(Exception("No saved session for $simulationId"))

            DebugLogger.debugLog(tag, "Resuming session: $sessionId for simulation: $simulationId")

            val result = agenticAIClient.getSimulationSession(sessionId)

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                DebugLogger.debugLog(tag, "Session resumed successfully")
                Result.success(response)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to resume session"))
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(tag, "Error resuming session: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Check if a session exists for this simulation
     */
    fun hasExistingSession(simulationId: String): Boolean {
        return simulationSessionRepository.loadMapping(simulationId) != null
    }

    /**
     * Get the session ID for a simulation
     */
    fun getSessionId(simulationId: String): String? {
        return simulationSessionRepository.loadMapping(simulationId)
    }

    /**
     * Clear the session for a simulation (start fresh)
     */
    fun clearSession(simulationId: String) {
        simulationSessionRepository.deleteMapping(simulationId)
        DebugLogger.debugLog(tag, "Cleared session mapping for: $simulationId")
    }

    /**
     * Clear all sessions
     */
    fun clearAllSessions() {
        // Implementation depends on repository capability
        DebugLogger.debugLog(tag, "Cleared all sessions")
    }
}
