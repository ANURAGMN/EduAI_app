package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.ChapterRepository
import com.anurag.eduai.repository.ConceptRepository
import com.anurag.eduai.repository.StudentLocalRepository
import com.anurag.eduai.repository.SubjectRepository
import com.anurag.eduai.ui.models.ConceptStatus
import com.anurag.eduai.ui.models.ConceptUiModel
import com.anurag.eduai.ui.models.ChapterProgressUiModel
import com.anurag.eduai.ui.screens.conceptscreen.dataclass.ConceptScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConceptViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val chapterRepository: ChapterRepository,
    private val subjectRepository: SubjectRepository,
    private val studentRepository: StudentLocalRepository,
    private val sharedPrefs: SharedPreferenceUtils
) : ViewModel() {

    private val _state = MutableStateFlow(ConceptScreenState())
    val state: StateFlow<ConceptScreenState> = _state.asStateFlow()

    fun loadConcepts(chapterId: String, type: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val concepts = conceptRepository.getConceptsForChapter(chapterId, type)
                val chapter = chapterRepository.getChapter(chapterId)
                val studentId = sharedPrefs.getUserId() ?: ""

                // Get subject and class level information
                val subject = chapter?.let { subjectRepository.getSubject(it.subjectId) }
                val student = studentRepository.getStudentSync(studentId)
                val classLevel = student?.classLevel ?: 7

                // Convert to UI models with status
                val conceptUiModels = concepts.mapIndexed { index, concept ->
                    val progress = conceptRepository.getProgress(
                        studentId = studentId,
                        itemType = "CONCEPT",
                        itemId = concept.conceptId
                    )

                    // Determine status with sequential unlocking logic
                    val status = determineConceptStatus(
                        progress = progress,
                        isFirstConcept = index == 0,
                        previousConceptStatus = if (index > 0) {
                            conceptRepository.getProgress(
                                studentId = studentId,
                                itemType = "CONCEPT",
                                itemId = concepts[index - 1].conceptId
                            )?.status
                        } else null
                    )

                    ConceptUiModel(
                        id = concept.conceptId,
                        name = concept.conceptName,
                        order = concept.orderIndex,
                        status = when (status) {
                            "COMPLETED" -> ConceptStatus.COMPLETED
                            "IN_PROGRESS", "STARTED" -> ConceptStatus.IN_PROGRESS
                            else -> ConceptStatus.NOT_STARTED
                        }
                    )
                }

                // Auto-unlock first concept if not started
                if (conceptUiModels.isNotEmpty() &&
                    conceptUiModels[0].status == ConceptStatus.NOT_STARTED) {
                    unlockFirstConcept(studentId, conceptUiModels[0].id)
                }

                // Count completed concepts
                val completedCount = conceptUiModels.count { it.status == ConceptStatus.COMPLETED }
                val totalCount = chapter?.totalConcepts ?: 0

                // progress UI model
                val progressUiModel = buildProgressUiModel(
                    completed = completedCount,
                    total = totalCount
                )

                _state.value = _state.value.copy(
                    concepts = conceptUiModels,
                    chapterName = chapter?.chapterName ?: "",
                    chapterId = chapterId,
                    progressUiModel = progressUiModel,
                    subjectName = subject?.subjectName ?: "",
                    classLevel = "Class $classLevel",
                    isLoading = false,
                    error = null
                )

                DebugLogger.debugLog("ConceptViewModel", "Loaded ${conceptUiModels.size} concepts")
            } catch (e: Exception) {
                DebugLogger.debugLog("ConceptViewModel", "Error: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun determineConceptStatus(
        progress: com.anurag.eduai.data.local.entities.ProgressEntity?,
        isFirstConcept: Boolean,
        previousConceptStatus: String?
    ): String {
        // If progress exists, use its status
        if (progress != null) {
            return progress.status
        }

        // First concept is always unlocked (IN_PROGRESS)
        if (isFirstConcept) {
            return "IN_PROGRESS"
        }

        // Unlock next concept only if previous is completed
        if (previousConceptStatus == "COMPLETED") {
            return "IN_PROGRESS"
        }

        // Otherwise, keep locked
        return "NOT_STARTED"
    }

    private suspend fun unlockFirstConcept(studentId: String, conceptId: String) {
        try {
            conceptRepository.updateProgressStatus(
                studentId = studentId,
                itemType = "CONCEPT",
                itemId = conceptId,
                newStatus = "IN_PROGRESS",
                timestamp = System.currentTimeMillis()
            )
            DebugLogger.debugLog("ConceptViewModel", "First concept unlocked: $conceptId")
        } catch (e: Exception) {
            DebugLogger.debugLog("ConceptViewModel", "Error unlocking first concept: ${e.message}")
        }
    }

    /**
     * ChapterProgressUiModel with all calculated progress data
     */
    private fun buildProgressUiModel(
        completed: Int,
        total: Int
    ): ChapterProgressUiModel {
        val safeTotal = total.coerceAtLeast(1)
        val fraction = completed.toFloat() / safeTotal

        return ChapterProgressUiModel(
            completed = completed,
            total = total,
            progressFraction = fraction.coerceIn(0f, 1f),
            progressPercentage = (fraction * 100).toInt(),
            remaining = (total - completed).coerceAtLeast(0)
        )
    }
}