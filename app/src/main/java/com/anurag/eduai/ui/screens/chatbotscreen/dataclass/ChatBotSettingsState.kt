package com.anurag.eduai.ui.screens.chatbotscreen.dataclass

data class ChatBotSettingsState(
    val selectedAvatar: String = "boy",
    val selectedSpeed: String = "0.75x",
    val selectedStudentLevel: String = "medium",
    val voiceOptions: List<String> = emptyList(),
    val displayedVoiceName: String = "",
    val availableConcepts: List<String> = emptyList(),
    val selectedConcept: String? = null,
    val isLoadingConcepts: Boolean = false
)