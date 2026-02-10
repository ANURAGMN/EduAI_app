package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.LocalDimensions

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