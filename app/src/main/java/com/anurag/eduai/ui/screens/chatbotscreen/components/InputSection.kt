package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.IconPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatUiState
import com.anurag.eduai.ui.viewModel.SpeechToText

/**
 * Comprehensive input section that handles:
 * - Auto-suggestions display
 * - Text input field with image, mic, and send buttons
 * - Listening overlay for speech-to-text
 */
@Composable
fun InputSection(
    chatState: ChatUiState,
    sttState: SpeechToText.STTState,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onSpeakClick: () -> Unit,
    onStopListening: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    shouldDisableSend: Boolean = false,
    showImageIcon: Boolean = true,
    onImagePickerClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Auto-suggestions
        val shouldShowAutosuggestions = !sttState.isListening &&
                chatState.showAutosuggestions &&
                chatState.inputText.isEmpty() &&
                !chatState.isLoading

        if (shouldShowAutosuggestions) {
            AutoSuggestionChips(
                suggestions = chatState.autosuggestions,
                visible = true,
                onSuggestionClick = onSuggestionClick
            )
        }
        //input field
        if (!sttState.isListening) {
            InputField(
                textValue = chatState.inputText,
                onTextChange = onTextChange,
                onSpeakClick = onSpeakClick,
                onSendClick = onSendClick,
                shouldDisableSend = shouldDisableSend,
                showImageIcon = showImageIcon,
                onImagePickerClick = onImagePickerClick ?: {}
            )
        } else {
            ListeningOverlay(
                text = sttState.resultText,
                amplitude = sttState.audioAmplitude,
                statusMessage = sttState.statusMessage,
                onStopClick = onStopListening
            )
        }
    }
}

/**
 * Input field component with image, text input, mic, and send buttons
 */
@Composable
private fun InputField(
    textValue: String,
    onTextChange: (String) -> Unit,
    onSpeakClick: () -> Unit,
    onSendClick: () -> Unit,
    shouldDisableSend: Boolean = false,
    showImageIcon: Boolean = true,
    onImagePickerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    val dimens = LocalDimensions.current
    val hasText = textValue.isNotBlank()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Determine if send should be enabled
    val canSend = hasText && !shouldDisableSend

    // Row layout with image icon, text field, and action buttons
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimens.inputPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Text Input Field
        TextField(
            value = textValue,
            shape = RoundedCornerShape(dimens.inputRadius),
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .border(
                    shape = RoundedCornerShape(dimens.inputRadius),
                    width = dimens.inputBorderWidth,
                    color = AccentBlue
                ),
            placeholder = {
                Text(
                    text = stringResource(R.string.type_or_speak),
                    color = TextPrimary
                )
            },
            leadingIcon = {
                // Leading Icon - Attach Image
                if (showImageIcon) {
                    IconButton(
                        onClick = onImagePickerClick,
                        modifier = Modifier.size(dimens.iconMedium),
                        enabled = !shouldDisableSend
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = stringResource(R.string.attach_image),
                            tint = if (shouldDisableSend) IconPrimary.copy(alpha = 0.5f) else IconPrimary,
                            modifier = Modifier.size(dimens.iconMedium)
                        )
                    }
                }
                },
            trailingIcon ={
                if (hasText) {
                    // Send Icon
                    IconButton(
                        onClick = {
                            if (canSend) {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onSendClick()
                            }
                        },
                        enabled = canSend,
                        modifier = Modifier.size(dimens.iconMedium)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.send_message),
                            tint = if (canSend) HeaderGradientStart else Color.Gray.copy(alpha = 0.5f),
                        )
                    }
                } else {
                    // Mic Icon - disable mic during AI response
                    IconButton(
                        onClick = onSpeakClick,
                        enabled = !shouldDisableSend,
                        modifier = Modifier.size(dimens.iconMedium)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = stringResource(R.string.start_listening),
                            tint = if (shouldDisableSend) IconPrimary.copy(alpha = 0.5f) else IconPrimary,
                        )
                    }
                }
                          },
            keyboardOptions = KeyboardOptions(
                imeAction = if (canSend) {
                    ImeAction.Send
                } else {
                    ImeAction.Default
                }
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (canSend) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onSendClick()
                    }
                }
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                disabledContainerColor = White.copy(alpha = 0.9f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                disabledTextColor = TextPrimary.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}


@Preview
@Composable
fun InputSectionPreview() {
    InputField(
        textValue = "",
        onTextChange = {},
        onSpeakClick = {},
        onSendClick = {},
        shouldDisableSend = false,
        showImageIcon = true,
        onImagePickerClick = {}
    )
}
