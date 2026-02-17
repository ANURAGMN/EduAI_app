package com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass

data class ChatBotSettingsState(
    val selectedAvatar: String = "disable",
    val selectedSpeed: String = "0.75x",
    val selectedStudentLevel: String = "medium",
    val voiceOptions: List<String> = emptyList(),
    val displayedVoiceName: String = "",
    val availableConcepts: List<String> = emptyList(),//available concepts fetched from backend
    val displayConcepts: List<String> = emptyList(), //translated concept names
    val selectedConcept: String? = null,
    val isLoadingConcepts: Boolean = false
)
