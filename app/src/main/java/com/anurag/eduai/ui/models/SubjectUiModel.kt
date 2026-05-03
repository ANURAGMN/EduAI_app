package com.anurag.eduai.ui.models

import androidx.compose.ui.graphics.Color

/**
 * UI Model for Subject data
 */
data class SubjectUiModel(
    val id: String,
    val name: String,
    val color: Color,
    val totalChapters: Int,
    val iconUrl: String? = null

)
