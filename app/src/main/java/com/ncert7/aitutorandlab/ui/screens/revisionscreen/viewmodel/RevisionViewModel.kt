package com.ncert7.aitutorandlab.ui.screens.revisionscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncert7.aitutorandlab.data.local.dao.ChapterDao
import com.ncert7.aitutorandlab.data.local.dao.ConceptDao
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.domain.chatbot.controller.TypingAnimationController
import com.ncert7.aitutorandlab.domain.chatbot.usecase.AvatarChangeUseCase
import com.ncert7.aitutorandlab.domain.revisionagent.usecase.RevisionUseCase
import com.ncert7.aitutorandlab.domain.chatbot.usecase.TranslationUseCase
import com.ncert7.aitutorandlab.domain.progress.ProgressEventTracker
import com.ncert7.aitutorandlab.ui.screens.chatbotscreen.components.dataclass.ChatBotSettingsState
import com.ncert7.aitutorandlab.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel
import com.ncert7.aitutorandlab.ui.screens.chatbotscreen.components.dataclass.ChatUiState
import com.ncert7.aitutorandlab.ui.viewModel.TextToSpeech
import com.ncert7.aitutorandlab.utils.getCurrentLanguageCode
import com.ncert7.aitutorandlab.utils.isKannada
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling revision chat sessions.
 * Similar to ChatViewModel but focused on revision-specific functionality.
 */
