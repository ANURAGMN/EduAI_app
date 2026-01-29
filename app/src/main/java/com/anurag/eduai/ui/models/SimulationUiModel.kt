package com.anurag.eduai.ui.models

/**
 * UI model representing a simulation
 */
data class SimulationUiModel(
    val id: String,              // e.g., "unit_8_1"
    val title: String,           // e.g., "unit_8_1.html"
    val htmlFileName: String,    // e.g., "unit_8_1.html"
    val chapterId: String        // e.g., "8"
)