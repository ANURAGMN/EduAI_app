package com.anurag.eduai.ui.viewModel

import com.anurag.eduai.data.remote.SessionMetadata
import com.anurag.eduai.ui.screens.chatbotscreen.components.ResourceContent
import com.anurag.eduai.ui.screens.chatbotscreen.components.ResourceDisplayMode
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel

/**
 * Consolidated UI state for the chat screen
 * Reduces number of StateFlows from 20+ to just 2-3
 */
data class ChatUiState(
    // Messages
    val messages: List<ChatMessageModel> = emptyList(),
    val inputText: String = "",

    // Loading & Typing
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
    val typingText: String = "",

    // Session
    val isSessionStarted: Boolean = false,
    val selectedConcept: String? = null,
    val availableConcepts: List<String> = emptyList(),

    // Auto-suggestions
    val autosuggestions: List<String> = emptyList(),
    val showAutosuggestions: Boolean = false,
    val isUserActive: Boolean = false,

    // Resources
    val showResourceCard: Boolean = false,
    val currentResource: ResourceContent? = null,
    val resourceDisplayMode: ResourceDisplayMode = ResourceDisplayMode.IMAGE,
    val conceptMapJSON: String = """{"visualization_type":"None","main_concept":"Chat for a Concept Map","nodes":[],"edges":[]}""",

    // TTS
    val shouldStartTTS: Boolean = false,
    val fullTextForTTS: String = "",
    val ttsPausedForResource: Boolean = false,

    // Settings
    val studentLevel: String = "medium",
    val isKannada: Boolean = false,
    val currentLanguage: String = "en",

    // Metadata
    val agentMetadata: SessionMetadata? = null
)

/**
 * Represents the last AI message for quick access
 */
val ChatUiState.lastAiMessage: ChatMessageModel?
    get() = messages.findLast { it.sender.lowercase() == "ai" }

/**
 * Check if conversation has started
 */
val ChatUiState.isConversationStarted: Boolean
    get() = messages.isNotEmpty()

