package com.anurag.eduai.ui.models

/**
 * UI Model for Concept data
 */
data class ConceptUiModel(
    val id: String,
    val name: String,
    val order: Int,
    val status: ConceptStatus
)

/**
 * Concept completion status for UI
 */
enum class ConceptStatus {
    COMPLETED,
    IN_PROGRESS,
    NOT_STARTED
}

