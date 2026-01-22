package com.anurag.eduai.ui.screens.simlation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.anurag.eduai.ui.viewModel.SimulationInfo
import com.anurag.eduai.ui.viewmodel_factory.SimulationAgentViewmodelFactory
import kotlinx.coroutines.launch

/** Main chat screen with WebView and chat interface */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {

    val viewModel: SimulationAgentViewModel = viewModel(
        factory = SimulationAgentViewmodelFactory()
    )
    val uiState by viewModel.uiState.collectAsState()
    val sessionData by viewModel.sessionData.collectAsState()
    val availableSimulations by viewModel.availableSimulations.collectAsState()
    val simulationsLoading by viewModel.simulationsLoading.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Local state
    var userInput by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<SimChatMessage>>(emptyList()) }
    var showWebViewPopup by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedSimulation by remember { mutableStateOf<SimulationInfo?>(null) }
    var simulationUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentConceptTitle by remember { mutableStateOf("") }
    var isSessionComplete by remember { mutableStateOf(false) }

    // Load available simulations on first composition
    LaunchedEffect(Unit) {
        viewModel.loadAvailableSimulations()
    }

    // Start session when simulations are loaded and selected simulation is set
    LaunchedEffect(availableSimulations) {
        if (availableSimulations.isNotEmpty() && selectedSimulation == null) {
            val firstSim = availableSimulations.first()
            selectedSimulation = firstSim
            viewModel.startNewSession(firstSim.id)
        }
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

    // Function to reset and start new simulation
    fun switchSimulation(newSimulation: SimulationInfo) {
        selectedSimulation = newSimulation
        messages = emptyList()
        simulationUrls = emptyList()
        currentConceptTitle = ""
        isSessionComplete = false
        userInput = ""
        showWebViewPopup = false
        viewModel.resetSession()
        viewModel.startNewSession(newSimulation.id)
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
                actions = {
                    // Simulation selection dropdown
                    Box {
                        IconButton(
                            onClick = { isDropdownExpanded = true },
                            enabled = !simulationsLoading
                        ) {
                            if (simulationsLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Simulation"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            if (availableSimulations.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Loading...") },
                                    onClick = {},
                                    enabled = false
                                )
                            } else {
                                availableSimulations.forEach { simulation ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    simulation.title,
                                                    fontWeight = if (simulation == selectedSimulation)
                                                        FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (simulation.description.isNotEmpty()) {
                                                    Text(
                                                        simulation.description,
                                                        fontSize = 11.sp,
                                                        color = Color.Gray,
                                                        maxLines = 2
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            switchSimulation(simulation)
                                            isDropdownExpanded = false
                                        },
                                        leadingIcon = {
                                            if (simulation == selectedSimulation) {
                                                Text("✓", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            selectedSimulation?.let { switchSimulation(it) }
                        },
                        enabled = selectedSimulation != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Session"
                        )
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
                            imageVector = Icons.Default.Send,
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
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        selectedSimulation?.let { switchSimulation(it) }
                                    }
                                ) {
                                    Text("Start New Session")
                                }
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