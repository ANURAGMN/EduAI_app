# SimulationAgentScreen Enhancement Implementation Plan

## Overview

This plan outlines the implementation of Speech-to-Text (STT) and Text-to-Speech (TTS) features in the SimulationAgentScreen, matching the functionality and UI design of the ChatbotScreen. The implementation will also refactor the monolithic screen into reusable components and implement conditional simulation visibility based on TTS state.

## Analysis Summary

### Current ChatbotScreen Features
- **STT Integration**: Uses `SpeechToText` ViewModel with permission handling
- **TTS Integration**: Uses `TextToSpeech` ViewModel with voice controls
- **Modular Components**: Well-organized component structure
  - `InputSection.kt` - Handles text/voice input
  - `ListeningOverlay.kt` - Shows STT state with wave animation
  - `VoiceWaveAnimation.kt` - Animated voice visualization
- **UI Patterns**: Smooth animations, rounded corners, gradient colors
- **Resource Management**: Uses `strings.xml` and `Dimens.kt` consistently

### Current SimulationAgentScreen Structure
- **Monolithic Design**: All UI in single file (429 lines)
- **Basic Input**: Simple text field with send button
- **WebView Integration**: Shows simulation in bottom 65% of screen
- **Chat Display**: Messages in top 35% of screen
- **No STT/TTS**: Missing voice interaction features

## User Review Required

> [!IMPORTANT]
> **Simulation Visibility Logic**
> 
> The simulation WebView will be hidden when TTS is speaking and only shown when:
> 1. TTS is NOT active (not speaking)
> 2. A simulation URL is available
> 
> This ensures users focus on the audio explanation without distraction from the simulation.

> [!WARNING]
> **Breaking Changes**
> 
> - The `SimulationAgentScreen.kt` file will be significantly refactored
> - New component files will be created in `simlation/component/` package
> - The screen will require `SpeechToText` and `TextToSpeech` ViewModels as parameters
> - Permission handling for `RECORD_AUDIO` will be added

## Proposed Changes

### Resource Files

#### [MODIFY] [strings.xml](file:///home/ragnar/Internship/EduAI_app/app/src/main/res/values/strings.xml)

Add simulation-specific string resources:
- `sim_type_or_speak` - "Type or speak..."
- `sim_send_message` - "Send message"
- `sim_start_listening` - "Start listening"
- `sim_stop_listening` - "Stop listening"
- `sim_teacher_thinking` - "Teacher is thinking..."
- `sim_session_complete` - "🎉 Session Complete! 🎉"
- `sim_no_simulation` - "No simulation to display"
- `sim_before_label` - "Before"
- `sim_after_label` - "After"
- `sim_close_simulation` - "Close simulation"

#### [MODIFY] [Dimens.kt](file:///home/ragnar/Internship/EduAI_app/app/src/main/java/com/anurag/eduai/ui/theme/Dimens.kt)

Add simulation-specific dimensions if needed:
- `simInputCornerRadius` - 24.dp (for input field)
- `simWaveHeight` - 80.dp (for voice animation)
- `simChatMaxHeight` - 0.35f (fraction for chat area)
- `simWebViewMaxHeight` - 0.65f (fraction for webview area)

---

### Component Package: `simlation/component/`

#### [NEW] [SimInputSection.kt](file:///home/ragnar/Internship/EduAI_app/app/src/main/java/com/anurag/eduai/ui/screens/simlation/component/SimInputSection.kt)

**Purpose**: Comprehensive input section handling text and voice input

**Features**:
- Text input field with send button
- Animated mic/send button transition (like chatbot)
- Voice listening overlay integration
- Permission handling for microphone
- Size change callback for layout adjustments

**Parameters**:
- `inputText: String` - Current input text
- `sttState: SpeechToText.STTState` - STT state
- `isSessionComplete: Boolean` - Disable input when session ends
- `isLoading: Boolean` - Disable during API calls
- `onTextChange: (String) -> Unit` - Text change callback
- `onSendClick: () -> Unit` - Send message callback
- `onSpeakClick: () -> Unit` - Start STT callback
- `onStopListening: () -> Unit` - Stop STT callback
- `onSizeChanged: (IntSize) -> Unit` - Size change callback

