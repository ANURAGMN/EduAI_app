package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.domain.chatbot.controller.TypingAnimationController
import com.anurag.eduai.domain.chatbot.usecase.AvatarChangeUseCase
import com.anurag.eduai.domain.chatbot.usecase.RevisionUseCase
import com.anurag.eduai.domain.chatbot.usecase.TranslationUseCase
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatBotSettingsState
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatUiState
import com.anurag.eduai.utils.getCurrentLanguageCode
import com.anurag.eduai.utils.isKannada
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
    private val avatarChangeUseCase: AvatarChangeUseCase
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

    /**
     * Normalize chapter name to match API format.
     * Converts "Light: Shadows And Reflections" to "Light Shadows And Reflections"
     * - Removes emojis and special characters
     * - Removes dashes/hyphens
     * - Removes colons
     * - Normalizes spacing and handles "and" capitalization
     */
    private fun normalizeChapterName(chapterName: String): String {
        var normalized = chapterName

        // Remove emojis and special Unicode characters (keep only ASCII and common text)
        normalized = normalized.replace(Regex("[^\\p{L}\\p{N}\\s,&]"), " ")

        // Remove commas and normalize spacing
        normalized = normalized.replace(", ", " ").replace(",", " ")

        // Remove dashes/hyphens and normalize spacing
        normalized = normalized.replace("-", " ")

        // Handle "and" capitalization: "and" should be "And" when it's a word separator
        normalized = normalized.replace(Regex("\\band\\b"), "And")

        // Clean up extra spaces
        normalized = normalized.replace(Regex("\\s+"), " ").trim()

        return normalized
    }

    /**
     * Initialize the ViewModel with userId and auto-start revision session
     */
    fun initialize(userId: String, chapterName: String) {
        if (initialized) return
        initialized = true
        this.userId = userId
        this.currentChapter = normalizeChapterName(chapterName)

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

        // Auto-start the revision session with normalized chapter name
        autoStartRevision(currentChapter)
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
     * Toggle language between English and Kannada
     */
    fun toggleLanguage() {
        val newIsKannada = !_uiState.value.isKannada
        val newLanguage = if (newIsKannada) "kn" else "en"
        _uiState.update {
            it.copy(
                isKannada = newIsKannada,
                currentLanguage = newLanguage
            )
        }
        DebugLogger.debugLog("RevisionViewModel", "Language toggled to: $newLanguage")
    }

    /**
     * Change to a different chapter
     */
    fun changeChapter(newChapter: String) = viewModelScope.launch {
        val normalizedNewChapter = normalizeChapterName(newChapter)

        if (normalizedNewChapter == currentChapter) return@launch

        DebugLogger.debugLog("RevisionViewModel", "Changing chapter from '$currentChapter' to '$normalizedNewChapter'")

        // Delete old session
        revisionUseCase.deleteRevisionSessionMapping(currentChapter)

        // Reset state
        currentChapter = normalizedNewChapter
        _uiState.update {
            ChatUiState(
                selectedConcept = newChapter,
                isKannada = it.isKannada,
                currentLanguage = it.currentLanguage
            )
        }

        // Start new session with new chapter
        autoStartRevision(normalizedNewChapter)
    }

    /**
     * Auto-start revision session for the given chapter
     */
    private fun autoStartRevision(chapter: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, selectedConcept = chapter) }

        // Check if there's an existing session
        val existingThreadId = revisionUseCase.getRevisionThreadId(chapter)
        if (existingThreadId != null) {
            DebugLogger.debugLog("RevisionViewModel", "Found existing revision session, resuming...")
            resumeRevisionSession(existingThreadId)
        } else {
            DebugLogger.debugLog("RevisionViewModel", "No existing session found, starting new revision session")
            startRevisionSession(chapter)
        }
    }

    /**
     * Start a new revision session
     */
    private suspend fun startRevisionSession(chapter: String) {
        DebugLogger.debugLog("RevisionViewModel", "startRevisionSession called for chapter: $chapter")
        val currentIsKannada = _uiState.value.isKannada
        val result = revisionUseCase.startRevisionSession(chapter, userId, currentIsKannada)

        if (!result.success) {
            DebugLogger.errorLog("RevisionViewModel", "Failed to start revision session for chapter: $chapter")
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

        val aiMessage = ChatMessageModel(
            content = translatedResponse,
            sender = "ai",
            timestamp = System.currentTimeMillis()
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
            content = message,
            sender = "user",
            timestamp = System.currentTimeMillis()
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isLoading = true
            )
        }

        // Send message to revision agent with current language setting
        val currentIsKannada = _uiState.value.isKannada
        val result = revisionUseCase.continueRevisionSession(currentChapter, message, currentIsKannada)

        if (!result.success) {
            DebugLogger.errorLog("RevisionViewModel", "Failed to send message in revision session")
            return@launch _uiState.update { it.copy(isLoading = false) }
        }

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

        val aiMessage = ChatMessageModel(
            content = translatedResponse,
            sender = "ai",
            timestamp = System.currentTimeMillis()
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
