package com.anurag.eduai.ui.screens.simlation

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.ui.screens.simlation.component.SimChatBubble
import com.anurag.eduai.ui.screens.simlation.component.SimChatMessage
import com.anurag.eduai.ui.screens.simlation.component.SimulationWebView
import com.anurag.eduai.ui.viewModel.SimAgentUiState
import com.anurag.eduai.ui.viewModel.SimulationAgentViewModel
import com.anurag.eduai.ui.viewmodel_factory.SimulationAgentViewmodelFactory
import kotlinx.coroutines.launch

/** Main chat screen with WebView and chat interface */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationAgentScreen(
    simulationId: String,
    onNavigateBack: () -> Unit
) {

    val viewModel: SimulationAgentViewModel = viewModel(
        factory = SimulationAgentViewmodelFactory()
    )
    val uiState by viewModel.uiState.collectAsState()
    val sessionData by viewModel.sessionData.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Local state
    var userInput by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<SimChatMessage>>(emptyList()) }
    var showWebViewPopup by remember { mutableStateOf(false) }
    var simulationUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentConceptTitle by remember { mutableStateOf("") }
    var isSessionComplete by remember { mutableStateOf(false) }

    // Handle back press - clear data and navigate back
    BackHandler {
        viewModel.resetSession()
        onNavigateBack()
    }

    // Initialize session with the provided simulationId
    LaunchedEffect(simulationId) {
        viewModel.startNewSession(simulationId)
    }

    // Handle UI state changes
    LaunchedEffect(uiState, sessionData) {
        when (val state = uiState) {
            is SimAgentUiState.Success -> {
                sessionData?.let { session ->
                    // Add teacher message to chat
                    val teacherMessage =
                        SimChatMessage(
                            text = session.teacherMessage.text,
                            isFromTeacher = true,
                            timestamp = System.currentTimeMillis()
                        )
                    messages = messages + teacherMessage

                    // Update simulation URLs
                    val urls = mutableListOf<String>()
                    urls.add(session.simulation.htmlUrl)

                    session.simulation.paramChange?.let { change ->
                        // If there's a param change, show before and after
                        urls.clear()
                        urls.add(change.beforeUrl)
                        urls.add(change.afterUrl)
                    }
                    simulationUrls = urls
                    showWebViewPopup = urls.isNotEmpty()

                    // Update concept title
                    session.concepts.currentConcept?.let { currentConceptTitle = it.title }

                    // Check if session is complete
                    isSessionComplete = session.learningState.sessionComplete

                    // Auto-scroll to bottom
                    if (messages.isNotEmpty()) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                }
            }
            is SimAgentUiState.Error -> {
                // Show error message
                val errorMessage =
                    SimChatMessage(
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
                            text = "Simulation AI",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentConceptTitle.isNotEmpty()) {
                            Text(
                                text = currentConceptTitle,
                                fontSize = 12.sp,
                                color = Color.Gray
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
            // Input field at bottom
            Surface(shadowElevation = 8.dp, tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        placeholder = { Text("Type your message...") },
                        maxLines = 3,
                        enabled = !isSessionComplete && uiState !is SimAgentUiState.Loading
                    )

                    FloatingActionButton(
                        onClick = {
                            if (userInput.isNotBlank()) {
                                // Add user message to chat
                                val userMessage =
                                    SimChatMessage(
                                        text = userInput,
                                        isFromTeacher = false,
                                        timestamp = System.currentTimeMillis()
                                    )
                                messages = messages + userMessage

                                // Send to API
                                viewModel.sendStudentResponse(userInput)

                                // Clear input
                                userInput = ""
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor =
                            if (userInput.isBlank() || isSessionComplete)
                                MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Chat messages - only visible in top 35%
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

                // Loading indicator
                if (uiState is SimAgentUiState.Loading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Teacher is thinking...")
                        }
                    }
                }

                // Session complete message
                if (isSessionComplete) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎉 Session Complete! 🎉",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // WebView Popup with slide-up animation
            AnimatedVisibility(
                visible = showWebViewPopup,
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
                        .padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // WebView content
                        when (simulationUrls.size) {
                            1 -> {
                                // Single WebView
                                SimulationWebView(
                                    url = simulationUrls[0],
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            2 -> {
                                // Two WebViews stacked vertically (Before/After comparison)
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Before WebView
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(8.dp),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 4.dp
                                        )
                                    ) {
                                        Column {
                                            Surface(
                                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Before",
                                                    modifier = Modifier.padding(6.dp),
                                                    fontSize = 13.sp,
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

                                    // After WebView
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(8.dp),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 4.dp
                                        )
                                    ) {
                                        Column {
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "After",
                                                    modifier = Modifier.padding(6.dp),
                                                    fontSize = 13.sp,
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
                                // Fallback or empty state
                                if (simulationUrls.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No simulation to display")
                                    }
                                } else {
                                    SimulationWebView(
                                        url = simulationUrls[0],
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        // Close button overlay
                        IconButton(
                            onClick = { showWebViewPopup = false },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                tonalElevation = 6.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}