package com.anurag.eduai.ui.screens.mathagentscreen

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anurag.eduai.R
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.ScreenName
import kotlinx.coroutines.delay
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.chatbotscreen.components.AppDialog
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatEffects
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatHeaderIcons
import com.anurag.eduai.ui.screens.chatbotscreen.components.ConversationView
import com.anurag.eduai.ui.screens.chatbotscreen.components.InitialAvatarView
import com.anurag.eduai.ui.screens.chatbotscreen.components.InputSection
import com.anurag.eduai.ui.screens.chatbotscreen.components.LogOverlay
import com.anurag.eduai.ui.screens.chatbotscreen.components.ResourcesCard
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatMessageModel
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ResourceCardUiState
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatBotSettingsState
import com.anurag.eduai.ui.viewModel.SpeechToText
import com.anurag.eduai.ui.viewModel.TextToSpeech
import com.anurag.eduai.ui.screens.mathagentscreen.dataclass.isConversationStarted
import com.anurag.eduai.ui.screens.mathagentscreen.dataclass.lastAiMessage
import com.anurag.eduai.domain.chatbot.usecase.ChatIntent
import com.anurag.eduai.ui.screens.chatbotscreen.viewmodel.ChatViewModel
import com.anurag.eduai.ui.screens.mathagentscreen.components.MathBotSettings
import com.anurag.eduai.ui.screens.mathagentscreen.viewmodel.MathViewModel
import com.anurag.eduai.domain.mathagent.usecase.MathIntent
import com.anurag.eduai.ui.screens.mathagentscreen.dataclass.MathMessageModel

