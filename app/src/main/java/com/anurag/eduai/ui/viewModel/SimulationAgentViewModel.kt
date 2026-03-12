package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.model.SimQuizAnswerRequest
import com.anurag.eduai.data.model.SimSessionResponse
import com.anurag.eduai.data.model.SimStartSessionRequest
import com.anurag.eduai.data.model.SimStudentResponseRequest
import com.anurag.eduai.data.remote.SimulationAgentAPI
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.domain.chatbot.usecase.AvatarChangeUseCase
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatBotSettingsState
import com.anurag.eduai.utils.isKannada
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
    private val api: SimulationAgentAPI = SimulationAgentAPI(),
    private val avatarChangeUseCase: AvatarChangeUseCase = AvatarChangeUseCase()
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

    // NEW: Track if TTS should be triggered for current message
    private val _shouldTriggerTts = MutableStateFlow(false)
    val shouldTriggerTts: StateFlow<Boolean> = _shouldTriggerTts.asStateFlow()

    // Input Enabling Logic
    private val _isInputEnabled = MutableStateFlow(true)
    val isInputEnabled: StateFlow<Boolean> = _isInputEnabled.asStateFlow()

    // WebView delay job
    private var webViewDelayJob: Job? = null

    // NEW: Track current simulation ID to prevent re-starting on config change
    private var currentSimulationId: String? = null

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
        val input = _userInput.value.trim()
        if (input.isBlank() || !_isInputEnabled.value) {
            return
        }

        // Hide webview immediately when sending
        _showWebView.value = false

        // Clear input IMMEDIATELY
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
        // Reset the trigger flag once TTS has started
        _shouldTriggerTts.value = false
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
     * Acknowledge that TTS was triggered - prevents re-triggering on config change
     */
    fun onTtsTriggered() {
        _shouldTriggerTts.value = false
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
        currentSimulationId = null // Reset to allow retry
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
     * Business Logic
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

            // IMPORTANT: Set flag to trigger TTS in UI
            _shouldTriggerTts.value = true

            // Process simulation URLs
            val paramChange = response.simulation.paramChange
            if (paramChange != null) {
                // Use the URL properties if available, otherwise use the main URL
                _simulationUrls.value = if (paramChange.beforeUrl != null && paramChange.afterUrl != null) {
                    listOf(
                        paramChange.beforeUrl,
                        paramChange.afterUrl
                    )
                } else {
                    // If URLs are not provided, just use the main simulation URL
                    listOf(response.simulation.htmlUrl)
                }
            } else {
                _simulationUrls.value = listOf(response.simulation.htmlUrl)
            }

            _isSessionStarted.value = true

            DebugLogger.debugLog(TAG, "📝 New teacher message processed:")
            DebugLogger.debugLog(TAG, "  Message: ${response.teacherMessage.text}")
            DebugLogger.debugLog(TAG, "  Has param change: ${paramChange != null}")
            DebugLogger.debugLog(TAG, "  URL count: ${_simulationUrls.value.size}")
        } else {
            DebugLogger.debugLog(TAG, "Same message - no state change needed")
        }
    }

    /**
     * Reset session data (for back navigation)
     */
    private fun resetSessionForNavigation() {
        currentSimulationId = null
        _sessionData.value = null
        _uiState.value = SimAgentUiState.Initial
        _currentTeacherMessage.value = ""
        _showWebView.value = false
        _simulationUrls.value = emptyList()
        _isSessionStarted.value = false
        _errorMessage.value = null
        _userInput.value = ""
        _hasSpokeCurrentMessage.value = false
        _shouldTriggerTts.value = false
        webViewDelayJob?.cancel()
        DebugLogger.debugLog(TAG, "Session reset for navigation")
    }

    /**
     * ERROR HANDLING
     */

    /**
     * Handle exceptions and return user-friendly error message
     */
    private fun handleError(e: Exception, operation: String): String {
        val errorMessage = when (e) {
            is SocketTimeoutException -> {
                "Connection timed out. Please check your internet connection."
            }

            is UnknownHostException -> {
                "Unable to reach server. Please check your internet connection."
            }

            is retrofit2.HttpException -> {
                when (e.code()) {
                    404 -> "Simulation not found. Please try a different simulation."
                    500 -> "Server error. Please try again later."
                    else -> "Network error (${e.code()}). Please try again."
                }
            }

            else -> {
                when (operation) {
                    "start_session" -> "Failed to start simulation. Please try again."
                    "send_response" -> "Failed to send response. Please try again."
                    "submit_quiz" -> "Failed to submit quiz answer. Please try again."
                    "get_session" -> "Failed to retrieve session. Please try again."
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
     * IMPORTANT: Only starts if simulation ID has changed (prevents re-start on config change)
     */
    fun startNewSession(simulationId: String) {
        // Check if we're already in this simulation session
        if (currentSimulationId == simulationId && _sessionData.value != null) {
            DebugLogger.debugLog(TAG, "⏭️ Already in session for $simulationId - skipping restart")
            return
        }

        currentSimulationId = simulationId

        viewModelScope.launch {
            try {
                _uiState.value = SimAgentUiState.Loading
                DebugLogger.debugLog(TAG, "Starting new session for simulation: $simulationId")

                // Get current app language
                val currentLanguage = if (isKannada()) "kannada" else "english"
                DebugLogger.debugLog(TAG, "Starting session with language: $currentLanguage")

                val response = api.startSession(
                    SimStartSessionRequest(
                        simulationId = simulationId,
                        language = currentLanguage,
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
                    DebugLogger.debugLog(TAG, "  Before Value: ${change.before}")
                    DebugLogger.debugLog(TAG, "  After Value: ${change.after}")
                    DebugLogger.debugLog(TAG, "  Before URL: ${change.beforeUrl}")
                    DebugLogger.debugLog(TAG, "  After URL: ${change.afterUrl}")
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
        resetSessionForNavigation()
    }

    /**
     * Handles avatar change with proper validation and delegation to use case
     * Returns updated ChatBotSettingsState with both code and display name
     */
    fun handleAvatarChange(
        displayName: String,
        boyDisplayName: String,
        girlDisplayName: String,
        ttsController: TextToSpeech,
        currentState: ChatBotSettingsState
    ): ChatBotSettingsState {
        // Convert display name to code
        val avatarCode = avatarChangeUseCase.getAvatarCodeFromDisplayName(
            displayName = displayName,
            boyDisplayName = boyDisplayName,
            girlDisplayName = girlDisplayName
        )

        // Apply avatar change through use case (simulation always uses "en" language)
        val normalizedCode = avatarChangeUseCase.changeAvatar(
            avatarCode = avatarCode,
            ttsController = ttsController,
            currentLanguage = "en"
        )

        // Return updated state with both code and display name
        return currentState.copy(
            selectedAvatar = normalizedCode,
            selectedAvatarDisplayName = displayName
        )
    }

    /**
     * Initialize settings state with proper display name for current avatar
     */
    fun initializeAvatarDisplayName(
        avatarCode: String,
        boyDisplayName: String,
        girlDisplayName: String,
        disableDisplayName: String,
        currentState: ChatBotSettingsState
    ): ChatBotSettingsState {
        val displayName = avatarChangeUseCase.getDisplayNameFromCode(
            avatarCode = avatarCode,
            boyDisplayName = boyDisplayName,
            girlDisplayName = girlDisplayName,
            disableDisplayName = disableDisplayName
        )
        return currentState.copy(
            selectedAvatar = avatarCode,
            selectedAvatarDisplayName = displayName
        )
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