**Styling**: Match chatbot input section with rounded corners, border, and smooth animations

---

#### [NEW] [SimListeningOverlay.kt](file:///home/ragnar/Internship/EduAI_app/app/src/main/java/com/anurag/eduai/ui/screens/simlation/component/SimListeningOverlay.kt)

**Purpose**: Display STT listening state with visual feedback

**Features**:
- Voice wave animation at top
- "Listening..." text display
- Real-time transcription display
- Stop button
- Auto-scroll for long transcriptions

**Parameters**:
- `text: String` - Transcribed text
- `amplitude: Float` - Voice amplitude (0f to 1f)
- `onStopClick: () -> Unit` - Stop listening callback

**Styling**: White background, rounded top corners, smooth animations

---

#### [NEW] [SimVoiceWaveAnimation.kt](file:///home/ragnar/Internship/EduAI_app/app/src/main/java/com/anurag/eduai/ui/screens/simlation/component/SimVoiceWaveAnimation.kt)

**Purpose**: Animated voice visualization during STT

**Features**:
- Parabolic curve animation
- Flowing gradient colors
- Amplitude-responsive animation
- Idle pulse when not speaking
- Glow effect below wave

**Parameters**:
- `amplitude: Float` - Voice amplitude
- `isListening: Boolean` - Listening state
- `colors: List<Color>` - Gradient colors
- `modifier: Modifier` - Compose modifier

**Styling**: Reuse chatbot animation with gradient colors from theme

---

#### [NEW] [SimTopBar.kt](file:///home/ragnar/Internship/EduAI_app/app/src/main/java/com/anurag/eduai/ui/screens/simlation/component/SimTopBar.kt)

**Purpose**: Top app bar with title and concept info

**Features**:
- "Simulation AI" title
- Current concept subtitle
- Consistent Material3 styling

**Parameters**:
- `currentConceptTitle: String` - Current concept name

---

#### [NEW] [SimChatSection.kt](file:///home/ragnar/Internship/EduAI_app/app/src/main/java/com/anurag/eduai/ui/screens/simlation/component/SimChatSection.kt)

**Purpose**: Chat messages display area

**Features**:
- LazyColumn for messages
- Loading indicator
- Session complete card
- Auto-scroll to latest message
- TTS speaker icon (play/pause)

**Parameters**:
- `messages: List<SimChatMessage>` - Chat messages
- `isLoading: Boolean` - Loading state
- `isSessionComplete: Boolean` - Session completion state
- `ttsState: TextToSpeech.TTSState` - TTS state
- `listState: LazyListState` - Scroll state
- `onTTSClick: () -> Unit` - TTS control callback

---

#### [NEW] [SimWebViewSection.kt](file:///home/ragnar/Internship/EduAI_app/app/src/main/java/com/anurag/eduai/ui/screens/simlation/component/SimWebViewSection.kt)

**Purpose**: Simulation WebView display with conditional visibility

**Features**:
- Single or dual WebView (before/after)
- Slide-up animation
- Close button
- Conditional visibility based on TTS state

**Parameters**:
- `visible: Boolean` - Visibility state (based on TTS and URL availability)
- `simulationUrls: List<String>` - Simulation URLs
- `onClose: () -> Unit` - Close callback

**Visibility Logic**:
```kotlin
visible = !ttsState.isSpeaking && simulationUrls.isNotEmpty()
```

---

#### [MODIFY] [ChatBubble.kt](file:///home/ragnar/Internship/EduAI_app/app/src/main/java/com/anurag/eduai/ui/screens/simlation/component/ChatBubble.kt)

**Changes**:
- Use dimensions from `Dimens.kt` instead of hardcoded values
- Use colors from theme instead of hardcoded colors
- Add TTS speaker icon for teacher messages
- Maintain existing bubble design

---

### Main Screen

