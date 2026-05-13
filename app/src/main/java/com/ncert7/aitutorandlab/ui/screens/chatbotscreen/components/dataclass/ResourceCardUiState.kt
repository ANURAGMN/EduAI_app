package com.ncert7.aitutorandlab.ui.screens.chatbotscreen.components.dataclass

/**
 * Sealed class resource card.
 */
sealed class ResourceCardUiState {

    /**
     * Resource card is hidden
     */
    object Hidden : ResourceCardUiState()

    /**
     * Displaying an image resource
     */
    data class Image(
        val imageUrl: String,
        val description: String?,
        val remainingSeconds: Int,
        val totalSeconds: Int
    ) : ResourceCardUiState()

    /**
     * Displaying a concept map resource
     */
    data class ConceptMap(
        val json: String,
        val audioProgress: Float,
        val isAudioPlaying: Boolean,
        val remainingSeconds: Int,
        val totalSeconds: Int
    ) : ResourceCardUiState()
}
