package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.model.SimQuizAnswerRequest
import com.anurag.eduai.data.model.SimSessionResponse
import com.anurag.eduai.data.model.SimStartSessionRequest
import com.anurag.eduai.data.model.SimStudentResponseRequest
import com.anurag.eduai.data.remote.SimulationAgentAPI
import com.anurag.eduai.debug.DebugLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * ViewModel for the Simulation Agent screen
 * Contains ALL business logic - UI is purely presentational
 */
class SimulationAgentViewModel(
    private val api: SimulationAgentAPI = SimulationAgentAPI()
) : ViewModel() {

    // API/Session State
    private val _uiState = MutableStateFlow<SimAgentUiState>(SimAgentUiState.Initial)
    val uiState: StateFlow<SimAgentUiState> = _uiState.asStateFlow()

    private val _sessionData = MutableStateFlow<SimSessionResponse?>(null)
    val sessionData: StateFlow<SimSessionResponse?> = _sessionData.asStateFlow()

    // Simulations List
    private val _availableSimulations = MutableStateFlow<List<SimulationInfo>>(emptyList())
    val availableSimulations: StateFlow<List<SimulationInfo>> = _availableSimulations.asStateFlow()

    private val _simulationsLoading = MutableStateFlow(false)
    val simulationsLoading: StateFlow<Boolean> = _simulationsLoading.asStateFlow()

    // UI Control State - ALL UI logic managed here
    private val _currentTeacherMessage = MutableStateFlow("")
    val currentTeacherMessage: StateFlow<String> = _currentTeacherMessage.asStateFlow()

    private val _showWebView = MutableStateFlow(false)
    val showWebView: StateFlow<Boolean> = _showWebView.asStateFlow()

    private val _simulationUrls = MutableStateFlow<List<String>>(emptyList())
    val simulationUrls: StateFlow<List<String>> = _simulationUrls.asStateFlow()

    private val _isSessionStarted = MutableStateFlow(false)
    val isSessionStarted: StateFlow<Boolean> = _isSessionStarted.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userInput = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput.asStateFlow()

    // TTS/Speech State Management
    private val _isTtsSpeaking = MutableStateFlow(false)
    val isTtsSpeaking: StateFlow<Boolean> = _isTtsSpeaking.asStateFlow()

    private val _hasSpokeCurrentMessage = MutableStateFlow(false)
    val hasSpokeCurrentMessage: StateFlow<Boolean> = _hasSpokeCurrentMessage.asStateFlow()

    // Input Enabling Logic
    private val _isInputEnabled = MutableStateFlow(true)
    val isInputEnabled: StateFlow<Boolean> = _isInputEnabled.asStateFlow()

    // WebView delay job
    private var webViewDelayJob: Job? = null

    companion object {
        private const val TAG = "SimulationAgentVM"
        private const val WEBVIEW_DELAY_MS = 300L
    }

    init {
        // Update input enabled state whenever TTS or loading state changes
        viewModelScope.launch {
            uiState.collect { state ->
                updateInputEnabledState(state)
            }
        }

        viewModelScope.launch {
            isTtsSpeaking.collect {
                updateInputEnabledState(uiState.value)
            }
        }
    }

    /**
     * Public API UI Logic
     */
    /**
     * Called when user types in input field
     */
    fun onUserInputChanged(text: String) {
        _userInput.value = text
    }

    /**
     * Called when user clicks send button
     */
    fun onSendClick() {
        val input = _userInput.value
        if (input.isBlank() || !_isInputEnabled.value) {
            return
        }

        // Hide webview immediately when sending
        _showWebView.value = false

        // Clear input
        _userInput.value = ""

        // Send response
        sendStudentResponse(input)
    }

    /**
     * Called when TTS starts speaking
     */
    fun onTtsStarted() {
        _isTtsSpeaking.value = true
        _hasSpokeCurrentMessage.value = false
    }

    /**
     * Called when TTS stops speaking (either finished or manually stopped)
     */
    fun onTtsStopped() {
        _isTtsSpeaking.value = false

        // If message hasn't been spoken yet, trigger webview display
        if (!_hasSpokeCurrentMessage.value &&
            _currentTeacherMessage.value.isNotEmpty() &&
            _simulationUrls.value.isNotEmpty()
        ) {
            scheduleWebViewDisplay()
        }
    }

    /**
     * Called when WebView close button is clicked
     */
    fun onWebViewClose() {
        _showWebView.value = false
    }

    /**
     * Called when back button is pressed
     */
    fun onBackPressed(): Boolean {
        return if (_showWebView.value) {
            _showWebView.value = false
            true // consumed
        } else {
            resetSession()
            false // not consumed - navigate back
        }
    }

    /**
     * Called when retry button is clicked in error state
     */
    fun onRetryClick(simulationId: String) {
        _errorMessage.value = null
        startNewSession(simulationId)
    }

    /**
     * Called when settings change avatar
     */
    fun onAvatarChanged() {
        // If TTS is speaking, it will be stopped and restarted by TTS controller
        // We just need to prevent showing webview again
        _hasSpokeCurrentMessage.value = true
    }

    /**
     * Called when settings change voice
     */
    fun onVoiceChanged() {
        // If TTS is speaking, it will be stopped and restarted by TTS controller
        // We just need to prevent showing webview again
        _hasSpokeCurrentMessage.value = true
    }

    /**
     * Called when settings change speed
     */
    fun onSpeedChanged() {
        // If TTS is speaking, it will be stopped and restarted by TTS controller
        // We just need to prevent showing webview again
        _hasSpokeCurrentMessage.value = true
    }

    /**
     * Business Login
     */

    /**
     * Update input enabled state based on loading and TTS state
     */
    private fun updateInputEnabledState(currentUiState: SimAgentUiState) {
        _isInputEnabled.value = currentUiState !is SimAgentUiState.Loading && !_isTtsSpeaking.value
    }

    /**
     * Schedule webview display after delay
     */
    private fun scheduleWebViewDisplay() {
        webViewDelayJob?.cancel()
        webViewDelayJob = viewModelScope.launch {
            delay(WEBVIEW_DELAY_MS)
            _hasSpokeCurrentMessage.value = true
            _showWebView.value = true
        }
    }

    /**
     * Process new session response
     */
    private fun processSessionResponse(response: SimSessionResponse) {
        // Check if this is a new message
        val isNewMessage = _currentTeacherMessage.value != response.teacherMessage.text

        if (isNewMessage) {
            // New message - reset state
            _showWebView.value = false
            _hasSpokeCurrentMessage.value = false
            _currentTeacherMessage.value = response.teacherMessage.text

            // Update simulation URLs
            val urls = buildSimulationUrls(response)
            _simulationUrls.value = urls

            // Mark session as started
            _isSessionStarted.value = true

            // Clear any previous errors
            _errorMessage.value = null
        }
    }

    /**
     * Build simulation URLs from response
     */
    private fun buildSimulationUrls(response: SimSessionResponse): List<String> {
        val urls = mutableListOf<String>()

        // Add main simulation URL
        urls.add(response.simulation.htmlUrl)

        // Check if there's a param change (before/after comparison)
        response.simulation.paramChange?.let { change ->
            urls.clear()
            urls.add(change.beforeUrl)
            urls.add(change.afterUrl)
        }

        return urls
    }

    /**
     * Handle error with user-friendly messages
     */
    private fun handleError(e: Exception, operation: String): String {
        val errorMessage = when (e) {
            is UnknownHostException -> {
                "Unable to connect to server. Please check your internet connection."
            }
            is SocketTimeoutException -> {
                when (operation) {
                    "start_session" -> "Connection timed out. The server took too long to respond. Please try again."
                    "send_response" -> "Request timed out. The teacher is taking too long to respond. Please try again."
                    "submit_quiz" -> "Quiz submission timed out. Please try again."
                    "get_session" -> "Session retrieval timed out. Please try again."
                    else -> "Connection timed out. Please try again."
                }
            }
            is java.io.IOException -> {
                when (operation) {
                    "start_session" -> "Network error occurred. Please check your connection and try again."
                    "send_response" -> "Network error. Please check your connection and try sending again."
                    "submit_quiz" -> "Network error during quiz submission. Please try again."
                    "get_session" -> "Network error. Unable to retrieve session state."
                    else -> "Network error occurred. Please try again."
                }
            }
            else -> {
                e.message?.let { msg ->
                    when {
                        msg.contains("500") -> when (operation) {
                            "start_session" -> "Server error occurred. Please try again later."
                            "send_response" -> "Server error. The teacher encountered a problem. Please try again."
                            "submit_quiz" -> "Server error. Failed to grade your answer. Please try again."
                            else -> "Server error occurred. Please try again later."
                        }
                        msg.contains("404") -> when (operation) {
                            "start_session" -> "Simulation not found. Please select a different simulation."
                            "send_response" -> "Session expired. Please restart the simulation."
                            "submit_quiz" -> "Session expired. Please restart the simulation."
                            "get_session" -> "Session not found or expired. Please start a new session."
                            else -> "Resource not found. Please try again."
                        }
                        msg.contains("401") || msg.contains("403") -> {
                            "Authentication failed. Please restart the app."
                        }
                        else -> when (operation) {
                            "start_session" -> "Failed to start session: $msg"
                            "send_response" -> "Failed to send response: $msg"
                            "submit_quiz" -> "Failed to submit quiz answer: $msg"
                            "get_session" -> "Failed to retrieve session: $msg"
                            else -> msg
                        }
                    }
                } ?: when (operation) {
                    "start_session" -> "Failed to start session. Please try again."
                    "send_response" -> "Failed to send your response. Please try again."
                    "submit_quiz" -> "Failed to submit quiz answer. Please try again."
                    "get_session" -> "Failed to get session state. Please try again."
                    else -> "An error occurred. Please try again."
                }
            }
        }

        DebugLogger.errorLog(TAG, "❌ $operation failed: ${e.javaClass.simpleName} - ${e.message}")
        return errorMessage
    }

    /**
     * API OPERATIONS
     */

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
                DebugLogger.errorLog(TAG, "❌ Failed to load simulations: ${e.message}")
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

                DebugLogger.debugLog(TAG, "✅ Session started successfully")
                DebugLogger.debugLog(TAG, "Session ID: ${response.sessionId}")
                DebugLogger.debugLog(TAG, "Teacher Message: ${response.teacherMessage.text}")
                DebugLogger.debugLog(TAG, "Simulation URL: ${response.simulation.htmlUrl}")

                _sessionData.value = response
                processSessionResponse(response)
                _uiState.value = SimAgentUiState.Success(response)

            } catch (e: Exception) {
                val errorMsg = handleError(e, "start_session")
                _errorMessage.value = errorMsg
                _uiState.value = SimAgentUiState.Error(errorMsg)
            }
        }
    }

    /**
     * Send a student response to the current session
     */
    private fun sendStudentResponse(response: String) {
        val currentSessionId = _sessionData.value?.sessionId
        if (currentSessionId == null) {
            val errorMsg = "No active session. Please restart the simulation."
            DebugLogger.errorLog(TAG, "❌ No active session")
            _errorMessage.value = errorMsg
            _uiState.value = SimAgentUiState.Error(errorMsg)
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

                DebugLogger.debugLog(TAG, "✅ Response received successfully")
                DebugLogger.debugLog(TAG, "Teacher Message: ${apiResponse.teacherMessage.text}")
                DebugLogger.debugLog(TAG, "Understanding Level: ${apiResponse.learningState.understandingLevel}")

                apiResponse.simulation.paramChange?.let { change ->
                    DebugLogger.debugLog(TAG, "📊 Parameter Changed!")
                    DebugLogger.debugLog(TAG, "  Parameter: ${change.parameter}")
                    DebugLogger.debugLog(TAG, "  Before: ${change.before}")
                    DebugLogger.debugLog(TAG, "  After: ${change.after}")
                }

                _sessionData.value = apiResponse
                processSessionResponse(apiResponse)
                _uiState.value = SimAgentUiState.Success(apiResponse)

            } catch (e: Exception) {
                val errorMsg = handleError(e, "send_response")
                _errorMessage.value = errorMsg
                _uiState.value = SimAgentUiState.Error(errorMsg)
            }
        }
    }

    /**
     * Submit quiz answer for the current session
     */
    fun submitQuizAnswer(answer: String) {
        val currentSessionId = _sessionData.value?.sessionId
        if (currentSessionId == null) {
            val errorMsg = "No active session. Please restart the simulation."
            DebugLogger.errorLog(TAG, "❌ No active session")
            _errorMessage.value = errorMsg
            _uiState.value = SimAgentUiState.Error(errorMsg)
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
                processSessionResponse(apiResponse)
                _uiState.value = SimAgentUiState.Success(apiResponse)

            } catch (e: Exception) {
                val errorMsg = handleError(e, "submit_quiz")
                _errorMessage.value = errorMsg
                _uiState.value = SimAgentUiState.Error(errorMsg)
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

                _sessionData.value = response
                processSessionResponse(response)
                _uiState.value = SimAgentUiState.Success(response)

            } catch (e: Exception) {
                val errorMsg = handleError(e, "get_session")
                _errorMessage.value = errorMsg
                _uiState.value = SimAgentUiState.Error(errorMsg)
            }
        }
    }

    /**
     * Reset session data
     */
    fun resetSession() {
        _sessionData.value = null
        _uiState.value = SimAgentUiState.Initial
        _currentTeacherMessage.value = ""
        _showWebView.value = false
        _simulationUrls.value = emptyList()
        _isSessionStarted.value = false
        _errorMessage.value = null
        _userInput.value = ""
        _hasSpokeCurrentMessage.value = false
        webViewDelayJob?.cancel()
        DebugLogger.debugLog(TAG, "Session reset")
    }

    override fun onCleared() {
        super.onCleared()
        webViewDelayJob?.cancel()
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