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
import com.anurag.eduai.data.remote.LLMClient
import com.anurag.eduai.data.remote.SessionMetadata
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.screens.chatbotscreen.components.ResourceContent
import com.anurag.eduai.ui.screens.chatbotscreen.components.ResourceDisplayMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.set


/**
 * ViewModel for managing chat interactions with an AI agent.
 */
class ChatViewModel (): ViewModel() {

    private val agenticAIClient = AgenticAIClient(BuildConfig.AGENTIC_AI_BASE_URL)
    private val model="meta-llama/llama-4-scout-17b-16e-instruct"

    private val llmClient = LLMClient(BuildConfig.GROQ_API_KEY, "6", "8", "250", model)

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
    
    //Kannada language toggle
    private val _isKannada = MutableStateFlow(false)
    val isKannada: StateFlow<Boolean> = _isKannada
    
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage
    // Selected concept state
    private val _selectedConcept = MutableStateFlow<String?>(null)
    val selectedConcept: StateFlow<String?> = _selectedConcept

    // Autosuggestions state
    private val _autosuggestions = MutableStateFlow<List<String>>(emptyList())
    val autosuggestions: StateFlow<List<String>> = _autosuggestions
    // State
    private val _showAutosuggestions = MutableStateFlow(false)
    val showAutosuggestions: StateFlow<Boolean> = _showAutosuggestions
    private var idleJob: Job? = null

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
    //resource card state
    private val _showResourceCard = MutableStateFlow(false)
    val showResourceCard: StateFlow<Boolean> = _showResourceCard

    private val _currentResource = MutableStateFlow<ResourceContent?>(null)
    val currentResource: StateFlow<ResourceContent?> = _currentResource

    private val _resourceDisplayMode = MutableStateFlow(ResourceDisplayMode.IMAGE)
    val resourceDisplayMode: StateFlow<ResourceDisplayMode> = _resourceDisplayMode

    // Pending message to show after resource card is dismissed
    private var pendingAgentMessage: String? = null
    private var pendingContext: Context? = null

    // Track if TTS was paused due to resource card
    private val _ttsPausedForResource = MutableStateFlow(false)
    val ttsPausedForResource: StateFlow<Boolean> = _ttsPausedForResource

    //concept map json state
    private val _conceptMapJSON = MutableStateFlow(
        """{"visualization_type":"None","main_concept":"Chat for a Concept Map","nodes":[],"edges":[]}"""
    )
    val conceptMapJSON: StateFlow<String> = _conceptMapJSON
    private var conceptMapJob: Job? = null

    private fun resetConceptMap() {
        _conceptMapJSON.value = """{"visualization_type":"None","main_concept":"Chat for a Concept Map","nodes":[],"edges":[]}"""
        DebugLogger.debugLog("ChatViewModel", "Concept map reset to default")
    }

    private fun fetchConceptMapWithLLM(
        aiResponse: String,
    ) {

        conceptMapJob?.cancel()

        conceptMapJob = viewModelScope.launch {
            try {

                val response = llmClient.queryLLM(aiResponse, _currentLanguage.value)

                val json =llmClient.extractConceptMapJSON(response)

                if (!isActive) {
                    DebugLogger.debugLog("ChatViewModel", "Concept map generation was cancelled")
                    resetConceptMap()
                    return@launch
                }
                // PROGRESSIVE RENDERING PHASE
                val progressiveRenderStartTime = System.currentTimeMillis()

                DebugLogger.debugLog(
                    "ChatViewModel",
                    "Full concept map JSON from LLM: $json"
                )

                // Show full concept map immediately (no progressive rendering)
                _conceptMapJSON.value = json
                _resourceDisplayMode.value = ResourceDisplayMode.CONCEPT_MAP
                _currentResource.value = ResourceContent.ConceptMap(
                    json = json,
                    description = null,
                    currentAudioTime = 0f,
                    isAudioPlaying = false
                )
                _showResourceCard.value = true

                DebugLogger.debugLog(
                    "ChatViewModel",
                    "Resource card shown with full concept map (all nodes/edges at once)"
                )

            } catch (e: Exception) {
                DebugLogger.debugLog("ChatViewModel", "Concept map generation error: ${e.message}")
                    resetConceptMap()
            }
        }
    }


