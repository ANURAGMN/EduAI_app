package com.anurag.eduai.ui.screens.chatbotscreen.components

import android.Manifest
import android.webkit.WebView
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.ChatUiState
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
    avatarPadding: Dp,
    chatState: ChatUiState,
    lastAIMessage: ChatMessageModel?,
    ttsController: TextToSpeech,
    inputSectionHeight: Dp,
    onDismissResource: () -> Unit,
    onResourceTimerComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens=LocalDimensions.current
    Column(modifier = modifier.fillMaxSize()) {
        // Avatar at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = avatarPadding + dimens.spaceMedium),
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

        Spacer(Modifier.height(16.dp))

        // Content area
        ChatContentArea(
            showResourceCard = chatState.showResourceCard,
            currentResource = chatState.currentResource,
            resourceDisplayMode = chatState.resourceDisplayMode,
            isLoading = chatState.isLoading,
            lastAIMessage = lastAIMessage,
            isTyping = chatState.isTyping,
            typingText = chatState.typingText,
            ttsController = ttsController,
            onDismissResource = onDismissResource,
            onResourceTimerComplete = onResourceTimerComplete,
            inputSectionHeight = inputSectionHeight,
            modifier = Modifier.weight(1f)
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
        chatViewModel.initialize(userId)
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
                DebugLogger.debugLog("ChatbotScreen", "TTS started: ${textToSpeak.take(50)}...")
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
    LaunchedEffect(chatState.showResourceCard) {
        if (chatState.showResourceCard && ttsState.isSpeaking) {
            ttsController.stop()
        }
    }

    // Handle speech recognition
    LaunchedEffect(sttState.isListening) {
        if (sttState.isListening) {
            chatViewModel.markUserActive()
            chatViewModel.hideAutosuggestions()
        } else {
            if (sttState.resultText.isNotEmpty() && sttState.resultText != lastProcessedSpeechText) {
                chatViewModel.updateInputText(sttState.resultText)
                onSpeechTextProcessed(sttState.resultText)
            }
            chatViewModel.markUserInactive()
        }
    }

    // Start idle timer 5 seconds AFTER TTS completes
    LaunchedEffect(ttsState.isSpeaking) {
        if (!ttsState.isSpeaking && chatState.messages.isNotEmpty()) {
            // TTS just stopped (or never started), wait 5 seconds then check conditions
            DebugLogger.debugLog("ChatbotScreen", " TTS stopped! Waiting 5 seconds before checking conditions...")
            delay(5000L)

            // Only start timer if conditions are still met
            DebugLogger.debugLog("ChatbotScreen", """
                ═══════════════════════════════════════════════════════
                IDLE TIMER CHECK (after 5s delay post-TTS)
                ═══════════════════════════════════════════════════════
                !ttsState.isSpeaking: ${!ttsState.isSpeaking}
                messages.isNotEmpty(): ${chatState.messages.isNotEmpty()}
                inputText.isEmpty(): ${chatState.inputText.isEmpty()}
                !isUserActive: ${!chatState.isUserActive}
                autosuggestions.size: ${chatState.autosuggestions.size}
                ═══════════════════════════════════════════════════════
            """.trimIndent())

            if (!ttsState.isSpeaking && chatState.messages.isNotEmpty() &&
                chatState.inputText.isEmpty() && !chatState.isUserActive) {
                DebugLogger.debugLog("ChatbotScreen", " Calling startIdleTimer()")
                chatViewModel.startIdleTimer()
            } else {
                DebugLogger.debugLog("ChatbotScreen", " NOT calling startIdleTimer() - conditions not met")
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

