package com.ncert7.aitutorandlab.ui.screens.mathagentscreen.dataclass

import com.ncert7.aitutorandlab.data.remote.SessionMetadata

data class MathUiState(
    val problemId: String = "",
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
    val typingText: String = "",
    val messages: List<MathMessageModel> = emptyList(),
    val currentState: String = "START",
    val threadId: String? = null,
    val isKannada: Boolean = false,
    val currentLanguage: String = "en",
    val problems: List<MathProblemUi> = emptyList(),
    val selectedProblem: MathProblemUi? = null,
    val showAutosuggestions: Boolean = false,
    val autosuggestions: List<String> = emptyList(),
    val sessionStarted: Boolean = false,
    val isSpeaking: Boolean = false,
    val metadata: SessionMetadata = SessionMetadata(),
    val errorMessage: String? = null,
    val selectedImageUri: String? = null,
    val showImagePicker: Boolean = false,
    val pendingProblemForDialog: String? = null,
    val showSessionDialog: Boolean = false,
    val shouldStartTTS: Boolean = false,
    val fullTextForTTS: String = ""
)

/**
 * Get the last AI message from the messages list
 */
val MathUiState.lastAiMessage: MathMessageModel?
    get() = messages.findLast { it.role.lowercase() == "assistant" }

/**
 * Check if conversation has started
 */
val MathUiState.isConversationStarted: Boolean
    get() = messages.isNotEmpty()