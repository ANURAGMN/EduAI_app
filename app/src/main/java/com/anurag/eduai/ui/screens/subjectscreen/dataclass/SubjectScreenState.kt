package com.anurag.eduai.ui.screens.subjectscreen.dataclass

import com.anurag.eduai.ui.models.SubjectUiModel

data class SubjectScreenState(
    val subjects: List<SubjectUiModel> = emptyList(),
    val classLevel: Int = 7,
    val isLoading: Boolean = false,
    val error: String? = null
)
