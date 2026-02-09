package com.anurag.eduai.domain.chatbot.usecase

sealed interface ChatIntent {
    data class Initialize(val userId: String) : ChatIntent
    data class UpdateInputText(val text: String) : ChatIntent
    data class SetStudentLevel(val level: String) : ChatIntent
    data class SetKannada(val enabled: Boolean) : ChatIntent
    data class SelectConcept(val concept: String) : ChatIntent
    data class SendMessage(val message: String) : ChatIntent
    data class TapAutosuggestion(val suggestion: String) : ChatIntent
    data class StartFreshSession(val concept: String) : ChatIntent
    data class HasExistingSession(val concept: String) : ChatIntent
    object StartIdleTimer : ChatIntent
    object HideAutosuggestions : ChatIntent
    object MarkUserActive : ChatIntent
    object MarkUserInactive : ChatIntent
    object RefreshConcepts : ChatIntent
    object DismissResource : ChatIntent
    object ResumeTTS : ChatIntent
}

