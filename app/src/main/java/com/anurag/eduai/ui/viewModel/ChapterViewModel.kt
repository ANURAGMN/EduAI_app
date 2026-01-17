package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.dao.ChapterDao
import com.anurag.eduai.data.local.dao.ChapterProgressSummary
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.data.local.dao.SubjectDao
import com.anurag.eduai.data.local.entities.ChapterEntity
import com.anurag.eduai.data.local.entities.SubjectEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChapterScreenState(
    val chapters: List<ChapterEntity> = emptyList(),
    val chapterProgress: Map<String, ChapterProgressSummary> = emptyMap(),
    val subject: SubjectEntity? = null,
    val subjectId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChapterViewModel(
    private val chapterDao: ChapterDao,
    private val subjectDao: SubjectDao,
    private val progressDao: ProgressDao,
    private val studentDao: StudentDao,
    private val sharedPrefs: SharedPreferenceUtils
) : ViewModel() {

    private val _state = MutableStateFlow(ChapterScreenState())
    val state: StateFlow<ChapterScreenState> = _state.asStateFlow()

    fun loadChapters(subjectId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val chapters = chapterDao.getChaptersForSubjectSync(subjectId)
                val subject = subjectDao.getSubject(subjectId)

                // Get student ID and class level
                val userId = sharedPrefs.getUserId() ?: ""
                val student = studentDao.getStudentSync(userId)
                val classLevel = student?.classLevel ?: 7

                // Get chapter-wise progress
                val progressList = progressDao.getChapterWiseProgress(
                    studentId = userId,
                    classLevel = classLevel,
                    subjectId = subjectId
                )

                // Convert to map for easy lookup
                val progressMap = progressList.associateBy { it.chapterId }

                _state.value = _state.value.copy(
                    chapters = chapters,
                    chapterProgress = progressMap,
                    subject = subject,
                    subjectId = subjectId,
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