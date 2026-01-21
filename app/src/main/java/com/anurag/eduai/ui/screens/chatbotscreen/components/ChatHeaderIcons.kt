package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.IconPrimary
import com.anurag.eduai.ui.theme.IconSecondary

/**
 * Header icons row for the chat screen
 * Handles Kannada toggle, TTS controls, and settings menu
 */
@Composable
fun ChatHeaderIcons(
    isKannada: Boolean,
    isSpeaking: Boolean,
    showResourceCard: Boolean,
    ttsPausedForResource: Boolean,
    showSettingsMenu: Boolean,
    onKannadaToggle: () -> Unit,
    onVolumeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    settingsContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 0.dp, end = 0.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kannada Toggle
        IconButton(onClick = onKannadaToggle) {
            Icon(
                imageVector = Icons.Default.ClosedCaption,
                contentDescription = if (isKannada) "Kannada Enabled" else "Kannada Disabled",
                tint = if (isKannada) IconPrimary else IconSecondary
            )
        }

        // Volume/TTS Control
        IconButton(onClick = onVolumeClick) {
            Icon(
                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (isSpeaking) "Stop" else "Play",
                tint = when {
                    isSpeaking -> IconPrimary
                    showResourceCard && ttsPausedForResource -> IconSecondary.copy(alpha = 0.5f)
                    else -> IconSecondary
                }
            )
        }

        // Settings Menu
        Box {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = stringResource(R.string.settings),
                    tint = Color.Gray.copy(alpha = 0.6f)
                )
            }
            if (showSettingsMenu) {
                settingsContent()
            }
        }
    }
}

