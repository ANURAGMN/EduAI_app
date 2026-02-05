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
import com.anurag.eduai.ui.theme.LocalDimensions

/**
 * Header icons row for the chat screen
 * Handles Kannada toggle, TTS controls, and settings menu
 */
@Composable
fun ChatHeaderIcons(
    isKannada: Boolean,
    isSpeaking: Boolean,
    showSettingsMenu: Boolean,
    onKannadaToggle: () -> Unit,
    onVolumeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    settingsContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens= LocalDimensions.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = dimens.spaceSmall),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kannada Toggle
        IconButton(onClick = onKannadaToggle) {
            Icon(
                imageVector = Icons.Default.ClosedCaption,
                contentDescription = if (isKannada) stringResource(R.string.kannada_enabled) else stringResource(
                    R.string.kannada_disabled
                ),
                tint = if (isKannada) IconPrimary else IconSecondary
            )
        }

        // Volume/TTS Control
        IconButton(onClick = onVolumeClick) {
            Icon(
                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription =
                    if (isSpeaking)
                        stringResource(R.string.stop)
                    else
                        stringResource(R.string.play
                    ),
                tint = when {
                    isSpeaking -> IconPrimary
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
                    tint = when {
                        showSettingsMenu -> IconPrimary
                        else -> IconSecondary
                    }
                )
            }
            if (showSettingsMenu) {
                settingsContent()
            }
        }
    }
}