    private fun startProgressiveConceptMap(
        conceptMapJson: String,
    ) {
        conceptMapJob?.cancel()

        conceptMapJob = viewModelScope.launch(Dispatchers.Main) {
            try {
                val progressiveRenderStartTime = System.currentTimeMillis()

                DebugLogger.debugLog("ChatViewModel", "=== PROGRESSIVE RENDERING STARTED ===")
                DebugLogger.debugLog("ChatViewModel", "Coroutine isActive: $isActive")

                if (!isActive) {
                    DebugLogger.debugLog("ChatViewModel", "Concept map rendering was cancelled before start")
                    return@launch
                }

                val jsonObj = JSONObject(conceptMapJson)
                val nodesArray = jsonObj.optJSONArray("nodes") ?: JSONArray()
                val edgesArray = jsonObj.optJSONArray("edges") ?: JSONArray()

                DebugLogger.debugLog("ChatViewModel", "├─ Nodes to render: ${nodesArray.length()}")
                DebugLogger.debugLog("ChatViewModel", "├─ Edges to render: ${edgesArray.length()}")
                DebugLogger.debugLog("ChatViewModel", "├─ Full JSON: $conceptMapJson")

                if (nodesArray.length() == 0 && edgesArray.length() == 0) {
                    _conceptMapJSON.value = conceptMapJson
                    DebugLogger.debugLog("ChatViewModel", "├─ Empty concept map - showing as-is")
                    return@launch
                }

                val progressiveNodes = JSONArray()
                val progressiveEdges = JSONArray()
                val audioSegments = jsonObj.optJSONArray("audioSegments") ?: JSONArray()

                DebugLogger.debugLog("ChatViewModel", "├─ Starting progressive node rendering...")

                // Add nodes one by one with delay for visible progressive rendering
                var nodeCount = 0
                for (i in 0 until nodesArray.length()) {
                    if (!isActive) {
                        DebugLogger.debugLog("ChatViewModel", "Concept map rendering cancelled at node $nodeCount")
                        return@launch
                    }
                    try {
                        progressiveNodes.put(nodesArray.getJSONObject(i))
                        updateConceptMapState(jsonObj, progressiveNodes, JSONArray(), audioSegments)

                        nodeCount++
                        DebugLogger.debugLog(
                            "ChatViewModel",
                            "├─ Node $nodeCount/${nodesArray.length()} added (${nodesArray.getJSONObject(i).optString("label", "?")})"
                        )

                        // Add delay after each node (except the last one)
                        if (i < nodesArray.length() - 1) {
                            delay(300L)  // 300ms delay between nodes for visible animation
                        }
                    } catch (e: Exception) {
                        DebugLogger.errorLog("ChatViewModel", "Error adding node $nodeCount: ${e.message}")
                        e.printStackTrace()
                        return@launch
                    }
                }

                DebugLogger.debugLog("ChatViewModel", "├─ All nodes added, starting edge rendering...")

                // Add edges one by one with delay
                var edgeCount = 0
                for (i in 0 until edgesArray.length()) {
                    if (!isActive) {
                        DebugLogger.debugLog("ChatViewModel", "Concept map rendering cancelled at edge $edgeCount")
                        return@launch
                    }
                    try {
                        progressiveEdges.put(edgesArray.getJSONObject(i))
                        updateConceptMapState(jsonObj, progressiveNodes, progressiveEdges, audioSegments)

                        edgeCount++
                        val edgeObj = edgesArray.getJSONObject(i)
                        DebugLogger.debugLog(
                            "ChatViewModel",
                            "├─ Edge $edgeCount/${edgesArray.length()} added (${edgeObj.optString("from", "?")} -> ${edgeObj.optString("to", "?")})"
                        )

                        // Add delay after each edge (except the last one)
                        if (i < edgesArray.length() - 1) {
                            delay(200L)  // 200ms delay between edges for visible animation
                        }
                    } catch (e: Exception) {
                        DebugLogger.errorLog("ChatViewModel", "Error adding edge $edgeCount: ${e.message}")
                        e.printStackTrace()
                        return@launch
                    }
                }

                val progressiveRenderEndTime = System.currentTimeMillis()
                val progressiveRenderDuration = progressiveRenderEndTime - progressiveRenderStartTime

                DebugLogger.debugLog("ChatViewModel", "=== Progressive Rendering Completed in ${progressiveRenderDuration}ms ===")
                DebugLogger.debugLog("ChatViewModel", "Total nodes rendered: $nodeCount")
                DebugLogger.debugLog("ChatViewModel", "Total edges rendered: $edgeCount")


            } catch (e: Exception) {
                DebugLogger.errorLog(
                    "ChatViewModel",
                    "Concept map animation error: ${e.message}"
                )
                _conceptMapJSON.value = conceptMapJson
            }
        }
    }
    private fun updateConceptMapState(
        jsonObj: JSONObject,
        nodes: JSONArray,
        edges: JSONArray,
        audioSegments: JSONArray
    ) {
        val progressMap = JSONObject().apply {
            put("visualization_type", jsonObj.optString("visualization_type", "Concept Map"))
            put("main_concept", jsonObj.optString("main_concept", ""))
            put("nodes", nodes)
            put("edges", edges)
            put("audioSegments", audioSegments)
        }
        val jsonString = progressMap.toString()
        _conceptMapJSON.value = jsonString

        // Update the resource content with the progressive JSON
        _currentResource.value = ResourceContent.ConceptMap(
            json = jsonString,
            description = null,
            currentAudioTime = 0f,
            isAudioPlaying = false
        )

        DebugLogger.debugLog(
            "ChatViewModel",
            "updateConceptMapState: nodes=${nodes.length()}, edges=${edges.length()}"
        )
    }

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

