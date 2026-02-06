package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.data.local.entities.ProgressEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.ConceptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConceptDetailScreenState(
    val concept: ConceptEntity? = null,
    val progress: ProgressEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val progressStatus: String = "NOT_STARTED"
)

@HiltViewModel
class ConceptDetailViewModel @Inject constructor(
    private val repository: ConceptRepository,
    private val sharedPrefs: SharedPreferenceUtils
) : ViewModel() {

    private val _state = MutableStateFlow(ConceptDetailScreenState())
    val state: StateFlow<ConceptDetailScreenState> = _state.asStateFlow()

    fun loadConcept(conceptId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val concept = repository.getConcept(conceptId)
                val studentId = sharedPrefs.getUserId() ?: ""

                val progress = repository.getProgress(
                    studentId = studentId,
                    itemType = "CONCEPT",
                    itemId = conceptId
                )

                // Auto-mark as STARTED if this is the first time opening
                val currentStatus = progress?.status ?: "NOT_STARTED"
                if (currentStatus == "NOT_STARTED" || currentStatus == "IN_PROGRESS") {
                    if (progress == null || progress.openedAt == null) {
                        markAsStartedAutomatically(studentId, conceptId)
                    }
                }

                val updatedProgress = repository.getProgress(
                    studentId = studentId,
                    itemType = "CONCEPT",
                    itemId = conceptId
                )

                _state.value = _state.value.copy(
                    concept = concept,
                    progress = updatedProgress,
                    progressStatus = updatedProgress?.status ?: "STARTED",
                    isLoading = false,
                    error = null
                )
                DebugLogger.debugLog("ConceptDetailViewModel", "Loaded concept: ${concept?.conceptName}, status: ${updatedProgress?.status}")
            } catch (e: Exception) {
                DebugLogger.debugLog("ConceptDetailViewModel", "Error loading concept: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private suspend fun markAsStartedAutomatically(studentId: String, conceptId: String) {
        try {
            repository.updateProgressStatus(
                studentId = studentId,
                itemType = "CONCEPT",
                itemId = conceptId,
                newStatus = "STARTED",
                timestamp = System.currentTimeMillis()
            )
            DebugLogger.debugLog("ConceptDetailViewModel", "Auto-marked as STARTED: $conceptId")
        } catch (e: Exception) {
            DebugLogger.debugLog("ConceptDetailViewModel", "Error auto-marking as started: ${e.message}")
        }
    }

    fun updateProgressStatus(status: String) {
        viewModelScope.launch {
            try {
                val studentId = sharedPrefs.getUserId() ?: return@launch
                val concept = _state.value.concept ?: return@launch

                // Validate status progression
                val currentStatus = _state.value.progressStatus
                if (!isValidStatusTransition(currentStatus, status)) {
                    DebugLogger.debugLog("ConceptDetailViewModel", "Invalid status transition: $currentStatus -> $status")
                    return@launch
                }

                // Update current concept status
                repository.updateProgressStatus(
                    studentId = studentId,
                    itemType = "CONCEPT",
                    itemId = concept.conceptId,
                    newStatus = status,
                    timestamp = System.currentTimeMillis()
                )

                // If completed, unlock next concept
                if (status == "COMPLETED") {
                    unlockNextConcept(studentId, concept)
                }

                // Reload the progress data
                val updatedProgress = repository.getProgress(
                    studentId = studentId,
                    itemType = "CONCEPT",
                    itemId = concept.conceptId
                )

                _state.value = _state.value.copy(
                    progress = updatedProgress,
                    progressStatus = status
                )

                DebugLogger.debugLog("ConceptDetailViewModel", "Updated status to: $status")
            } catch (e: Exception) {
                DebugLogger.debugLog("ConceptDetailViewModel", "Error updating status: ${e.message}")
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    private fun isValidStatusTransition(currentStatus: String, newStatus: String): Boolean {
        return when (currentStatus) {
            "NOT_STARTED" -> newStatus == "STARTED"
            "STARTED" -> newStatus in listOf("STARTED", "IN_PROGRESS")
            "IN_PROGRESS" -> newStatus in listOf("IN_PROGRESS", "COMPLETED")
            "COMPLETED" -> newStatus == "COMPLETED"
            else -> false
        }
    }

    private suspend fun unlockNextConcept(studentId: String, currentConcept: ConceptEntity) {
        try {
            val allConcepts = repository.getConceptsForChapter(currentConcept.chapterId, type = "STUDY")

            val nextConcept = allConcepts.firstOrNull {
                it.orderIndex == currentConcept.orderIndex + 1
            }

            if (nextConcept != null) {
                val nextProgress = repository.getProgress(
                    studentId = studentId,
                    itemType = "CONCEPT",
                    itemId = nextConcept.conceptId
                )

                if (nextProgress == null || nextProgress.status == "NOT_STARTED") {
                    repository.updateProgressStatus(
                        studentId = studentId,
                        itemType = "CONCEPT",
                        itemId = nextConcept.conceptId,
                        newStatus = "IN_PROGRESS",
                        timestamp = System.currentTimeMillis()
                    )
                    DebugLogger.debugLog("ConceptDetailViewModel", "Unlocked next concept: ${nextConcept.conceptName}")
                }
            }
        } catch (e: Exception) {
            DebugLogger.debugLog("ConceptDetailViewModel", "Error unlocking next concept: ${e.message}")
        }
    }
}