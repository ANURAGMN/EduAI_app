package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.remote.SessionMetadata
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.domain.chatbot.controller.IdleTimerController
import com.anurag.eduai.domain.chatbot.controller.ResourceController
import com.anurag.eduai.domain.chatbot.controller.TypingAnimationController
import com.anurag.eduai.domain.chatbot.usecase.AutoSuggestionUseCase
import com.anurag.eduai.domain.chatbot.usecase.AvatarChangeUseCase
import com.anurag.eduai.domain.chatbot.usecase.ChatIntent
import com.anurag.eduai.domain.chatbot.usecase.ConceptMapUseCase
import com.anurag.eduai.domain.chatbot.usecase.HandleAgentResponseUseCase
import com.anurag.eduai.domain.chatbot.model.ResourceDecision
import com.anurag.eduai.domain.chatbot.usecase.ConceptProgressUseCase
import com.anurag.eduai.domain.chatbot.usecase.ResourceDecisionUseCase
import com.anurag.eduai.domain.chatbot.usecase.SendMessageUseCase
import com.anurag.eduai.domain.chatbot.usecase.SessionUseCase
import com.anurag.eduai.domain.chatbot.usecase.TranslationUseCase
import com.anurag.eduai.repository.ConceptRepository
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatBotSettingsState
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatUiState
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ResourceCardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sessionUseCase: SessionUseCase,
    private val autoSuggestionUseCase: AutoSuggestionUseCase,
    private val resourceDecisionUseCase: ResourceDecisionUseCase,
    private val conceptMapUseCase: ConceptMapUseCase,
    private val idleTimerController: IdleTimerController,
    private val typingAnimationController: TypingAnimationController,
    private val resourceController: ResourceController,
    private val sendMessageUseCase: SendMessageUseCase,
    private val handleAgentResponseUseCase: HandleAgentResponseUseCase,
    private val translationUseCase: TranslationUseCase,
    private val conceptRepository: ConceptRepository,
    private val conceptProgressUseCase: ConceptProgressUseCase,
    private val avatarChangeUseCase: AvatarChangeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var userId = ""
    private var initialized = false

    /**
     * handles all intents from the UI and routes them to appropriate functions
     */
    fun onIntent(intent: ChatIntent) = when (intent) {
        is ChatIntent.Initialize -> initialize(intent.userId)
        is ChatIntent.AutoStartWithConcept -> autoStartWithConcept(intent.conceptId)
        is ChatIntent.UpdateInputText -> updateInput(intent.text)
        is ChatIntent.SetStudentLevel -> _uiState.update { it.copy(studentLevel = intent.level) }
        is ChatIntent.SetKannada -> {
            _uiState.update { it.copy(isKannada = intent.enabled, currentLanguage = if (intent.enabled) "kn" else "en") }
            refreshConcepts()
        }
        is ChatIntent.SelectConcept -> selectConcept(intent.concept)
        is ChatIntent.SendMessage -> sendMessage(intent.message, false)
        is ChatIntent.TapAutosuggestion -> sendMessage(intent.suggestion, true)
        is ChatIntent.StartFreshSession -> startFreshSession(intent.concept)
        is ChatIntent.HasExistingSession -> Unit
        is ChatIntent.StartIdleTimer -> startIdleTimer()
        is ChatIntent.HideAutosuggestions -> hideAutosuggestions()
        is ChatIntent.MarkUserActive -> markUserActive()
        is ChatIntent.MarkUserInactive -> markUserInactive()
        is ChatIntent.RefreshConcepts -> refreshConcepts()
        is ChatIntent.DismissResource -> dismissResource()
        is ChatIntent.ResumeTTS -> resumeTTS()
    }

    /**
     * Checks if there's an existing session for the given concept
     */
    fun hasExistingSession(concept: String) = sessionUseCase.hasExistingSession(concept)

    /**
     * Initializes the ViewModel with the user ID and loads the list of available concepts.
     * This should be called once when the chat screen is opened.
     */
    private fun initialize(id: String) {
        if (initialized) return
        initialized = true
        userId = id

        val appLanguage = sessionUseCase.getAppLanguage()
        val isKannada = appLanguage == "kn"
        _uiState.update {
            it.copy(
                isKannada = isKannada,
                currentLanguage = appLanguage
            )
        }
        DebugLogger.debugLog("ChatViewModel", "Initialized with app language: $appLanguage, isKannada: $isKannada")

        refreshConcepts()
    }

    /**
     * Auto-starts a session with a concept from navigation (when user clicks a concept).
     * Fetches the concept name from the database using conceptId and delegates to selectConcept.
     * Uses only English concept name for starting the session, not Kannada.
     */
    private fun autoStartWithConcept(conceptId: String) = viewModelScope.launch {
        try {
            DebugLogger.debugLog("ChatViewModel", "autoStartWithConcept called with conceptId: $conceptId, userId: $userId")

            // Fetch concept from repository and use English name only
            val conceptEntity = conceptRepository.getConcept(conceptId)
            if (conceptEntity == null) {
                DebugLogger.errorLog("ChatViewModel", "Concept not found for ID: $conceptId")
                return@launch
            }

            DebugLogger.debugLog("ChatViewModel", "Auto-starting with concept: ${conceptEntity.conceptName}")
            selectConcept(conceptEntity.conceptName)
        } catch (e: Exception) {
            DebugLogger.errorLog("ChatViewModel", "Error auto-starting concept: ${e.message}")
        }
    }

    /**
     * Fetches the list of available concepts from the local database and updates the UI state.
     * Shows a loading indicator while fetching.
     * Uses Kannada concept names if Kannada mode is enabled.
     */
    private fun refreshConcepts() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            // Fetch all concepts from the database
            val conceptEntities = conceptRepository.getAllConcepts()

            // Extract English concept names for internal use (session management)
            val concepts = conceptEntities.map { it.conceptName }

            // Use Kannada names for display if Kannada mode is enabled
            val displayConcepts = if (_uiState.value.isKannada) {
                conceptEntities.map {
                    it.conceptNameKannada.ifBlank { it.conceptName }
                }
            } else {
                concepts
            }

            _uiState.update {
                it.copy(
                    availableConcepts = concepts,
                    displayConcepts = displayConcepts
                )
            }
            DebugLogger.debugLog("ChatViewModel", "Concepts loaded from DB: ${concepts.size}, Display concepts: ${displayConcepts.size}")
        } catch (e: Exception) {
            DebugLogger.errorLog("ChatViewModel", "Error loading concepts from DB: ${e.message}")
        }
        _uiState.update { it.copy(isLoading = false) }
    }

    /**
     * Updates the input text in the UI state and marks the user as active if the text is not blank.
     * Also hides autosuggestions when the user starts typing.
     */
    private fun updateInput(text: String) {
        _uiState.update {
            it.copy(inputText = text,
                isUserActive = text.isNotBlank(),
                showAutosuggestions = false)
        }
        if (text.isNotBlank()) markUserActive()
    }

    /**
     * Marks the user as active and starts monitoring for inactivity.
     * When the user is active, autosuggestions are hidden.
     */
    private fun markUserActive() = idleTimerController.markUserActive(
        scope = viewModelScope,
        onActive = { _uiState.update {
                        it.copy(isUserActive = true,
                            showAutosuggestions = false)
                        }
                   },
        onInactive = { _uiState.update { it.copy(isUserActive = false) } }
    )

    /**
     * Marks the user as inactive after a short delay.
     * This is called when the user stops interacting with the chat.
     */
    private fun markUserInactive() = idleTimerController.markUserInactive(
        scope = viewModelScope,
        onInactive = { _uiState.update { it.copy(isUserActive = false) } }
    )

    /**
     * Starts the idle timer which will show autosuggestions after a delay if the user remains inactive.
     */
    private fun startIdleTimer() = idleTimerController.startIdleTimer(viewModelScope) {
        if (autoSuggestionUseCase.shouldShowAutosuggestions(_uiState.value))
            _uiState.update { it.copy(showAutosuggestions = true) }
    }

    /**
     * Hides autosuggestions and cancels the idle timer.
     * This is called when the user starts typing or interacts with the chat in a way that indicates they are active.
     */
    private fun hideAutosuggestions() {
        idleTimerController.cancelIdleTimer()
        _uiState.update { it.copy(showAutosuggestions = false) }
    }

    /**
     * Handles the selection of a concept by the user.
     * It checks if there's an existing session for the selected concept.
     * If from ConceptScreen and session exists, shows dialog. Otherwise proceeds directly.
     */
    private fun selectConcept(concept: String, showDialogIfExists: Boolean = false) = viewModelScope.launch {
        DebugLogger.debugLog("ChatViewModel", "selectConcept called with concept: $concept, showDialog: $showDialogIfExists")

        // Check if session exists
        val mapping = sessionUseCase.loadThreadMapping(concept)

        if (showDialogIfExists && mapping != null) {
            // Show dialog for user to choose resume or start new
            DebugLogger.debugLog("ChatViewModel", "Session exists, showing dialog for concept: $concept")
            _uiState.update { it.copy(pendingConceptForDialog = concept) }
        } else {
            // Proceed with session (resume if exists, start new if not)
            resetUiForConcept(concept)
            if (mapping != null) {
                DebugLogger.debugLog("ChatViewModel", "Found existing session, resuming: threadId=${mapping.first}, sessionId=${mapping.second}")
                resumeSession(mapping.first, mapping.second)
            } else {
                DebugLogger.debugLog("ChatViewModel", "No existing session found, starting new session")
                startSession(concept)
            }
        }
    }

    /**
     * Overloaded method for ConceptScreen to check and show dialog if session exists
     */
    fun selectConceptWithDialog(concept: String) {
        if (sessionUseCase.hasExistingSession(concept)) {
            // Show dialog
            _uiState.update { it.copy(pendingConceptForDialog = concept) }
        } else {
            // No session exists, start directly
            selectConcept(concept, showDialogIfExists = false)
        }
    }

    /**
     * Dismiss the session dialog without taking action
     */
    fun dismissSessionDialog() {
        _uiState.update { it.copy(pendingConceptForDialog = null) }
    }

    /**
     * Resets the UI state for a new concept selection.
     * This includes clearing messages, hiding autosuggestions, resetting typing state, and showing a loading indicator.
     */
    private fun resetUiForConcept(concept: String) {
        typingAnimationController.cancel()
        resourceController.cancel()
        _uiState.update {
            it.copy(
                selectedConcept = concept,
                messages = emptyList(),
                autosuggestions = emptyList(),
                typingText = "",
                isTyping = false,
                resourceCardState = ResourceCardUiState.Hidden,
                pendingAgentResponse = null,
                loadingResourceMessage = null,
                isLoading = true,
                currentProgressPercentage = 0
            )
        }
    }

    /**
     * Helper method to get current progress
     */
    fun getCurrentProgress(): Int {
        return _uiState.value.currentProgressPercentage
    }

    /**
     * Helper method to get visited states from metadata
     */
    fun getVisitedStates(): Set<String> {
        return conceptProgressUseCase.getVisitedStates(_uiState.value.agentMetadata)
    }

    /**
     * Starts a new session for the given concept by calling the SessionUseCase.
     * Translates autosuggestions if Kannada mode is enabled.
     */
    private suspend fun startSession(concept: String) {
        DebugLogger.debugLog("ChatViewModel", "startSession called for concept: $concept, userId: $userId, studentLevel: ${_uiState.value.studentLevel}")
        val result = sessionUseCase.startSession(concept, userId, _uiState.value.isKannada, _uiState.value.studentLevel)

        if (!result.success) {
            DebugLogger.errorLog("ChatViewModel", "Failed to start session for concept: $concept")
            return _uiState.update { it.copy(isLoading = false) }
        }

        DebugLogger.debugLog("ChatViewModel", "Session started successfully for concept: $concept")

        // Mark concept as IN_PROGRESS when session starts successfully
        // Match by English concept name since concept parameter is always in English
        val conceptEntity = conceptRepository.getAllConcepts().find { it.conceptName == concept }
        if (conceptEntity != null && userId.isNotEmpty()) {
            conceptProgressUseCase.markConceptInProgress(userId, conceptEntity.conceptId)
        }

        // Translate autosuggestions if Kannada mode is enabled
        val translatedSuggestions = if (_uiState.value.isKannada && result.autosuggestions.isNotEmpty()) {
            translationUseCase.translateListToKannada(result.autosuggestions)
        } else {
            result.autosuggestions
        }

        _uiState.update { it.copy(
            isSessionStarted = true,
            autosuggestions = translatedSuggestions,
            agentMetadata = result.metadata,
            currentState = result.currentState,
            showAutosuggestions = false,
            isLoading = false) }

        // Update progress based on explicit currentState from API
        val progress = conceptProgressUseCase.calculateProgressPercentage(result.currentState, result.metadata)
        _uiState.update { it.copy(currentProgressPercentage = progress) }

        result.agentResponse?.let { handleAgentMessage(it, result.metadata) }
    }

    /**
     * Resumes an existing session using the thread ID and session ID.
     * It fetches the session history and updates the UI state with the previous messages and session information.
     * Translates only the last AI message to Kannada if Kannada mode is enabled (since only last message is displayed).
     */
    private suspend fun resumeSession(threadId: String, sessionId: String?) {
        val result = sessionUseCase.resumeSession(threadId, sessionId)

        DebugLogger.debugLog("ChatViewModel", "resumeSession - isKannada=${_uiState.value.isKannada}, messages count=${result.messages.size}")

        // Translate only the last AI message if Kannada mode is enabled (optimization - only last message is displayed)
        val translatedMessages = if (_uiState.value.isKannada && result.messages.isNotEmpty()) {
            val lastAiMessageIndex = result.messages.indexOfLast { it.sender.lowercase() == "ai" }

            DebugLogger.debugLog("ChatViewModel", "resumeSession - Last AI message index: $lastAiMessageIndex")

            if (lastAiMessageIndex >= 0) {
                result.messages.mapIndexed { index, message ->
                    if (index == lastAiMessageIndex) {
                        DebugLogger.debugLog("ChatViewModel", "resumeSession - Translating last AI message: ${message.content.take(50)}...")
                        val translated = translationUseCase.translateToKannada(message.content)
                        DebugLogger.debugLog("ChatViewModel", "resumeSession - Translation result: ${translated.take(50)}...")
                        message.copy(content = translated)
                    } else {
                        message
                    }
                }
            } else {
                DebugLogger.debugLog("ChatViewModel", "resumeSession - No AI messages found")
                result.messages
            }
        } else {
            DebugLogger.debugLog("ChatViewModel", "resumeSession - Skipping translation (isKannada=${_uiState.value.isKannada}, messages empty=${result.messages.isEmpty()})")
            result.messages
        }

        _uiState.update { it.copy(
            isSessionStarted = result.success,
            messages = translatedMessages,
            isLoading = false,
            agentMetadata = result.metadata,
            currentState = result.currentState
        ) }

        // Update progress using explicit currentState from resume response
        val resumedProgress = conceptProgressUseCase.calculateProgressPercentage(result.currentState, result.metadata)
        _uiState.update { it.copy(currentProgressPercentage = resumedProgress) }
    }

    /**
     * Starts a fresh session for the given concept by deleting any existing session mapping and then starting a new session.
     */
    private fun startFreshSession(concept: String) = viewModelScope.launch {
        sessionUseCase.deleteSessionMapping(concept)
        _uiState.update { ChatUiState(
            selectedConcept = concept,
            availableConcepts = it.availableConcepts,
            currentLanguage = it.currentLanguage,
            studentLevel = it.studentLevel,
            isKannada = it.isKannada)
        }
        selectConcept(concept)
    }

    /**
     * Sends a message to the agent and handles the response.
     * It updates the UI state with the user's message, shows a loading indicator,
     * and then processes the agent's response to update the chat messages, autosuggestions,
     * and any resources that need to be displayed.
     * Translates autosuggestions if Kannada mode is enabled.
     */
    private fun sendMessage(message: String, fromSuggestion: Boolean) {
        if (message.isBlank()) return
        viewModelScope.launch {
            hideAutosuggestions(); markUserActive()
            _uiState.update { it.copy(
                messages = it.messages + sendMessageUseCase.createUserMessage(message),
                isLoading = true)
            }
            if (!_uiState.value.isSessionStarted)
                _uiState.value.selectedConcept?.let { startSession(it) }
            val response = sessionUseCase.continueSession(
                message,
                fromSuggestion,
                _uiState.value.studentLevel)

            if (!response.success) return@launch appendError()

            // Translate autosuggestions if Kannada mode is enabled
            val translatedSuggestions = if (_uiState.value.isKannada && response.autosuggestions.isNotEmpty()) {
                translationUseCase.translateListToKannada(response.autosuggestions)
            } else {
                response.autosuggestions
            }

            _uiState.update { it.copy(
                autosuggestions = translatedSuggestions,
                agentMetadata = response.metadata,
                currentState = response.currentState,
                showAutosuggestions = false)
            }

            // Calculate and update progress in database
            val continuedProgress = conceptProgressUseCase.calculateProgressPercentage(
                response.currentState,
                response.metadata
            )
            _uiState.update { it.copy(currentProgressPercentage = continuedProgress) }

            // Update progress in database for persistence
            // Match by English concept name since selectedConcept is always in English
            val conceptEntity = conceptRepository.getAllConcepts().find {
                it.conceptName == _uiState.value.selectedConcept
            }
            if (conceptEntity != null && userId.isNotEmpty()) {
                conceptRepository.updateProgressStatus(
                    studentId = userId,
                    itemType = "CONCEPT",
                    itemId = conceptEntity.conceptId,
                    newStatus = if (continuedProgress == 100) "COMPLETED" else "IN_PROGRESS",
                    progressPercentage = continuedProgress,
                    timestamp = System.currentTimeMillis()
                )
            }

            // Check if END node and mark as completed
            if (response.currentState?.uppercase() == "END") {
                if (conceptEntity != null && userId.isNotEmpty()) {
                    conceptProgressUseCase.markConceptCompleted(userId, conceptEntity.conceptId)
                    DebugLogger.debugLog("ChatViewModel", "Concept ${conceptEntity.conceptId} marked as COMPLETED - END node reached")
                }
            }

            val resourceShown = response.metadata?.let { metadata ->
                response.agentResponse?.let { agentText ->
                    handleResource(metadata, agentText)
                } ?: false
            } ?: false
            response.agentResponse?.let { agentText ->
                if (resourceShown) _uiState.update { it.copy(pendingAgentResponse = agentText) }
                else handleAgentMessage(agentText, response.metadata)
            }
            // Only set isLoading to false if no resource is being generated
            if (!resourceShown) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Appends an error message to the chat when something goes wrong with sending a message or processing the agent's response.
     */
    private fun appendError() = _uiState.update {
        it.copy(
            messages = it.messages + sendMessageUseCase.createAIMessage("Sorry, I couldn't process that. Please try again."),
            isLoading = false)
    }

    /**
     * Processes the agent's response text,
     * updates the chat messages with the new response, and starts the typing animation.
     * Translates the response to Kannada if Kannada mode is enabled.
     */
    private fun handleAgentMessage(text: String, metadata: SessionMetadata?) {
        viewModelScope.launch {
            val cleaned = handleAgentResponseUseCase.processAgentResponse(text)

            // Translate to Kannada if enabled
            val displayText = if (_uiState.value.isKannada) {
                DebugLogger.debugLog("ChatViewModel", "Translating agent response to Kannada...")
                translationUseCase.translateToKannada(cleaned)
            } else {
                cleaned
            }

            _uiState.update { it.copy(messages = it.messages + sendMessageUseCase.createAIMessage(displayText), isTyping = true, typingText = "", fullTextForTTS = displayText, shouldStartTTS = true, isTypingComplete = false, showAutosuggestions = false) }
            typingAnimationController.startTypingAnimation(displayText, viewModelScope) { typingText, complete ->
                _uiState.update { it.copy(
                    typingText = typingText,
                    isTyping = !complete,
                    isTypingComplete = complete,
                    shouldStartTTS = if (complete) false
                                    else it.shouldStartTTS)
                }
            }
        }
    }

    /**
     * Determines if any resource (like an image or concept map)
     * should be shown based on the agent's response and session metadata.
     */
    private fun handleResource(metadata: SessionMetadata, agentResponse: String): Boolean {
        return when (
            val decision = resourceDecisionUseCase.decide(metadata)) {
            is ResourceDecision.ShowImage -> {
                DebugLogger.debugLog("ChatViewModel", "Showing image: ${decision.url}")
                startImageResource(decision.url, decision.description)
                true
            }
            is ResourceDecision.ShowConceptMap -> {
                DebugLogger.debugLog("ChatViewModel", "Generating concept map")
                generateAndShowConceptMap(agentResponse)
                true
            }
            ResourceDecision.None -> {
                DebugLogger.debugLog("ChatViewModel", "No resource to show")
                false
            }
        }
    }

    /**
     * Starts a timer to show an image resource for a certain duration.
     */
    private fun startImageResource(url: String, description: String?, duration: Int=10) = startResource(duration) { remaining ->
        ResourceCardUiState.Image(url, description, remaining, duration)
    }

    private fun generateAndShowConceptMap(agentResponse: String) {
        viewModelScope.launch {
            // Show loading message "Generating concept map..." in ChatContentArea
            _uiState.update { it.copy(
                loadingResourceMessage = "Generating concept map...",
                isLoading = true
            ) }

            // Generate concept map from agent response
            val result = conceptMapUseCase.generateConceptMap(
                aiResponse = agentResponse,
                language = _uiState.value.currentLanguage
            )

            // Clear loading state
            _uiState.update { it.copy(
                loadingResourceMessage = null,
                isLoading = false
            ) }

            if (result.success && result.json != null && !result.isDefault) {
                // Successfully generated concept map - show ResourceCard
                DebugLogger.debugLog("ChatViewModel", "Concept map generated successfully")
                startConceptMap(result.json)
            } else {
                // Failed to generate or got default map - log error and show agent message normally
                val errorMsg = if (result.isDefault)
                    "Default concept map detected - skipping"
                else
                    "Error generating concept map"

                DebugLogger.debugLog("ChatViewModel", errorMsg)
                _uiState.update { it.copy(conceptMapStatus = errorMsg) }

                // Show the pending agent message with typing animation
                val pending = _uiState.value.pendingAgentResponse
                pending?.let {
                    _uiState.update { it.copy(pendingAgentResponse = null) }
                    handleAgentMessage(it, _uiState.value.agentMetadata)
                }
            }
        }
    }

    /**
     * Starts a timer to show a concept map resource for a certain duration, using the provided JSON data.
     */
    private fun startConceptMap(json: String, duration: Int = 10) = startResource(duration) { remaining ->
        ResourceCardUiState.ConceptMap(json, 0f, false, remaining, duration)
    }


    /**
     * Starts a timer to display a resource (image or concept map) for a specified duration.
     */
    private fun startResource(duration: Int, builder: (Int) -> ResourceCardUiState) {
        // Immediately show the resource card
        _uiState.update {
            it.copy(
                resourceCardState = builder(duration),
                loadingResourceMessage = null,
                isLoading = false,
                ttsPausedForResource = true
            )
        }

        DebugLogger.debugLog("ChatViewModel", "ResourceCard shown: ${_uiState.value.resourceCardState}")

        // Start the countdown timer
        resourceController.startTimer(viewModelScope, duration,
            onTick = { remaining -> _uiState.update {
                it.copy(resourceCardState = builder(remaining),
                    loadingResourceMessage = null,
                    ttsPausedForResource = true) } },
            onFinish = { dismissResource() }
        )
    }

    /**
     * Dismisses the currently displayed resource and resets the related UI state.
     */
    private fun dismissResource() {
        resourceController.cancel()
        val pending = _uiState.value.pendingAgentResponse
        _uiState.update { it.copy(resourceCardState = ResourceCardUiState.Hidden, ttsPausedForResource = false, pendingAgentResponse = null, loadingResourceMessage = null, isLoading = false) }
        pending?.let { handleAgentMessage(it, _uiState.value.agentMetadata) }
    }

    /**
     * Resumes TTS playback if it was paused for a resource.
     * This is called when a resource is dismissed to allow the agent's response to be read aloud again.
     */
    private fun resumeTTS() = _uiState.update { it.copy(ttsPausedForResource = false) }

    /**
     * Handles avatar change with proper validation and delegation to use case
     * Returns updated ChatBotSettingsState with both code and display name
     * @param displayName The localized display name from UI
     * @param boyDisplayName The localized "boy" string
     * @param girlDisplayName The localized "girl" string
     * @param ttsController The TTS controller to apply voice changes
     * @param currentState The current settings state
     * @return Updated ChatBotSettingsState with normalized code and display name
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

        // Apply avatar change through use case
        val normalizedCode = avatarChangeUseCase.changeAvatar(
            avatarCode = avatarCode,
            ttsController = ttsController,
            currentLanguage = _uiState.value.currentLanguage
        )

        // Return updated state with both code and display name
        return currentState.copy(
            selectedAvatar = normalizedCode,
            selectedAvatarDisplayName = displayName
        )
    }

    /**
     * Initialize settings state with proper display name for current avatar
     * @param avatarCode Current avatar code
     * @param boyDisplayName Localized "boy" string
     * @param girlDisplayName Localized "girl" string
     * @param disableDisplayName Localized "disable" string
     * @param currentState Current settings state
     * @return Updated state with display name
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
