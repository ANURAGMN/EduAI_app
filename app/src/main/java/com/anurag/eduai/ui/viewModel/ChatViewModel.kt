package com.anurag.eduai.ui.viewModel

import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.BuildConfig
import com.anurag.eduai.R
import com.anurag.eduai.data.local.ConceptSessionRepository
import com.anurag.eduai.data.remote.AgenticAIClient
import com.anurag.eduai.data.remote.GeminiLLMClient
import com.anurag.eduai.data.remote.SessionMetadata
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatUiState
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ResourceCardUiState
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.lastAiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject


/**
 * ViewModel for managing chat interactions with an AI agent.
 */
@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    private val agenticAIClient = AgenticAIClient(BuildConfig.AGENTIC_AI_BASE_URL)
    //    private val  llmClient= LLMClient(
//        BuildConfig.GROQ_API_KEY, "7", "8", "250",
//        "meta-llama/llama-4-scout-17b-16e-instruct"
//    )
    private val llmClient= GeminiLLMClient(
        BuildConfig.GEMINI_API_KEY,
        "7","8", "250","gemma-3-27b-it"
    )
    // Consolidated UI State for chat screen
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Internal state
    private var userId: String = ""
    private var clickedAutosuggestion = false
    private var isInitialized = false

    // Jobs
    private var typingJob: Job? = null
    private var idleJob: Job? = null
    private var userActivityJob: Job? = null
    private var conceptMapJob: Job? = null
    private var resourceCardTimerJob: Job? = null

    // Session mapping
    private val conceptThreadMap = mutableMapOf<String, String>()
    private val conceptSessionMap = mutableMapOf<String, String>()

    /**
     * initialize function
     * - refreshes available concepts
     * - sets userId if not already set
     */
    fun initialize(id: String) {
        if (isInitialized) {
            DebugLogger.debugLog("ChatViewModel", "Already initialized, skipping re-initialization")
            return
        }

        viewModelScope.launch {
            DebugLogger.debugLog("ChatViewModel", "Starting full initialization")
            refreshAvailableConcepts()
            if (userId.isEmpty()) {
                userId = id
            }
            isInitialized = true
            DebugLogger.debugLog("ChatViewModel", "Initialization complete")
        }
    }

    /**
     * updates the input text state
     * - marks user as active if text is not empty
     * - hides auto-suggestions when user types
     */

    fun updateInputText(text: String) {
        _uiState.update {
            it.copy(inputText = text, isUserActive = text.isNotEmpty())
        }
        if (text.isNotEmpty()) {
            markUserActive()
            hideAutosuggestions()
        }
    }

    /**
     * sets the student level in the UI state
     */

    fun setStudentLevel(level: String) {
        _uiState.update { it.copy(studentLevel = level) }
        DebugLogger.debugLog("ChatViewModel", "Student level changed to: $level")
    }

    /**
     * checks and sets Kannada language
     */
    fun setKannada(enabled: Boolean) {
        _uiState.update {
            it.copy(
                isKannada = enabled,
                currentLanguage = if (enabled) "kn" else "en"
            )
        }
    }

    /**
     * starts the idle timer for showing auto-suggestions
     * - shows auto-suggestions after 5 seconds of inactivity if conditions are met
     */

    fun startIdleTimer() {
        idleJob?.cancel()
        idleJob = viewModelScope.launch {
            DebugLogger.debugLog("ChatViewModel", " Starting idle timer (5s countdown)")
            delay(5000L)// 5 seconds delay
            val state = _uiState.value


            DebugLogger.debugLog("ChatViewModel", """
                =======================================================
                AUTO-SUGGESTIONS CHECK (after 5s idle timer)
                =======================================================
                autosuggestions.isNotEmpty(): ${state.autosuggestions.isNotEmpty()} (${state.autosuggestions.size} suggestions)
                !isUserActive: ${!state.isUserActive}
                inputText.isEmpty(): ${state.inputText.isEmpty()}
                !isLoading: ${!state.isLoading}
                !isTyping: ${!state.isTyping}
                !waitingForTTSToComplete: ${!state.waitingForTTSToComplete}
                -------------------------------------------------------
                Suggestions: ${state.autosuggestions}
                showAutosuggestions: ${state.showAutosuggestions}
                =======================================================
            """.trimIndent())

            // Only show auto-suggestions if user is truly idle
            if (state.autosuggestions.isNotEmpty() &&
                !state.isUserActive &&
                state.inputText.isEmpty() &&
                !state.isLoading &&
                !state.isTyping &&
                !state.waitingForTTSToComplete) {
                _uiState.update { it.copy(showAutosuggestions = true) }
                DebugLogger.debugLog("ChatViewModel", " Auto-suggestions SHOWN!")
            } else {
                DebugLogger.debugLog("ChatViewModel", " Auto-suggestions NOT shown (condition failed)")
            }
        }
    }

    /**
     * hides auto-suggestions and cancels idle timer
     */
    fun hideAutosuggestions() {
        _uiState.update { it.copy(showAutosuggestions = false) }
        idleJob?.cancel()
    }

    /**
     * marks user as active and resets inactivity timer
     * - hides auto-suggestions when user is active
     * - sets user as inactive after 2 seconds of no activity
     * - cancels any existing user activity job
     */
    fun markUserActive() {
        _uiState.update { it.copy(isUserActive = true) }
        hideAutosuggestions()
        userActivityJob?.cancel()
        userActivityJob = viewModelScope.launch {
            delay(2000L)
            _uiState.update { it.copy(isUserActive = false) }
        }
    }

    /**
     * marks user as inactive after 500ms delay
     * - cancels any existing user activity job
     * - used when speech recognition stops
     */
    fun markUserInactive() {
        userActivityJob?.cancel()
        viewModelScope.launch {
            delay(500L)
            _uiState.update { it.copy(isUserActive = false) }
        }
    }

    /**
     * handles tap on an auto-suggestion
     * - sends the suggestion as a message
     * - sets clickedAutosuggestion flag during the sending process
     */
    fun tapAutosuggestion(suggestion: String, context: Context) {
        viewModelScope.launch {
            clickedAutosuggestion = true
            sendMessage(suggestion, context)
            delay(100)
            clickedAutosuggestion = false
        }
    }

    /**
     * -refreshes the list of available concepts from the backend
     * - updates the UI state with the new concepts
     */

    fun refreshAvailableConcepts() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                // Fetch concepts from backend
                val result = agenticAIClient.getConceptsList()

                if (result.isSuccess) {
                    val concepts = result.getOrNull()?.concepts ?: emptyList()
                    _uiState.update { it.copy(availableConcepts = concepts) }
                    DebugLogger.debugLog("ChatViewModel", "Loaded ${concepts.size} concepts")
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "refreshAvailableConcepts: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * selects a concept and "starts" or "resumes" the corresponding session
     * - clears existing messages and state
     * - loads existing session if available, otherwise starts a new session
     */
    fun selectConcept(concept: String, context: Context) {
        viewModelScope.launch {
            hideAutosuggestions()//hide autosuggestions on concept change

            // Reset UI state for new concept
            _uiState.update {
                it.copy(
                    isLoading = true,
                    messages = emptyList(),
                    autosuggestions = emptyList(),
                    typingText = "",
                    isTyping = false,
                    selectedConcept = concept,
                    shouldStartTTS = false,
                    fullTextForTTS = ""
                )
            }
            //cancel animations
            cancelAnimations()

            try {
                // get existing session mapping
                val stored = loadThreadMapping(context, concept)
                if (stored != null) {
                    resumeExistingSession( stored.first, stored.second)//resume existing session
                } else {
                    sessionStart(context, concept)//start new session
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "selectConcept error: ${e.message}")
                _uiState.update { it.copy(isLoading = false)
                }
            }
        }
    }

    /**
     * checks if there is an existing session for the given concept
     * - first checks in-memory mapping
     * - then checks persistent storage
     */
    fun hasExistingSession(concept: String, context: Context): Boolean {
        if (conceptThreadMap[concept] != null) return true
        val repository = ConceptSessionRepository(context.applicationContext)
        return repository.loadMapping(concept) != null
    }


    /**
     * Shows the concept map in the UI
     * - Updates the UI state with the concept map JSON
     * - Logs performance metrics
     * @param jsonString The concept map JSON string
     * @param generationTimeMs Time taken to generate the concept map JSON
     */
    private fun showConceptMap(jsonString: String, generationTimeMs: Long) {

        //timer duration for concept map display in seconds
        val durationSeconds = 10

        // Check if this is the default/error concept map
        if (llmClient.isDefaultConceptMap(jsonString)) {
            DebugLogger.debugLog("ChatViewModel", "Default concept map detected - not showing resource card")

            val pendingMsg = _uiState.value.pendingAgentResponse

            _uiState.update {
                it.copy(
                    conceptMapStatus = "default - error in generation",
                    pendingAgentResponse = null,
                    resourceCardState = ResourceCardUiState.Hidden,
                    loadingResourceMessage = null,
                    isLoading = false
                )
            }

            // Process pending message since we're not showing the resource card
            pendingMsg?.let { msg ->
                DebugLogger.debugLog("ChatViewModel", "Processing pending message since concept map is default")
                startTypingAnimation(msg)
            }
            return
        }

        val renderStartTime = System.currentTimeMillis()

        // Show the concept map with timer
        startResourceCardTimer(durationSeconds) { remaining ->
            _uiState.update {
                it.copy(
                    resourceCardState = ResourceCardUiState.ConceptMap(
                        json = jsonString,
                        audioProgress = 0f,
                        isAudioPlaying = false,  // Load full map at once (no progressive rendering)
                        remainingSeconds = remaining,
                        totalSeconds = durationSeconds
                    ),
                    loadingResourceMessage = null
                )
            }
        }

        val renderLatency = System.currentTimeMillis() - renderStartTime
        DebugLogger.debugLog("ChatViewModel", """
            ═══════════════════════════════════════════════════════
            CONCEPT MAP PERFORMANCE METRICS
            ═══════════════════════════════════════════════════════
            Generation Time: ${generationTimeMs}ms
            Render Latency: ${renderLatency}ms
            Total Time: ${generationTimeMs + renderLatency}ms
            ═══════════════════════════════════════════════════════
        """.trimIndent())
    }

    /**
     * fetches concept map JSON using LLM based on the AI response
     * - cancels any existing concept map job
     * - queries the LLM client for concept map generation
     * - extracts and shows the concept map in the UI

     */
    private fun fetchConceptMapWithLLM(aiResponse: String) {
        conceptMapJob?.cancel()
        conceptMapJob = viewModelScope.launch {
            try {
                val generationStartTime = System.currentTimeMillis()
                DebugLogger.debugLog("ChatViewModel", "Starting concept map generation...")

                val response = llmClient.queryLLM(aiResponse, _uiState.value.currentLanguage)
                DebugLogger.debugLog("ChatViewModel", "LLM response received ")
                val json = llmClient.extractConceptMapJSON(response)

                // return if the coroutine is not active (cancelled)
                if (!isActive) return@launch

                val generationTimeMs = System.currentTimeMillis() - generationStartTime
                DebugLogger.debugLog("ChatViewModel", "Concept map JSON extracted in ${generationTimeMs}ms")

                // Show the concept map
                showConceptMap(json, generationTimeMs)
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "Concept map error: ${e.message}")
            }
        }
    }

    //remove all session when logout
    fun clearAllSessions(context: Context) {
        viewModelScope.launch {
            try {
                conceptThreadMap.clear()
                conceptSessionMap.clear()
                withContext(Dispatchers.IO) {
                    ConceptSessionRepository(context.applicationContext).clearAllMappings()
                }
                agenticAIClient.setCurrentThreadAndSession(null, null)

                _uiState.update {
                    it.copy(
                        isSessionStarted = false,
                        messages = emptyList(),
                        selectedConcept = null,
                        shouldStartTTS = false,
                        fullTextForTTS = "",
                        autosuggestions = emptyList()
                    )
                }

                DebugLogger.debugLog("ChatViewModel", "All sessions cleared")
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "clearAllSessions failed: ${e.message}")
            }
        }
    }

    /**
     * saves the thread and session mapping for a concept
     * - updates in-memory maps
     * - persists the mapping asynchronously
     */
    private fun saveThreadMapping(context: Context, concept: String, threadId: String?, sessionId: String?) {
        if (threadId.isNullOrBlank()) return
        conceptThreadMap[concept] = threadId
        sessionId?.let { conceptSessionMap[concept] = it }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Save mapping to persistent storage
                ConceptSessionRepository(context.applicationContext).saveMapping(concept, threadId, sessionId)
                DebugLogger.debugLog("ChatViewModel", "Saved mapping for concept: $concept (thread: $threadId, session: $sessionId)")
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "saveThreadMapping: ${e.message}")
            }
        }
    }

    /**
     * loads the thread and session mapping for a concept
     * - first checks in-memory maps
     * - then checks persistent storage if not found in memory
     */

    private suspend fun loadThreadMapping(context: Context, concept: String): Pair<String, String?>? {
        conceptThreadMap[concept]?.let { threadId ->
            return Pair(threadId, conceptSessionMap[concept])
        }

        return withContext(Dispatchers.IO) {
            try {
                ConceptSessionRepository(context.applicationContext)
                    .loadMapping(concept)?.also { (thread, session) ->
                        conceptThreadMap[concept] = thread
                        session?.let { conceptSessionMap[concept] = it }
                    }
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "loadThreadMapping: ${e.message}")
                null
            }
        }
    }

    /**
     * starts a new session for the given concept
     * - calls the backend to create a new session
     * - saves the thread and session mapping
     * - updates the UI state with initial data
     */

    fun sessionStart(context: Context, concept: String) {
        viewModelScope.launch {
            try {
                //fetch  start new session response from backend
                val result = agenticAIClient.startSession(
                    conceptTitle = concept,
                    studentId = userId,
                    isKannada = _uiState.value.isKannada,
                    studentLevel = _uiState.value.studentLevel
                )

                //if success, save mapping and update UI state
                if (result.isSuccess) {
                    val response = result.getOrNull() ?: return@launch
                    if (!response.success) return@launch

                    //save thread and session mapping
                    saveThreadMapping(context, concept, response.threadId, response.sessionId)
                    agenticAIClient.setCurrentThreadAndSession(response.threadId, response.sessionId)

                    // Cancel any pending idle timer
                    idleJob?.cancel()

                    _uiState.update {
                        it.copy(
                            isSessionStarted = true,
                            autosuggestions = response.autosuggestions,
                            agentMetadata = response.metadata,
                            showAutosuggestions = false,
                            isUserActive = false  // Ensure user is not marked as active after session start
                        )
                    }

                    // Handle agent response - resource card will be shown AFTER typing completes
                    response.agentResponse?.takeIf { it.isNotBlank() }?.let { text ->
                        handleAgentResponse(text)
                    }

                    DebugLogger.debugLog("ChatViewModel", "Session started for: $concept with ${response.autosuggestions.size} autosuggestions")
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "sessionStart error: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * resumes an existing session for the given thread and session IDs
     * - loads session history from backend to show past message at the start
     * - updates the UI state with loaded messages
     */
    private suspend fun resumeExistingSession( threadId: String, sessionId: String?) {
        DebugLogger.debugLog("ChatViewModel", "Resuming session - thread=$threadId")

        agenticAIClient.setCurrentThreadAndSession(threadId, sessionId)
        _uiState.update { it.copy(isSessionStarted = true, messages = emptyList()) }

        try {
            //history result from backend
            val histResult = agenticAIClient.getSessionHistory(threadId)

            if (histResult.isSuccess) {
                val messages = histResult.getOrNull()?.messages ?: emptyList()
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

                _uiState.update { it.copy(messages = chatMessages) }
                DebugLogger.debugLog("ChatViewModel", "Loaded ${chatMessages.size} messages")
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("ChatViewModel", "resumeExistingSession: ${e.message}")
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * starts a fresh session for the given concept
     * - clears existing thread and session mapping
     * - deletes persistent mapping
     * - resets UI state while preserving important fields
     * - selects the concept to start a new session
     */
    fun startFreshSession(concept: String, context: Context) {
        viewModelScope.launch {
            try {
                conceptThreadMap.remove(concept)
                conceptSessionMap.remove(concept)
                withContext(Dispatchers.IO) {
                    ConceptSessionRepository(context.applicationContext).deleteMapping(concept)
                }

                agenticAIClient.setCurrentThreadAndSession(null, null)
                cancelAnimations()

                // Preserve availableConcepts and other important state when resetting
                val currentConcepts = _uiState.value.availableConcepts
                val currentLanguage = _uiState.value.currentLanguage
                val studentLevel = _uiState.value.studentLevel
                val isKannada = _uiState.value.isKannada

                _uiState.update {
                    ChatUiState(
                        selectedConcept = concept,
                        availableConcepts = currentConcepts,
                        currentLanguage = currentLanguage,
                        studentLevel = studentLevel,
                        isKannada = isKannada
                    )
                }

                selectConcept(concept, context)
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "startFreshSession: ${e.message}")
                addErrorMessage(context)
            }
        }
    }

    // ===== Messaging =====

    /**
     * sends a user message to the AI agent
     * - adds the user message to the UI state
     * - if session not started, queues the message until session is ready
     * - otherwise, continues the session with the user message
     */
    fun sendMessage(userMessage: String, context: Context) {
        if (userMessage.isBlank()) return

        hideAutosuggestions()
        markUserActive()

        _uiState.update {
            it.copy(messages = it.messages + ChatMessageModel(content = userMessage, sender = "user"))
        }

        if (!_uiState.value.isSessionStarted) {
            // Queue message until session is ready
            viewModelScope.launch {
                delay(100)
                sendMessageAfterSessionReady(userMessage, context)
            }
            return
        }
        // Continue session immediately
        sendMessageAfterSessionReady(userMessage, context)
    }

    /**
     * continues the session with the given user message
     * - shows loading state during the process
     * - updates UI state with agent response and metadata
     */
    private fun sendMessageAfterSessionReady(userMessage: String, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                hideAutosuggestions()

                val response = withTimeout(120_000L) {
                    agenticAIClient.continueSession(
                        userMessage = userMessage,
                        clickedAutosuggestion = clickedAutosuggestion,
                        studentLevel = _uiState.value.studentLevel
                    )
                }

                if (response.isSuccess) {
                    val resp = response.getOrNull() ?: return@launch

                    // Cancel any pending idle timer to prevent showing old auto-suggestions
                    idleJob?.cancel()

                    _uiState.update {
                        it.copy(
                            autosuggestions = resp.autosuggestions,
                            agentMetadata = resp.metadata,
                            showAutosuggestions = false,
                            conceptMapStatus = null  // Reset concept map status
                        )
                    }
                    DebugLogger.debugLog("ChatViewModel","Node transitions: ${resp.metadata.nodeTransitions}")
                    DebugLogger.debugLog("ChatViewModel","Image URL: ${resp.metadata.imageUrl}")
                    // Check and show resource card FIRST (before typing animation)
                    val shouldShowResource = checkAndShowResourceCard(resp.metadata)

                    // Then handle agent response (typing animation)
                    resp.agentResponse?.let { text ->
                        if (shouldShowResource) {
                            // Queue the message to show after resource card is dismissed
                            _uiState.update { it.copy(pendingAgentResponse = text) }
                        } else {
                            // No resource card, show typing immediately and end loading
                            _uiState.update { it.copy(isLoading = false) }
                            handleAgentResponse(text)
                        }
                    }
                } else {
                    addErrorMessage(context)
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "sendMessage error: ${e.message}")
                addErrorMessage(context)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * handles the AI agent's response
     * - starts the typing animation immediately
     */
    private fun handleAgentResponse(text: String) {
        startTypingAnimation(text)
    }

    /// adds a standardized error message to the chat if any unexpected error occurs
    private fun addErrorMessage(context: Context) {
        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessageModel(
                    content = context.getString(R.string.sorry_i_couldn_t_process_that_please_try_again),
                    sender = "ai",
                    isError = true,
                    canRetry = true
                )
            )
        }
    }

    /**
     * starts the typing animation for the AI agent's message
     * - adds the full message immediately to the chat
     * - animates the typing effect word by word
     * - triggers TTS start after a brief delay
     */
    private fun startTypingAnimation(fullText: String) {
        typingJob?.cancel()
        idleJob?.cancel()  // Cancel any pending idle timer

        typingJob = viewModelScope.launch {
            // Add full message immediately
            _uiState.update {
                it.copy(
                    messages = it.messages + ChatMessageModel(sender = "ai", content = fullText),
                    isTyping = true,
                    typingText = "",
                    fullTextForTTS = fullText,
                    isTypingComplete = false,
                    showAutosuggestions = false  // Hide auto-suggestions during typing
                )
            }

            delay(50)
            _uiState.update { it.copy(shouldStartTTS = true) }
            delay(50)
            _uiState.update { it.copy(shouldStartTTS = false) }

            // Animate typing
            val words = fullText.split(" ")
            words.forEachIndexed { index, word ->
                _uiState.update {
                    it.copy(typingText = it.typingText + if (index == 0) word else " $word")
                }
                delay(120L + (word.length * 8L).coerceAtMost(200L))
            }

            _uiState.update { it.copy(isTyping = false, typingText = "", isTypingComplete = true) }
        }
    }

    // ===== Resource Management =====

    /**
     * dismisses the currently shown resource card and processes any pending messages
     */
    fun dismissResourceCard() {
        resourceCardTimerJob?.cancel()
        resourceCardTimerJob = null

        val pendingMsg = _uiState.value.pendingAgentResponse

        _uiState.update {
            it.copy(
                resourceCardState = ResourceCardUiState.Hidden,
                ttsPausedForResource = false,
                pendingAgentResponse = null,
                loadingResourceMessage = null,
                isLoading = false
            )
        }

        // Process pending message if exists
        pendingMsg?.let { msg ->
            DebugLogger.debugLog("ChatViewModel", "Processing pending agent response after resource card dismissed")
            startTypingAnimation(msg)
        }
        DebugLogger.debugLog("ChatViewModel", "Resource card dismissed")
    }

    /**
     * Resumes TTS when resource card is dismissed
     */
    fun resumeTTSForResource() {
        _uiState.update { it.copy(ttsPausedForResource = false) }
    }

    /**
     * Shows an image resource card with auto-dismiss timer
     */
    private fun showImageResource(
        imageUrl: String,
        description: String?,
        durationSeconds: Int = 150
    ) {
        // Process image URL (convert GitHub blob URLs to raw URLs)
        val imageUrl = processImageUrl(imageUrl)

        startResourceCardTimer(durationSeconds) { remaining ->
            _uiState.update {
                it.copy(
                    resourceCardState = ResourceCardUiState.Image(
                        imageUrl = imageUrl,
                        description = description,
                        remainingSeconds = remaining,
                        totalSeconds = durationSeconds
                    ),
                    loadingResourceMessage = null
                )
            }
        }
    }

    /**
     * Converts GitHub blob URLs to raw URLs
     */
    private fun processImageUrl(url: String): String {
        return when {
            url.contains("github.com") && url.contains("/blob/") -> {
                url.replace("github.com", "raw.githubusercontent.com")
                    .replace("/blob/", "/")
            }
            else -> url
        }
    }

    /* ---------------- INTERNAL TIMER ---------------- */

    /**
     * Starts a countdown timer for the resource card
     */
    private fun startResourceCardTimer(
        durationSeconds: Int,
        onTick: (remaining: Int) -> Unit
    ) {
        resourceCardTimerJob?.cancel()

        resourceCardTimerJob = viewModelScope.launch {
            for (remaining in durationSeconds downTo 0) {
                onTick(remaining)
                if (remaining > 0) {
                    delay(1000)
                }
            }
            // Auto-dismiss when timer reaches 0
            dismissResourceCard()
        }
    }

    /**
     * - checks session metadata for resource triggers
     * - shows image or concept map cards based on node transitions
     * - returns true if a resource card was shown, false otherwise
     *
     * for image condition : APK -> CI with valid image URL
     * for concept map condition : CI -> SIM_CC
     */
    private fun checkAndShowResourceCard(metadata: SessionMetadata): Boolean {
        try {
            // Check if we have any transitions
            if (metadata.nodeTransitions.isEmpty()) {
                DebugLogger.debugLog("ChatViewModel", "No transitions")
                return false
            }

            // Get current (last) transition
            val currentTransition = metadata.nodeTransitions.lastOrNull() ?: return false

            val fromNode = currentTransition["from_node"] as? String
            val toNode = currentTransition["to_node"] as? String

            DebugLogger.debugLog("ChatViewModel", "Current transition: $fromNode → $toNode")
            DebugLogger.debugLog("ChatViewModel", "imageUrl: ${metadata.imageUrl}")

            // Check conditions and show resource if valid
            when {
                fromNode == "APK" && toNode == "CI" && !metadata.imageUrl.isNullOrBlank() -> {
                    DebugLogger.debugLog("ChatViewModel", "Image is showing")

                    // Set loading message first
                    _uiState.update {
                        it.copy(loadingResourceMessage = "Loading image...")
                    }

                    showImageResource(
                        imageUrl = metadata.imageUrl,
                        description = metadata.imageDescription
                    )
                    return true
                }

                fromNode == "CI" && toNode == "SIM_CC" -> {
                    DebugLogger.debugLog("ChatViewModel", " generating concept map")

                    // Set loading message
                    _uiState.update {
                        it.copy(loadingResourceMessage = "Loading concept map...")
                    }

                    val lastAiMsg = _uiState.value.lastAiMessage?.content
                    if (!lastAiMsg.isNullOrBlank()) {
                        fetchConceptMapWithLLM(lastAiMsg)
                        return true
                    } else {
                        DebugLogger.debugLog("ChatViewModel", " No Agent message for concept map")
                        _uiState.update { it.copy(loadingResourceMessage = null) }
                        return false
                    }
                }
                // No matching condition - don't show
                else -> {
                    DebugLogger.debugLog("ChatViewModel", " No resource Card for $fromNode → $toNode")
                    return false
                }
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("ChatViewModel", "checkAndShowResourceCard error: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    //set the typing state to false and clear typing text
    private fun cancelAnimations() {
        typingJob?.cancel()
        _uiState.update { it.copy(isTyping = false, typingText = "") }
    }

}
