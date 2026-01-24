package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.model.SimQuizAnswerRequest
import com.anurag.eduai.data.model.SimSessionResponse
import com.anurag.eduai.data.model.SimStartSessionRequest
import com.anurag.eduai.data.model.SimStudentResponseRequest
import com.anurag.eduai.data.remote.SimulationAgentAPI
import com.anurag.eduai.debug.DebugLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Teaching Agent screen
 * Handles all API interactions and state management
 */
class SimulationAgentViewModel(
    private val api: SimulationAgentAPI = SimulationAgentAPI()
) : ViewModel() {

    private val _uiState = MutableStateFlow<SimAgentUiState>(SimAgentUiState.Initial)
    val uiState: StateFlow<SimAgentUiState> = _uiState.asStateFlow()

    private val _sessionData = MutableStateFlow<SimSessionResponse?>(null)
    val sessionData: StateFlow<SimSessionResponse?> = _sessionData.asStateFlow()

    private val _availableSimulations = MutableStateFlow<List<SimulationInfo>>(emptyList())
    val availableSimulations: StateFlow<List<SimulationInfo>> = _availableSimulations.asStateFlow()

    private val _simulationsLoading = MutableStateFlow(false)
    val simulationsLoading: StateFlow<Boolean> = _simulationsLoading.asStateFlow()

    companion object {
        private const val TAG = "SimulationAgentVM"
    }

    /**
     * Load all available simulations from the API
     */
    fun loadAvailableSimulations() {
        viewModelScope.launch {
            try {
                _simulationsLoading.value = true
                DebugLogger.debugLog(TAG, "Loading available simulations...")

                val response = api.getAvailableSimulations()

                val simulations = response.simulations.map { sim ->
                    SimulationInfo(
                        id = sim.id,
                        title = sim.title,
                        description = sim.description
                    )
                }

                _availableSimulations.value = simulations
                 DebugLogger.debugLog(TAG, "✅ Loaded ${simulations.size} simulations")
                simulations.forEach {
                     DebugLogger.debugLog(TAG, "  - ${it.title} (${it.id})")
                }

            } catch (e: Exception) {
                 DebugLogger.errorLog(TAG, "❌ Failed to load simulations\n$e")
                // Fallback to default simulations
                _availableSimulations.value = listOf(
                    SimulationInfo("simple_pendulum", "Simple Pendulum", ""),
                    SimulationInfo("earth_rotation_revolution", "Earth Rotation & Revolution", ""),
                    SimulationInfo("light_shadows", "Light & Shadows", "")
                )
            } finally {
                _simulationsLoading.value = false
            }
        }
    }

    /**
     * Start a new teaching session
     */
    fun startNewSession(simulationId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = SimAgentUiState.Loading
                 DebugLogger.debugLog(TAG, "Starting new session for simulation: $simulationId")

                val response = api.startSession(
                    SimStartSessionRequest(
                        simulationId = simulationId,
                        studentId = null
                    )
                )

                 DebugLogger.debugLog(TAG, "Session started successfully")
                 DebugLogger.debugLog(TAG, "Session ID: ${response.sessionId}")
                 DebugLogger.debugLog(TAG, "Teacher Message: ${response.teacherMessage.text}")
                 DebugLogger.debugLog(TAG, "Simulation URL: ${response.simulation.htmlUrl}")
                 DebugLogger.debugLog(TAG, "Current Params: ${response.simulation.currentParams}")
                 DebugLogger.debugLog(TAG, "Full Response: $response")

                _sessionData.value = response
                _uiState.value = SimAgentUiState.Success(response)

            } catch (e: Exception) {
                 DebugLogger.errorLog(TAG, "Failed to start session\n$e")
                _uiState.value = SimAgentUiState.Error(
                    e.message ?: "Failed to start session"
                )
            }
        }
    }

    /**
     * Send a student response to the current session
     */
    fun sendStudentResponse(response: String) {
        val currentSessionId = _sessionData.value?.sessionId
        if (currentSessionId == null) {
             DebugLogger.errorLog(TAG, "No active session")
            _uiState.value = SimAgentUiState.Error("No active session")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = SimAgentUiState.Loading
                 DebugLogger.debugLog(TAG, "Sending student response: $response")

                val apiResponse = api.sendResponse(
                    sessionId = currentSessionId,
                    request = SimStudentResponseRequest(studentResponse = response)
                )

                 DebugLogger.debugLog(TAG, "Response received successfully")
                 DebugLogger.debugLog(TAG, "Teacher Message: ${apiResponse.teacherMessage.text}")
                 DebugLogger.debugLog(TAG, "Understanding Level: ${apiResponse.learningState.understandingLevel}")
                 DebugLogger.debugLog(TAG, "Exchange Count: ${apiResponse.learningState.exchangeCount}")

                apiResponse.simulation.paramChange?.let { change ->
                     DebugLogger.debugLog(TAG, "Parameter Changed!")
                     DebugLogger.debugLog(TAG, "  Parameter: ${change.parameter}")
                     DebugLogger.debugLog(TAG, "  Before: ${change.before}")
                     DebugLogger.debugLog(TAG, "  After: ${change.after}")
                     DebugLogger.debugLog(TAG, "  Reason: ${change.reason}")
                     DebugLogger.debugLog(TAG, "  New URL: ${change.afterUrl}")
                }

                 DebugLogger.debugLog(TAG, "Full Response: $apiResponse")

                _sessionData.value = apiResponse
                _uiState.value = SimAgentUiState.Success(apiResponse)

            } catch (e: Exception) {
                 DebugLogger.errorLog(TAG, "Failed to send response\n$e")
                _uiState.value = SimAgentUiState.Error(
                    e.message ?: "Failed to send response"
                )
            }
        }
    }

    /**
     * Submit quiz answer for the current session
     */
    fun submitQuizAnswer(answer: String) {
        val currentSessionId = _sessionData.value?.sessionId
        if (currentSessionId == null) {
             DebugLogger.errorLog(TAG, "❌ No active session")
            _uiState.value = SimAgentUiState.Error("No active session")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = SimAgentUiState.Loading
                 DebugLogger.debugLog(TAG, "Submitting quiz answer: $answer")

                val apiResponse = api.submitQuizAnswer(
                    sessionId = currentSessionId,
                    request = SimQuizAnswerRequest(answer = answer)
                )

                 DebugLogger.debugLog(TAG, "✅ Quiz answer submitted successfully")
                 DebugLogger.debugLog(TAG, "Teacher Message: ${apiResponse.teacherMessage.text}")

                _sessionData.value = apiResponse
                _uiState.value = SimAgentUiState.Success(apiResponse)

            } catch (e: Exception) {
                 DebugLogger.errorLog(TAG, "❌ Failed to submit quiz answer\n$e")
                _uiState.value = SimAgentUiState.Error(
                    e.message ?: "Failed to submit quiz answer"
                )
            }
        }
    }

    /**
     * Get the current session state (for recovery)
     */
    fun getSessionState(sessionId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = SimAgentUiState.Loading
                 DebugLogger.debugLog(TAG, "Fetching session state for: $sessionId")

                val response = api.getSession(sessionId)

                 DebugLogger.debugLog(TAG, "✅ Session state retrieved")
                 DebugLogger.debugLog(TAG, "Exchange Count: ${response.learningState.exchangeCount}")
                 DebugLogger.debugLog(TAG, "Current Concept: ${response.concepts.currentConcept?.title}")
                 DebugLogger.debugLog(TAG, "Full Response: $response")

                _sessionData.value = response
                _uiState.value = SimAgentUiState.Success(response)

            } catch (e: Exception) {
                 DebugLogger.errorLog(TAG, "❌ Failed to get session state\n$e")
                _uiState.value = SimAgentUiState.Error(
                    e.message ?: "Failed to get session state"
                )
            }
        }
    }

    /**
     * Reset session data
     */
    fun resetSession() {
        _sessionData.value = null
        _uiState.value = SimAgentUiState.Initial
    }

    /**
     * Test health check endpoint
     */
    fun testHealthCheck() {
        viewModelScope.launch {
            try {
                 DebugLogger.debugLog(TAG, "Testing health check...")
                val health = api.healthCheck()
                 DebugLogger.debugLog(TAG, "✅ Health check successful: $health")
            } catch (e: Exception) {
                 DebugLogger.errorLog(TAG, "❌ Health check failed $e")
            }
        }
    }
}

/**
 * UI State for the teaching screen
 */
sealed class SimAgentUiState {
    object Initial : SimAgentUiState()
    object Loading : SimAgentUiState()
    data class Success(val data: SimSessionResponse) : SimAgentUiState()
    data class Error(val message: String) : SimAgentUiState()
}

/**
 * Simulation info data class
 */
data class SimulationInfo(
    val id: String,
    val title: String,
    val description: String
)