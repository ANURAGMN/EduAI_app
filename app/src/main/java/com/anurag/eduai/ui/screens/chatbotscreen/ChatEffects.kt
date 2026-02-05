package com.anurag.eduai.ui.screens.chatbotscreen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.screens.chatbotscreen.dataclass.ChatUiState
import com.anurag.eduai.ui.screens.chatbotscreen.dataclass.ResourceCardUiState
import com.anurag.eduai.ui.viewModel.ChatViewModel
import com.anurag.eduai.ui.viewModel.SpeechToText
import com.anurag.eduai.ui.viewModel.TextToSpeech
import kotlinx.coroutines.delay

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
            chatViewModel.markUserActive()
            chatViewModel.hideAutosuggestions()
            // Stop TTS when user starts listening
            if (ttsState.isSpeaking) {
                ttsController.stop()
            }
        } else {
            if (sttState.resultText.isNotEmpty() && sttState.resultText != lastProcessedSpeechText) {
                chatViewModel.updateInputText(sttState.resultText)
                onSpeechTextProcessed(sttState.resultText)
            }
            chatViewModel.markUserInactive()
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
                chatViewModel.startIdleTimer()
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

