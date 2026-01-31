package com.anurag.eduai.ui.screens.chatbotscreen

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.R
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.chatbotscreen.components.AppDialog
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatBotSettings
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatBotSettingsState
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatEffects
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatHeaderIcons
import com.anurag.eduai.ui.screens.chatbotscreen.components.ConversationView
import com.anurag.eduai.ui.screens.chatbotscreen.components.InitialAvatarView
import com.anurag.eduai.ui.screens.chatbotscreen.components.InputSection
import com.anurag.eduai.ui.screens.chatbotscreen.components.LogOverlay
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.ui.viewModel.ChatUiState
import com.anurag.eduai.ui.viewModel.ChatViewModel
import com.anurag.eduai.ui.viewModel.SpeechToText
import com.anurag.eduai.ui.viewModel.TextToSpeech
import com.anurag.eduai.ui.viewModel.isConversationStarted
import com.anurag.eduai.ui.viewModel.lastAiMessage

@Composable
fun ChatbotScreen(
    chatViewModel: ChatViewModel = viewModel(),
    ttsController: TextToSpeech = viewModel(),
    sttController: SpeechToText = viewModel(),
) {
    // Track screen analytics
    TrackScreenEvent(ScreenName.CHATBOT)

    val context = LocalContext.current

    // State collectors - using consolidated UI state
    val chatState by chatViewModel.uiState.collectAsState()
    val ttsState by ttsController.state.collectAsState()
    val sttState by sttController.state.collectAsState()

    // Local UI state
    var permissionGranted by remember { mutableStateOf(false) }
    var lastProcessedSpeechText by remember { mutableStateOf("") }
    var showSessionResumeDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var pendingConceptSelection by remember { mutableStateOf<String?>(null) }

    //settings state
    var settingsState by remember { mutableStateOf(ChatBotSettingsState()) }

    val lastAIMessage = chatState.lastAiMessage
    val isConversationStarted = chatState.isConversationStarted

    val voiceOptions = remember(ttsState.availableVoices, chatState.currentLanguage, settingsState.selectedAvatar) {
        ttsController.getFilteredVoiceOptions(chatState.currentLanguage, settingsState.selectedAvatar)
    }

    val displayedVoiceName = remember(ttsState.selectedVoice, chatState.currentLanguage, settingsState.selectedAvatar) {
        ttsState.selectedVoice?.let { ttsController.formatVoiceName(it) }
            ?: ttsController.getDefaultVoiceName(chatState.currentLanguage, settingsState.selectedAvatar)
    }

    val aiMessageOutput = remember(chatState.isTyping, chatState.typingText, lastAIMessage) {
        when {
            chatState.isTyping -> chatState.typingText
            else -> lastAIMessage?.content ?: ""
        }
    }

    // Animation values
    val avatarSize by animateDpAsState(
        targetValue = if (isConversationStarted) 100.dp else 180.dp,
        label = "avatarSize"
    )

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        sttController.handlePermissionResult(
            SpeechToText.RECORD_AUDIO_PERMISSION_REQUEST,
            if (isGranted) intArrayOf(PackageManager.PERMISSION_GRANTED)
            else intArrayOf(PackageManager.PERMISSION_DENIED)
        )
    }

    // Effects
    ChatEffects(
        chatViewModel = chatViewModel,
        ttsController = ttsController,
        sttController = sttController,
        chatState = chatState,
        ttsState = ttsState,
        sttState = sttState,
        permissionLauncher = permissionLauncher,
        onPermissionGranted = { permissionGranted = it },
        onSpeechTextProcessed =  {lastProcessedSpeechText = it} ,
        lastProcessedSpeechText = lastProcessedSpeechText
    )

        // Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = White,
                contentColor = White,
                bottomBar = {
                    InputSection(
                        chatState = chatState,
                        sttState = sttState,
                        onTextChange = { chatViewModel.updateInputText(it) },
                        onSendClick = {
                            if (chatState.inputText.isNotBlank()) {
                                chatViewModel.hideAutosuggestions()
                                chatViewModel.sendMessage(chatState.inputText, context)
                                chatViewModel.updateInputText("")
                            }
                        },
                        onSpeakClick = {
                            chatViewModel.hideAutosuggestions()
                            chatViewModel.markUserActive()
                            if (permissionGranted && sttState.isInitialized) {
                                sttController.startListening("en-IN")
                            } else if (!permissionGranted) {
                                permissionLauncher.launch(RECORD_AUDIO)
                            }
                        },
                        onStopListening = { sttController.stopListening() },
                        onSuggestionClick = { suggestion ->
                            chatViewModel.tapAutosuggestion(suggestion, context)
                            chatViewModel.hideAutosuggestions()
                        },
                        modifier = Modifier
                            .imePadding()
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Header icons (settings, tts icon, kannada toggle)
                    ChatHeaderIcons(
                        isKannada = chatState.isKannada,
                        isSpeaking = ttsState.isSpeaking,
                        showResourceCard = chatState.showResourceCard,
                        ttsPausedForResource = chatState.ttsPausedForResource,
                        showSettingsMenu = showSettingsMenu,
                        onKannadaToggle = { chatViewModel.setKannada(!chatState.isKannada) },
                        onVolumeClick = {
                            handleVolumeClick(
                                chatState = chatState,
                                ttsState = ttsState,
                                lastAIMessage = lastAIMessage,
                                chatViewModel = chatViewModel,
                                ttsController = ttsController
                            )
                        },
                        onSettingsClick = { showSettingsMenu = !showSettingsMenu },
                        settingsContent = {
                            ChatBotSettings(
                                expanded = true,
                                onDismiss = { showSettingsMenu = false },
                                state = settingsState.copy(
                                    voiceOptions = voiceOptions,
                                    displayedVoiceName = displayedVoiceName,
                                    availableConcepts = chatState.availableConcepts,
                                    selectedConcept = chatState.selectedConcept,
                                    isLoadingConcepts = chatState.availableConcepts.isEmpty()
                                ),
                                onAvatarChange = { avatarCode ->
                                    settingsState = settingsState.copy(selectedAvatar = avatarCode)
                                    ttsController.switchCharacter(avatarCode)
                                    if (avatarCode != "disable") {
                                        ttsController.applyDefaultsForAvatarLanguage(avatarCode, chatState.currentLanguage)
                                    } else if (ttsState.isSpeaking) {
                                        ttsController.stop()
                                    }
                                },
                                onVoiceChange = { selectedDisplayName ->
                                    handleVoiceChange(selectedDisplayName, ttsState, ttsController, aiMessageOutput)
                                },
                                onConceptChange = { concept ->
                                    pendingConceptSelection = concept
                                    if (chatViewModel.hasExistingSession(concept, context)) {
                                        showSessionResumeDialog = true
                                    } else {
                                        chatViewModel.selectConcept(concept, context)
                                        showSettingsMenu = false
                                    }
                                },
                                onLevelChange = { levelCode ->
                                    settingsState = settingsState.copy(selectedStudentLevel = levelCode)
                                    chatViewModel.setStudentLevel(levelCode)
                                },
                                onSpeedChange = { label ->
                                    settingsState = settingsState.copy(selectedSpeed = label)
                                    handleSpeedChange(label, ttsController, ttsState, aiMessageOutput)
                                }
                            )
                        }
                    )
                    if (!isConversationStarted) {
                        // Initial centered avatar
                        InitialAvatarView(
                            avatarSize = avatarSize,
                            ttsController = ttsController,
                            modifier = Modifier.weight(0.1f).background(White)
                        )
                    } else {
                        // Conversation view with avatar and scrollable content
                        ConversationView(
                            avatarSize = avatarSize,
                            chatState = chatState,
                            lastAIMessage = lastAIMessage,
                            ttsController = ttsController,
                            onDismissResource = { chatViewModel.dismissResourceCard() },
                            onResourceTimerComplete = {
                                DebugLogger.debugLog("ChatViewModel", "Resource card timer completed")
                            },
                            modifier = Modifier.weight(0.1f).background(White)
                        )
                    }
                }
            }
            // Debug LogOverlay
            LogOverlay(
                metadata = chatState.agentMetadata,
                conceptMapStatus = chatState.conceptMapStatus,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
    }


    // Session resume dialog
    AppDialog(
        show = showSessionResumeDialog,
        title = stringResource(R.string.existing_session_found),
        message = stringResource(R.string.resume_or_start_fresh),
        confirmText = stringResource(R.string.continue_session),
        dismissText = stringResource(R.string.start_new),
        onConfirm = {
            pendingConceptSelection?.let { chatViewModel.selectConcept(it, context) }
            showSessionResumeDialog = false
            pendingConceptSelection = null
            showSettingsMenu = false
        },
        onDismiss = {
            pendingConceptSelection?.let { chatViewModel.startFreshSession(it, context) }
            showSessionResumeDialog = false
            pendingConceptSelection = null
            showSettingsMenu = false
        }
    )
}

