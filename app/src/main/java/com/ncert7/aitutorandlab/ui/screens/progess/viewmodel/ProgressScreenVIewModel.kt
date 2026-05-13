package com.ncert7.aitutorandlab.ui.screens.progess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncert7.aitutorandlab.data.local.dao.ChapterProgressSummary
import com.ncert7.aitutorandlab.data.local.dao.DailyConceptCount
import com.ncert7.aitutorandlab.data.local.entities.StudentEntity
import com.ncert7.aitutorandlab.data.local.entities.SubjectEntity
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.repository.ChapterRepository
import com.ncert7.aitutorandlab.repository.ProgressRepository
import com.ncert7.aitutorandlab.repository.StreakRepository
import com.ncert7.aitutorandlab.repository.StudentLocalRepository
import com.ncert7.aitutorandlab.repository.SubjectRepository
import com.ncert7.aitutorandlab.utils.isKannada
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for Progress Screen.
 *
 * Uses repositories only — no direct DAO access.
 *
 * Chapter progress display uses ChapterRepository.getChapterWiseProgress()
 * which correctly returns totalConcepts and completedConcepts from the
 */
@HiltViewModel
class ProgressScreenViewModel @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val chapterRepository: ChapterRepository,
    private val subjectRepository: SubjectRepository,
    private val studentRepository: StudentLocalRepository,
    private val streakRepository: StreakRepository,
    private val sharedPrefs: com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
) : ViewModel() {

    private val userId: String
        get() = sharedPrefs.getUserId() ?: ""


    // --- State holders ---
    private val _totalCompletedConcept = MutableStateFlow(0)
    val totalCompletedConcept: StateFlow<Int> = _totalCompletedConcept.asStateFlow()

    private val _totalCompletedSimulation = MutableStateFlow(0)
    val totalCompletedSimulation: StateFlow<Int> = _totalCompletedSimulation.asStateFlow()

    private val _streakCount = MutableStateFlow(0)
    val streakCount: StateFlow<Int> = _streakCount.asStateFlow()

    private val _sevenDayProgress = MutableStateFlow<List<DailyConceptCount>>(emptyList())
    val sevenDayProgress: StateFlow<List<DailyConceptCount>> = _sevenDayProgress.asStateFlow()

    /**
     * Chapter-wise progress using ProgressDao.ChapterProgressSummary which includes:
     *   - chapterId, chapterName
     *   - totalConcepts (real count from concepts table)
     *   - completedConcepts (concepts with status=COMPLETED in progress table)
     *   - completionPercentage (Float)
     */
    private val _chapterProgressSummary = MutableStateFlow<List<ChapterProgressSummary>>(emptyList())
    val chapterProgressSummary: StateFlow<List<ChapterProgressSummary>> = _chapterProgressSummary.asStateFlow()

    private val _subjects = MutableStateFlow<List<SubjectEntity>>(emptyList())
    val subjects: StateFlow<List<SubjectEntity>> = _subjects.asStateFlow()

    private val _selectedSubject = MutableStateFlow<SubjectEntity?>(null)
    val selectedSubject: StateFlow<SubjectEntity?> = _selectedSubject.asStateFlow()

    private val _student = MutableStateFlow<StudentEntity?>(null)
    val student: StateFlow<StudentEntity?> = _student.asStateFlow()

    // --- Processed Weekly Data (UI-ready) ---
    private val _weeklyProgressData = MutableStateFlow<List<DayProgress>>(emptyList())
    val weeklyProgressData: StateFlow<List<DayProgress>> = _weeklyProgressData.asStateFlow()

    private val _maxWeeklyValue = MutableStateFlow(1)
    val maxWeeklyValue: StateFlow<Int> = _maxWeeklyValue.asStateFlow()

    // totalScore: derived from completed concepts (each concept = 10 pts, each sim = 20 pts)
    private val _totalScore = MutableStateFlow(0)
    val totalScore: StateFlow<Int> = _totalScore.asStateFlow()

    // --- Chapter Progress Categorization (UI-ready) ---
    private val _inProgressChapters = MutableStateFlow<List<ChapterProgressSummary>>(emptyList())
    val inProgressChapters: StateFlow<List<ChapterProgressSummary>> = _inProgressChapters.asStateFlow()

    private val _completedChapters = MutableStateFlow<List<ChapterProgressSummary>>(emptyList())
    val completedChapters: StateFlow<List<ChapterProgressSummary>> = _completedChapters.asStateFlow()

    private val _notStartedChapters = MutableStateFlow<List<ChapterProgressSummary>>(emptyList())
    val notStartedChapters: StateFlow<List<ChapterProgressSummary>> = _notStartedChapters.asStateFlow()

    private val _chaptersToShow = MutableStateFlow<List<ChapterProgressSummary>>(emptyList())
    val chaptersToShow: StateFlow<List<ChapterProgressSummary>> = _chaptersToShow.asStateFlow()

    private val _showAllChapters = MutableStateFlow(false)
    val showAllChapters: StateFlow<Boolean> = _showAllChapters.asStateFlow()

    private val _hasMoreChapters = MutableStateFlow(false)
    val hasMoreChapters: StateFlow<Boolean> = _hasMoreChapters.asStateFlow()

    init {
        getStudent()
        observeStreak()
        observeConceptCount()
        observeSimulationCount()
        observeTotalScore()
    }

    // --- Reactive Data Observation ---

    private fun observeConceptCount() {
        viewModelScope.launch {
            progressRepository.getTotalCompletedConceptsFlow(userId)
                .collectLatest { count ->
                    _totalCompletedConcept.value = count
                }
        }
    }

    private fun observeSimulationCount() {
        viewModelScope.launch {
            progressRepository.getTotalCompletedSimulationsFlow(userId)
                .collectLatest { count ->
                    _totalCompletedSimulation.value = count
                }
        }
    }

    private fun observeTotalScore() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                progressRepository.getTotalCompletedConceptsFlow(userId),
                progressRepository.getTotalCompletedSimulationsFlow(userId)
            ) { concepts, sims ->
                (concepts * 10) + (sims * 20)
            }.collectLatest { score ->
                _totalScore.value = score
            }
        }
    }

    private fun observeStreak() {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            // Use Flow for seamless sync - UI updates immediately when DB updates
            streakRepository.getStreakFlow(userId).collectLatest { streak ->
                // Default to 1 as requested for better initial experience
                _streakCount.value = streak?.streakCount ?: 1
            }
        }
    }

    fun getSevenDayProgress(sevenDaysAgoTimeStamp: Long) {
        viewModelScope.launch {
            try {
                // Count ALL activities (concepts + simulations + revision) for the weekly chart
                val result = progressRepository.getDailyCompletedActivityLast7Days(userId, sevenDaysAgoTimeStamp)
                DebugLogger.debugLog("ProgressVM", "Weekly Activity Data: $result")
                _sevenDayProgress.value = result
                processWeeklyData(result)
            } catch (e: Exception) {
                DebugLogger.errorLog("ProgressVM", "Error loading weekly activity: ${e.message}")
            }
        }
    }

    /**
     * Load chapter-wise progress for a given subject.
     *
     * Uses ChapterRepository.getChapterWiseProgress() which queries the `progress` table
     * via a LEFT JOIN — giving real totalConcepts + completedConcepts counts for every chapter,
     * even those with no progress rows yet (they show 0%).
     *
     * Collects as a Flow so the UI updates automatically when progress changes.
     */
    // Holds the active collection Job so we can cancel it when subject changes
    private var chapterProgressJob: Job? = null

    fun getChapterProgressSummary(classLevel: Int, subjectId: String) {
        // Cancel the previous flow collection before starting a new one
        chapterProgressJob?.cancel()
        chapterProgressJob = viewModelScope.launch {
            val language = if (isKannada()) "kn" else "en"
            chapterRepository.getChapterWiseProgress(userId, subjectId, language)
                .collectLatest { result ->
                    _chapterProgressSummary.value = result
                    categorizeChapters(result)
                    DebugLogger.debugLog(
                        "ProgressScreenViewModel",
                        "Loaded ${result.size} chapters for subject=$subjectId " +
                        "(${result.count { it.completionPercentage > 0 }} with progress)"
                    )
                }
        }
    }

    fun loadSubjects(classLevel: Int) {
        viewModelScope.launch {
            // Hardcode to class 7 to ensure syllabus is independent of user's profile class level
            val subjectList = subjectRepository.getSubjectsForClass(7)
            _subjects.value = subjectList
            if (subjectList.isNotEmpty() && _selectedSubject.value == null) {
                _selectedSubject.value = subjectList.first()
            }
            DebugLogger.debugLog("ProgressScreenViewModel", "Loaded ${subjectList.size} subjects for class $classLevel")
        }
    }

    fun selectSubject(subject: SubjectEntity) {
        _selectedSubject.value = subject
        _showAllChapters.value = false
    }

    fun getStudent() {
        viewModelScope.launch {
            _student.value = studentRepository.getStudentSync(userId)
        }
    }

    // --- Business Logic ---

    private fun processWeeklyData(rawData: List<DailyConceptCount>) {
        val today = LocalDate.now()
        val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()).toString() }
        val progressMap = rawData.associateBy { it.date }
        val weeklyData = last7Days.map { date ->
            DayProgress(dayLabel = getDayOfWeek(date), count = progressMap[date]?.count ?: 0)
        }
        _weeklyProgressData.value = weeklyData
        _maxWeeklyValue.value = (weeklyData.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    }

    private fun getDayOfWeek(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            date.dayOfWeek.name.take(3).lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } catch (e: Exception) { "???" }
    }

    private fun categorizeChapters(chapters: List<ChapterProgressSummary>) {
        val inProgress  = chapters.filter { it.completionPercentage > 0 && it.completionPercentage < 100 }
        val completed   = chapters.filter { it.completionPercentage >= 100 }
        val notStarted  = chapters.filter { it.completionPercentage <= 0f }

        _inProgressChapters.value  = inProgress
        _completedChapters.value   = completed
        _notStartedChapters.value  = notStarted

        updateChaptersToShow(chapters, inProgress, notStarted)
    }

    private fun updateChaptersToShow(
        allChapters: List<ChapterProgressSummary>,
        inProgress: List<ChapterProgressSummary>,
        notStarted: List<ChapterProgressSummary>
    ) {
        val chaptersToDisplay = if (_showAllChapters.value) {
            allChapters
        } else {
            val selected = inProgress.take(4).toMutableList()
            if (selected.size < 4) selected.addAll(notStarted.take(4 - selected.size))
            selected
        }
        _chaptersToShow.value  = chaptersToDisplay
        _hasMoreChapters.value = allChapters.size > chaptersToDisplay.size
    }

    fun toggleShowAllChapters() {
        _showAllChapters.value = !_showAllChapters.value
        categorizeChapters(_chapterProgressSummary.value)
    }

    fun calculateBarHeight(count: Int): Float {
        val maxValue = _maxWeeklyValue.value
        return (count.toFloat() / maxValue * 100).coerceAtLeast(4f)
    }

    fun getProgressColor(percentage: Float): ProgressColorType = when {
        percentage >= 100 -> ProgressColorType.COMPLETED
        percentage >= 80  -> ProgressColorType.HIGH_PROGRESS
        percentage >= 50  -> ProgressColorType.MEDIUM_PROGRESS
        percentage > 0    -> ProgressColorType.STARTED
        else              -> ProgressColorType.NOT_STARTED
    }

    fun capitalizeFirstLetter(text: String): String =
        text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    fun getShowMoreButtonText(): String =
        if (_showAllChapters.value) "show_less" else "show_more_count"

    fun getHiddenChaptersCount(): Int =
        _chapterProgressSummary.value.size - _chaptersToShow.value.size

    fun getSevenDaysAgoInMillis(): Long {
        val sevenDaysAgo = LocalDate.now().minusDays(7)
        return sevenDaysAgo.toEpochDay() * 24 * 60 * 60 * 1000
    }
}

data class DayProgress(val dayLabel: String, val count: Int)

enum class ProgressColorType {
    COMPLETED, HIGH_PROGRESS, MEDIUM_PROGRESS, STARTED, NOT_STARTED
}