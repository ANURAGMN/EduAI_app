package com.anurag.eduai.ui.screens.conceptscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.domain.progress.ProgressEventTracker
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val progressEventTracker: ProgressEventTracker,
    private val sharedPrefs: SharedPreferenceUtils
) : ViewModel() {

    companion object {
        private const val TAG = "ConceptSimulationVM"
    }

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
}
