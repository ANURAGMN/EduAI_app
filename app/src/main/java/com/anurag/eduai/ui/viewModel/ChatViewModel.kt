package com.anurag.eduai.ui.viewModel

import ChatMessageModel
import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.BuildConfig
import com.anurag.eduai.data.local.ConceptSessionRepository
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.remote.AgenticAIClient
import com.anurag.eduai.debug.DebugLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.set


/**
 * ViewModel for managing chat interactions with an AI agent.
 */
class ChatViewModel (
    application: Application
): ViewModel() {


    private val agenticAIClient = AgenticAIClient(BuildConfig.AGENTIC_AI_BASE_URL)

    private val sharedPreferenceUtils = SharedPreferenceUtils(application)
    val userId = sharedPreferenceUtils.getUserId().toString()
    //student level
    private val _studentLevel = MutableStateFlow("medium")
    val studentLevel: StateFlow<String> = _studentLevel
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Typing animation state
    private val _typingText = MutableStateFlow("")
    val typingText: StateFlow<String> = _typingText

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

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
    val lastUserMessage: StateFlow<String> = _lastUserMessage

    // Chat messages
    private val _messages = MutableStateFlow<List<ChatMessageModel>>(emptyList())
    val messages: StateFlow<List<ChatMessageModel>> = _messages


    //Job for typing animation
    private var typingJob: Job? = null


    // Available concepts
    private val _availableConcepts = MutableStateFlow<List<String>>(emptyList())
    val availableConcepts: StateFlow<List<String>> = _availableConcepts


    /**
     * Set the student level
     */
    fun setStudentLevel(level: String) {
        _studentLevel.value = level
        DebugLogger.debugLog("ChatViewModel", "Student level changed to: $level")
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

        // Add user message
        _messages.update { it + ChatMessageModel(content = userMessage, sender = "user") }

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
            //reset autosuggestions when selecting new concept
            _autosuggestions.value = emptyList()
            try {
                val stored = loadThreadMapping(context, _selectedConcept.toString())
                if (stored != null) {
                    resumeExistingSession(context, stored.first,stored.second)
                } else {
                    sessionStart(context,concept)
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("ChatViewModel", "selectConcept error: ${e.message}")
            }
        }
    }

    fun sessionStart(context: Context, concept: String) {
        viewModelScope.launch {

            try {
                val result =
                    agenticAIClient.startSession(
                        conceptTitle = concept,
                        studentId = userId,
                        isKannada = false,
                        studentLevel = _studentLevel.value
                    )
                if (result.isSuccess) {
                    val agentResponse = result.getOrNull()
                    if (agentResponse != null && agentResponse.success) {
                        saveThreadMapping(
                            context, concept, agentResponse.threadId, agentResponse.sessionId
                        )
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    /**
     * Continue an existing session with a user message
     */
    fun SessionContinue(context: Context, userMessage: String) {
        viewModelScope.launch{
            try{
                val response =
                    agenticAIClient.continueSession(
                        userMessage = userMessage,
                        clickedAutosuggestion = false,
                        studentLevel = _studentLevel.value
                    )
                if(response.isSuccess){
                    val agentResponse = response.getOrNull()?.agentResponse ?: "No response"
                    addAgentMessage(agentResponse)
                }
            } catch (e: Exception){

            }
        }
    }


    /**
     * Resume an existing session given thread and session IDs
     * - Sets current thread and session
     * - Marks session as started
     * - Fetches session history and appends last assistant message with typing animation
     * - Sends any pending user message that was queued before session was ready
     */
    private suspend fun resumeExistingSession(
        context: Context,
        threadId: String,
        sessionId: String?
    ) {
        DebugLogger.debugLog("ChatViewModel", "Resuming session - thread=$threadId")

        // Set current thread and session
        agenticAIClient.setCurrentThreadAndSession(threadId, sessionId)
        // Mark session as started
        _isSessionStarted.value = true

        try {
            // Fetch session history
            val histResult = agenticAIClient.getSessionHistory(threadId)

            // On success, extract last assistant message
            if (histResult.isSuccess) {
                val history = histResult.getOrNull()
                val messages = history?.messages ?: emptyList()
                // Append past messages to chat
                val lastAssistant = messages
                    .lastOrNull { msg ->
                        (msg["role"] as? String)?.lowercase() in listOf("assistant", "ai")
                    }
                    ?.get("content") as? String
                // Show last assistant message with typing animation
                if (!lastAssistant.isNullOrBlank()) {
                    _fullTextForTTS.value = lastAssistant

                    startTypingAnimation(lastAssistant, context)
                }
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("ChatViewModel", "Error resuming session: ${e.message}")
        } finally {
            _isLoading.value = false
        }

    }

    /**
     * Start typing animation for AI response
     * - Translates text if needed
     * - Updates original AI response
     * - Updates translated output and TTS text
     * - Animates typing word by word
     * - Finally appends full message to chat
     */
    private fun startTypingAnimation(fullText: String, context: Context) {

        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            _isTyping.value = true
            _typingText.value = ""

            DebugLogger.debugLog("ChatViewModel", "AI RAW OUTPUT (preview): ${fullText.take(1000)}")

            val words = fullText.split(" ")
            words.forEachIndexed { index, word ->
                _typingText.value += if (index == 0) word else " $word"
                delay(120L + (word.length * 8L).coerceAtMost(200L))
            }

            _messages.update {
                it+ChatMessageModel( "ai",fullText)
            }
            _isTyping.value = false
            _typingText.value = ""
        }
    }


    fun addAgentMessage(text: String) {
        val agentMessage = ChatMessageModel("ai",text)
        val updatedMessages = agentMessage
    }

    fun addUserMessage(text: String) {
        val userMessage = ChatMessageModel("user",text)
        val updatedMessages =  userMessage
    }

    fun hasExistingSession(concept: String, context: Context): Boolean {
        val stored = conceptThreadMap[concept]
        return stored != null
    }
}

