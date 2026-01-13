package com.anurag.eduai.ui.screens.chatbotscreen

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.R
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.screens.chatbotscreen.components.AgentMessage
import com.anurag.eduai.ui.screens.chatbotscreen.components.AppDialog
import com.anurag.eduai.ui.screens.chatbotscreen.components.AutoSuggestionChips
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatBotSettings
import com.anurag.eduai.ui.screens.chatbotscreen.components.ChatBotSettingsState
import com.anurag.eduai.ui.screens.chatbotscreen.components.InputSection
import com.anurag.eduai.ui.screens.chatbotscreen.components.ListeningOverlay
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.IconPrimary
import com.anurag.eduai.ui.theme.IconSecondary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.viewModel.ChatViewModel

import com.anurag.eduai.ui.viewModel.SpeechToText
import com.anurag.eduai.ui.viewModel.TextToSpeech
import kotlinx.coroutines.delay

@Composable
fun ChatbotScreen(
    chatViewModel: ChatViewModel = viewModel(),
    ttsController: TextToSpeech = viewModel(),
    sttController: SpeechToText = viewModel(),
) {
    val context = LocalContext.current
    val dimens = LocalDimensions.current

    // Observe states
    val ttsState by ttsController.state.collectAsState()
    val sttState by sttController.state.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    // Observe chat UI state
    val chatState by chatViewModel.uiState.collectAsState()

    // Get the last AI message only
    val lastAIMessage = remember(chatState.messages) {
        chatState.messages.findLast { it.sender.lowercase() == "ai" }
    }
    //is kannada
    val isKannada by chatViewModel.isKannada.collectAsState()

    // Check if conversation has started
    val isConversationStarted = chatState.messages.isNotEmpty()

    // Track permission state
    var permissionGranted by remember { mutableStateOf(false) }

    // Track last processed speech text to avoid duplicates
    var lastProcessedSpeechText by remember { mutableStateOf("") }

    //showSessionResumeDialog
    var showSessionResumeDialog by remember { mutableStateOf(false) }

    // Settings menu state
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Settings state
    var settingsState by remember {
        mutableStateOf(
            ChatBotSettingsState()
        )
    }
    //typing state
    val typingText by chatViewModel.typingText.collectAsState()
    val isTyping by chatViewModel.isTyping.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()

    // Collect available concepts and selected concept from ViewModel
    val availableConcepts by chatViewModel.availableConcepts.collectAsState()
    val selectedConcept by chatViewModel.selectedConcept.collectAsState()
    var pendingConceptSelection by remember { mutableStateOf<String?>(null) }

    // Current language
    val currentLanguage by chatViewModel.currentLanguage.collectAsState()
    val voiceOptions = remember(ttsState.availableVoices, currentLanguage, settingsState.selectedAvatar) {
        ttsController.getFilteredVoiceOptions(currentLanguage, settingsState.selectedAvatar)
    }
    //input section height
    var inputSectionHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    // Autosuggestions state
    val autosuggestions by chatViewModel.autosuggestions.collectAsState()
    val showAutosuggestions by chatViewModel.showAutosuggestions.collectAsState()

    // Permission launcher for audio recording
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
    // voices
    val displayedVoiceName = remember(ttsState.selectedVoice, currentLanguage, settingsState.selectedAvatar) {
        ttsState.selectedVoice?.let { ttsController.formatVoiceName(it) }
            ?: ttsController.getDefaultVoiceName(currentLanguage, settingsState.selectedAvatar)
    }

    // AI message output logic
    val aiMessageOutput = remember(isTyping, typingText) {
        when {
            isTyping -> typingText
            else -> lastAIMessage?.content ?: ""
        }
    }

    LaunchedEffect(Unit) {
        // Initialize all controllers
        val sharedPrefs = SharedPreferenceUtils(context)
        val userId = sharedPrefs.getUserId().toString()
        chatViewModel.initialize(userId)
        sttController.initialize(context)
        ttsController.initialize(context)

        // Check and request audio permission
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        permissionGranted = hasPermission

        if (!hasPermission) {
            permissionLauncher.launch(RECORD_AUDIO)
        }
    }
    LaunchedEffect(ttsState.isInitialized) {
        chatViewModel.shouldStartTTS.collect { shouldStart ->
            if (shouldStart && ttsState.isInitialized) {
                val textToSpeak = chatViewModel.fullTextForTTS.value
                if (textToSpeak.isNotEmpty()) {
                    // Stop any ongoing TTS first
                    if (ttsState.isSpeaking) {
                        ttsController.stop()
                        delay(50)
                    }

                    ttsController.speak(textToSpeak)
                    DebugLogger.debugLog("ChatbotScreen", "TTS started immediately: ${textToSpeak.take(50)}...")
                }
            }
        }
    }
    LaunchedEffect(selectedConcept) {
        if (ttsState.isSpeaking) {
            ttsController.stop()
            DebugLogger.debugLog("ChatbotScreen", "TTS stopped due to concept change")
        }
    }
    // Transfer recognized speech to input field when listening stops
    LaunchedEffect(sttState.isListening) {
        if (!sttState.isListening && sttState.resultText.isNotEmpty()) {
            // Only update if this is a new speech result (different from last processed)
            if (sttState.resultText != lastProcessedSpeechText) {
                chatViewModel.updateInputText(sttState.resultText)
                lastProcessedSpeechText = sttState.resultText
            }
        }
    }


    LaunchedEffect(ttsState.isSpeaking) {
        if (ttsState.isSpeaking) {
            while (ttsState.isSpeaking) {
                delay(50)
            }
        }
        if (!ttsState.isSpeaking && chatState.messages.isNotEmpty()) {
            chatViewModel.startIdleTimer()
        }
    }

    DisposableEffect(Unit) {
        sttController.initialize(context)
        onDispose {
            sttController.destroy()
        }
    }

    val avatarSize by animateDpAsState(
        targetValue = if (isConversationStarted) 100.dp else 180.dp,
        label = "avatarSize"
    )
    val avatarPadding by animateDpAsState(
        targetValue = if (isConversationStarted) 20.dp else 0.dp,
        label = "avatarPadding"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Main content (Avatar and Messages)
        Column(modifier = Modifier.fillMaxSize()) {
            // Avatar and Messages Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (!isConversationStarted) {
                    // Initial state: Avatar centered
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                } else {
                    // Conversation started - Show Avatar + Message Display
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Avatar at top
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = avatarPadding),
                            contentAlignment = Alignment.TopCenter
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
                        Spacer(Modifier.height(dimens.spaceSmall))

                        // Agent Message Display - Single scrollable message
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(bottom = inputSectionHeight + 16.dp)
                        ) {
                            if (lastAIMessage != null) {
                                AgentMessage(
                                    text = if (isTyping) typingText else lastAIMessage.content,
                                    isTyping = isTyping,
                                    typingText = typingText,
                                    fullText = lastAIMessage.content,
                                    isError = lastAIMessage.isError,
                                    ttsController = ttsController
                                )
                            }

                            // Top fade overlay (below avatar)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .align(Alignment.TopCenter)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.95f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            // Bottom fade overlay (above input section)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.95f)
                                            )
                                        )
                                    )
                            )

                            if (isLoading && !isTyping) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                        .align(Alignment.BottomStart),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = BrandPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.thinking),
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Icons Row (overlaid at top-right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        chatViewModel.setKannada(!isKannada)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ClosedCaption,
                            contentDescription = if (isKannada) "Kannada Enabled" else "Kannada Disabled",
                            tint = if (isKannada)
                                IconPrimary
                            else
                                IconSecondary
                        )
                    }
                    IconButton(onClick = {
                        if (ttsState.isSpeaking) {
                            ttsController.stop()
                        } else {
                            // Replay the last AI message
                            lastAIMessage?.let {
                                ttsController.speak(it.content)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (ttsState.isSpeaking)
                                Icons.Default.Stop
                            else
                                Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (ttsState.isSpeaking) "Stop" else "Play",
                            tint = if (ttsState.isSpeaking)
                                IconPrimary
                            else
                                IconSecondary
                        )
                    }
                    Box {
                        IconButton(onClick = { showSettingsMenu = !showSettingsMenu }) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = stringResource(R.string.settings),
                                tint = Color.Gray.copy(alpha = 0.6f)
                            )
                        }
                        if (showSettingsMenu) {
                            ChatBotSettings(
                                expanded = true,
                                onDismiss = { showSettingsMenu = false },
                                state = settingsState.copy(
                                    voiceOptions = voiceOptions,
                                    displayedVoiceName = displayedVoiceName,
                                    availableConcepts = availableConcepts,
                                    selectedConcept = selectedConcept,
                                    isLoadingConcepts = availableConcepts.isEmpty()
                                ),
                                onAvatarChange = { avatarCode ->
                                    settingsState = settingsState.copy(selectedAvatar = avatarCode)
                                    ttsController.switchCharacter(avatarCode)
                                    if (avatarCode != "disable") {
                                        ttsController.applyDefaultsForAvatarLanguage(avatarCode, currentLanguage)
                                    } else {
                                        if (ttsState.isSpeaking) ttsController.stop()
                                    }
                                },
                                onVoiceChange = { selectedDisplayName ->
                                    val selectedVoice = ttsState.availableVoices.find {
                                        ttsController.formatVoiceName(it) == selectedDisplayName
                                    }
                                    selectedVoice?.let {
                                        ttsController.setVoice(it)
                                        if (ttsState.isSpeaking) {
                                            ttsController.stop()
                                            ttsController.speak(aiMessageOutput)
                                        }
                                    }
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
                            )
                        }
                    }
                }
            }
        }

        // Floating Input Section (overlaid at bottom)
        Surface(
            modifier = Modifier
                .onSizeChanged { size ->
                    inputSectionHeight = with(density) { size.height.toDp() }
                }
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .imePadding(),
            color = Color.White,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Show autosuggestions when NOT listening
                if (!sttState.isListening && showAutosuggestions) {
                    AutoSuggestionChips(
                        suggestions = autosuggestions,
                        visible = true,
                        onSuggestionClick = {
                            chatViewModel.tapAutosuggestion(it, context)
                            chatViewModel.hideAutosuggestions()
                        }
                    )

                }
                // Input Section or Listening Overlay
                if (!sttState.isListening) {
                    InputSection(
                        textValue = chatState.inputText,
                        onTextChange = { chatViewModel.updateInputText(it) },
                        onSpeakClick = {
                            if (permissionGranted && sttState.isInitialized) {
                                sttController.startListening("en-IN")
                            } else if (!permissionGranted) {
                                permissionLauncher.launch(RECORD_AUDIO)
                            }
                        },
                        onSendClick = {
                            if (chatState.inputText.isNotBlank()) {
                                chatViewModel.sendMessage(chatState.inputText, context)
                                chatViewModel.updateInputText("")
                                keyboardController?.hide()
                            }
                        }
                    )

                } else {
                    ListeningOverlay(
                        text = sttState.resultText,
                        onStopClick = { sttController.stopListening() }
                    )
                }
            }
        }
    }


        AppDialog(
            show = showSessionResumeDialog,
            title = stringResource(R.string.existing_session_found),
            message = stringResource(R.string.resume_or_start_fresh),
            confirmText = stringResource(R.string.continue_session),
            dismissText = stringResource(R.string.start_new),

            onConfirm = {
                pendingConceptSelection?.let { concept ->
                    chatViewModel.selectConcept(concept, context)
                }
                showSessionResumeDialog = false
                pendingConceptSelection = null
                showSettingsMenu = false
            },

            onDismiss = {
                pendingConceptSelection?.let { concept ->
                    chatViewModel.startFreshSession(concept, context)
                }
                showSessionResumeDialog = false
                pendingConceptSelection = null
                showSettingsMenu = false
            }
        )

}