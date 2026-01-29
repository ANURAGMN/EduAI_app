package com.anurag.eduai.ui.screens.simulation_agent

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatBotSettings
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatBotSettingsState
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatHeaderIcons
import com.anurag.eduai.ui.screens.chatbotscreen.components.InitialAvatarView
import com.anurag.eduai.ui.screens.chatbotscreen.components.InputSection
import com.anurag.eduai.ui.screens.simulation_agent.components.SimulationConversationView
import com.anurag.eduai.ui.screens.simulation_agent.components.SimulationWebViewCard
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.ChatUiState
import com.anurag.eduai.ui.viewModel.SimAgentUiState
import com.anurag.eduai.ui.viewModel.SimulationAgentViewModel
import com.anurag.eduai.ui.viewModel.SpeechToText
import com.anurag.eduai.ui.viewModel.TextToSpeech
import com.anurag.eduai.ui.viewmodel_factory.SimulationAgentViewmodelFactory
import kotlinx.coroutines.delay

/**
 * Main simulation screen that matches chatbot design exactly Same header, avatar, blur effects, and
 * overall structure
 */
@Composable
fun SimulationScreen(
    simulationId: String,
    onNavigateBack: () -> Unit,
    ttsController: TextToSpeech = viewModel(),
    sttController: SpeechToText = viewModel()
) {
    val dimens = LocalDimensions.current
    val viewModel: SimulationAgentViewModel = viewModel(factory = SimulationAgentViewmodelFactory())
    val uiState by viewModel.uiState.collectAsState()
    val sessionData by viewModel.sessionData.collectAsState()
    val ttsState by ttsController.state.collectAsState()
    val sttState by sttController.state.collectAsState()

    val context = LocalContext.current

    // State
    var userInput by remember { mutableStateOf("") }
    var currentTeacherMessage by remember { mutableStateOf("") }
    var showWebView by remember { mutableStateOf(false) }
    var simulationUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSessionStarted by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }
    var lastProcessedSpeechText by remember { mutableStateOf("") }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var settingsState by remember { mutableStateOf(ChatBotSettingsState()) }

    // Animation values
    val avatarSize by
    animateDpAsState(
        targetValue = if (isSessionStarted) dimens.avatarSizeLarge else dimens.avatarSizeLarge * 2.5f,
        label = "avatarSize"
    )

    // Permission launcher
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted ->
            permissionGranted = isGranted
            sttController.handlePermissionResult(
                SpeechToText.RECORD_AUDIO_PERMISSION_REQUEST,
                if (isGranted) intArrayOf(PackageManager.PERMISSION_GRANTED)
                else intArrayOf(PackageManager.PERMISSION_DENIED)
            )
        }

    // Initialize STT and TTS
    LaunchedEffect(Unit) {
        sttController.initialize(context)
        ttsController.initialize(context)
        viewModel.loadAvailableSimulations()
    }

    // Handle back press
    BackHandler {
        if (showWebView) {
            showWebView = false
        } else {
            viewModel.resetSession()
            onNavigateBack()
        }
    }

    // Initialize session and start simulation automatically
    LaunchedEffect(simulationId) {
        viewModel.startNewSession(simulationId)
    }

    // Handle STT result - update input field when speech recognition completes
    LaunchedEffect(sttState.resultText, sttState.isListening) {
        // Only process when: result exists, not listening, and it's a new result
        if (sttState.resultText.isNotEmpty() &&
            !sttState.isListening &&
            sttState.resultText != lastProcessedSpeechText
        ) {
            lastProcessedSpeechText = sttState.resultText
            userInput = sttState.resultText
        }
    }

    // Reset last processed text when user manually clears input
    LaunchedEffect(userInput) {
        if (userInput.isEmpty() && lastProcessedSpeechText.isNotEmpty()) {
            lastProcessedSpeechText = ""
        }
    }

    // Handle UI state changes and update simulation data
    LaunchedEffect(uiState, sessionData) {
        when (val state = uiState) {
            is SimAgentUiState.Success -> {
                sessionData?.let { session ->
                    // Mark session as started
                    isSessionStarted = true

                    // New message arrived - hide WebView and update text
                    showWebView = false
                    currentTeacherMessage = session.teacherMessage.text

                    // Start TTS
                    if (!ttsState.isSpeaking) {
                        ttsController.speak(session.teacherMessage.text)
                    }

                    // Update simulation URLs
                    val urls = mutableListOf<String>()
                    urls.add(session.simulation.htmlUrl)

                    // Check if there's a param change (before/after comparison)
                    session.simulation.paramChange?.let { change ->
                        urls.clear()
                        urls.add(change.beforeUrl)
                        urls.add(change.afterUrl)
                    }
                    simulationUrls = urls
                }
            }
            is SimAgentUiState.Error -> {
                currentTeacherMessage = state.message
            }
            else -> {}
        }
    }

    // Show WebView only after TTS completes
    LaunchedEffect(ttsState.isSpeaking) {
        if (!ttsState.isSpeaking &&
            simulationUrls.isNotEmpty() &&
            currentTeacherMessage.isNotEmpty()
        ) {
            delay(dimens.spaceExtraSmall.value.toLong() * 75) // 300ms delay
            showWebView = true
        }
    }

    // Voice options for settings
    val voiceOptions =
        remember(ttsState.availableVoices, settingsState.selectedAvatar) {
            ttsController.getFilteredVoiceOptions("en", settingsState.selectedAvatar)
        }

    val displayedVoiceName =
        remember(ttsState.selectedVoice, settingsState.selectedAvatar) {
            ttsState.selectedVoice?.let { ttsController.formatVoiceName(it) }
                ?: ttsController.getDefaultVoiceName("en", settingsState.selectedAvatar)
        }

    // Background - Same structure as chatbot
    Box(modifier = Modifier.fillMaxSize().background(White)) {
        // Main content Column
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            // Header icons (same as chatbot)
            ChatHeaderIcons(
                isKannada = false,
                isSpeaking = ttsState.isSpeaking,
                showResourceCard = false,
                ttsPausedForResource = false,
                showSettingsMenu = showSettingsMenu,
                onKannadaToggle = { /* Not used in simulation */},
                onVolumeClick = {
                    if (ttsState.isSpeaking) {
                        ttsController.stop()
                    } else if (currentTeacherMessage.isNotEmpty()) {
                        ttsController.speak(currentTeacherMessage)
                    }
                },
                onSettingsClick = { showSettingsMenu = !showSettingsMenu },
                settingsContent = {
                    ChatBotSettings(
                        expanded = true,
                        onDismiss = { showSettingsMenu = false },
                        state =
                            settingsState.copy(
                                voiceOptions = voiceOptions,
                                displayedVoiceName = displayedVoiceName,
                                availableConcepts =
                                    viewModel.availableSimulations
                                        .collectAsState()
                                        .value
                                        .map { it.title },
                                selectedConcept = simulationId,
                                isLoadingConcepts =
                                    viewModel.simulationsLoading
                                        .collectAsState()
                                        .value
                            ),
                        onAvatarChange = { avatarCode ->
                            settingsState = settingsState.copy(selectedAvatar = avatarCode)
                            ttsController.switchCharacter(avatarCode)
                            if (avatarCode != "disable") {
                                ttsController.applyDefaultsForAvatarLanguage(
                                    avatarCode,
                                    "en"
                                )
                            } else if (ttsState.isSpeaking) {
                                ttsController.stop()
                            }
                        },
                        onVoiceChange = { selectedDisplayName ->
                            ttsState.availableVoices
                                .find {
                                    ttsController.formatVoiceName(it) ==
                                            selectedDisplayName
                                }
                                ?.let { voice ->
                                    ttsController.setVoice(voice)
                                    if (ttsState.isSpeaking) {
                                        ttsController.stop()
                                        ttsController.speak(currentTeacherMessage)
                                    }
                                }
                        },
                        onConceptChange = { /* Not used - simulations are selected from home */
                        },
                        onLevelChange = { levelCode ->
                            settingsState =
                                settingsState.copy(selectedStudentLevel = levelCode)
                        },
                        onSpeedChange = { label ->
                            settingsState = settingsState.copy(selectedSpeed = label)
                            val speed =
                                when (label) {
                                    "0.75x" -> 0.75f
                                    "1.0x" -> 1.0f
                                    "1.25x" -> 1.25f
                                    "1.5x" -> 1.5f
                                    else -> 0.75f
                                }
                            ttsController.setSpeechRate(speed)
                            if (ttsState.isSpeaking) {
                                ttsController.stop()
                                ttsController.speak(currentTeacherMessage)
                            }
                        }
                    )
                }
            )

            if (!isSessionStarted) {
                // Initial centered avatar (same as chatbot)
                InitialAvatarView(
                    avatarSize = avatarSize,
                    ttsController = ttsController,
                    modifier = Modifier.weight(0.1f).background(White)
                )
            } else {
                // Conversation view with avatar and scrollable content
                SimulationConversationView(
                    avatarSize = avatarSize,
                    currentMessage = currentTeacherMessage,
                    isLoading = uiState is SimAgentUiState.Loading,
                    ttsController = ttsController,
                    modifier = Modifier.weight(0.1f).background(White)
                )
            }

            // Input section (same as chatbot)
            val chatState =
                ChatUiState(
                    inputText = userInput,
                    isLoading = uiState is SimAgentUiState.Loading
                )

            InputSection(
                chatState = chatState,
                sttState = sttState,
                onTextChange = { userInput = it },
                onSendClick = {
                    if (userInput.isNotBlank()) {
                        if (ttsState.isSpeaking) {
                            ttsController.stop()
                        }
                        viewModel.sendStudentResponse(userInput)
                        userInput = ""
                    }
                },
                onSpeakClick = {
                    if (ttsState.isSpeaking) {
                        ttsController.stop()
                    }
                    if (permissionGranted && sttState.isInitialized) {
                        sttController.startListening("en-IN")
                    } else if (!permissionGranted) {
                        permissionLauncher.launch(RECORD_AUDIO)
                    }
                },
                onStopListening = {
                    sttController.stopListening()
                    // Result will be processed by LaunchedEffect above
                },
                onSuggestionClick = { /* Not used in simulation */}
            )
        }

        // WebView overlay (with blur effect on background)
        SimulationWebViewCard(
            visible = showWebView && !ttsState.isSpeaking,
            simulationUrls = simulationUrls,
            onClose = { showWebView = false },
            blurBackground = true
        )
    }
}