package com.anurag.eduai.ui.screens.chatbotscreen.components

import android.Manifest
import android.content.pm.PackageManager
import android.webkit.WebView
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.domain.chatbot.usecase.ChatIntent
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatUiState
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ResourceCardUiState
import com.anurag.eduai.ui.viewModel.ChatViewModel
import com.anurag.eduai.ui.viewModel.SpeechToText
import com.anurag.eduai.ui.viewModel.TextToSpeech
import kotlinx.coroutines.delay

/**
 * Initial avatar view shown before conversation starts
 */
@Composable
fun InitialAvatarView(
    avatarSize: Dp,
    ttsController: TextToSpeech,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            shape = CircleShape,
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
        ) {
            AndroidView(
                factory = {
                    WebView(it).apply {
                        setBackgroundColor(0)
                        ttsController.setupWebView(this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Conversation view with avatar and content area
 */
@Composable
fun ConversationView(
    avatarSize: Dp,
    chatState: ChatUiState,
    lastAIMessage: ChatMessageModel?,
    ttsController: TextToSpeech,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current
    Column(modifier = modifier.fillMaxSize()) {
        // Avatar at top -
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = dimens.cardElevation),
                shape = CircleShape,
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
            ) {
                AndroidView(
                    factory = {
                        WebView(it).apply {
                            setBackgroundColor(0)
                            ttsController.setupWebView(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.height(dimens.spaceMedium))

        // Content area - This scrolls
        ChatContentArea(
            isLoading = chatState.isLoading,
            loadingResourceMessage = chatState.loadingResourceMessage,
            lastAIMessage = lastAIMessage,
            isTyping = chatState.isTyping,
            typingText = chatState.typingText,
            ttsController = ttsController,
            isResourceCardShowing = chatState.resourceCardState !is ResourceCardUiState.Hidden,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

/**
 * All LaunchedEffects consolidated in one place
 */
@Composable
fun ChatEffects(
    chatViewModel: ChatViewModel,
    ttsController: TextToSpeech,
    sttController: SpeechToText,
    chatState: ChatUiState,
    ttsState: TextToSpeech.TTSState,
    sttState: SpeechToText.STTState,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    onPermissionGranted: (Boolean) -> Unit,
    onSpeechTextProcessed: (String) -> Unit,
    lastProcessedSpeechText: String
){
    val context = LocalContext.current

    // Initialize controllers and check permissions
    LaunchedEffect(Unit) {
        val sharedPrefs = SharedPreferenceUtils(context)
        val userId = sharedPrefs.getUserId().toString()
        chatViewModel.onIntent(ChatIntent.Initialize(userId))
        sttController.initialize(context)
        ttsController.initialize(context)

        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        onPermissionGranted(hasPermission)

        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // TTS trigger - monitor chatState.shouldStartTTS changes
    LaunchedEffect(chatState.shouldStartTTS, ttsState.isInitialized) {
        if (chatState.shouldStartTTS && ttsState.isInitialized) {
            val textToSpeak = chatState.fullTextForTTS
            if (textToSpeak.isNotEmpty()) {
                if (ttsState.isSpeaking) {
                    ttsController.stop()
                    delay(50)
                }
                ttsController.speak(textToSpeak)
            }
        }
    }

    // Stop TTS on concept change
    LaunchedEffect(chatState.selectedConcept) {
        if (ttsState.isSpeaking) {
            ttsController.stop()
        }
    }

    // Stop TTS when resource card is shown
    LaunchedEffect(chatState.resourceCardState) {
        if (chatState.resourceCardState !is ResourceCardUiState.Hidden && ttsState.isSpeaking) {
            ttsController.stop()
        }
    }


    // Handle speech recognition
    LaunchedEffect(sttState.isListening) {
        if (sttState.isListening) {
            chatViewModel.onIntent(ChatIntent.MarkUserActive)
            chatViewModel.onIntent(ChatIntent.HideAutosuggestions)
            // Stop TTS when user starts listening
            if (ttsState.isSpeaking) {
                ttsController.stop()
            }
        } else {
            if (sttState.resultText.isNotEmpty() && sttState.resultText != lastProcessedSpeechText) {
                chatViewModel.onIntent(ChatIntent.UpdateInputText(sttState.resultText))
                onSpeechTextProcessed(sttState.resultText)
            }
            chatViewModel.onIntent(ChatIntent.MarkUserInactive)
        }
    }

    // Start idle timer AFTER everything completes (typing, TTS, and resource card if exists)
    LaunchedEffect(ttsState.isSpeaking, chatState.isLoading, chatState.isTyping, chatState.waitingForTTSToComplete, chatState.isUserActive, chatState.resourceCardState) {
        val isResourceCardShowing = chatState.resourceCardState !is ResourceCardUiState.Hidden

        // Only trigger if all agent message components are complete AND user is idle
        if (!ttsState.isSpeaking &&
            !chatState.isLoading &&
            !chatState.isTyping &&
            !chatState.waitingForTTSToComplete &&
            !chatState.isUserActive &&
            !isResourceCardShowing &&  // Wait for resource card to be dismissed
            chatState.messages.isNotEmpty()) {

            // All agent message components complete, check if we should start idle timer
            DebugLogger.debugLog("ChatScreenHelpers", """
                ═══════════════════════════════════════════════════════
                IDLE TIMER TRIGGER CONDITIONS MET
                ═══════════════════════════════════════════════════════
                !ttsState.isSpeaking: ${!ttsState.isSpeaking}
                !isLoading: ${!chatState.isLoading}
                !isTyping: ${!chatState.isTyping}
                !waitingForTTSToComplete: ${!chatState.waitingForTTSToComplete}
                !isUserActive: ${!chatState.isUserActive}
                !isResourceCardShowing: ${!isResourceCardShowing}
                messages.isNotEmpty(): ${chatState.messages.isNotEmpty()}
                inputText.isEmpty(): ${chatState.inputText.isEmpty()}
                autosuggestions.size: ${chatState.autosuggestions.size}
                ═══════════════════════════════════════════════════════
            """.trimIndent())

            // Start the idle timer which will show autosuggestions after 5s delay
            if (chatState.inputText.isEmpty() && chatState.autosuggestions.isNotEmpty()) {
                DebugLogger.debugLog("ChatScreenHelpers", " Starting idle timer (5s countdown)")
                chatViewModel.onIntent(ChatIntent.StartIdleTimer)
            } else {
                DebugLogger.debugLog("ChatScreenHelpers", " NOT starting timer - inputText: '${chatState.inputText}', suggestions: ${chatState.autosuggestions.size}")
            }
        }
    }

    // Cleanup STT
    DisposableEffect(Unit) {
        sttController.initialize(context)
        onDispose {
            sttController.destroy()
        }
    }
}
