package com.anurag.eduai.domain.simulation.usecase

/**
 * Pure Kotlin sealed class representing user intents/actions on the Simulation Agent screen.
 * All user interactions should be modeled as intents and dispatched to the ViewModel.
 * This ensures clear separation between UI events and business logic.
 */
sealed class SimulationIntent {
    /**
     * User sent a text response to the teacher message
     */
    data class SendUserResponse(val text: String) : SimulationIntent()

    /**
     * User changed simulation parameters in the WebView
     */
    data class ParametersChanged(val params: Map<String, Any>) : SimulationIntent()

    /**
     * User pressed back button
     */
    object OnBackPressed : SimulationIntent()

    /**
     * User updated input text field
     */
    data class UpdateInput(val text: String) : SimulationIntent()

    /**
     * TTS started speaking
     */
    object TtsStarted : SimulationIntent()

    /**
     * TTS finished speaking
     */
    object TtsStopped : SimulationIntent()

    /**
     * TTS trigger acknowledged (prevent re-triggering on config change)
     */
    object TtsTriggered : SimulationIntent()

    /**
     * Load available simulations
     */
    object LoadSimulations : SimulationIntent()

    /**
     * Start a new teaching session for a specific simulation
     */
    data class StartNewSession(val simulationId: String) : SimulationIntent()

    /**
     * Retry after error occurred
     */
    data class RetrySession(val simulationId: String) : SimulationIntent()

    /**
     * Avatar changed in settings
     */
    object AvatarChanged : SimulationIntent()

    /**
     * Voice changed in settings
     */
    object VoiceChanged : SimulationIntent()

    /**
     * Speech speed changed in settings
     */
    object SpeedChanged : SimulationIntent()

    /**
     * Submit a quiz answer
     */
    data class SubmitQuizAnswer(val answer: String) : SimulationIntent()

    /**
     * Dismiss the session resume dialog
     */
    object DismissSessionDialog : SimulationIntent()

    /**
     * Continue with existing session (don't start fresh)
     */
    data class ContinueExistingSession(val simulationId: String) : SimulationIntent()

    /**
     * Start fresh session (clear old session and start new one)
     */
    data class StartFreshSession(val simulationId: String) : SimulationIntent()
}