#### [MODIFY] [SimulationAgentScreen.kt](file:///home/ragnar/Internship/EduAI_app/app/src/main/java/com/anurag/eduai/ui/screens/simlation/SimulationAgentScreen.kt)

**Major Refactoring**:

1. **Add ViewModel Parameters**:
```kotlin
@Composable
fun SimulationAgentScreen(
    simulationId: String,
    onNavigateBack: () -> Unit,
    ttsController: TextToSpeech = viewModel(),
    sttController: SpeechToText = viewModel()
)
```

2. **Add State Management**:
- Collect TTS state: `val ttsState by ttsController.state.collectAsState()`
- Collect STT state: `val sttState by sttController.state.collectAsState()`
- Add permission state: `var permissionGranted by remember { mutableStateOf(false) }`
- Add input section height: `var inputSectionHeight by remember { mutableStateOf(0.dp) }`

3. **Add Permission Handling**:
```kotlin
val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    permissionGranted = isGranted
    sttController.handlePermissionResult(...)
}
```

4. **Implement TTS for Teacher Messages**:
- Speak teacher messages automatically when received
- Add play/pause controls in chat bubbles
- Stop TTS when user starts typing

5. **Replace Monolithic UI with Components**:
- Use `SimTopBar` for top app bar
- Use `SimChatSection` for chat display
- Use `SimInputSection` for input area
- Use `SimWebViewSection` for simulation display

6. **Implement Conditional Simulation Visibility**:
```kotlin
val showWebView = !ttsState.isSpeaking && simulationUrls.isNotEmpty()

SimWebViewSection(
    visible = showWebView,
    simulationUrls = simulationUrls,
    onClose = { /* manual close logic */ }
)
```

7. **Update Layout Structure**:
```kotlin
Scaffold(
    topBar = { SimTopBar(currentConceptTitle) },
    bottomBar = { 
        SimInputSection(
            inputText = userInput,
            sttState = sttState,
            isSessionComplete = isSessionComplete,
            isLoading = uiState is SimAgentUiState.Loading,
            onTextChange = { userInput = it },
            onSendClick = { /* send logic */ },
            onSpeakClick = { /* STT logic */ },
            onStopListening = { sttController.stopListening() },
            onSizeChanged = { size -> inputSectionHeight = with(density) { size.height.toDp() } }
        )
    }
) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        // Chat section at top
        SimChatSection(
            messages = messages,
            isLoading = uiState is SimAgentUiState.Loading,
            isSessionComplete = isSessionComplete,
            ttsState = ttsState,
            listState = listState,
            onTTSClick = { /* TTS control */ },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .align(Alignment.TopCenter)
        )
        
        // WebView section at bottom (conditionally visible)
        SimWebViewSection(
            visible = !ttsState.isSpeaking && simulationUrls.isNotEmpty(),
            simulationUrls = simulationUrls,
            onClose = { /* close logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .align(Alignment.BottomCenter)
        )
    }
}
```

## Verification Plan

### Automated Tests
- Build the project: `./gradlew assembleDebug`
- Check for compilation errors
- Verify resource references

### Manual Verification

1. **STT Functionality**:
   - Grant microphone permission
   - Tap mic button
   - Verify listening overlay appears
   - Speak and verify transcription
   - Verify message sends correctly

2. **TTS Functionality**:
   - Receive teacher message
   - Verify TTS starts automatically
   - Verify simulation hides during TTS
   - Tap speaker icon to pause/resume
   - Verify TTS stops when typing

3. **Simulation Visibility**:
   - Verify simulation hidden during TTS
   - Verify simulation shows when TTS stops
   - Verify simulation shows when URL available
   - Verify smooth animations

4. **UI Consistency**:
   - Compare input section with chatbot screen
   - Verify all text from strings.xml
   - Verify all dimensions from Dimens.kt
   - Verify responsive design on different screen sizes

5. **Component Refactoring**:
   - Verify all components render correctly
   - Verify no regression in existing functionality
   - Verify smooth integration between components
