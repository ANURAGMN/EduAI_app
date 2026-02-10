package com.anurag.eduai.domain.chatbot.model

import com.anurag.eduai.data.remote.SessionMetadata
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel

data class SessionResult(
    val success: Boolean,
    val autosuggestions: List<String> = emptyList(),
    val agentResponse: String? = null,
    val metadata: SessionMetadata? = null,
    val messages: List<ChatMessageModel> = emptyList()
)

data class SessionData(
    val threadId: String,
    val sessionId: String?,
    val messages: List<ChatMessageModel>
)
