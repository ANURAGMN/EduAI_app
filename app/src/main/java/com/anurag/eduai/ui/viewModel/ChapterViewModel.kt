package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.repository.ChapterRepository
import com.anurag.eduai.repository.StudentLocalRepository
import com.anurag.eduai.repository.SubjectRepository
import com.anurag.eduai.ui.models.ChapterStatus
import com.anurag.eduai.ui.models.ChapterUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChapterScreenState(
    val isLoading: Boolean = false,
    val classLevel: Int = 7,
    val subjectName: String = "",
    val chapters: List<ChapterUiModel> = emptyList(),
    val error: String? = null
)


class ChapterViewModel(
    private val chapterRepository: ChapterRepository,
    private val subjectRepository: SubjectRepository,
    private val studentRepository: StudentLocalRepository,
    private val sharedPrefs: SharedPreferenceUtils
) : ViewModel() {

    private val _state = MutableStateFlow(ChapterScreenState())
    val state: StateFlow<ChapterScreenState> = _state.asStateFlow()

    fun loadChapters(subjectId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // Get student ID and class level
                val userId = sharedPrefs.getUserId() ?: ""
                val student = studentRepository.getStudentSync(userId)
                val classLevel = student?.classLevel ?: 7

                // Fetch data from repositories
                val chapters = chapterRepository.getChaptersForSubject(subjectId)
                val subject = subjectRepository.getSubject(subjectId)
                val progressList = chapterRepository.getChapterWiseProgress(
                    studentId = userId,
                    classLevel = classLevel,
                    subjectId = subjectId
                )

                // Convert to map for easy lookup
                val progressMap = progressList.associateBy { it.chapterId }

                val chapterUiModels = chapters.map { chapter ->
                    val progress = progressMap[chapter.chapterId]
                    val completedConcepts = progress?.completedConcepts ?: 0
                    val totalConcepts = chapter.totalConcepts

                    // Determine status based on completion
                    val status = when {
                        completedConcepts == 0 -> ChapterStatus.NOT_STARTED
                        completedConcepts >= totalConcepts -> ChapterStatus.COMPLETED
                        else -> ChapterStatus.IN_PROGRESS
                    }

                    ChapterUiModel(
                        id = chapter.chapterId,
                        name = chapter.chapterName,
                        totalConcepts = totalConcepts,
                        completedConcepts = completedConcepts,
                        status = status
                    )
                }

                _state.value = _state.value.copy(
                    chapters = chapterUiModels,
                    subjectName = subject?.subjectName ?: "",
                    classLevel = classLevel,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}