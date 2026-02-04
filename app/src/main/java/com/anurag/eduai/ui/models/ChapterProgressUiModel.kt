package com.anurag.eduai.ui.models

/**
 * UI Model for Chapter Progress
 */
data class ChapterProgressUiModel(
    val completed: Int,
    val total: Int,
    val progressFraction: Float,   // 0f..1f
    val progressPercentage: Int,   // 0..100
    val remaining: Int
)