package com.anurag.eduai.ui.viewModel

import ChatMessageModel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.BuildConfig
import com.anurag.eduai.R
import com.anurag.eduai.data.local.ConceptSessionRepository
import com.anurag.eduai.data.remote.AgenticAIClient
import com.anurag.eduai.data.remote.SessionMetadata
import com.anurag.eduai.debug.DebugLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.collections.set


/**
 * ViewModel for managing chat interactions with an AI agent.
 */
class ChatViewModel (): ViewModel() {

    private val agenticAIClient = AgenticAIClient(BuildConfig.AGENTIC_AI_BASE_URL)

    // Chat messages
    private val _messages = MutableStateFlow<List<ChatMessageModel>>(emptyList())
    val messages: StateFlow<List<ChatMessageModel>> = _messages

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Typing animation state
    private val _typingText = MutableStateFlow("")
    val typingText: StateFlow<String> = _typingText

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    //student level
    private val _studentLevel = MutableStateFlow("medium")
    val studentLevel: StateFlow<String> = _studentLevel
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage
    // Selected concept state
    private val _selectedConcept = MutableStateFlow<String?>(null)
    val selectedConcept: StateFlow<String?> = _selectedConcept

    // Autosuggestions state
    private val _autosuggestions = MutableStateFlow<List<String>>(emptyList())
    val autosuggestions: StateFlow<List<String>> = _autosuggestions

    // Track if last autosuggestion was clicked
    private var clickedAutosuggestion = false

    // Maps to store thread and session IDs for concepts
    private val conceptThreadMap = mutableMapOf<String, String>()
    private val conceptSessionMap = mutableMapOf<String, String>()
    // Session started state
    private val _isSessionStarted = MutableStateFlow(false)
    val isSessionStarted: StateFlow<Boolean> = _isSessionStarted

    //TTS trigger state
    private val _shouldStartTTS = MutableStateFlow(false)
    val shouldStartTTS: StateFlow<Boolean> = _shouldStartTTS

    private val _fullTextForTTS = MutableStateFlow("")
    val fullTextForTTS: StateFlow<String> = _fullTextForTTS


    // Last user message for when the send msg failed
    private val _lastUserMessage = MutableStateFlow("")
    //Job for typing animation
    private var typingJob: Job? = null

    //agent/session metadata
    private val _agentMetadata = MutableStateFlow<SessionMetadata?>(null)
    val agentMetadata: StateFlow<SessionMetadata?> = _agentMetadata

    // Available concepts
    private val _availableConcepts = MutableStateFlow<List<String>>(emptyList())
    val availableConcepts: StateFlow<List<String>> = _availableConcepts

    // Input text state
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText

    // Pending first user message if session not ready
    private val _pendingFirstUserMessage = MutableStateFlow<String?>(null)


    // Chat UI state (wrapper for all UI state)
    data class ChatUIState(
        val messages: List<ChatMessageModel> = emptyList(),
        val inputText: String = ""
    )

    private val _uiState = MutableStateFlow(ChatUIState())
    val uiState: StateFlow<ChatUIState> = _uiState
    private var userId: String = ""

    /**
     * Update the input text field
     */
    fun updateInputText(text: String) {
        _inputText.value = text
        _uiState.update { it.copy(inputText = text) }
    }


    /**
     * Update UI state with new messages
     */
    private fun updateUIState() {
        _uiState.update { currentState ->
            currentState.copy(
                messages = _messages.value,
                inputText = _inputText.value
            )
        }
    }
    /**
     * Set the student level
     */
    fun setStudentLevel(level: String) {
        _studentLevel.value = level
        DebugLogger.debugLog("ChatViewModel", "Student level changed to: $level")
    }
    fun initialize(id : String) {
        viewModelScope.launch {
            DebugLogger.debugLog("ChatViewModel", "Starting full initialization")
            refreshAvailableConcepts()
            if (userId.isEmpty()) {
                userId = id
            }
            DebugLogger.debugLog("ChatViewModel", "Initialization complete")
        }
    }

