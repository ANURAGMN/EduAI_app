package com.anurag.eduai.ui.screens.simlation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.IconPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.ui.viewModel.SpeechToText

@Composable
fun SimInputSection(
    inputText: String,
    sttState: SpeechToText.STTState,
    isSessionComplete: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onSpeakClick: () -> Unit,
    onStopListening: () -> Unit,
    onSizeChanged: (IntSize) -> Unit = {}
) {
    val dimens = LocalDimensions.current

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = White,
        shadowElevation = dimens.cardElevation,
        tonalElevation = dimens.cardElevation,
        shape = RoundedCornerShape(
            topStart = dimens.cornerRadiusLarge,
            topEnd = dimens.cornerRadiusLarge
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged(onSizeChanged)
        ) {
            if (!sttState.isListening) {
                InputField(
                    textValue = inputText,
                    isEnabled = !isSessionComplete && !isLoading,
                    onTextChange = onTextChange,
                    onSpeakClick = onSpeakClick,
                    onSendClick = onSendClick
                )
            } else {
                SimListeningOverlay(
                    text = sttState.resultText,
                    amplitude = sttState.audioAmplitude,
                    onStopClick = onStopListening
                )
            }
        }
    }
}

@Composable
private fun InputField(
    textValue: String,
    isEnabled: Boolean,
    onTextChange: (String) -> Unit,
    onSpeakClick: () -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hasText = textValue.isNotEmpty()
    val dimens = LocalDimensions.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimens.spaceMedium,
                vertical = dimens.messagePadding
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.cornerRadiusLarge))
                .background(Color.White)
                .border(
                    shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                    width = dimens.inputBorderWidth,
                    color = AccentBlue
                )
                .padding(
                    horizontal = dimens.spaceExtraSmall,
                    vertical = dimens.spaceExtraSmall
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = textValue,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = dimens.messagePadding),
                placeholder = {
                    Text(
                        text = stringResource(R.string.sim_type_or_speak),
                        color = TextPrimary.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                singleLine = true,
                enabled = isEnabled,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledTextColor = Color.Gray,
                    cursorColor = HeaderGradientStart
                ),
                interactionSource = interactionSource
            )

            AnimatedContent(
                targetState = hasText,
                transitionSpec = {
                    fadeIn(animationSpec = tween(150)) togetherWith
                            fadeOut(animationSpec = tween(150))
                },
                modifier = Modifier.padding(end = dimens.spaceSmall),
                label = "icon_animation"
            ) { showSend ->
                if (showSend) {
                    IconButton(
                        onClick = onSendClick,
                        enabled = isEnabled,
                        modifier = Modifier.size(dimens.buttonHeightSmall)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.sim_send_message),
                            tint = if (isEnabled) HeaderGradientStart else Color.Gray,
                            modifier = Modifier.size(dimens.iconMedium)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onSpeakClick,
                        enabled = isEnabled,
                        modifier = Modifier.size(dimens.buttonHeightSmall)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = stringResource(R.string.sim_start_listening),
                            tint = if (isEnabled) IconPrimary else Color.Gray,
                            modifier = Modifier.size(dimens.iconMedium)
                        )
                    }
                }
            }
        }
    }
}