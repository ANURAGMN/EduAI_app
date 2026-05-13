package com.ncert7.aitutorandlab.ui.screens.chatbotscreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * ConceptMapResourceContent - Displays a concept map resource
 */
@Composable
fun ConceptMapResourceContent(
    json: String,
    currentAudioTime: Float,
    isAudioPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        ConceptMapModel(
            json = json,
            currentAudioTime = currentAudioTime,
            isAudioPlaying = isAudioPlaying
        )
    }
}