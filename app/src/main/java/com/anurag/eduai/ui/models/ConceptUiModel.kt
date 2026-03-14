package com.anurag.eduai.ui.models

/**
 * UI Model for Concept data
 */
data class ConceptUiModel(
    val id: String,
    val name: String,
    val order: Int,
    val status: ConceptStatus,
    val type: String = "study",
    val simulationUrl: String? = null,
    val simulationUrlKannada: String? = null,
    val simulationId: String? = null
)

/**
 * Concept completion status for UI
 */
enum class ConceptStatus {
    COMPLETED,
    IN_PROGRESS,
    NOT_STARTED
}

