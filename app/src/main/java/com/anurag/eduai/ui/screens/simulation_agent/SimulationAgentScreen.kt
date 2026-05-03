package com.anurag.eduai.ui.screens.simulation_agent

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.anurag.eduai.R
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatBotSettings
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatHeaderIcons
import com.anurag.eduai.ui.screens.chatbotscreen.components.InputSection
import com.anurag.eduai.ui.screens.chatbotscreen.components.AppDialog
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatBotSettingsState
import com.anurag.eduai.ui.screens.chatbotscreen.components.dataclass.ChatUiState
import com.anurag.eduai.ui.screens.simulation_agent.components.SimulationConversationView
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.screens.simulation_agent.viewmodel.SimAgentUiState
import com.anurag.eduai.ui.screens.simulation_agent.viewmodel.SimulationAgentViewModel
import com.anurag.eduai.ui.screens.simulation_agent.viewmodel.SimulationIntent
import com.anurag.eduai.ui.viewModel.SpeechToText
import com.anurag.eduai.ui.viewModel.TextToSpeech

/**
 * Simulation Agent Screen - PURELY PRESENTATIONAL
 * All business logic is in SimulationAgentViewModel
 * This composable only:
 * 1. Observes state from ViewModel
 * 2. Renders UI based on state
 * 3. Forwards user actions to ViewModel
 */
