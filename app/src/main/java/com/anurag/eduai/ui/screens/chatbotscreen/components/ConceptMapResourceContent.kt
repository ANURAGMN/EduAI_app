package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ConceptMapResourceContent(
    json: String,
    currentAudioTime: Float,
    isAudioPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Render the concept map (it has its own zoom controls)
        ConceptMapModel(
            json = json,
            currentAudioTime = currentAudioTime,
            isAudioPlaying = isAudioPlaying
        )

    }
}