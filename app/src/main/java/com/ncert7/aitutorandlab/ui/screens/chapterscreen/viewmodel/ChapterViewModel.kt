package com.ncert7.aitutorandlab.ui.screens.chapterscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.domain.progress.ChapterProgressService
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
    private val chapterProgressService: ChapterProgressService
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
                val userId   = sharedPrefs.getUserId() ?: ""
                val language = if (isKannada()) "kn" else "en"
                val subject  = subjectRepository.getSubject(subjectId)
                val chapters = chapterRepository.getChaptersForSubject(subjectId)
                val isMathSubject = subjectId == MATH_SUBJECT_ID

                DebugLogger.debugLog(TAG, "loadChapters: subjectId=$subjectId, isMath=$isMathSubject, language=$language")

                // ── Background seed: ensure a chapter_agent_progress row exists ──────────
                // This is fire-and-forget; the reactive Flow below drives the UI.
                viewModelScope.launch {
                    chapters.forEach { chapter ->
                        val existing = chapterProgressService.getChapterProgressBreakdown(
                            userId, chapter.chapterId, language
                        )
                        if (existing.overallPercentage == 0 && existing.studyPercentage == 0) {
                            chapterProgressService.updateChapterProgress(userId, chapter.chapterId, language)
                        }
                    }
                }

                // ── Filter chapters that have at least one learning component ─────────────
                val filteredChapters = chapters.filter { chapter ->
                    val hasStudy = if (isMathSubject) {
                        conceptRepository.getMathProblemConceptCount(chapter.chapterId) > 0
                    } else {
                        conceptRepository.getStudyConceptCount(chapter.chapterId) > 0
                    }
                    val hasSimulation = conceptRepository.getSimulationConceptCount(chapter.chapterId, language) > 0
                    val hasRevision   = chapter.revisionId.isNotEmpty()

                    val shouldInclude = hasStudy || hasSimulation || hasRevision
                    DebugLogger.debugLog(
                        TAG,
                        "Chapter ${chapter.chapterId}: study=$hasStudy sim=$hasSimulation rev=$hasRevision → $shouldInclude"
                    )
                    shouldInclude
                }

                DebugLogger.debugLog(TAG, "Filtered: ${filteredChapters.size}/${chapters.size} chapters")

                // Pre-calculate flags for filtered chapters
                val chapterFlags = filteredChapters.associate { chapter ->
                    val hasStudy = if (isMathSubject) {
                        conceptRepository.getMathProblemConceptCount(chapter.chapterId) > 0
                    } else {
                        conceptRepository.getStudyConceptCount(chapter.chapterId) > 0
                    }
                    val hasSimulation = conceptRepository.getSimulationConceptCount(chapter.chapterId, language) > 0
                    val hasRevision   = chapter.revisionId.isNotEmpty()

                    chapter.chapterId to Triple(hasStudy, hasSimulation, hasRevision)
                }

                // ── Reactive collection: updates every time progress changes ──────────────
                // getChapterWiseProgress() returns a Flow backed by ProgressDao — so whenever
                // ANY progress row for this subject changes, the Flow emits and the UI redraws.
                chapterRepository.getChapterWiseProgress(userId, subjectId, language)
                    .collect { progressSummaries ->
                        DebugLogger.debugLog(TAG, "🔄 CHAPTER PROGRESS FLOW TRIGGERED - Updating ${progressSummaries.size} chapter(s)...")

                        val progressMap = progressSummaries.associateBy { it.chapterId }

                        val chapterUiModels = filteredChapters.map { chapter ->
                            val summary = progressMap[chapter.chapterId]
                            val flags = chapterFlags[chapter.chapterId] ?: Triple(false, false, false)

                            // ✅ FIXED: Use the real-time summary data directly
                            // This ensures consistent progress across all screens
                            val totalConcepts     = summary?.totalConcepts ?: 0
                            val completedConcepts = summary?.completedConcepts ?: 0
                            val overallPct        = summary?.completionPercentage ?: 0

                            val status = when {
                                overallPct >= 100 -> ProgressStatus.COMPLETED
                                overallPct > 0    -> ProgressStatus.IN_PROGRESS
                                else               -> ProgressStatus.NOT_STARTED
                            }

                            DebugLogger.debugLog(TAG, "  Chapter ${chapter.chapterName}: $completedConcepts/$totalConcepts ($overallPct%) - $status")

                            ChapterUiModel(
                                id               = chapter.chapterId,
                                orderIndex       = chapter.orderIndex,
                                name             = chapter.getLocalizedName(),
                                englishName      = chapter.chapterName,
                                totalConcepts    = totalConcepts,
                                completedConcepts = completedConcepts,
                                status           = status,
                                revisionId       = chapter.revisionId,
                                subjectId        = chapter.subjectId,
                                progressUiModel  = com.ncert7.aitutorandlab.ui.models.ChapterProgressUiModel(
                                    completed = completedConcepts,
                                    total = totalConcepts,
                                    progressFraction = overallPct / 100f,
                                    progressPercentage = overallPct,
                                    remaining = (totalConcepts - completedConcepts).coerceAtLeast(0)
                                ),
                                hasStudy         = flags.first,
                                hasSimulation    = flags.second,
                                hasRevision      = flags.third
                            )
                        }

                        DebugLogger.debugLog(TAG, "✅ All chapter progress bars updated")

                        _state.value = _state.value.copy(
                            chapters    = chapterUiModels,
                            subjectName = subject?.getLocalizedName() ?: "",
                            classLevel  = 7,
                            isLoading   = false,
                            error       = null
                        )
                    }

            } catch (e: Exception) {
                DebugLogger.errorLog(TAG, "Error loading chapters: ${e.message}")
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
