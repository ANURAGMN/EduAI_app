package com.anurag.eduai.ui.screens.chatbotscreen

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.R
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.screens.chatbotscreen.components.AgentMessageBubble
import com.anurag.eduai.ui.screens.chatbotscreen.components.DropDownMenuModel
import com.anurag.eduai.ui.screens.chatbotscreen.components.InputSection
import com.anurag.eduai.ui.screens.chatbotscreen.components.ListeningOverlay
import com.anurag.eduai.ui.screens.chatbotscreen.components.UserMessageBubble
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.IconPrimary
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.ui.viewModel.ChatViewModel
import com.anurag.eduai.ui.viewModel.SpeechToText
import com.anurag.eduai.ui.viewModel.TextToSpeech
import kotlinx.coroutines.delay

@Composable
fun ChatbotScreen(
    chatViewModel: ChatViewModel ,
    ttsController: TextToSpeech = viewModel(),
    sttController: SpeechToText = viewModel(),
) {
    val context = LocalContext.current

    // Observe states
    val ttsState by ttsController.state.collectAsState()
    val sttState by sttController.state.collectAsState()


    var currentAudioTime by remember { mutableFloatStateOf(0f) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Observe chat UI state
    val chatState by chatViewModel.uiState.collectAsState()

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
    val boyDisplayName = stringResource(R.string.boy)
    val girlDisplayName = stringResource(R.string.girl)
    val disableAvatar =stringResource(R.string.disable)
    val levelLowText = stringResource(R.string.level_low)
    val levelMediumText = stringResource(R.string.level_medium)
    val levelAdvancedText = stringResource(R.string.level_advanced)

    // Language selection state
    var selectedAvatar by remember { mutableStateOf("disable") }
    var selectedSpeed by remember { mutableStateOf("0.75x") }

    // Student level state
    var selectedStudentLevel by remember { mutableStateOf("medium") }

    val typingText by chatViewModel.typingText.collectAsState()
    val isTyping by chatViewModel.isTyping.collectAsState()

    // Collect available concepts and selected concept from ViewModel
    val availableConcepts by chatViewModel.availableConcepts.collectAsState()
    val selectedConcept by chatViewModel.selectedConcept.collectAsState()

    // Current language
    val currentLanguage by chatViewModel.currentLanguage.collectAsState()
    val voiceOptions = remember(ttsState.availableVoices, currentLanguage, selectedAvatar) {
        ttsController.getFilteredVoiceOptions(currentLanguage, selectedAvatar)
    }

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
    val displayedVoiceName = remember(ttsState.selectedVoice, currentLanguage, selectedAvatar) {
        if (ttsState.selectedVoice != null) {
            ttsController.formatVoiceName(ttsState.selectedVoice!!)
        } else {
            ttsController.getDefaultVoiceName(currentLanguage, selectedAvatar)
        }
    }

    // AI message output logic
    val aiMessageOutput = remember(isTyping, typingText) {
        when {
            isTyping -> typingText
            else -> ""
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        permissionGranted = hasPermission
        sttController.initialize(context)

        if (!hasPermission) {
            permissionLauncher.launch(RECORD_AUDIO)
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

    // Reset speech tracking when input is manually cleared
    LaunchedEffect(chatState.inputText) {
        if (chatState.inputText.isEmpty()) {
            lastProcessedSpeechText = ""
        }
    }
    LaunchedEffect(ttsState.isSpeaking) {
        if (ttsState.isSpeaking) {
            val startTime = System.currentTimeMillis()
            while (ttsState.isSpeaking) {
                currentAudioTime = (System.currentTimeMillis() - startTime) / 1000f
                delay(50)
            }
        } else {
            currentAudioTime = 0f
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Messages Column
                if (isConversationStarted) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(180.dp))
                        }
                        items(chatState.messages.size) { index ->
                            val message = chatState.messages[index]
                            if (message.isUser) {
                                UserMessageBubble(text = message.text)
                            } else {
                                AgentMessageBubble(
                                    text = message.text,
                                    onListenClick = {
                                        if (ttsState.isInitialized) {
                                            if (!ttsState.isSpeaking) {
                                                ttsController.speak(message.text)
                                            } else {
                                                ttsController.stop()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Avatar WebView
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = avatarPadding),
                    contentAlignment = if (isConversationStarted) Alignment.TopCenter else Alignment.Center
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

                // Icons Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.ClosedCaption,
                            contentDescription = "Captions",
                            tint = Color.Gray.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = { /* Handle Volume */ }) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Volume",
                            tint = Color.Gray.copy(alpha = 0.6f)
                        )
                    }
                    Box {
                        IconButton(onClick = { showSettingsMenu = !showSettingsMenu }) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Settings",
                                tint = Color.Gray.copy(alpha = 0.6f)
                            )
                        }

                        // Settings Dropdown Menu
                        DropdownMenu(
                            expanded = showSettingsMenu,
                            onDismissRequest = { showSettingsMenu = false },
                            modifier = Modifier
                                .background(White)
                                .border(1.dp, BrandPrimary, RoundedCornerShape(0.dp))
                        )
                        {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .widthIn(min = 220.dp, max = 320.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings),
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.titleSmall,
                                    )

                                    IconButton(
                                        onClick = { showSettingsMenu = false },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close Settings",
                                            tint = IconPrimary
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                Text(
                                    stringResource(R.string.select_avatar),
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(Modifier.height(8.dp))
                                DropDownMenuModel(
                                    label = stringResource(R.string.avatar),
                                    options = listOf(disableAvatar,boyDisplayName, girlDisplayName),
                                    selectedValue = when (selectedAvatar.lowercase()) {
                                        "disable"-> disableAvatar
                                        "girl" -> girlDisplayName
                                        "boy" -> boyDisplayName
                                        else -> disableAvatar
                                    },
                                    onValueSelected = { displayName ->
                                        val avatarCode = when (displayName) {
                                            disableAvatar -> "disable"
                                            girlDisplayName -> "girl"
                                            boyDisplayName -> "boy"
                                            else -> disableAvatar
                                        }
                                        selectedAvatar = avatarCode
                                        ttsController.switchCharacter(avatarCode)

                                        if (avatarCode != "disable") {
                                            ttsController.applyDefaultsForAvatarLanguage(avatarCode, currentLanguage)
                                        } else {
                                            if (ttsState.isSpeaking) ttsController.stop()
                                        }
                                    }
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    stringResource(R.string.select_voice),
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(Modifier.height(8.dp))
                                DropDownMenuModel(
                                    label = stringResource(R.string.voice),
                                    options = voiceOptions,
                                    selectedValue = displayedVoiceName,
                                    onValueSelected = { selectedDisplayName ->
                                        val selectedVoice =
                                            ttsState.availableVoices.find {
                                                ttsController.formatVoiceName(it) == selectedDisplayName
                                            }
                                        selectedVoice?.let {
                                            ttsController.setVoice(it)
                                            if (ttsState.isSpeaking) {
                                                ttsController.stop()
                                                ttsController.speak(aiMessageOutput)
                                            }
                                        }
                                    }
                                )

                                Spacer(Modifier.height(12.dp))

                                if (availableConcepts.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = BrandPrimary)
                                    }
                                    Text(
                                        text = stringResource(R.string.loading_topics),
                                        color = TextSecondary,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                            .padding(top = 8.dp)
                                    )
                                } else {
                                    DropDownMenuModel(
                                        label = stringResource(R.string.select_concepts),
                                        options = availableConcepts,
                                        selectedValue = selectedConcept
                                            ?: stringResource(R.string.tap_to_choose_topic),
                                        onValueSelected = { concept ->
                                            if (chatViewModel.hasExistingSession(
                                                    concept,
                                                    context
                                                )
                                            ) {
                                                showSessionResumeDialog = true
                                            } else {
                                                chatViewModel.selectConcept(concept,context)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    stringResource(R.string.select_student_level),
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(Modifier.height(8.dp))

                                DropDownMenuModel(
                                    label = stringResource(R.string.student_level),
                                    options = listOf(
                                        stringResource(R.string.level_low),
                                        stringResource(R.string.level_medium),
                                        stringResource(R.string.level_advanced)
                                    ),
                                    selectedValue = when (selectedStudentLevel) {
                                        "low" -> stringResource(R.string.level_low)
                                        "medium" -> stringResource(R.string.level_medium)
                                        "advanced" -> stringResource(R.string.level_advanced)
                                        else -> stringResource(R.string.level_medium)
                                    },
                                    onValueSelected = { displayName ->
                                        val levelCode = when (displayName) {
                                            levelLowText -> "low"
                                            levelMediumText -> "medium"
                                            levelAdvancedText -> "advanced"
                                            else -> "medium"
                                        }
                                        selectedStudentLevel = levelCode
                                        chatViewModel.setStudentLevel(levelCode)
                                        DebugLogger.debugLog("ChatBotScreen", "Student level changed to: $levelCode")
                                    }
                                )

                                Spacer(Modifier.height(12.dp))
                                Text(
                                    stringResource(R.string.select_speed),
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(Modifier.height(8.dp))
                                DropDownMenuModel(
                                    label = stringResource(R.string.speed),
                                    options = listOf("0.75x", "1.0x", "1.25x", "1.5x"),
                                    selectedValue = selectedSpeed,
                                    onValueSelected = { label ->
                                        selectedSpeed = label
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

            // Input Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Text Input field and Send Button
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !sttState.isListening,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                                    chatViewModel.sendMessage(chatState.inputText)
                                    // Hide keyboard after sending
                                    keyboardController?.hide()
                                }
                            }
                        )
                    }

                    // Listening Overlay
                    androidx.compose.animation.AnimatedVisibility(
                        visible = sttState.isListening,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListeningOverlay(
                            text = sttState.resultText,
                            onStopClick = {
                                sttController.stopListening()
                            }
                        )
                    }
                }
            }
        }
    }
