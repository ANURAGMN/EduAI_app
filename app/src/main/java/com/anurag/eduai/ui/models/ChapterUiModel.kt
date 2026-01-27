package com.anurag.eduai.ui.models

/**
 * UI Model for Chapter data
 */
data class ChapterUiModel(
    val id: String,
    val name: String,
    val totalConcepts: Int,
    val completedConcepts: Int,
    val status: ChapterStatus
)

/**
 * Chapter completion status for UI
 */
enum class ChapterStatus {
    COMPLETED,
    IN_PROGRESS,
    NOT_STARTED
}

