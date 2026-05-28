package com.ncert7.aitutorandlab.ui.screens.chapterscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.domain.progress.ChapterProgressService
import com.ncert7.aitutorandlab.domain.progress.buildProgressUiModel
import com.ncert7.aitutorandlab.domain.progress.model.ProgressStatus
import com.ncert7.aitutorandlab.repository.ChapterRepository
import com.ncert7.aitutorandlab.repository.ConceptRepository
import com.ncert7.aitutorandlab.repository.SubjectRepository
import com.ncert7.aitutorandlab.ui.models.ChapterUiModel
import com.ncert7.aitutorandlab.ui.screens.chapterscreen.dataclass.ChapterUiState
import com.ncert7.aitutorandlab.utils.getLocalizedName
import com.ncert7.aitutorandlab.utils.isKannada
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChapterViewModel @Inject constructor(
    private val chapterRepository: ChapterRepository,
    private val subjectRepository: SubjectRepository,
    private val conceptRepository: ConceptRepository,
    private val sharedPrefs: SharedPreferenceUtils,
    private val chapterProgressService: ChapterProgressService,
) : ViewModel() {

    companion object {
        private const val TAG = "ChapterViewModel"
        private const val MATH_SUBJECT_ID = "5c0a6b6d-7c6b-4f35-9d5b-9fd0fd8e8a01"
    }

    private val _state = MutableStateFlow(ChapterUiState())
    val state: StateFlow<ChapterUiState> = _state.asStateFlow()

    fun loadChapters(subjectId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val userId = sharedPrefs.getUserId() ?: ""
                val language = if (isKannada()) "kn" else "en"
                val subject = subjectRepository.getSubject(subjectId)
                val chapters = chapterRepository.getChaptersForSubject(subjectId)
                val classLevel = 7 // Force class 7 syllabus display
                val isMathSubject = subjectId == MATH_SUBJECT_ID

                DebugLogger.debugLog(TAG, "loadChapters: subjectId=$subjectId, isMath=$isMathSubject, language=$language")
                // Seed the progress table in the background if needed
                viewModelScope.launch {
                    chapters.forEach { chapter ->
                        val existing = chapterProgressService.getChapterProgressBreakdown(userId, chapter.chapterId, language)
                        if (existing.overallPercentage == 0 && existing.studyPercentage == 0) {
                            chapterProgressService.updateChapterProgress(userId, chapter.chapterId, language)
                        }
                    }
                }

                // Check if this is a Math subject

                // Step 1: Filter chapters based on available concepts
                val filteredChapters = chapters.filter { chapter ->

                    val hasStudy = if (isMathSubject) {
                        // For Math: check if chapter has MATH PROBLEM concepts with valid problemId
                        conceptRepository.getMathProblemConceptCount(chapter.chapterId) > 0
                    } else {
                        // For other subjects: check if chapter has STUDY concepts
                        conceptRepository.getStudyConceptCount(chapter.chapterId) > 0
                    }

                    val studyConcepts = if (isMathSubject) {
                        conceptRepository.getConceptsForChapter(chapter.chapterId, "MATH PROBLEM")
                            .filter { it.problemId.isNotEmpty() }
                    } else {
                        conceptRepository.getConceptsForChapter(chapter.chapterId, "STUDY")
                    }

                    val hasSimulation = conceptRepository.getSimulationConceptCount(
                        chapter.chapterId,
                        language
                    ) > 0

                    val hasRevision = chapter.revisionId.isNotEmpty()

                    // Include chapter if it has at least one of: study, simulation, or revision
                    val shouldInclude = hasStudy || hasSimulation || hasRevision

                    DebugLogger.debugLog(
                        TAG,
                        "Chapter ${chapter.chapterId}: hasStudy=$hasStudy, hasSimulation=$hasSimulation, hasRevision=$hasRevision, include=$shouldInclude"
                    )

                    shouldInclude
                }

                DebugLogger.debugLog(
                    TAG,
                    "Filtered chapters: ${filteredChapters.size} / ${chapters.size} (isMath=$isMathSubject)"
                )

                // Step 2: Collect progress from the reactive Flow
                // This ensures the UI updates immediately when progress changes
                chapterRepository.getChapterWiseProgress(userId, subjectId, language)
                    .collect { progressSummaries ->
                        val progressMap = progressSummaries.associateBy { it.chapterId }

                        val chapterUiModels = filteredChapters.map { chapter ->
                            val summary = progressMap[chapter.chapterId]
                            val totalConcepts = summary?.totalConcepts ?: chapter.totalConcepts
                            val completedConcepts = summary?.completedConcepts ?: 0
                            val overallPct = summary?.completionPercentage ?: 0f

                            val status = when {
                                overallPct >= 100f -> ProgressStatus.COMPLETED
                                overallPct > 0f -> ProgressStatus.IN_PROGRESS
                                else -> ProgressStatus.NOT_STARTED
                            }
                            val progressUiModel = buildProgressUiModel(
                                completed = completedConcepts,
                                total = totalConcepts
                            )
                            ChapterUiModel(
                                id = chapter.chapterId,
                                orderIndex = chapter.orderIndex,
                                name = chapter.getLocalizedName(),
                                englishName = chapter.chapterName,
                                totalConcepts = totalConcepts,
                                completedConcepts = completedConcepts,
                                status = status,
                                revisionId = chapter.revisionId,
                                subjectId = chapter.subjectId,
                                progressUiModel = progressUiModel
                            )
                        }

                        _state.value = _state.value.copy(
                            chapters = chapterUiModels,
                            subjectName = subject?.getLocalizedName() ?: "",
                            classLevel = classLevel,
                            isLoading = false,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                DebugLogger.errorLog("ChapterViewModel", "Error loading chapters: ${e.message}")
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}