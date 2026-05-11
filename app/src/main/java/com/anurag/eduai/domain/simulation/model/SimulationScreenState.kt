package com.anurag.eduai.domain.simulation.model

import com.anurag.eduai.data.remote.SimSessionResponse

/**
 * Consolidated UI state for the Simulation Agent screen.
 * Single source of truth for all UI rendering.
 * ViewModel exposes this as a single StateFlow for predictable rendering.
 */
data class SimulationScreenState(
    // Session State
    val isSessionStarted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Content State
    val currentTeacherMessage: String = "",
    val simulationUrls: List<String> = emptyList(),
    val showWebView: Boolean = false,
    val hasSimulationHTMLUrl: Boolean = true, // Track if htmlUrl exists

    // User Input State
    val userInput: String = "",
    val isInputEnabled: Boolean = true,

    // TTS State
    val shouldTriggerTts: Boolean = false,
    val hasSpokeCurrentMessage: Boolean = false,

    // Session Data
    val sessionData: SimSessionResponse? = null,

    // Language State
    val currentLanguage: String = ""
) {
    /**
     * Determine if the simulation view should show the "No simulation exist" message
     * This happens when we are in session but don't have valid simulation URLs
     */
    val shouldShowNoSimulationMessage: Boolean
        get() = isSessionStarted && !isLoading && simulationUrls.isEmpty() && !hasSimulationHTMLUrl
}
