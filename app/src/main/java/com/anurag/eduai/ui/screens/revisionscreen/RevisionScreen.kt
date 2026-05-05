package com.anurag.eduai.ui.screens.revisionscreen

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anurag.eduai.R
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatBotSettings
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatHeaderIcons
import com.anurag.eduai.ui.screens.chatbotscreen.components.ConversationView
import com.anurag.eduai.ui.screens.chatbotscreen.components.InitialAvatarView
import com.anurag.eduai.ui.screens.chatbotscreen.components.InputSection
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatBotSettingsState
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.isConversationStarted
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.lastAiMessage
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.ui.screens.revisionscreen.viewmodel.RevisionViewModel
import com.anurag.eduai.ui.viewModel.SpeechToText
import com.anurag.eduai.ui.viewModel.TextToSpeech

/**
 * Revision screen for chapter revision sessions.
 * Uses full ChatBotSettings with chapter selection instead of concepts.
 */
@Composable
fun RevisionScreen(
    chapterName: String,
    onBackClick: () -> Unit = {},
    revisionViewModel: RevisionViewModel = hiltViewModel(),
    sttController: SpeechToText = hiltViewModel(),
    ttsController: TextToSpeech = hiltViewModel()
) {
    DebugLogger.debugLog("RevisionScreen", "RevisionScreen composable - chapter: $chapterName")

    TrackScreenEvent(ScreenName.REVISION)

    val context = LocalContext.current
    val sharedPrefs = remember { SharedPreferenceUtils(context) }

    // State collectors
    val chatState by revisionViewModel.uiState.collectAsState()
    val sttState by sttController.state.collectAsState()
    val ttsState by ttsController.state.collectAsState()
    val availableChapters by revisionViewModel.availableChapters.collectAsState()
    val isLoadingChapters by revisionViewModel.isLoadingChapters.collectAsState()

    // Local UI state
    var permissionGranted by remember { mutableStateOf(false) }
    var lastProcessedSpeechText by remember { mutableStateOf("") }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var settingsState by remember { mutableStateOf(ChatBotSettingsState()) }

    val lastAIMessage = chatState.lastAiMessage
    val isConversationStarted = chatState.isConversationStarted

    // Voice options for TTS
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

    // String resources
    val boyDisplayName = stringResource(R.string.boy)
    val girlDisplayName = stringResource(R.string.girl)
    val disableDisplayName = stringResource(R.string.disable)

    // Initialize
    LaunchedEffect(Unit) {
        val userId = sharedPrefs.getUserId() ?: "guest"
        revisionViewModel.initialize(userId, chapterName)
        sttController.initialize(context)
        ttsController.initialize(context)

        // Initialize avatar display name
        settingsState = revisionViewModel.initializeAvatarDisplayName(
            avatarCode = settingsState.selectedAvatar,
            boyDisplayName = boyDisplayName,
            girlDisplayName = girlDisplayName,
            disableDisplayName = disableDisplayName,
            currentState = settingsState
        )
    }

    // Handle speech recognition - populate input field, DON'T auto-send
    LaunchedEffect(sttState.isListening) {
        if (!sttState.isListening && sttState.resultText.isNotEmpty() && sttState.resultText != lastProcessedSpeechText) {
            revisionViewModel.updateInputText(sttState.resultText)
            lastProcessedSpeechText = sttState.resultText
        }
    }

    // Stop TTS when user starts speaking
    LaunchedEffect(sttState.isListening) {
        if (sttState.isListening && ttsState.isSpeaking) {
            ttsController.stop()
        }
    }

    LaunchedEffect(chatState.shouldStartTTS, ttsState.isInitialized) {
        if (chatState.shouldStartTTS && ttsState.isInitialized) {
            val textToSpeak = chatState.fullTextForTTS
            if (textToSpeak.isNotEmpty()) {
                if (ttsState.isSpeaking) {
                    ttsController.stop()
                    kotlinx.coroutines.delay(50)
                }
                DebugLogger.debugLog("RevisionScreen", "Starting TTS in parallel with typing animation")
                ttsController.speak(textToSpeak)
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            sttController.destroy()
        }
    }

    // Calculate shouldDisableSend - disable send/mic when typing, loading, OR TTS is speaking
    val shouldDisableSend = remember(chatState.isTyping, chatState.isLoading, ttsState.isSpeaking) {
        chatState.isTyping || chatState.isLoading || ttsState.isSpeaking
    }

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
                    onTextChange = { revisionViewModel.updateInputText(it) },
                    onSendClick = {
                        if (chatState.inputText.isNotBlank()) {
                            revisionViewModel.sendMessage(chatState.inputText)
                        }
                    },
                    onSpeakClick = {
                        if (permissionGranted && sttState.isInitialized) {
                            val language = if (chatState.isKannada) "kn-IN" else "en-IN"
                            sttController.startListening(language)
                        } else if (!permissionGranted) {
                            permissionLauncher.launch(RECORD_AUDIO)
                        }
                    },
                    onStopListening = { sttController.stopListening() },
                    onSuggestionClick = { },
                    shouldDisableSend = shouldDisableSend,
                    showImageIcon = false,
                    modifier = Modifier.imePadding()
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header with full ChatBotSettings
                ChatHeaderIcons(
                    isKannada = chatState.isKannada,
                    isSpeaking = ttsState.isSpeaking,
                    showResourceCard = false,
                    ttsPausedForResource = false,
                    showSettingsMenu = showSettingsMenu,
                    onKannadaToggle = { revisionViewModel.toggleLanguage() },
                    onVolumeClick = {
                        if (ttsState.isSpeaking) {
                            ttsController.stop()
                        } else if (lastAIMessage != null) {
                            ttsController.speak(lastAIMessage.content)
                        }
                    },
                    onSettingsClick = { showSettingsMenu = !showSettingsMenu },
                    settingsContent = {
                        ChatBotSettings(
                            expanded = true,
                            onDismiss = { showSettingsMenu = false },
                            state = settingsState.copy(
                                voiceOptions = voiceOptions,
                                displayedVoiceName = displayedVoiceName,
                                availableConcepts = availableChapters,
                                displayConcepts = availableChapters,
                                selectedConcept = chatState.selectedConcept,
                                isLoadingConcepts = isLoadingChapters
                            ),
                            onAvatarChange = { displayName ->
                                settingsState = revisionViewModel.handleAvatarChange(
                                    displayName = displayName,
                                    boyDisplayName = boyDisplayName,
                                    girlDisplayName = girlDisplayName,
                                    ttsController = ttsController,
                                    currentState = settingsState
                                )
                            },
                            onVoiceChange = { selectedDisplayName ->
                                val selectedVoice = ttsState.availableVoices.find { voice ->
                                    ttsController.formatVoiceName(voice) == selectedDisplayName
                                }
                                selectedVoice?.let {
                                    ttsController.setVoice(it)
                                    if (aiMessageOutput.isNotEmpty() && !ttsState.isSpeaking) {
                                        ttsController.speak(aiMessageOutput)
                                    }
                                }
                            },
                            onConceptChange = { chapter ->
                                revisionViewModel.changeChapter(chapter)
                                showSettingsMenu = false
                            },
                            onLevelChange = { levelCode ->
                                settingsState = settingsState.copy(selectedStudentLevel = levelCode)
                            },
                            onSpeedChange = { speedLabel ->
                                settingsState = settingsState.copy(selectedSpeed = speedLabel)
                                val speedValue = when (speedLabel) {
                                    "0.5x" -> 0.5f
                                    "0.75x" -> 0.75f
                                    "1.0x" -> 1.0f
                                    "1.25x" -> 1.25f
                                    "1.5x" -> 1.5f
                                    else -> 0.75f
                                }
                                ttsController.setSpeechRate(speedValue)
                                if (aiMessageOutput.isNotEmpty() && !ttsState.isSpeaking) {
                                    ttsController.speak(aiMessageOutput)
                                }
                            },
                            isRevisionMode = true
                        )
                    }
                )

                if (isConversationStarted) {
                    ConversationView(
                        avatarSize = avatarSize,
                        chatState = chatState,
                        lastAIMessage = lastAIMessage,
                        ttsController = ttsController,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    InitialAvatarView(
                        avatarSize = avatarSize,
                        ttsController = ttsController,
                        isLoading = chatState.isLoading,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
