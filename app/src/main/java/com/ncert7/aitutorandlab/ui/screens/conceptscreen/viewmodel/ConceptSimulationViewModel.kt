package com.ncert7.aitutorandlab.ui.screens.conceptscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.domain.progress.ProgressEventTracker
import com.ncert7.aitutorandlab.repository.ConceptRepository
import com.ncert7.aitutorandlab.service.sync.DataSyncService
import com.ncert7.aitutorandlab.ui.screens.conceptscreen.dataclass.ConceptScreenState
import com.ncert7.aitutorandlab.utils.StreakManager
import com.ncert7.aitutorandlab.utils.isKannada
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lightweight ViewModel used by [ConceptSimulationViewer] to track simulation URL progress.
 * Delegates to [ProgressEventTracker] which handles the full chain:
 *   1. Write to progress table
 *   2. Recalculate chapter progress
 *   3. Record streak activity
 */
@HiltViewModel
class ConceptSimulationViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val progressEventTracker: ProgressEventTracker,
    private val streakManager: StreakManager,
    private val sharedPrefs: SharedPreferenceUtils
) : ViewModel() {

    companion object {
        private const val TAG = "ConceptSimulationVM"
    }
    private val _state = MutableStateFlow(ConceptScreenState())
    val state: StateFlow<ConceptScreenState> = _state.asStateFlow()

    private val _showAdBeforeSimulation = MutableStateFlow(false)
    val showAdBeforeSimulation: StateFlow<Boolean> = _showAdBeforeSimulation.asStateFlow()

    private val _simulationTitle = MutableStateFlow("")
    val simulationTitle: StateFlow<String> = _simulationTitle.asStateFlow()

    private val _simulationUrl = MutableStateFlow("")
    val simulationUrl: StateFlow<String> = _simulationUrl.asStateFlow()

    /**
     * Mark the simulation URL for [conceptId] as loaded/completed.
     * Safe to call multiple times — idempotent at the DB level.
     */
    fun markSimulationUrlCompleted(conceptId: String) {
        if (conceptId.isBlank()) return
        viewModelScope.launch {
            val studentId = sharedPrefs.getUserId() ?: run {
                DebugLogger.errorLog(TAG, "No studentId — cannot mark simulation URL completed")
                return@launch
            }
            progressEventTracker.markSimulationUrlCompleted(studentId, conceptId)
            DebugLogger.debugLog(TAG, "Simulation URL completed tracked: conceptId=$conceptId")
        }
    }

    /**
     * Check if ad should be shown before viewing a simulation
     * Shows ad AFTER the first 3 simulations are completed
     * 1st, 2nd, 3rd simulations = NO AD
     * 4th simulation onwards = ALWAYS SHOW AD
     * Returns true if ad should be shown before simulation
     */
    suspend fun shouldShowAdBeforeSimulation(): Boolean {
        return try {
            val studentId = sharedPrefs.getUserId() ?: ""
            if (studentId.isEmpty()) return false

            val todayCompleted = conceptRepository.getTodayCompletedSimulations(studentId)

            // Show ad if user has completed 5 or more simulations today (6th onwards)
            // This means: 1st 5 simulations per day = no ad, 6th onwards = always show ad
            val shouldShow = todayCompleted >= 5

            DebugLogger.debugLog(
                "ConceptViewModel",
                "shouldShowAdBeforeSimulation: $shouldShow | Simulations completed today: $todayCompleted"
            )

            shouldShow
        } catch (e: Exception) {
            DebugLogger.errorLog("ConceptViewModel", "Error checking ad before simulation: ${e.message}")
            false
        }
    }

    /**
     * Initialize ad display when entering simulation viewer
     *
     * @param conceptId The ID of the concept
     * @param simulationUrl Optional pre-computed URL (if provided, skips state search)
     * @param simulationTitle Optional title (if provided, uses this instead of searching state)
     *
     * If URL/title are not provided, searches in ViewModel state
     * If provided, uses them directly (useful for PracticeSimulationCard which has data ready)
     */
    fun initializeSimulationWithAdCheck(
        conceptId: String,
        simulationUrl: String? = null,
        simulationTitle: String? = null
    ) {
        viewModelScope.launch {
            try {
                if (simulationUrl != null && simulationTitle != null) {
                    _simulationTitle.value = simulationTitle
                    _simulationUrl.value = simulationUrl

                    DebugLogger.debugLog(
                        "ConceptViewModel",
                        "initializeSimulationWithAdCheck (external data) for $conceptId: title=$simulationTitle, url=$simulationUrl"
                    )
                } else {
                    val concept = _state.value.concepts.find { it.id == conceptId }
                    _simulationTitle.value = concept?.name ?: "Simulation"

                    if (concept == null) {
                        DebugLogger.errorLog("ConceptViewModel", "Concept not found in state for ID: $conceptId")
                        _simulationUrl.value = ""
                        return@launch
                    }

                    //TODO:handle nullable
                    val selectedUrl = getSelectedSimulationUrl(concept.simulationUrl,concept.simulationUrlKannada)
                    _simulationUrl.value = selectedUrl ?: ""

                    DebugLogger.debugLog(
                        "ConceptViewModel",
                        "initializeSimulationWithAdCheck (state search) for $conceptId: title=${_simulationTitle.value}, url=${_simulationUrl.value}"
                    )
                }

                // Check if ad should be shown
                val shouldShowAd = shouldShowAdBeforeSimulation()
                _showAdBeforeSimulation.value = shouldShowAd

                DebugLogger.debugLog(
                    "ConceptViewModel",
                    "Ad check result: shouldShowAd=$shouldShowAd"
                )
            } catch (e: Exception) {
                DebugLogger.errorLog("ConceptViewModel", "Error initializing simulation ad: ${e.message} | ${e.stackTraceToString()}")
                _showAdBeforeSimulation.value = false
            }
        }
    }

    /**
     * Dismiss the ad and allow simulation to load
     */
    fun dismissAd() {
        _showAdBeforeSimulation.value = false
        DebugLogger.debugLog("ConceptViewModel", "Ad dismissed, showing simulation")
    }

    /**
     * Selects the appropriate simulation URL based on device language preference
     * NO FALLBACK: If Kannada is selected but Kannada URL doesn't exist, returns null
     * If English is selected but English URL doesn't exist, returns null
     */
    private fun getSelectedSimulationUrl(
        englishUrl: String?,
        kannadaUrl: String?
    ): String? {
        return if (isKannada()) {
            // Use ONLY Kannada URL, no fallback to English
            kannadaUrl?.takeIf { it.isNotBlank() && it != "Not found" }
        } else {
            // Use ONLY English URL
            englishUrl?.takeIf { it.isNotBlank() && it != "Not found" }
        }
    }
    /**
     * Track that a simulation has completed
     * Called when the simulation WebView finishes loading successfully
     * Sets status to COMPLETED and records both startedAt and completedAt times
     * This simplifies the flow - when simulation loads, it's considered completed by the user
     */
    fun markSimulationCompleted(conceptId: String) {
        viewModelScope.launch {
            try {
                val studentId = sharedPrefs.getUserId() ?: ""
                val currentTime = System.currentTimeMillis()
                DebugLogger.debugLog(
                    "ConceptViewModel",
                    "markSimulationCompleted called for conceptId: $conceptId, studentId: $studentId"
                )

                if (studentId.isNotEmpty() && conceptId.isNotEmpty()) {
                    val language = if (isKannada()) "kn" else "en"
                    conceptRepository.updateProgressStatus(
                        studentId = studentId,
                        itemType = "CONCEPT",
                        itemId = conceptId,
                        language = language,
                        newStatus = "COMPLETED",
                        progressPercentage = 100,
                        timestamp = currentTime
                    )
                    DebugLogger.debugLog(
                        "ConceptViewModel",
                        " Simulation marked as COMPLETED for concept: $conceptId at $currentTime ($language)"
                    )

                    // Update user's streak when simulation is completed
                    DebugLogger.debugLog(
                        "ConceptViewModel",
                        "Updating streak on simulation completion"
                    )
                    streakManager.onConceptOpened { newStreak ->
                        DebugLogger.debugLog(
                            "ConceptViewModel",
                            "Streak updated to: $newStreak on simulation completion"
                        )
                    }

                    // Trigger real-time sync to Firestore
                    // Get the progress ID from the database to sync
                    val progress = conceptRepository.getProgress(studentId, "CONCEPT", conceptId, language)
                    if (progress != null) {
                        DataSyncService.syncProgressUpdate(progress.progressId, studentId)
                    }
                } else {
                    DebugLogger.errorLog(
                        "ConceptViewModel",
                        " Failed to mark simulation completed - studentId: $studentId, conceptId: $conceptId"
                    )
                }
            } catch (e: Exception) {
                DebugLogger.errorLog(
                    "ConceptViewModel",
                    " Error marking simulation completed: ${e.message} | ${e.stackTraceToString()}"
                )
            }
        }
    }
}
