package com.anurag.eduai.ui.models

import com.anurag.eduai.domain.progress.model.ProgressStatus

/**
 * UI Model for Concept data
 */
data class ConceptUiModel(
    val id: String,
    val name: String,
    val order: Int,
    val status: ProgressStatus,
    val type: String = "STUDY",
    val simulationId: String? = null,
    val simulationUrl: String? = null
)


