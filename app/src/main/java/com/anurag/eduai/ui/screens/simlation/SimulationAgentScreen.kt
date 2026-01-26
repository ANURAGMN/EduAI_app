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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.R
import com.anurag.eduai.ui.screens.simlation.component.SimChatBubble
import com.anurag.eduai.ui.screens.simlation.component.SimChatMessage
import com.anurag.eduai.ui.screens.simlation.component.SimInputSection
import com.anurag.eduai.ui.screens.simlation.component.SimulationWebView
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.SimAgentUiState
import com.anurag.eduai.ui.viewModel.SimulationAgentViewModel
import com.anurag.eduai.ui.viewModel.SpeechToText
import com.anurag.eduai.ui.viewModel.TextToSpeech
import com.anurag.eduai.ui.viewmodel_factory.SimulationAgentViewmodelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationAgentScreen(
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
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var userInput by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<SimChatMessage>>(emptyList()) }
    var showWebViewPopup by remember { mutableStateOf(false) }
    var simulationUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentConceptTitle by remember { mutableStateOf("") }
    var isSessionComplete by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }
    var lastProcessedSpeechText by remember { mutableStateOf("") }

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

    LaunchedEffect(Unit) {
        sttController.initialize(context)
        ttsController.initialize(context)
    }

    BackHandler {
        viewModel.resetSession()
        onNavigateBack()
    }

    LaunchedEffect(simulationId) {
        viewModel.startNewSession(simulationId)
    }

    LaunchedEffect(sttState.resultText) {
        if (sttState.resultText.isNotEmpty() &&
            sttState.resultText != lastProcessedSpeechText &&
            !sttState.isListening
        ) {
            lastProcessedSpeechText = sttState.resultText
            userInput = sttState.resultText
        }
    }

    LaunchedEffect(uiState, sessionData) {
        when (val state = uiState) {
            is SimAgentUiState.Success -> {
                sessionData?.let { session ->
                    val teacherMessage = SimChatMessage(
                        text = session.teacherMessage.text,
                        isFromTeacher = true,
                        timestamp = System.currentTimeMillis()
                    )
                    messages = messages + teacherMessage

                    if (!ttsState.isSpeaking) {
                        ttsController.speak(session.teacherMessage.text)
                    }

                    val urls = mutableListOf<String>()
                    urls.add(session.simulation.htmlUrl)

                    session.simulation.paramChange?.let { change ->
                        urls.clear()
                        urls.add(change.beforeUrl)
                        urls.add(change.afterUrl)
                    }
                    simulationUrls = urls
                    showWebViewPopup = urls.isNotEmpty()

                    session.concepts.currentConcept?.let {
                        currentConceptTitle = it.title
                    }

                    isSessionComplete = session.learningState.sessionComplete

                    if (messages.isNotEmpty()) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                }
            }
            is SimAgentUiState.Error -> {
                val errorMessage = SimChatMessage(
                    text = "Error: ${state.message}",
                    isFromTeacher = true,
                    timestamp = System.currentTimeMillis()
                )
                messages = messages + errorMessage
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.sim_title),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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

                        val userMessage = SimChatMessage(
                            text = userInput,
                            isFromTeacher = false,
                            timestamp = System.currentTimeMillis()
                        )
                        messages = messages + userMessage
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
                onSizeChanged = { /* No longer needed */ }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .align(Alignment.TopCenter),
                state = listState
            ) {
                items(messages) { message ->
                    SimChatBubble(message = message)
                }

                if (uiState is SimAgentUiState.Loading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimens.spaceMedium),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(dimens.iconLarge)
                            )
                            Spacer(modifier = Modifier.width(dimens.spaceSmall))
                            Text(
                                text = stringResource(R.string.sim_teacher_thinking),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                if (isSessionComplete) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimens.spaceMedium),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(dimens.spaceMedium),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.sim_session_complete),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !ttsState.isSpeaking && simulationUrls.isNotEmpty(),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f)
                        .padding(horizontal = dimens.spaceMedium),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = dimens.cardElevation
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (simulationUrls.size) {
                            1 -> {
                                SimulationWebView(
                                    url = simulationUrls[0],
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            2 -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(dimens.spaceSmall),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = dimens.cardElevation
                                        )
                                    ) {
                                        Column {
                                            Surface(
                                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.sim_before_label),
                                                    modifier = Modifier.padding(dimens.spaceSmall),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                            }
                                            SimulationWebView(
                                                url = simulationUrls[0],
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(dimens.spaceSmall),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = dimens.cardElevation
                                        )
                                    ) {
                                        Column {
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.sim_after_label),
                                                    modifier = Modifier.padding(dimens.spaceSmall),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                            SimulationWebView(
                                                url = simulationUrls[1],
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {
                                if (simulationUrls.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.sim_no_simulation),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                } else {
                                    SimulationWebView(
                                        url = simulationUrls[0],
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { showWebViewPopup = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(dimens.spaceSmall)
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                tonalElevation = dimens.cardElevation
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.sim_close_simulation),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(dimens.spaceSmall)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}