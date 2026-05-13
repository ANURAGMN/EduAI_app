package com.ncert7.aitutorandlab.ui.screens.subjectscreen.dataclass

import com.ncert7.aitutorandlab.ui.models.SubjectUiModel

data class SubjectScreenState(
    val subjects: List<SubjectUiModel> = emptyList(),
    val classLevel: Int = 7,
    val isLoading: Boolean = false,
    val error: String? = null
)
