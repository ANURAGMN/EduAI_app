package com.anurag.eduai.ui.screens.conceptscreen.dataclass

import com.anurag.eduai.ui.models.ChapterProgressUiModel
import com.anurag.eduai.ui.models.ConceptUiModel


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
    val classLevel: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
