package com.ncert7.aitutorandlab.ui.screens.chapterscreen.dataclass

import com.ncert7.aitutorandlab.ui.models.ChapterUiModel

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