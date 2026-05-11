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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anurag.eduai.R
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.chatbotscreen.components.AppDialog
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatBotSettings
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatEffects
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatHeaderIcons
import com.anurag.eduai.ui.screens.chatbotscreen.components.ConversationView
import com.anurag.eduai.ui.screens.chatbotscreen.components.InitialAvatarView
import com.anurag.eduai.ui.screens.chatbotscreen.components.InputSection
import com.anurag.eduai.ui.screens.chatbotscreen.components.LogOverlay
import com.anurag.eduai.ui.screens.chatbotscreen.components.ResourcesCard
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatUiState
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ResourceCardUiState
import com.anurag.eduai.ui.screens.chatbotscreen.viewmodel.ChatViewModel
import com.anurag.eduai.ui.viewModel.SpeechToText
import com.anurag.eduai.ui.viewModel.TextToSpeech
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.isConversationStarted
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.lastAiMessage
import com.anurag.eduai.domain.chatbot.usecase.ChatIntent
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatBotSettingsState

@Composable
fun ChatbotScreen(
    conceptId: String? = null,
    chatViewModel: ChatViewModel = hiltViewModel(),
    ttsController: TextToSpeech = hiltViewModel(),
    sttController: SpeechToText = hiltViewModel()
) {
    // Debug logging
    com.anurag.eduai.debug.DebugLogger.debugLog("ChatbotScreen", "ChatbotScreen composable - conceptId: $conceptId")

    // Track screen analytics
    TrackScreenEvent(ScreenName.CHATBOT)

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

    val shouldDisableSend = remember(
        chatState.isTyping,
        chatState.isLoading,
        ttsState.isSpeaking,
        chatState.resourceCardState
    ) {
        chatState.isTyping ||
                chatState.isLoading ||
                ttsState.isSpeaking ||
                chatState.resourceCardState !is ResourceCardUiState.Hidden
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
        lastProcessedSpeechText = lastProcessedSpeechText,
        conceptId = conceptId,
        settingsState = settingsState,
        onSettingsStateUpdate = { settingsState = it },
        avatarBoyDisplayName = stringResource(R.string.boy),
        avatarGirlDisplayName = stringResource(R.string.girl),
        avatarDisableDisplayName = stringResource(R.string.disable)
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
                    onTextChange = { chatViewModel.onIntent(ChatIntent.UpdateInputText(it)) },
                    onSendClick = {
                        if (chatState.inputText.isNotBlank()) {
                            chatViewModel.onIntent(ChatIntent.HideAutosuggestions)
                            chatViewModel.onIntent(ChatIntent.SendMessage(chatState.inputText))
                            chatViewModel.onIntent(ChatIntent.UpdateInputText(""))
                        }
                    },
                    onSpeakClick = {
                        chatViewModel.onIntent(ChatIntent.HideAutosuggestions)
                        chatViewModel.onIntent(ChatIntent.MarkUserActive)
                        if (permissionGranted && sttState.isInitialized) {
                            val language = if (chatState.isKannada) "kn-IN" else "en-IN"
                            sttController.startListening(language)
                        } else if (!permissionGranted) {
                            permissionLauncher.launch(RECORD_AUDIO)
                        }
                    },
                    onStopListening = { sttController.stopListening() },
                    onSuggestionClick = { suggestion ->
                        chatViewModel.onIntent(ChatIntent.TapAutosuggestion(suggestion))
                        chatViewModel.onIntent(ChatIntent.HideAutosuggestions)
                    },
                    shouldDisableSend = shouldDisableSend,
                    showImageIcon = false,
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
                    isSpeaking = ttsState.isSpeaking,
                    showSettingsMenu = showSettingsMenu,
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
                        val boyDisplayName = stringResource(R.string.boy)
                        val girlDisplayName = stringResource(R.string.girl)

                        ChatBotSettings(
                            expanded = true,
                            onDismiss = { showSettingsMenu = false },
                            state = settingsState.copy(
                                voiceOptions = voiceOptions,
                                displayedVoiceName = displayedVoiceName,
                                availableConcepts = chatState.availableConcepts,
                                displayConcepts = chatState.displayConcepts,
                                selectedConcept = chatState.selectedConcept,
                                isLoadingConcepts = chatState.availableConcepts.isEmpty()
                            ),
                            onAvatarChange = { displayName ->
                                // Handle avatar change through ViewModel - receives display name
                                settingsState = chatViewModel.handleAvatarChange(
                                    displayName = displayName,
                                    boyDisplayName = boyDisplayName,
                                    girlDisplayName = girlDisplayName,
                                    ttsController = ttsController,
                                    currentState = settingsState
                                )
                            },
                            onVoiceChange = { selectedDisplayName ->
                                handleVoiceChange(selectedDisplayName, ttsState, ttsController, aiMessageOutput)
                            },
                            onConceptChange = { concept ->
                                pendingConceptSelection = concept
                                if (chatViewModel.hasExistingSession(concept)) {
                                    pendingConceptSelection = concept
                                    showSessionResumeDialog = true
                                } else {
                                    chatViewModel.onIntent(ChatIntent.SelectConcept(concept))
                                }
                            },
                            onLevelChange = { levelCode ->
                                settingsState = settingsState.copy(selectedStudentLevel = levelCode)
                                chatViewModel.onIntent(ChatIntent.SetStudentLevel(levelCode))
                            },
                            onSpeedChange = { label ->
                                settingsState = settingsState.copy(selectedSpeed = label)
                                handleSpeedChange(label, ttsController, ttsState, aiMessageOutput)
                            }
                        )
                    }
                )
                if (!isConversationStarted) {
                    // Initial centered avatar with loading indicator
                    InitialAvatarView(
                        avatarSize = avatarSize,
                        ttsController = ttsController,
                        modifier = Modifier.weight(0.1f).background(White),
                        isLoading = chatState.isLoading
                    )
                } else {
                    // Conversation view with avatar and scrollable content
                    ConversationView(
                        avatarSize = avatarSize,
                        chatState = chatState,
                        lastAIMessage = lastAIMessage,
                        ttsController = ttsController,
                        modifier = Modifier.weight(0.1f).background(White)
                    )
                }
            }
        }

        // Resource Card - centered on screen
        if (chatState.resourceCardState !is ResourceCardUiState.Hidden) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                ResourcesCard(
                    state = chatState.resourceCardState,
                    onDismiss = { chatViewModel.onIntent(ChatIntent.DismissResource) }
                )
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
            pendingConceptSelection?.let { chatViewModel.onIntent(ChatIntent.SelectConcept(it)) }
            showSessionResumeDialog = false
            pendingConceptSelection = null
            showSettingsMenu = false
        },
        onDismiss = {
            pendingConceptSelection?.let { chatViewModel.onIntent(ChatIntent.StartFreshSession(it)) }
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
        chatState.resourceCardState !is ResourceCardUiState.Hidden && chatState.ttsPausedForResource -> {
            chatViewModel.onIntent(ChatIntent.ResumeTTS)
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