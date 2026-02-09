package com.anurag.eduai.domain.chatbot.usecase

sealed class ResourceDecision {
    object None : ResourceDecision()

    data class ShowImage(
        val url: String,
        val description: String?
    ) : ResourceDecision()

    data class ShowConceptMap(
        val triggerText: String
    ) : ResourceDecision()
}
