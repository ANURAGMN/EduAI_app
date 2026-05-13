package com.ncert7.aitutorandlab.domain.mathagent.model

import com.ncert7.aitutorandlab.data.remote.SessionMetadata
import com.ncert7.aitutorandlab.ui.screens.mathagentscreen.dataclass.MathMessageModel

/**
 * Result of a math session operation
 * Similar to SessionResult from chatbot domain
 */
data class MathSessionResult(
    val success: Boolean,
    val agentResponse: String? = null,
    val currentState: String? = null,
    val metadata: SessionMetadata? = null,
    val messages: List<MathMessageModel> = emptyList(),
    val threadId: String? = null,
    val sessionId: String? = null
)


