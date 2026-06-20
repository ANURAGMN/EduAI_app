package com.ncert7.aitutorandlab.ui.screens.conceptscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.domain.progress.ProgressEventTracker
import com.ncert7.aitutorandlab.repository.ConceptRepository
import com.ncert7.aitutorandlab.service.analytics.SimulationAnalyticsTracker
import com.ncert7.aitutorandlab.service.analytics.SimulationInteraction
import com.ncert7.aitutorandlab.service.sync.DataSyncService
import com.ncert7.aitutorandlab.ui.screens.conceptscreen.dataclass.ConceptScreenState
import com.ncert7.aitutorandlab.utils.StreakManager
import com.ncert7.aitutorandlab.utils.getCurrentLanguageCode
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

    private val _isAdCheckPending = MutableStateFlow(true)
    val isAdCheckPending: StateFlow<Boolean> = _isAdCheckPending.asStateFlow()

    private val _simulationTitle = MutableStateFlow("")
    val simulationTitle: StateFlow<String> = _simulationTitle.asStateFlow()

    private val _simulationUrl = MutableStateFlow("")
    val simulationUrl: StateFlow<String> = _simulationUrl.asStateFlow()

    // Trigger for forcing recomposition after progress update
    private val _progressUpdateTrigger = MutableStateFlow(0)
    val progressUpdateTrigger: StateFlow<Int> = _progressUpdateTrigger.asStateFlow()

    /**
     * Mark the simulation URL for [conceptId] as loaded/completed.
     * Safe to call multiple times — idempotent at the DB level.
     *
     *  FIXED: Properly marks both SIMULATION (URL) and updates chapter progress
     * This ensures progress bars update in real-time across all 3 screens
     */
    fun markSimulationUrlCompleted(conceptId: String) {
        if (conceptId.isBlank()) return
        viewModelScope.launch {
            val studentId = sharedPrefs.getUserId() ?: run {
                DebugLogger.errorLog(TAG, "No studentId — cannot mark simulation URL completed")
                return@launch
            }
            val language = getCurrentLanguageCode()

            // Mark the URL as completed - this triggers chapter progress update via ProgressEventTracker
            progressEventTracker.markSimulationUrlCompleted(studentId, conceptId, language)
            DebugLogger.debugLog(TAG, " Simulation URL completed tracked: conceptId=$conceptId [$language]")
        }
    }

    /**
     * Prepare simulation viewer state. Ad gate runs at navigation time (before this screen).
     */
    fun initializeSimulationWithAdCheck(
        conceptId: String,
        simulationUrl: String? = null,
        simulationTitle: String? = null
    ) {
        _isAdCheckPending.value = true
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

                DebugLogger.debugLog(
                    "ConceptViewModel",
                    "Simulation viewer ready for $conceptId"
                )
            } catch (e: Exception) {
                DebugLogger.errorLog("ConceptViewModel", "Error initializing simulation: ${e.message} | ${e.stackTraceToString()}")
            } finally {
                _isAdCheckPending.value = false
            }
        }
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
     * ✅ FIXED: Uses ProgressEventTracker to ensure chapter progress is updated
     * and flows through all 3 screens (ProgressScreen, ChapterScreen, ConceptScreen header)
     */
    fun markSimulationCompleted(conceptId: String) {
        viewModelScope.launch {
            try {
                val studentId = sharedPrefs.getUserId() ?: ""
                val language = getCurrentLanguageCode()

                DebugLogger.debugLog(
                    TAG,
                    "🔄 markSimulationCompleted called for conceptId: $conceptId, studentId: $studentId [$language]"
                )

                if (studentId.isNotEmpty() && conceptId.isNotEmpty()) {
                    // Use ProgressEventTracker to ensure chapter progress is recalculated
                    // This triggers updates in ALL 3 screens via the reactive Flow
                    DebugLogger.debugLog(TAG, "📍 About to call progressEventTracker.markSimulationUrlCompleted with language=$language")
                    progressEventTracker.markSimulationUrlCompleted(studentId, conceptId, language)

                    DebugLogger.debugLog(
                        TAG,
                        "✅ Simulation URL marked as COMPLETED for concept: $conceptId [$language] - Progress bars should update!"
                    )

                    // Verify progress was actually saved
                    val progress = conceptRepository.getProgress(studentId, "SIMULATION", conceptId, language)
                    DebugLogger.debugLog(
                        TAG,
                        "🔍 Verification: Progress for SIMULATION/$conceptId/$language = ${progress?.status ?: "NOT FOUND"} (progress was ${if (progress != null) "FOUND" else "NOT FOUND"})"
                    )

                    // Update user's streak when simulation is completed
                    streakManager.onConceptOpened { newStreak ->
                        DebugLogger.debugLog(TAG, "Streak updated to: $newStreak on simulation completion")
                    }

                    SimulationAnalyticsTracker.trackSimulationComplete(
                        conceptId = conceptId,
                        interaction = SimulationInteraction.URL
                    )

                    // Trigger real-time sync to Firestore
                    if (progress != null) {
                        DebugLogger.debugLog(TAG, "📤 Syncing progress to Firestore for progressId=${progress.progressId}")
                        DataSyncService.syncProgressUpdate(progress.progressId, studentId)
                    } else {
                        DebugLogger.errorLog(TAG, "⚠️ Progress was null after marking - sync skipped")
                    }

                    // Force UI recomposition by incrementing trigger
                    _progressUpdateTrigger.value = _progressUpdateTrigger.value + 1
                    DebugLogger.debugLog(TAG, "🔄 UI recomposition triggered: ${_progressUpdateTrigger.value}")
                } else {
                    DebugLogger.errorLog(
                        TAG,
                        " Failed to mark simulation completed - studentId: $studentId, conceptId: $conceptId"
                    )
                }
            } catch (e: Exception) {
                DebugLogger.errorLog(
                    TAG,
                    " Error marking simulation completed: ${e.message} | ${e.stackTraceToString()}"
                )
            }
        }
    }
}