    /**
     * Refresh the list of available concepts from the server
     */
    fun refreshAvailableConcepts( ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Call getConceptsList() which returns Result<ConceptsListResponse>
                val result = agenticAIClient.getConceptsList()

                if (result.isSuccess) {
                    val response = result.getOrNull()

                    // Extract the concepts list from the response
                    val conceptsList = response?.concepts ?: emptyList()

                    if (conceptsList.isNotEmpty()) {
                        // Update the state with the concepts
                        _availableConcepts.value = conceptsList
                        DebugLogger.debugLog("ChatViewModel", "Concepts refreshed successfully: ${conceptsList.size} concepts loaded")
                    } else {
                        DebugLogger.debugLog("ChatViewModel", "No concepts returned from server")
                    }
                } else {
                    DebugLogger.errorLog("ChatViewModel", "Failed to refresh concepts: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "refreshAvailableConcepts exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAllSessions(context: Context) {
        viewModelScope.launch {
            try {
                // Clear in-memory maps
                conceptThreadMap.clear()
                conceptSessionMap.clear()

                // Clear SharedPreferences
                withContext(Dispatchers.IO) {
                    ConceptSessionRepository(context.applicationContext).clearAllMappings()
                }

                // Reset current session state
                _isSessionStarted.value = false
                agenticAIClient.setCurrentThreadAndSession(null, null)

                // Clear messages and states
                _messages.value = emptyList()
                _selectedConcept.value = null
                _pendingFirstUserMessage.value = null
                _fullTextForTTS.value = ""
                _autosuggestions.value = emptyList()

                DebugLogger.debugLog("ChatViewModel", "All sessions cleared successfully")
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "clearAllSessions failed: ${e.message}")
            }
        }
    }
    // Save the thread and session mapping for a concept
    private fun saveThreadMapping(context: Context, concept: String, threadId: String?, sessionId: String?) {
        if (threadId.isNullOrBlank()) return

        conceptThreadMap[concept] = threadId
        sessionId?.let { conceptSessionMap[concept] = it }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                ConceptSessionRepository(context.applicationContext).saveMapping(concept, threadId, sessionId)
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "saveThreadMapping failed: ${e.message}")
            }
        }
    }

    // Load the thread and session mapping for a concept
    private suspend fun loadThreadMapping(context: Context, concept: String): Pair<String, String?>? {
        conceptThreadMap[concept]?.let { threadId ->
            val sessionId = conceptSessionMap[concept]
            return Pair(threadId, sessionId)
        }

        return withContext(Dispatchers.IO) {
            try {
                ConceptSessionRepository(context.applicationContext).loadMapping(concept)?.also { (thread, session) ->
                    conceptThreadMap[concept] = thread
                    if (!session.isNullOrBlank()) conceptSessionMap[concept] = session
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "loadThreadMapping failed: ${e.message}")
                null
            }
        }
    }


    fun sendMessage(userMessage: String,context: Context) {
        if (userMessage.isBlank()) return

        _lastUserMessage.value = userMessage

        _messages.update { it + ChatMessageModel(content = userMessage, sender = "user") }
        updateUIState()
        if (!_isSessionStarted.value) {
            _pendingFirstUserMessage.value = userMessage
            DebugLogger.debugLog("ChatViewModel", "Session not ready - queued message")
            return
        }
        sendMessageAfterSessionReady(userMessage, context)

        }

    private fun sendMessageAfterSessionReady(userMessage: String, context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Get response from AI agent
                val response =withTimeout(120_000L) {
                        agenticAIClient.continueSession(
                        userMessage = userMessage,
                        clickedAutosuggestion = clickedAutosuggestion,
                        studentLevel = _studentLevel.value
                    )
                }

                if (response.isSuccess) {
                    val resp = response.getOrNull()!!
                    val text = resp.agentResponse.orEmpty()
                    // Add agent message with typing animation
                    resp.metadata.let { _agentMetadata.value = it }

                    updateAutosuggestions(resp.autosuggestions)
                    DebugLogger.debugLog("ChatViewModel", "├─ Autosuggestions from continueSession: ${resp.autosuggestions.size}")
                    resp.autosuggestions.forEachIndexed { idx, suggestion ->
                        DebugLogger.debugLog("ChatViewModel", "  [$idx] $suggestion")
                    }
                    startTypingAnimation(text, context)


                } else {
                    // Handle error - add error message
                    _messages.update {
                        it + ChatMessageModel(
                            content = context.getString(R.string.sorry_i_couldn_t_process_that_please_try_again),
                            sender = "ai",
                            isError = true,
                            canRetry = true
                        )
                    }
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "sendMessage error: ${e.message}")
                _messages.update {
                    it + ChatMessageModel(
                        content = "An error occurred. Please try again.",
                        sender = "ai",
                        isError = true,
                        canRetry = true
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }

    }
    /**
     * Update autosuggestions from API response
     */
    private fun updateAutosuggestions(suggestions: List<String>) {
        _autosuggestions.value = suggestions
        DebugLogger.debugLog(
            "ChatViewModel",
            "Autosuggestions updated: ${suggestions.size} suggestions"
        )
        suggestions.forEachIndexed { index, suggestion ->
            DebugLogger.debugLog("ChatViewModel", "  [$index] $suggestion")
        }
    }

    /**
     * Select a concept to start or resume a session
     * - Check for existing session mapping
     * - Resume or start new session accordingly
     * - Manage loading and animation states
     *
     */
    fun selectConcept( concept: String,context: Context) {
        viewModelScope.launch {

            _isLoading.value = true
            _messages.value = emptyList()
            //reset autosuggestions when selecting new concept
            _autosuggestions.value = emptyList()
            _typingText.value = ""
            _isTyping.value = false
            updateUIState()

            _selectedConcept.value = concept
            cancelAnimations()

            try {
                val stored = loadThreadMapping(context, concept)
                if (stored != null) {
                    resumeExistingSession(context, stored.first,stored.second)
                } else {
                    sessionStart(context,concept)
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "selectConcept error: ${e.message}")
                _isLoading.value =false
            }
        }
    }
    /**
     * Start a new session for a concept
     * - Creates new thread and session on server
     * - Clears all previous messages
     * - Displays only the initial AI response
     */
    fun sessionStart(context: Context, concept: String) {
        viewModelScope.launch {
            try {
                val result = agenticAIClient.startSession(
                    conceptTitle = concept,
                    studentId = userId,
                    isKannada = false,
                    studentLevel = _studentLevel.value
                )

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    val text= response?.agentResponse
                    if (response != null && response.success) {
                        // Save the mapping and update thread/session
                        _isSessionStarted.value = true
                        saveThreadMapping(context, concept, response.threadId, response.sessionId)
                        agenticAIClient.setCurrentThreadAndSession(response.threadId, response.sessionId)
                        updateAutosuggestions(response.autosuggestions)
                        _isSessionStarted.value = true

                        // Show the initial agent response with typing animation if available
                        if (!text.isNullOrBlank()) {
                            startTypingAnimation(text, context)
                        }

                        DebugLogger.debugLog("ChatViewModel", "Session started for concept: $concept")
                    }
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "sessionStart error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
            // Send any pending user message that was queued before session was ready
            _pendingFirstUserMessage.value?.let { msg ->
                _pendingFirstUserMessage.value = null
                sendMessageAfterSessionReady(msg, context)
            }
        }
    }

    /**
     * Resume an existing session given thread and session IDs
     * - Clears previous messages
     * - Sets current thread and session
     * - Marks session as started
     * - Fetches FULL session history and loads ALL messages
     * - Loads last assistant message with typing animation
     */
    private suspend fun resumeExistingSession(
        context: Context,
        threadId: String,
        sessionId: String?
    ) {
        DebugLogger.debugLog("ChatViewModel", "Resuming session - thread=$threadId")

        // ✓ CLEAR previous messages first
        _messages.value = emptyList()
        updateUIState()

        // Set current thread and session
        agenticAIClient.setCurrentThreadAndSession(threadId, sessionId)

        // Mark session as started
        _isSessionStarted.value = true

        try {
            // Fetch session history
            val histResult = agenticAIClient.getSessionHistory(threadId)

            // On success, load ALL messages from history
            if (histResult.isSuccess) {
                val history = histResult.getOrNull()
                val messages = history?.messages ?: emptyList()

                DebugLogger.debugLog(
                    "ChatViewModel",
                    "Loaded ${messages.size} messages from history"
                )

                // Convert history to ChatMessageModel and add to messages
                val chatMessages = messages.mapNotNull { msg ->
                    val role = (msg["role"] as? String)?.lowercase() ?: return@mapNotNull null
                    val content = msg["content"] as? String ?: return@mapNotNull null

                    val sender = when (role) {
                        "assistant", "ai" -> "ai"
                        "user" -> "user"
                        else -> return@mapNotNull null
                    }

                    ChatMessageModel(
                        sender = sender,
                        content = content,
                        timestamp = (msg["timestamp"] as? Long) ?: System.currentTimeMillis()
                    )
                }

                // Add all messages to chat
                if (chatMessages.isNotEmpty()) {
                    _messages.value = chatMessages
                    updateUIState()

                    DebugLogger.debugLog(
                        "ChatViewModel",
                        "Added ${chatMessages.size} messages to chat"
                    )
                }
            } else {
                DebugLogger.errorLog(
                    "ChatViewModel",
                    "Failed to fetch history: ${histResult.exceptionOrNull()?.message}"
                )
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("ChatViewModel", "Error resuming session: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Start typing animation for AI response
     * - Adds message to chat IMMEDIATELY (before animation)
     * - Updates TTS and typing text state
     * - Animates typing word by word
     */
    private fun startTypingAnimation(fullText: String, context: Context) {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            _messages.update {
                it + ChatMessageModel(sender = "ai", content = fullText)
            }
            updateUIState()

            _isTyping.value = true
            _typingText.value = ""
            _fullTextForTTS.value = fullText

            // Trigger TTS to start
            _shouldStartTTS.value = true
            delay(100)
            _shouldStartTTS.value = false

            DebugLogger.debugLog("ChatViewModel", "AI RAW OUTPUT (preview): ${fullText.take(1000)}")

            val words = fullText.split(" ")
            words.forEachIndexed { index, word ->
                _typingText.value += if (index == 0) word else " $word"
                delay(120L + (word.length * 8L).coerceAtMost(200L))
            }

            _isTyping.value = false
            _typingText.value = ""
        }
    }
    /**
     * Start fresh session - clears all history
     */
    fun startFreshSession(concept: String, context: Context) {
        viewModelScope.launch {
            try {
                conceptThreadMap.remove(concept)
                conceptSessionMap.remove(concept)

                withContext(Dispatchers.IO) {
                    ConceptSessionRepository(context.applicationContext).deleteMapping(concept)
                }

                // Clear messages BEFORE selecting new concept
                _isSessionStarted.value = false
                agenticAIClient.setCurrentThreadAndSession(null, null)
                _messages.value = emptyList()
                _autosuggestions.value = emptyList()
                _selectedConcept.value = null
                _pendingFirstUserMessage.value = null
                _typingText.value = ""
                _isTyping.value = false
                updateUIState()
                cancelAnimations()

                // Now start the new session
                selectConcept(concept, context)
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "startFreshSession failed: ${e.message}")
                _messages.update {
                    it + ChatMessageModel(
                        content = "Error: ${e.message}",
                        sender = "ai",
                        isError = true,
                        canRetry = true
                    )
                }
                _isTyping.value = false
                _isLoading.value = false
            }
        }
    }

    /**
     * Add agent message directly (no typing animation)
     */
    fun addAgentMessage(text: String) {
        _messages.update {
            it + ChatMessageModel(content = text, sender = "ai")
        }
    }
    /**
     * Add user message directly
     */
    fun addUserMessage(text: String) {
        _messages.update {
            it + ChatMessageModel(content = text, sender = "user")
        }
    }

    private fun cancelAnimations() {
        typingJob?.cancel()
        _isTyping.value = false
        _typingText.value = ""
    }
    fun hasExistingSession(concept: String, context: Context): Boolean {
        val stored = conceptThreadMap[concept]
        return stored != null
    }
}

