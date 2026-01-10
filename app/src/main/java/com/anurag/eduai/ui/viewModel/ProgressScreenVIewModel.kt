package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.dao.ChapterProgressSummary
import com.anurag.eduai.data.local.dao.DailyConceptCount
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.dao.SubjectDao
import com.anurag.eduai.data.local.entities.SubjectEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.StreakManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProgressScreenVIewModel(
    private val progressDao: ProgressDao,
    private val subjectDao: SubjectDao,
    private val streakManager: StreakManager
) : ViewModel() {

    // --- State holders ---
    private val _totalCompletedConcept = MutableStateFlow("0")
    val totalCompletedConcept: StateFlow<String> = _totalCompletedConcept

    private val _streakCount = MutableStateFlow("0")
    val streakCount: StateFlow<String> = _streakCount

    private val _sevenDayProgress = MutableStateFlow<List<DailyConceptCount>>(emptyList())
    val sevenDayProgress: StateFlow<List<DailyConceptCount>> = _sevenDayProgress

    private val _chapterProgressSummary = MutableStateFlow<List<ChapterProgressSummary>>(emptyList())
    val chapterProgressSummary: StateFlow<List<ChapterProgressSummary>> = _chapterProgressSummary

    // --- New State holders for subjects ---
    private val _subjects = MutableStateFlow<List<SubjectEntity>>(emptyList())
    val subjects: StateFlow<List<SubjectEntity>> = _subjects

    private val _selectedSubject = MutableStateFlow<SubjectEntity?>(null)
    val selectedSubject: StateFlow<SubjectEntity?> = _selectedSubject


    fun getTotalCompletedConcept(userId: String) {
        viewModelScope.launch {
            val result = progressDao.getTotalCompletedConcepts(userId).toString()
            _totalCompletedConcept.value = result
        }
    }

    fun getSevenDayProgress(userId: String, sevenDaysAgoTimeStamp: Long){
        viewModelScope.launch {
            val result = progressDao.getConceptsClearedLast7Days(userId, sevenDaysAgoTimeStamp)
            _sevenDayProgress.value = result
        }
    }

    fun getStreak() {
        val result = streakManager.getCurrentStreak()
        _streakCount.value = result.toString() ?: "0"
    }

    fun getChapterProgressSummary(userId: String, classLevel: Int, subject: String){
        viewModelScope.launch {
            val result = progressDao.getChapterWiseProgress(userId, classLevel,subject )
            _chapterProgressSummary.value = result

            DebugLogger.debugLog(
                "ProgressScreenViewModel",
                "ChapterWiseProgress = $result"
            )
        }
    }

    fun loadSubjects(classLevel: Int) {
        viewModelScope.launch {
            val subjectList = subjectDao.getSubjectsForClassSync(classLevel)
            _subjects.value = subjectList

            // Auto-select first subject if available and none selected
            if (subjectList.isNotEmpty() && _selectedSubject.value == null) {
                _selectedSubject.value = subjectList.first()
            }

            DebugLogger.debugLog(
                "ProgressScreenViewModel",
                "Loaded ${subjectList.size} subjects for class $classLevel"
            )
        }
    }

    fun selectSubject(subject: SubjectEntity) {
        _selectedSubject.value = subject
        DebugLogger.debugLog(
            "ProgressScreenViewModel",
            "Selected subject: ${subject.subjectName}"
        )
    }
}
