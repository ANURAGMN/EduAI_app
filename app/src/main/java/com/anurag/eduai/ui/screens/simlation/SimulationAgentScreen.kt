package com.anurag.eduai.ui.screens.simlation

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.R
import com.anurag.eduai.ui.screens.simlation.component.SimAgentMessage
import com.anurag.eduai.ui.screens.simlation.component.SimInputSection
import com.anurag.eduai.ui.screens.simlation.component.SimulationWebView
import com.anurag.eduai.ui.viewModel.SimAgentUiState
import com.anurag.eduai.ui.viewModel.SimulationAgentViewModel
import com.anurag.eduai.ui.viewModel.SpeechToText
import com.anurag.eduai.ui.viewModel.TextToSpeech
import com.anurag.eduai.ui.viewmodel_factory.SimulationAgentViewmodelFactory
import kotlinx.coroutines.delay

/** Main simulation screen with popup WebView overlay */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationAgentScreen(
    simulationId: String,
    onNavigateBack: () -> Unit,
    ttsController: TextToSpeech = viewModel(),
    sttController: SpeechToText = viewModel()
) {
    val viewModel: SimulationAgentViewModel = viewModel(factory = SimulationAgentViewmodelFactory())
    val uiState by viewModel.uiState.collectAsState()
    val sessionData by viewModel.sessionData.collectAsState()
    val ttsState by ttsController.state.collectAsState()
    val sttState by sttController.state.collectAsState()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val processor = remember {
        com.anurag.eduai.ui.screens.chatbotscreen.components.text.TextProcessor()
    }

    // State
    var userInput by remember { mutableStateOf("") }
    var currentTeacherMessage by remember { mutableStateOf("") }
    var showWebView by remember { mutableStateOf(false) }
    var simulationUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentConceptTitle by remember { mutableStateOf("") }
    var isSessionComplete by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }
    var lastProcessedSpeechText by remember { mutableStateOf("") }
    var isWaitingForResponse by remember { mutableStateOf(false) }

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

    // Initialize session
    LaunchedEffect(simulationId) { viewModel.startNewSession(simulationId) }

    // Handle STT result
    LaunchedEffect(sttState.resultText) {
        if (sttState.resultText.isNotEmpty() &&
            sttState.resultText != lastProcessedSpeechText &&
            !sttState.isListening
        ) {
            lastProcessedSpeechText = sttState.resultText
            userInput = sttState.resultText
        }
    }

    // Hide WebView when new message arrives, show after TTS completes
    LaunchedEffect(uiState, sessionData) {
        when (val state = uiState) {
            is SimAgentUiState.Success -> {
                isWaitingForResponse = false
                sessionData?.let { session ->
                    // New message arrived - hide WebView and update text
                    showWebView = false
                    currentTeacherMessage = session.teacherMessage.text

                    // Start TTS with word positions for highlighting
                    if (!ttsState.isSpeaking) {
                        val processed = processor.process(session.teacherMessage.text)
                        ttsController.speak(session.teacherMessage.text, processed)
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

                    // Update concept title
                    session.concepts.currentConcept?.let { currentConceptTitle = it.title }

                    // Check if session is complete
                    isSessionComplete = session.learningState.sessionComplete
                }
            }
            is SimAgentUiState.Error -> {
                isWaitingForResponse = false
                currentTeacherMessage = "Error: ${state.message}"
            }
            is SimAgentUiState.Loading -> {
                isWaitingForResponse = true
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
            delay(300)
            showWebView = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background layer with scaffold
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .blur(if (showWebView) 4.dp else 0.dp),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.sim_ai_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentConceptTitle.isNotEmpty()) {
                                Text(
                                    text = currentConceptTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor =
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                )
            },
            bottomBar = {
                SimInputSection(
                    inputText = userInput,
                    sttState = sttState,
                    isSessionComplete = isSessionComplete,
                    isLoading = uiState is SimAgentUiState.Loading,
                    onTextChange = { userInput = it },
                    onSendClick = {
                        if (userInput.isNotBlank()) {
                            if (ttsState.isSpeaking) {
                                ttsController.stop()
                            }
                            viewModel.sendStudentResponse(userInput)
                            userInput = ""
                            focusManager.clearFocus()
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
                    onStopListening = { sttController.stopListening() },
                    onSizeChanged = { /* Not needed */ }
                )
            }
        ) { paddingValues ->
            // Scrollable text area or thinking overlay
            Box(modifier = Modifier.fillMaxSize()) {
                SimAgentMessage(
                    text = currentTeacherMessage,
                    isTyping = uiState is SimAgentUiState.Loading,
                    fullText = currentTeacherMessage,
                    ttsController = ttsController,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )

                // Thinking overlay when waiting for response
                AnimatedVisibility(
                    visible = isWaitingForResponse,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 5.dp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = stringResource(R.string.sim_teacher_thinking),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.processing_speech),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Semi-transparent overlay when WebView is shown
        AnimatedVisibility(
            visible = showWebView && !ttsState.isSpeaking && simulationUrls.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize().zIndex(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        // Popup WebView Card (foreground) - only visible after TTS finishes
        AnimatedVisibility(
            visible = showWebView && !ttsState.isSpeaking && simulationUrls.isNotEmpty(),
            enter = scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(200)
            ) + fadeOut(animationSpec = tween(200)),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // WebView content
                        if (simulationUrls.size == 1) {
                            SimulationWebView(url = simulationUrls[0])
                        } else if (simulationUrls.size == 2) {
                            // Before/After comparison
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = stringResource(R.string.sim_before_label),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    SimulationWebView(url = simulationUrls[0])
                                }
                                Text(
                                    text = stringResource(R.string.sim_after_label),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    SimulationWebView(url = simulationUrls[1])
                                }
                            }
                        }

                        // Close button with elevated background
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            shape = MaterialTheme.shapes.small,
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            IconButton(onClick = { showWebView = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.sim_close_simulation),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}