    // Start 5s timer after TTS
    fun startIdleTimer() {
        idleJob?.cancel()
        idleJob = viewModelScope.launch {
            delay(5000L)
            if (_autosuggestions.value.isNotEmpty()) {
                _showAutosuggestions.value = true
            }
        }
    }

    // Hide on user action
    fun hideAutosuggestions() {
        _showAutosuggestions.value = false
        idleJob?.cancel()
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

    /**
     * Set Kannada language preference
     */
    fun setKannada(enabled: Boolean) {
        _isKannada.value = enabled
        DebugLogger.debugLog("ChatViewModel", "Kannada language: ${if (enabled) "enabled" else "disabled"}")
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

                _shouldStartTTS.value = false
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

    /**
     * Handle autosuggestion tap
     * Sends the suggestion as a message
     */
    fun tapAutosuggestion(suggestion: String, context: Context) {
        viewModelScope.launch {
            clickedAutosuggestion = true
            sendMessage(suggestion, context)
            // Reset after sending
            delay(100)
            clickedAutosuggestion = false
        }
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
                    resp.metadata.let {metadata ->
                        _agentMetadata.value = metadata
                        DebugLogger.debugLog(
                            "ChatViewModel",
                            "Node transitions count: ${metadata.nodeTransitions.size}"
                        )
                        metadata.nodeTransitions.forEachIndexed { index, transition ->
                            DebugLogger.debugLog(
                                "ChatViewModel",
                                "  [$index] ${transition["from_node"]} -> ${transition["to_node"]}"
                            )
                        }
                        checkAndShowResourceCard(metadata)
                    }

                    updateAutosuggestions(resp.autosuggestions)
                    DebugLogger.debugLog("ChatViewModel", "├─ Autosuggestions from continueSession: ${resp.autosuggestions.size}")
                    resp.autosuggestions.forEachIndexed { idx, suggestion ->
                        DebugLogger.debugLog("ChatViewModel", "  [$idx] $suggestion")
                    }

                    // If resource card is showing, store the message to be shown after card is dismissed
                    // Otherwise, start typing animation immediately
                    if (_showResourceCard.value) {
                        pendingAgentMessage = text
                        pendingContext = context
                        DebugLogger.debugLog("ChatViewModel", "Resource card is showing - pending agent message stored for later")
                    } else {
                        startTypingAnimation(text, context)
                    }


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

   fun dismissResourceCard() {
        _showResourceCard.value = false
        _currentResource.value = null
       _resourceDisplayMode.value = ResourceDisplayMode.IMAGE
        _ttsPausedForResource.value = false  // Reset TTS pause flag
        DebugLogger.debugLog("ChatViewModel", "Resource card dismissed")

        // If there's a pending agent message, show it now with typing animation
        if (pendingAgentMessage != null && pendingContext != null) {
            DebugLogger.debugLog("ChatViewModel", "Starting typing animation for pending message after resource card dismissal")
            startTypingAnimation(pendingAgentMessage!!, pendingContext!!)
            pendingAgentMessage = null
            pendingContext = null
        }
    }

    /**
     * Pause TTS when resource card is shown
     */
    fun pauseTTSForResource() {
        _ttsPausedForResource.value = true
        DebugLogger.debugLog("ChatViewModel", "TTS paused for resource card")
    }

    /**
     * Resume TTS (used when volume button is clicked while resource card is showing)
     */
    fun resumeTTSForResource() {
        _ttsPausedForResource.value = false
        DebugLogger.debugLog("ChatViewModel", "TTS resume requested for resource card")
    }

    /**
     * Handle resource card timer completion
     */
    fun onResourceTimerComplete() {
        DebugLogger.debugLog("ChatViewModel", "Resource card timer completed")
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
            //stop any ongoing TTS
            hideAutosuggestions()
            _shouldStartTTS.value = false
            _fullTextForTTS.value = ""

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
                    isKannada = _isKannada.value,
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

                        response.metadata.let { metadata ->
                            _agentMetadata.value = metadata

                            // Log initial transitions
                            DebugLogger.debugLog(
                                "ChatViewModel",
                                "Initial node transitions count: ${metadata.nodeTransitions.size}"
                            )
                            metadata.nodeTransitions.forEachIndexed { index, transition ->
                                DebugLogger.debugLog(
                                    "ChatViewModel",
                                    "  [$index] ${transition["from_node"]} -> ${transition["to_node"]}"
                                )
                            }

                            checkAndShowResourceCard(metadata)
                        }

                        // Show the initial agent response with typing animation if available
                        if (!text.isNullOrBlank()) {
                            // If resource card is showing, store the message to be shown after card is dismissed
                            // Otherwise, start typing animation immediately
                            if (_showResourceCard.value) {
                                pendingAgentMessage = text
                                pendingContext = context
                                DebugLogger.debugLog("ChatViewModel", "Resource card is showing - pending agent message stored for later")
                            } else {
                                startTypingAnimation(text, context)
                            }
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

        // CLEAR previous messages first
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
     * - TTS will play simultaneously with typing animation
     *
     */

    private fun startTypingAnimation(fullText: String, context: Context) {

//
//        viewModelScope.launch {
//            fetchConceptMapWithLLM(fullText)
//        }
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            // Add full message to chat immediately
            _messages.update {
                it + ChatMessageModel(sender = "ai", content = fullText)
            }
            updateUIState()

            // Start typing animation
            _isTyping.value = true
            _typingText.value = ""
            _fullTextForTTS.value = fullText

            DebugLogger.debugLog("ChatViewModel", "AI RAW OUTPUT (preview): ${fullText.take(100)}")

            // Small delay to ensure UI updates before TTS starts
            delay(50)
            // Now trigger TTS
            _shouldStartTTS.value = true
            delay(100)
            _shouldStartTTS.value = false

            // Animate typing word by word
            val words = fullText.split(" ")
            words.forEachIndexed { index, word ->
                _typingText.value += if (index == 0) word else " $word"
                delay(120L + (word.length * 8L).coerceAtMost(200L))
            }

            // Typing complete
            _isTyping.value = false
            _typingText.value = ""

            DebugLogger.debugLog("ChatViewModel", "Typing animation complete, TTS triggered")
        }
    }    /**
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

                _shouldStartTTS.value = false
                _fullTextForTTS.value = ""

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
        // Check in-memory cache first
        val cachedThread = conceptThreadMap[concept]
        if (cachedThread != null) {
            DebugLogger.debugLog("ChatViewModel", "Found existing session in cache for: $concept")
            return true
        }

        // Check SharedPreferences
        val repository = ConceptSessionRepository(context.applicationContext)
        val mapping = repository.loadMapping(concept)
        val exists = mapping != null

        if (exists) {
            DebugLogger.debugLog("ChatViewModel", "Found existing session in SharedPreferences for: $concept")
        } else {
            DebugLogger.debugLog("ChatViewModel", "No existing session found for: $concept")
        }

        return exists
    }

    private fun checkAndShowResourceCard(metadata: SessionMetadata) {
        viewModelScope.launch {
            try {
                DebugLogger.debugLog(
                    "ChatViewModel",
                    "=== CHECKING RESOURCE CARD TRANSITIONS ==="
                )
                DebugLogger.debugLog(
                    "ChatViewModel",
                    "Total transitions: ${metadata.nodeTransitions.size}"
                )
                metadata.nodeTransitions.forEach { transition ->
                    val fromNode = transition["from_node"] as? String
                    val toNode = transition["to_node"] as? String

                    DebugLogger.debugLog(
                        "ChatViewModel",
                        "[\$index] Checking transition: $fromNode -> $toNode"
                    )

                    // APK -> CI = Show Image
                    if (fromNode == "APK" && toNode == "CI") {
                        if (!metadata.imageUrl.isNullOrBlank()) {
                            _currentResource.value = ResourceContent.Image(
                                url = metadata.imageUrl,
                                description = metadata.imageDescription
                            )
                            _resourceDisplayMode.value = ResourceDisplayMode.IMAGE
                            _showResourceCard.value = true

                            DebugLogger.debugLog(
                                "ChatViewModel",
                                "current node transition from $fromNode to $toNode Showing IMAGE for APK->CI: ${metadata.imageUrl}"

                            )
                            return@launch
                        } else {
                            DebugLogger.debugLog(
                                "ChatViewModel",
                                " APK->CI found but imageUrl is null"
                            )
                        }
                    }

                    // CI -> SIM_CC = Fetch and show ConceptMap from LLM
                    if (fromNode == "CI" && toNode == "SIM_CC") {
                        val lastAIMessage = _messages.value.findLast { it.sender == "ai" }
                        val aiResponseText = lastAIMessage?.content ?: ""

                        if (aiResponseText.isNotBlank()) {
                            DebugLogger.debugLog(
                                "ChatViewModel",
                                "current node transition from $fromNode to $toNode  transition - triggering concept map for resource card"
                            )
                            fetchConceptMapWithLLM(aiResponseText)
                            return@launch
                        }else{
                            DebugLogger.debugLog(
                                "ChatViewModel",
                                "CI->SIM_CC found but last AI message is blank"
                            )
                        }
                    }
                }

                DebugLogger.debugLog(
                    "ChatViewModel",
                    "No matching transitions found for resource display"
                )

            } catch (e: Exception) {
                DebugLogger.errorLog(
                    "ChatViewModel",
                    "Error checking resource card: ${e.message}"
                )
            }
        }
    }

}