@Composable
fun MathAgentScreen(
    problemId: String? = null,
    chatViewModel: ChatViewModel = hiltViewModel(),
    ttsController: TextToSpeech = hiltViewModel(),
    sttController: SpeechToText = hiltViewModel(),
    mathViewModel: MathViewModel = hiltViewModel()
) {
    // Debug logging
    DebugLogger.debugLog("MathAgentScreen", "MathAgentScreen composable - problemId: $problemId")

    // Track screen analytics
    TrackScreenEvent(ScreenName.MATH_AGENT)

    // State collectors - using consolidated UI state
    val chatState by chatViewModel.uiState.collectAsState()
    val ttsState by ttsController.state.collectAsState()
    val sttState by sttController.state.collectAsState()
    val mathState by mathViewModel.uiState.collectAsState()
    val context = LocalContext.current
    // Local UI state
    var permissionGranted by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Settings state
    var settingsState by remember { mutableStateOf(ChatBotSettingsState()) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            DebugLogger.debugLog("MathAgentScreen", "Image selected: $it")
            mathViewModel.onIntent(MathIntent.SelectImage(it.toString()))
        }
    }

    // Convert math messages to chat messages for display
    val mathMessagesAsChatMessages = remember(mathState.messages) {
        mathState.messages.map { mathMsg ->
            ChatMessageModel(
                sender = if (mathMsg.role.lowercase() == "assistant") "ai" else "user",
                content = mathMsg.content,
                timestamp = mathMsg.timestamp,
                isError = mathMsg.isError
            )
        }
    }

    // Create a temporary chat state with math messages for display
    val displayChatState = remember(chatState, mathMessagesAsChatMessages, mathState) {
        chatState.copy(
            messages = mathMessagesAsChatMessages,
            inputText = mathState.inputText,
            isLoading = mathState.isLoading,
            isTyping = mathState.isTyping,
            typingText = mathState.typingText
        )
    }

    // TTS trigger - start speaking when typing animation begins
    LaunchedEffect(mathState.isTyping, ttsState.isInitialized) {
        if (mathState.isTyping && ttsState.isInitialized && !ttsState.isSpeaking) {
            val textToSpeak = mathState.typingText
            if (textToSpeak.isNotEmpty()) {
                if (ttsState.isSpeaking) {
                    ttsController.stop()
                    delay(50)
                }
                DebugLogger.debugLog("MathAgentScreen", "Starting TTS for typing text: ${textToSpeak.take(50)}...")
                ttsController.speak(textToSpeak)
            }
        }
    }

    // Stop TTS when user starts listening
    LaunchedEffect(sttState.isListening) {
        if (sttState.isListening && ttsState.isSpeaking) {
            ttsController.stop()
        }
    }

    // Use extension properties directly (no alias needed)
    val lastAIMessage: MathMessageModel? = mathState.lastAiMessage
    val isConversationStarted: Boolean = mathState.isConversationStarted

    val voiceOptions = remember(ttsState.availableVoices, mathState.currentLanguage, settingsState.selectedAvatar) {
        ttsController.getFilteredVoiceOptions(mathState.currentLanguage, settingsState.selectedAvatar)
    }

    val displayedVoiceName = remember(ttsState.selectedVoice, mathState.currentLanguage, settingsState.selectedAvatar) {
        ttsState.selectedVoice?.let { ttsController.formatVoiceName(it) }
            ?: ttsController.getDefaultVoiceName(mathState.currentLanguage, settingsState.selectedAvatar)
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

    // Auto-start with provided problem ID
    LaunchedEffect(problemId, mathState.problems) {
        DebugLogger.debugLog(
            "MathAgentScreen",
            "LaunchedEffect triggered - problemId: $problemId, problemsCount: ${mathState.problems.size}"
        )

        if (!problemId.isNullOrEmpty() && problemId != "null") {
            DebugLogger.debugLog("MathAgentScreen", "Auto-starting with provided problemId: $problemId")
            mathViewModel.onIntent(MathIntent.AutoStartWithProblem(problemId))
        } else if (mathState.problems.isNotEmpty() && !mathState.sessionStarted) {
            DebugLogger.debugLog(
                "MathAgentScreen",
                "Auto-starting with first problem from list: ${mathState.problems.first().id}"
            )
            val firstProblem = mathState.problems.first()
            mathViewModel.onIntent(MathIntent.AutoStartWithProblem(firstProblem.id))
        } else {
            DebugLogger.debugLog("MathAgentScreen", "Session already started or waiting for problems to load")
        }
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
        onSpeechTextProcessed = { },
        lastProcessedSpeechText = "",
        conceptId = problemId,
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
                    chatState = displayChatState,
                    sttState = sttState,
                    onTextChange = { mathViewModel.onIntent(MathIntent.UpdateInputText(it)) },
                onSendClick = {
                    if (mathState.inputText.isNotBlank()) {
                        // Send message with or without image
                        if (mathState.selectedImageUri != null) {
                            mathViewModel.onIntent(
                                MathIntent.SendMessageWithImage(mathState.inputText, mathState.selectedImageUri!!)
                            )
                        } else {
                            mathViewModel.onIntent(MathIntent.SendMessage(mathState.inputText))
                        }
                        mathViewModel.onIntent(MathIntent.UpdateInputText(""))
                    }
                },
                    onSpeakClick = {
                        mathViewModel.onIntent(MathIntent.HideAutosuggestions)
                        mathViewModel.onIntent(MathIntent.MarkUserActive)
                        if (permissionGranted && sttState.isInitialized) {
                            val language = if (mathState.isKannada) "kn-IN" else "en-IN"
                            sttController.startListening(language)
                        } else if (!permissionGranted) {
                            permissionLauncher.launch(RECORD_AUDIO)
                        }
                    },
                    onStopListening = { sttController.stopListening() },
                    onSuggestionClick = { },
                    shouldDisableSend = mathState.isLoading || mathState.isTyping || !mathState.sessionStarted,
                    showImageIcon = true,
                    onImagePickerClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    modifier = Modifier.imePadding()
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
                    showResourceCard = chatState.resourceCardState !is ResourceCardUiState.Hidden,
                    ttsPausedForResource = chatState.ttsPausedForResource,
                    showSettingsMenu = showSettingsMenu,
                    onKannadaToggle = {
                        chatViewModel.onIntent(ChatIntent.SetKannada(!chatState.isKannada))
                    },
                    onVolumeClick = {
                        handleVolumeClick(
                            ttsState = ttsState,
                            lastAIMessage = lastAIMessage,
                            ttsController = ttsController
                        )
                    },
                    onSettingsClick = { showSettingsMenu = true },
                    settingsContent = { }
                )

                Box(modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Avatar or Conversation View
                        if (!isConversationStarted) {
                            InitialAvatarView(
                                avatarSize = avatarSize,
                                ttsController = ttsController,
                                isLoading = mathState.isLoading
                            )
                        } else {
                            ConversationView(
                                avatarSize = avatarSize,
                                chatState = displayChatState,
                                lastAIMessage = lastAIMessage?.let { mathMsg ->
                                    ChatMessageModel(
                                        sender = "ai",
                                        content = mathMsg.content,
                                        timestamp = mathMsg.timestamp,
                                        isError = mathMsg.isError
                                    )
                                },
                                ttsController = ttsController
                            )
                        }
                    }

                    // Resource card overlay (if needed for math)
                    if (chatState.resourceCardState !is ResourceCardUiState.Hidden) {
                        ResourcesCard(
                            state = chatState.resourceCardState,
                            onDismiss = { chatViewModel.onIntent(ChatIntent.DismissResource) }
                        )
                    }

                    // Logs for debug
                    LogOverlay(
                        metadata = mathState.metadata,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    )
                }
            }
        }

        // Settings Dialog — using correct MathBotSettings signature
        if (showSettingsMenu) {
            MathBotSettings(
                expanded = true,
                onDismiss = { showSettingsMenu = false },
                state = settingsState.copy(
                    voiceOptions = voiceOptions,
                    displayedVoiceName = displayedVoiceName,
                    availableConcepts = mathState.problems.map { it.id },
                    displayConcepts = mathState.problems.map { it.id },
                    selectedConcept = mathState.problemId.ifEmpty { null},
                    isLoadingConcepts = mathState.problems.isEmpty() && mathState.isLoading
                ),
                onAvatarChange = { displayName ->
                    settingsState = settingsState.copy(
                        selectedAvatar = displayName,
                        selectedAvatarDisplayName = displayName
                    )
                },
                onVoiceChange = { selectedDisplayName ->
                    ttsState.availableVoices
                        .find { ttsController.formatVoiceName(it) == selectedDisplayName }
                        ?.let { ttsController.setVoice(it) }
                },
                onProblemChange = { selectedProblemId ->
                    mathViewModel.onIntent(MathIntent.SelectProblem(selectedProblemId))
                    showSettingsMenu = false
                },
                onLevelChange = { /* Student level not used in MathViewModel yet */ },
                onSpeedChange = { label ->
                    val speed = when (label) {
                        "0.75x" -> 0.75f
                        "1.0x" -> 1.0f
                        "1.25x" -> 1.25f
                        "1.5x" -> 1.5f
                        else -> 1.0f
                    }
                    ttsController.setSpeechRate(speed)
                    settingsState = settingsState.copy(selectedSpeed = label)
                }
            )
        }

        // Existing session dialog — using correct intent names
        if (mathState.showSessionDialog) {
            AppDialog(
                show = mathState.showSessionDialog,
                title = stringResource(R.string.existing_session_found),
                message = stringResource(R.string.resume_or_start_fresh),
                confirmText = stringResource(R.string.continue_session),
                dismissText = stringResource(R.string.start_new),
                onConfirm = {
                    mathState.pendingProblemForDialog?.let { pendingId ->
                        mathViewModel.onIntent(MathIntent.ContinueExistingSession(pendingId))
                    }
                },
                onDismiss = {
                    mathState.pendingProblemForDialog?.let { pendingId ->
                        mathViewModel.onIntent(MathIntent.StartFreshSession(pendingId))
                    }
                }
            )
        }
    }
}

private fun handleVolumeClick(
    ttsState: TextToSpeech.TTSState,
    lastAIMessage: MathMessageModel?,
    ttsController: TextToSpeech
) {
    if (ttsState.isSpeaking) {
        ttsController.stop()
    } else {
        lastAIMessage?.let {
            ttsController.speak(it.content)
        }
    }
}
