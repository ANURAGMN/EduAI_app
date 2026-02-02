package com.anurag.eduai.ui.screens.chapterscreen.dataclass

import com.anurag.eduai.ui.models.ChapterUiModel

/**
 * UI State for Chapter Screen
 */
data class ChapterUiState(
    val isLoading: Boolean = false,
    val classLevel: Int = 7,
    val subjectName: String = "",
    val chapters: List<ChapterUiModel> = emptyList(),
    val error: String? = null
)