@Composable
fun SimulationAgentScreen(
    simulationId: String,
    onNavigateBack: () -> Unit,
    ttsController: TextToSpeech = viewModel(),
    sttController: SpeechToText = viewModel()
) {
    val dimens = LocalDimensions.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val viewModel: SimulationAgentViewModel = hiltViewModel()

    var errorCardHeightDp by remember { mutableStateOf(0.dp) }

    // Observe ALL state from ViewModel - no local state management
    val uiState by viewModel.uiState.collectAsState()
    val currentTeacherMessage by viewModel.currentTeacherMessage.collectAsState()
    val showWebView by viewModel.showWebView.collectAsState()
    val simulationUrls by viewModel.simulationUrls.collectAsState()
    val isSessionStarted by viewModel.isSessionStarted.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val userInput by viewModel.userInput.collectAsState()
    val isInputEnabled by viewModel.isInputEnabled.collectAsState()
    val shouldTriggerTts by viewModel.shouldTriggerTts.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val showSessionResumeDialog by viewModel.showSessionResumeDialog.collectAsState()

    // TTS/STT states
    val ttsState by ttsController.state.collectAsState()
    val sttState by sttController.state.collectAsState()

    // Settings state (local UI-only state)
    var showSettingsMenu by remember { mutableStateOf(false) }
    var settingsState by remember { mutableStateOf(ChatBotSettingsState()) }
    var permissionGranted by remember { mutableStateOf(false) }
    var lastProcessedSpeechText by remember { mutableStateOf("") }

    // Initialize avatar display name only once when string resources are available
    val boyDisplayName = stringResource(R.string.boy)
    val girlDisplayName = stringResource(R.string.girl)
    val disableDisplayName = stringResource(R.string.disable)

    // Initialize settings state with proper display name on first composition
    LaunchedEffect(Unit) {
        settingsState = viewModel.initializeAvatarDisplayName(
            avatarCode = settingsState.selectedAvatar,
            boyDisplayName = boyDisplayName,
            girlDisplayName = girlDisplayName,
            disableDisplayName = disableDisplayName,
            currentState = settingsState
        )
    }


    /**
     * Animation values (UI-only)
     */
    val avatarSize by animateDpAsState(
        targetValue = if (isSessionStarted) dimens.avatarSizeLarge * 1.5f else dimens.avatarSizeLarge * 2.5f,
        label = "avatarSize"
    )

    /**
     * Permission launcher (UI-only)
     */
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

    /**
     * INITIALIZATION - One-time setup
     * Uses Unit as key so it only runs once per composition lifecycle
     */
    LaunchedEffect(Unit) {
        sttController.initialize(context)
        ttsController.initialize(context)
        viewModel.loadAvailableSimulations()
    }

    /**
     * SESSION INITIALIZATION
     * Uses simulationId as key so it only runs when simulationId changes
     * ViewModel internally checks if session is already started for this ID
     */
    LaunchedEffect(simulationId) {
        viewModel.startNewSession(simulationId)
    }

    LaunchedEffect(uiState) {
        if (uiState is SimAgentUiState.Loading) {
            ttsController.stop()
            viewModel.onTtsStopped()
        }
    }

    /**
     * TTS STATE SYNCHRONIZATION
     * Notify ViewModel when TTS state changes
     */
    LaunchedEffect(ttsState.isSpeaking) {
        if (ttsState.isSpeaking) {
            viewModel.handleIntent(SimulationIntent.TtsStarted)
        } else {
            viewModel.handleIntent(SimulationIntent.TtsStopped)
        }
    }

    /**
     * TTS PLAYBACK CONTROL
     * CRITICAL: Uses shouldTriggerTts flag from ViewModel to prevent re-triggering on config changes
     * Only triggers when ViewModel explicitly sets shouldTriggerTts to true (on new message)
     */
    LaunchedEffect(shouldTriggerTts) {
        if (shouldTriggerTts && currentTeacherMessage.isNotEmpty() && !ttsState.isSpeaking) {
            ttsController.speak(currentTeacherMessage)
            viewModel.handleIntent(SimulationIntent.TtsTriggered) // Acknowledge that TTS was triggered
        }
    }

    /**
     * STT Result Handling
     * Processes speech-to-text results and updates input
     */
    LaunchedEffect(sttState.resultText, sttState.isListening) {
        if (sttState.resultText.isNotEmpty() &&
            !sttState.isListening &&
            sttState.resultText != lastProcessedSpeechText
        ) {
            lastProcessedSpeechText = sttState.resultText
            viewModel.onUserInputChanged(sttState.resultText)
        }
    }

    /**
     * Clean up STT tracking when input is cleared
     */
    LaunchedEffect(userInput) {
        if (userInput.isEmpty() && lastProcessedSpeechText.isNotEmpty()) {
            lastProcessedSpeechText = ""
        }
    }

    /**
     * Back press handling
     */
    BackHandler {
        val consumed = viewModel.onBackPressed()
        if (!consumed) {
            onNavigateBack()
        }
    }

    /**
     * Voice options (derived state)
     */
    val voiceOptions = remember(ttsState.availableVoices, settingsState.selectedAvatar) {
        ttsController.getFilteredVoiceOptions("en", settingsState.selectedAvatar)
    }

    val displayedVoiceName = remember(ttsState.selectedVoice, settingsState.selectedAvatar) {
        ttsState.selectedVoice?.let { ttsController.formatVoiceName(it) }
            ?: ttsController.getDefaultVoiceName("en", settingsState.selectedAvatar)
    }

    /**
     * UI RENDERING
     */
    Box(modifier = Modifier.fillMaxSize().background(White)) {
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            ChatHeaderIcons(
                isKannada = false,
                isSpeaking = ttsState.isSpeaking,
                showResourceCard = false,
                ttsPausedForResource = false,
                showSettingsMenu = showSettingsMenu,
                onKannadaToggle = { /* Not used */ },
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
                        state = settingsState.copy(
                            voiceOptions = voiceOptions,
                            displayedVoiceName = displayedVoiceName,
                            availableConcepts = viewModel.availableSimulations.collectAsState().value.map { it.title },
                            selectedConcept = simulationId,
                            isLoadingConcepts = viewModel.simulationsLoading.collectAsState().value
                        ),
                        onAvatarChange = { displayName ->
                            // Handle avatar change through ViewModel
                            settingsState = viewModel.handleAvatarChange(
                                displayName = displayName,
                                boyDisplayName = boyDisplayName,
                                girlDisplayName = girlDisplayName,
                                ttsController = ttsController,
                                currentState = settingsState
                            )
                            viewModel.onAvatarChanged()
                        },
                        onVoiceChange = { selectedDisplayName ->
                            ttsState.availableVoices.find {
                                ttsController.formatVoiceName(it) == selectedDisplayName
                            }?.let { voice ->
                                ttsController.setVoice(voice)
                                if (ttsState.isSpeaking) {
                                    ttsController.stop()
                                    ttsController.speak(currentTeacherMessage)
                                }
                                viewModel.onVoiceChanged()
                            }
                        },
                        onConceptChange = { selectedTitle ->
                            // Find the simulation ID from the title
                            val selectedSimulation = viewModel.availableSimulations.value.find { it.title == selectedTitle }
                            selectedSimulation?.let { simulation ->
                                // Close settings menu
                                showSettingsMenu = false
                                // Start the selected simulation
                                viewModel.startNewSession(simulation.id)
                            }
                        },
                        onLevelChange = { levelCode ->
                            settingsState = settingsState.copy(selectedStudentLevel = levelCode)
                        },
                        onSpeedChange = { label ->
                            settingsState = settingsState.copy(selectedSpeed = label)
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
                                ttsController.speak(currentTeacherMessage)
                            }
                            viewModel.onSpeedChanged()
                        }
                    )
                }
            )

            /**
             * Error handling
             */
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.spaceMedium)
                        .onGloballyPositioned { coords ->
                            errorCardHeightDp = with(density) { coords.size.height.toDp() }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(dimens.spaceMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(dimens.spaceSmall))
                        Text(
                            text = errorMessage ?: "Unknown error occurred",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(dimens.spaceMedium))
                        Button(
                            onClick = { viewModel.handleIntent(SimulationIntent.RetrySession(simulationId)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            /**
             * Main content area
             */
            SimulationConversationView(
                avatarSize = avatarSize,
                currentMessage = currentTeacherMessage,
                isLoading = uiState is SimAgentUiState.Loading,
                ttsController = ttsController,
                onParamsChanged = { viewModel.handleIntent(SimulationIntent.ParametersChanged(it)) },
                modifier = Modifier.weight(1f).background(White),
            )

            /**
             * User Input section
             */
            InputSection(
                chatState = ChatUiState(
                    inputText = userInput,
                    isLoading = !isInputEnabled
                ),
                sttState = sttState,
                onTextChange = { viewModel.handleIntent(SimulationIntent.UpdateInput(it)) },
                onSendClick = {
                    ttsController.stop()
                    viewModel.handleIntent(SimulationIntent.SendUserResponse(userInput))
                },
                onSpeakClick = {
                    if (ttsState.isSpeaking) {
                        ttsController.stop()
                    }
                    if (permissionGranted && sttState.isInitialized) {
                        sttController.startListening(currentLanguage)
                    } else if (!permissionGranted) {
                        permissionLauncher.launch(RECORD_AUDIO)
                    }
                },
                onStopListening = { sttController.stopListening() },
                onSuggestionClick = { /* Not used */ },
                shouldDisableSend = !isInputEnabled
            )
        }
    }

    // Session Resume Dialog - Ask to continue or start fresh
    AppDialog(
        show = showSessionResumeDialog,
        title = stringResource(R.string.existing_session_found),
        message = stringResource(R.string.resume_or_start_fresh),
        confirmText = stringResource(R.string.continue_session),
        dismissText = stringResource(R.string.start_new),
        onConfirm = {
            viewModel.handleIntent(SimulationIntent.ContinueExistingSession)
        },
        onDismiss = {
            viewModel.handleIntent(SimulationIntent.StartFreshSession)
        }
    )}
