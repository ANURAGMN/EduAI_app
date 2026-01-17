package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.dao.ChapterDao
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.data.local.dao.SubjectDao
import com.anurag.eduai.data.local.entities.ChapterEntity
import com.anurag.eduai.debug.DebugLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConceptScreenState(
    val concepts: List<ConceptWithProgress> = emptyList(),
    val chapter: ChapterEntity? = null,
    val chapterId: String = "",
    val completedConceptsCount: Int = 0,
    val subjectName: String = "",
    val classLevel: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ConceptViewModel(
    private val conceptDao: ConceptDao,
    private val chapterDao: ChapterDao,
    private val progressDao: ProgressDao,
    private val subjectDao: SubjectDao,
    private val studentDao: StudentDao,
    private val sharedPrefs: SharedPreferenceUtils
) : ViewModel() {

    private val _state = MutableStateFlow(ConceptScreenState())
    val state: StateFlow<ConceptScreenState> = _state.asStateFlow()

    fun loadConcepts(chapterId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val concepts = conceptDao.getConceptsForChapterSync(chapterId)
                val chapter = chapterDao.getChapter(chapterId)
                val studentId = sharedPrefs.getUserId() ?: ""

                // Get subject and class level information
                val subject = chapter?.let { subjectDao.getSubject(it.subjectId) }
                val student = studentDao.getStudentSync(studentId)
                val classLevel = student?.classLevel ?: 7

                // Get progress for all concepts
                val conceptsWithProgress = concepts.mapIndexed { index, concept ->
                    val progress = progressDao.getProgress(
                        studentId = studentId,
                        itemType = "CONCEPT",
                        itemId = concept.conceptId
                    )

                    // Determine status with sequential unlocking logic
                    val status = determineConceptStatus(
                        progress = progress,
                        isFirstConcept = index == 0,
                        previousConceptStatus = if (index > 0) {
                            progressDao.getProgress(
                                studentId = studentId,
                                itemType = "CONCEPT",
                                itemId = concepts[index - 1].conceptId
                            )?.status
                        } else null
                    )

                    ConceptWithProgress(
                        concept = concept,
                        progress = progress,
                        status = status
                    )
                }

                // Auto-unlock first concept if not started
                if (conceptsWithProgress.isNotEmpty() &&
                    conceptsWithProgress[0].status == "NOT_STARTED") {
                    unlockFirstConcept(studentId, conceptsWithProgress[0].concept.conceptId)
                }

                // Count completed concepts
                val completedCount = conceptsWithProgress.count { it.status == "COMPLETED" }

                _state.value = _state.value.copy(
                    concepts = conceptsWithProgress,
                    chapter = chapter,
                    chapterId = chapterId,
                    completedConceptsCount = completedCount,
                    subjectName = subject?.subjectName ?: "",
                    classLevel = "Class $classLevel",
                    isLoading = false,
                    error = null
                )

                DebugLogger.debugLog("ConceptViewModel", "Loaded ${conceptsWithProgress.size} concepts")
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
            progressDao.updateProgressStatus(
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
}