package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.anurag.eduai.ui.viewModel.ChatUiState
import com.anurag.eduai.ui.viewModel.SpeechToText

/**
 * Comprehensive input section that handles:
 * - Auto-suggestions display
 * - Text input field
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
    modifier: Modifier = Modifier
) {

    // Inner Column has rounded corners and content
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
                onSendClick = onSendClick
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
 * Input field component
 */
@Composable
private fun InputField(
    textValue: String,
    onTextChange: (String) -> Unit,
    onSpeakClick: () -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val dimens = LocalDimensions.current
    val hasText = textValue.isNotBlank()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Text Input Field
    TextField(
        value = textValue,
        shape= RoundedCornerShape(dimens.inputRadius),
        onValueChange = onTextChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(dimens.inputPadding)
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
        trailingIcon = {
            if (hasText) {
                // Send Icon
                IconButton(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onSendClick()
                    },
                    modifier = Modifier.size(dimens.iconMedium)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.send_message),
                        tint = HeaderGradientStart,
                    )
                }
            } else {
                // Mic Icon
                IconButton(
                    onClick = onSpeakClick,
                    modifier = Modifier.size(dimens.iconMedium)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = stringResource(R.string.start_listening),
                        tint = IconPrimary,
                    )
                }
            }
        },
        // Better Keyboard Support
        keyboardOptions = KeyboardOptions(
            imeAction = if (hasText){
                ImeAction.Send
            }else {
                ImeAction.Default
            }
        ),
        keyboardActions = KeyboardActions(
            onSend = {
                if (hasText) {
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
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}


@Preview
@Composable
fun InputSectionPreview() {
    InputField(
        textValue = "",
        onTextChange = {},
        onSpeakClick = {},
        onSendClick = {}
    )
}