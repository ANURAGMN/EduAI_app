package com.anurag.eduai.ui.screens.chapterscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.domain.progress.ChapterProgressCalculator
import com.anurag.eduai.domain.progress.ChapterProgressService
import com.anurag.eduai.domain.progress.model.ProgressStatus
import com.anurag.eduai.repository.ChapterRepository
import com.anurag.eduai.repository.StudentLocalRepository
import com.anurag.eduai.repository.SubjectRepository
import com.anurag.eduai.ui.models.ChapterUiModel
import com.anurag.eduai.ui.screens.chapterscreen.dataclass.ChapterUiState
import com.anurag.eduai.utils.getLocalizedName
import com.anurag.eduai.utils.isKannada
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
    private val studentRepository: StudentLocalRepository,
    private val sharedPrefs: SharedPreferenceUtils,
    private val chapterProgressService: ChapterProgressService,   // ADD THIS
    private val chapterProgressCalculator: ChapterProgressCalculator  // ADD THIS
) : ViewModel() {

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

                // Seed the progress table in the background if needed
                viewModelScope.launch {
                    chapters.forEach { chapter ->
                        val existing = chapterProgressService.getChapterProgressBreakdown(userId, chapter.chapterId, language)
                        if (existing.overallPercentage == 0 && existing.studyPercentage == 0) {
                            chapterProgressService.updateChapterProgress(userId, chapter.chapterId, language)
                        }
                    }
                }

                // Step 1: Collect progress from the reactive Flow
                // This ensures the UI updates immediately when progress changes
                chapterRepository.getChapterWiseProgress(userId, subjectId, language)
                    .collect { progressSummaries ->
                        val progressMap = progressSummaries.associateBy { it.chapterId }

                        val chapterUiModels = chapters.map { chapter ->
                            val summary = progressMap[chapter.chapterId]
                            val totalConcepts = summary?.totalConcepts ?: chapter.totalConcepts
                            val completedConcepts = summary?.completedConcepts ?: 0
                            val overallPct = summary?.completionPercentage ?: 0f

                            val status = when {
                                overallPct >= 100f -> ProgressStatus.COMPLETED
                                overallPct > 0f -> ProgressStatus.IN_PROGRESS
                                else -> ProgressStatus.NOT_STARTED
                            }

                            ChapterUiModel(
                                id = chapter.chapterId,
                                orderIndex = chapter.orderIndex,
                                name = chapter.getLocalizedName(),
                                englishName = chapter.chapterName,
                                totalConcepts = totalConcepts,
                                completedConcepts = completedConcepts,
                                status = status
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
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}