// Helper functions

/**
 * volume Click function check
 * 1. If resource card is showing and TTS was paused for resource, resume TTS for resource
 * 2. If TTS is currently speaking, stop it
 * 3. Else, speak the last AI message
 */
private fun handleVolumeClick(
    chatState: ChatUiState,
    ttsState: TextToSpeech.TTSState,
    lastAIMessage: ChatMessageModel?,
    chatViewModel: ChatViewModel,
    ttsController: TextToSpeech
) {
    when {
        chatState.showResourceCard && chatState.ttsPausedForResource -> {
            chatViewModel.resumeTTSForResource()
            lastAIMessage?.let { ttsController.speak(it.content) }
        }
        ttsState.isSpeaking -> ttsController.stop()
        else -> lastAIMessage?.let { ttsController.speak(it.content) }
    }
}

/**
 * Handle voice change from settings
 * 1. it do the voice change
 * 2. if TTS is speaking, stop and restart with new voice
 * 3. if no voice found, do nothing
 */
private fun handleVoiceChange(
    selectedDisplayName: String,
    ttsState: TextToSpeech.TTSState,
    ttsController: TextToSpeech,
    aiMessageOutput: String
) {
    ttsState.availableVoices.find { ttsController.formatVoiceName(it) == selectedDisplayName }?.let { voice ->
        ttsController.setVoice(voice)
        if (ttsState.isSpeaking) {
            ttsController.stop()
            ttsController.speak(aiMessageOutput)
        }
    }
}

/**
 * this function handle speed change from settings
 * 1. it do the speed change
 * 2. if TTS is speaking, stop and restart with new speed
 * 3. if no speed found, set to default 0.75x
 */
private fun handleSpeedChange(
    label: String,
    ttsController: TextToSpeech,
    ttsState: TextToSpeech.TTSState,
    aiMessageOutput: String
) {
    val speed = when (label) {
        "0.75x" -> 0.75f
        "1.0x" -> 1.0f
        "1.25x" -> 1.25f
        "1.5x" -> 1.5f
        else -> 0.75f
    }
    ttsController.setSpeechRate(speed)
    if (ttsState.isSpeaking) {
        ttsController.stop()
        ttsController.speak(aiMessageOutput)
    }
}