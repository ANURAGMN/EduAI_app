package com.anurag.eduai.ui.viewModel

import com.anurag.eduai.data.local.entities.ChapterEntity
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.data.local.entities.ProgressEntity

/**
 * Data class combining Concept + Progress for UI display
 */
data class ConceptWithProgress(
    val concept: ConceptEntity,
    val progress: ProgressEntity?,
    val status: String = "NOT_STARTED"
)

/**
 * Data class combining Chapter + Progress for UI display
 */
data class ChapterWithProgress(
    val chapter: ChapterEntity,
    val progress: ProgressEntity?,
    val status: String = "NOT_STARTED"
)