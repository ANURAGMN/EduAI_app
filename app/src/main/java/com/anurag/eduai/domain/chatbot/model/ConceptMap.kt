package com.anurag.eduai.domain.chatbot.model

data class ConceptMap(
    val success: Boolean,
    val json: String? = null,
    val generationTimeMs: Long = 0,
    val isDefault: Boolean = false
)