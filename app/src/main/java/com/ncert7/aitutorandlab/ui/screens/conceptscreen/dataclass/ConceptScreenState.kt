package com.ncert7.aitutorandlab.ui.screens.conceptscreen.dataclass

import com.ncert7.aitutorandlab.ui.models.ChapterProgressUiModel
import com.ncert7.aitutorandlab.ui.models.ConceptUiModel


data class ConceptScreenState(
    val concepts: List<ConceptUiModel> = emptyList(),
    val chapterName: String = "",
    val chapterId: String = "",
    val progressUiModel: ChapterProgressUiModel = ChapterProgressUiModel(
        completed = 0,
        total = 0,
        progressFraction = 0f,
        progressPercentage = 0,
        remaining = 0
    ),
    val type :String = "",
    val subjectName: String = "",
    val classLevel: Int = 7,
    val isLoading: Boolean = false,
    val error: String? = null
)