@HiltViewModel
class RevisionViewModel @Inject constructor(
    private val revisionUseCase: RevisionUseCase,
    private val typingAnimationController: TypingAnimationController,
    private val translationUseCase: TranslationUseCase,
    private val avatarChangeUseCase: AvatarChangeUseCase,
    private val progressEventTracker: ProgressEventTracker,
    private val chapterDao: ChapterDao,
    private val conceptDao: ConceptDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _availableChapters = MutableStateFlow<List<String>>(emptyList())
    val availableChapters: StateFlow<List<String>> = _availableChapters.asStateFlow()

    private val _isLoadingChapters = MutableStateFlow(false)
    val isLoadingChapters: StateFlow<Boolean> = _isLoadingChapters.asStateFlow()

    private var userId = ""
    private var initialized = false
    private var currentChapter = ""
    private var currentRevisionId = ""

    /**
     * Initialize the ViewModel with userId and auto-start revision session
     */
    fun initialize(userId: String, chapterName: String) {
        if (initialized) return
        initialized = true
        this.userId = userId
        this.currentChapter = chapterName

        // Use LocalizationUtils for language detection
        val appLanguage = getCurrentLanguageCode()
        val isKannadaMode = isKannada()

        _uiState.update {
            it.copy(
                isKannada = isKannadaMode,
                currentLanguage = appLanguage,
                selectedConcept = chapterName
            )
        }

        DebugLogger.debugLog("RevisionViewModel", "Initialized with chapter: $chapterName, userId: $userId, language: $appLanguage, isKannada: $isKannadaMode")

        // Fetch available chapters from backend
        fetchAvailableChapters()

        // Auto-start the revision session by fetching revisionId from database
        viewModelScope.launch {
            try {
                val chapterEntity = chapterDao.getChapterByName(chapterName)
                if (chapterEntity != null) {
                    currentRevisionId = chapterEntity.revisionId
                    DebugLogger.debugLog("RevisionViewModel", "Fetched revisionId: $currentRevisionId for chapter: $chapterName")
                    autoStartRevision(currentRevisionId)
                } else {
                    DebugLogger.errorLog("RevisionViewModel", "Could not find chapter in database: $chapterName")
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("RevisionViewModel", "Error fetching revisionId: ${e.message}")
            }
        }
    }

    /**
     * Fetch available chapters from backend /revision/chapters endpoint
     */
    private fun fetchAvailableChapters() = viewModelScope.launch {
        _isLoadingChapters.value = true
        try {
            val result = revisionUseCase.getAvailableChapters()
            if (result.isNotEmpty()) {
                _availableChapters.value = result
                DebugLogger.debugLog("RevisionViewModel", "Fetched ${result.size} chapters from backend")
            } else {
                DebugLogger.errorLog("RevisionViewModel", "No chapters received from backend")
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("RevisionViewModel", "Error fetching chapters: ${e.message}")
        } finally {
            _isLoadingChapters.value = false
        }
    }

    /**
     * Change to a different chapter
     */
    fun changeChapter(newChapter: String) = viewModelScope.launch {
        if (newChapter == currentChapter) return@launch

        DebugLogger.debugLog("RevisionViewModel", "Changing chapter from '$currentChapter' to '$newChapter'")

        // Delete old session
        revisionUseCase.deleteRevisionSessionMapping(currentRevisionId)

        // Reset state and fetch new revisionId
        currentChapter = newChapter
        _uiState.update {
            ChatUiState(
                selectedConcept = newChapter,
                isKannada = it.isKannada,
                currentLanguage = it.currentLanguage
            )
        }

        // Fetch revisionId for new chapter from database
        try {
            val chapterEntity = chapterDao.getChapterByName(newChapter)
            if (chapterEntity != null) {
                currentRevisionId = chapterEntity.revisionId
                autoStartRevision(currentRevisionId)
            } else {
                DebugLogger.errorLog("RevisionViewModel", "Could not find chapter in database: $newChapter")
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("RevisionViewModel", "Error fetching revisionId for new chapter: ${e.message}")
        }
    }

    /**
     * Auto-start revision session for the given revisionId
     */
    private fun autoStartRevision(revisionId: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, selectedConcept = currentChapter) }

        // Check if there's an existing session using revisionId
        val existingThreadId = revisionUseCase.getRevisionThreadId(revisionId)
        if (existingThreadId != null) {
            DebugLogger.debugLog("RevisionViewModel", "Found existing revision session for revisionId: $revisionId, resuming...")
            resumeRevisionSession(existingThreadId)
        } else {
            DebugLogger.debugLog("RevisionViewModel", "No existing session found for revisionId: $revisionId, starting new revision session")
            startRevisionSession(revisionId)
        }
    }

    /**
     * Start a new revision session using revisionId
     */
    private suspend fun startRevisionSession(revisionId: String) {
        DebugLogger.debugLog("RevisionViewModel", "startRevisionSession called for revisionId: $revisionId")
        val currentIsKannada = _uiState.value.isKannada
        val result = revisionUseCase.startRevisionSession(revisionId, userId, currentIsKannada)

        if (!result.success) {
            DebugLogger.errorLog("RevisionViewModel", "Failed to start revision session for revisionId: $revisionId")
            return _uiState.update { it.copy(isLoading = false) }
        }

        DebugLogger.debugLog("RevisionViewModel", "Revision session started successfully")

        val agentResponse = result.agentResponse ?: ""

        // Smart translation: Only translate if needed based on current app language
        val translatedResponse = if (currentIsKannada) {
            // App is in Kannada - translate if response is in English
            if (isTextInKannada(agentResponse)) {
                agentResponse // Already in Kannada
            } else {
                translationUseCase.translateToKannada(agentResponse)
            }
        } else {
            // App is in English - translate if response is in Kannada
            if (isTextInKannada(agentResponse)) {
                translationUseCase.translateToEnglish(agentResponse)
            } else {
                agentResponse // Already in English
            }
        }

        // Track Revision Progress - mark each concept in this chapter as REVISION_AGENT completed
        viewModelScope.launch {
            try {
                // Fetch chapter entity using currentChapter name to get chapterId
                val matchedChapter = chapterDao.getChapterByName(currentChapter)

                if (matchedChapter != null) {
                    // Get all STUDY concepts in the chapter and mark each as REVISION_AGENT COMPLETED
                    val concepts = conceptDao.getConceptsForChapterSync(matchedChapter.chapterId, "STUDY")
                    concepts.forEach { concept ->
                        progressEventTracker.markRevisionCompleted(userId, concept.conceptId)
                    }
                    DebugLogger.debugLog("RevisionViewModel", " Marked ${concepts.size} concepts as revision-completed for revisionId: $revisionId")
                } else {
                    DebugLogger.errorLog("RevisionViewModel", "Could not find chapter in DB for name: $currentChapter — revision progress not tracked")
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("RevisionViewModel", "Error tracking revision progress: ${e.message}")
            }
        }

        val aiMessage = ChatMessageModel(
            sender = "ai",
            content = translatedResponse,
        )

        _uiState.update {
            it.copy(
                isSessionStarted = true,
                messages = listOf(aiMessage),
                isLoading = false,
                currentState = result.currentState,
                isTyping = true,
                typingText = "",
                fullTextForTTS = translatedResponse,
                shouldStartTTS = true,
                isTypingComplete = false
            )
        }

        // Start typing animation
        typingAnimationController.startTypingAnimation(
            fullText = translatedResponse,
            scope = viewModelScope
        ) { currentText, isComplete ->
            _uiState.update { state ->
                if (isComplete) {
                    state.copy(
                        isTyping = false,
                        typingText = "",
                        isTypingComplete = true,
                        shouldStartTTS = false
                    )
                } else {
                    state.copy(typingText = currentText)
                }
            }
        }
    }

    /**
     * Simple heuristic to detect if text contains Kannada characters
     */
    private fun isTextInKannada(text: String): Boolean {
        // Kannada Unicode range: \u0C80-\u0CFF
        return text.any { it in '\u0C80'..'\u0CFF' }
    }

    /**
     * Resume an existing revision session
     * Translation is already handled by RevisionUseCase based on current app language
     */
    private suspend fun resumeRevisionSession(threadId: String) {
        val result = revisionUseCase.resumeRevisionSession(threadId, null)

        if (!result.success) {
            DebugLogger.errorLog("RevisionViewModel", "Failed to resume revision session")
            return _uiState.update { it.copy(isLoading = false) }
        }

        // Messages are already translated by RevisionUseCase based on current app language
        _uiState.update {
            it.copy(
                isSessionStarted = result.success,
                messages = result.messages,
                isLoading = false,
                currentState = result.currentState
            )
        }
    }

    /**
     * Send a user message in the revision session
     */
    fun sendMessage(message: String) = viewModelScope.launch {
        if (message.isBlank()) return@launch

        val userMessage = ChatMessageModel(
            sender = "user",
            content = message,
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isLoading = true
            )
        }

        // Send message to revision agent with current language setting using revisionId
        val isKannada = _uiState.value.isKannada
        val result = revisionUseCase.continueRevisionSession(currentRevisionId, message, isKannada)

        if (!result.success) {
            DebugLogger.errorLog("RevisionViewModel", "Failed to send message in revision session")
            return@launch _uiState.update { it.copy(isLoading = false) }
        }

        val agentResponse = result.agentResponse ?: ""

        // Smart translation: Only translate if needed based on current app language
        val translatedResponse = if (isKannada) {
            // App is in Kannada - translate if response is in English
            if (isTextInKannada(agentResponse)) {
                agentResponse // Already in Kannada
            } else {
                translationUseCase.translateToKannada(agentResponse)
            }
        } else {
            // App is in English - translate if response is in Kannada
            if (isTextInKannada(agentResponse)) {
                translationUseCase.translateToEnglish(agentResponse)
            } else {
                agentResponse // Already in English
            }
        }

        val aiMessage = ChatMessageModel(
            sender = "ai",
            content = translatedResponse,
        )

        _uiState.update {
            it.copy(
                messages = it.messages + aiMessage,
                isLoading = false,
                currentState = result.currentState,
                isTyping = true,
                typingText = "",
                fullTextForTTS = translatedResponse,
                shouldStartTTS = true,
                isTypingComplete = false
            )
        }

        // Start typing animation
        typingAnimationController.startTypingAnimation(
            fullText = translatedResponse,
            scope = viewModelScope
        ) { currentText, isComplete ->
            _uiState.update { state ->
                if (isComplete) {
                    state.copy(
                        isTyping = false,
                        typingText = "",
                        isTypingComplete = true,
                        shouldStartTTS = false
                    )
                } else {
                    state.copy(typingText = currentText)
                }
            }
        }
    }

    /**
     * Check if there's an existing revision session for a chapter
     */
    fun hasExistingSession(chapter: String): Boolean {
        return revisionUseCase.getRevisionThreadId(chapter) != null
    }

    /**
     * Update input text
     */
    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    /**
     * Start a fresh revision session (delete existing and start new)
     */
    fun startFreshSession() = viewModelScope.launch {
        revisionUseCase.deleteRevisionSessionMapping(currentChapter)
        _uiState.update {
            ChatUiState(
                selectedConcept = currentChapter,
                isKannada = it.isKannada,
                currentLanguage = it.currentLanguage
            )
        }
        startRevisionSession(currentChapter)
    }

    /**
     * Handle avatar change
     */
    fun handleAvatarChange(
        displayName: String,
        boyDisplayName: String,
        girlDisplayName: String,
        ttsController: TextToSpeech,
        currentState: ChatBotSettingsState
    ): ChatBotSettingsState {
        val avatarCode = avatarChangeUseCase.getAvatarCodeFromDisplayName(
            displayName = displayName,
            boyDisplayName = boyDisplayName,
            girlDisplayName = girlDisplayName
        )

        val normalizedCode = avatarChangeUseCase.changeAvatar(
            avatarCode = avatarCode,
            ttsController = ttsController,
            currentLanguage = _uiState.value.currentLanguage
        )

        return currentState.copy(
            selectedAvatar = normalizedCode,
            selectedAvatarDisplayName = displayName
        )
    }

    /**
     * Initialize avatar display name
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