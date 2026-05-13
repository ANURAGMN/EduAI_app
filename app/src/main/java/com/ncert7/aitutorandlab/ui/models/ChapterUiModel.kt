package com.ncert7.aitutorandlab.ui.models

import com.ncert7.aitutorandlab.domain.progress.model.ProgressStatus

/**
 * UI Model for Chapter data
 */
data class ChapterUiModel(
    val id: String,
    val name: String,
    val englishName: String,
    val orderIndex: Int,
    val totalConcepts: Int,
    val completedConcepts: Int,
    val status: ProgressStatus
)

