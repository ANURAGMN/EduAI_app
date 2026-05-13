package com.ncert7.aitutorandlab.domain.chatbot.model

data class HighlightResult(
        val displayText: String,
        val boldRanges: List<IntRange>,
        val highlightRange: IntRange?
    )