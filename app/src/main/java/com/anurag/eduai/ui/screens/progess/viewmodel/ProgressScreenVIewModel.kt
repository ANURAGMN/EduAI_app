package com.anurag.eduai.ui.screens.progess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.dao.ChapterDao
import com.anurag.eduai.data.local.dao.ChapterProgressSummary
import com.anurag.eduai.data.local.dao.DailyConceptCount
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.data.local.dao.SubjectDao
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.data.local.entities.SubjectEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.StreakManager
import com.anurag.eduai.utils.getLocalizedName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for Progress Screen
 * Contains ALL business logic, data fetching, and state management
 * UI layer should only observe state and trigger actions
 */
@HiltViewModel
class ProgressScreenViewModel @Inject constructor(
    private val progressDao: ProgressDao,
    private val subjectDao: SubjectDao,
    private val chapterDao: ChapterDao,
    private val streakManager: StreakManager,
    private val studentDao: StudentDao,
    private val userId: String
) : ViewModel() {

    // --- State holders ---
    private val _totalCompletedConcept = MutableStateFlow(0)
    val totalCompletedConcept: StateFlow<Int> = _totalCompletedConcept.asStateFlow()

    private val _streakCount = MutableStateFlow(0)
    val streakCount: StateFlow<Int> = _streakCount.asStateFlow()

    private val _sevenDayProgress = MutableStateFlow<List<DailyConceptCount>>(emptyList())
    val sevenDayProgress: StateFlow<List<DailyConceptCount>> = _sevenDayProgress.asStateFlow()

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
        getStreak()
        getTotalCompletedConcept()
    }

    // --- Data Loading Functions ---

    fun getTotalCompletedConcept() {
        viewModelScope.launch {
            val result = progressDao.getTotalCompletedConcepts(userId)
            _totalCompletedConcept.value = result
        }
    }

    fun getSevenDayProgress(sevenDaysAgoTimeStamp: Long) {
        viewModelScope.launch {
            val result = progressDao.getConceptsClearedLast7Days(userId, sevenDaysAgoTimeStamp)
            _sevenDayProgress.value = result
            processWeeklyData(result)
        }
    }

    fun getStreak() {
        viewModelScope.launch {
            val result = streakManager.getCurrentStreak()
            _streakCount.value = result
        }
    }

    fun getChapterProgressSummary(classLevel: Int, subject: String) {
        viewModelScope.launch {
            val result = progressDao.getChapterWiseProgress(userId, classLevel, subject)

            // Get localized chapter names
            val localizedResult = result.map { summary ->
                val chapter = chapterDao.getChapter(summary.chapterId)
                summary.copy(
                    chapterName = chapter?.getLocalizedName() ?: summary.chapterName
                )
            }

            _chapterProgressSummary.value = localizedResult
            categorizeChapters(localizedResult)

            DebugLogger.debugLog(
                "ProgressScreenViewModel",
                "ChapterWiseProgress = $localizedResult"
            )
        }
    }

    fun loadSubjects(classLevel: Int) {
        viewModelScope.launch {
            val subjectList = subjectDao.getSubjectsForClassSync(classLevel)
            // Subjects already have localized names via getLocalizedName() extension
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
        _showAllChapters.value = false // Reset show all when subject changes
        DebugLogger.debugLog(
            "ProgressScreenViewModel",
            "Selected subject: ${subject.subjectName}"
        )
    }

    fun getStudent() {
        viewModelScope.launch {
            val result = studentDao.getStudentSync(userId)
            _student.value = result
        }
    }

    // --- Business Logic Functions ---

    /**
     * Process weekly data to create UI-ready list
     * Converts raw data into structured DayProgress objects with proper labels
     */
    private fun processWeeklyData(rawData: List<DailyConceptCount>) {
        val today = LocalDate.now()
        val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()).toString() }

        // Convert to map for easy lookup
        val progressMap = rawData.associateBy { it.date }

        // Build full 7-day dataset
        val weeklyData = last7Days.map { date ->
            DayProgress(
                dayLabel = getDayOfWeek(date),
                count = progressMap[date]?.count ?: 0
            )
        }

        _weeklyProgressData.value = weeklyData
        _maxWeeklyValue.value = (weeklyData.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    }

    /**
     * Get day of week abbreviation from date string
     * Logic moved from WeeklyProgressUtils to ViewModel
     */
    private fun getDayOfWeek(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            date.dayOfWeek.name.take(3).lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } catch (e: Exception) {
            "???"
        }
    }

    /**
     * Categorize chapters into in-progress, completed, and not-started
     * Determines which chapters to show based on showAll state
     */
    private fun categorizeChapters(chapters: List<ChapterProgressSummary>) {
        val inProgress = chapters.filter { it.completionPercentage > 0 && it.completionPercentage < 100 }
        val completed = chapters.filter { it.completionPercentage >= 100 }
        val notStarted = chapters.filter { it.completionPercentage == 0f }

        _inProgressChapters.value = inProgress
        _completedChapters.value = completed
        _notStartedChapters.value = notStarted

        updateChaptersToShow(chapters, inProgress, notStarted)
    }

    /**
     * Update which chapters should be displayed based on showAll state
     */
    private fun updateChaptersToShow(
        allChapters: List<ChapterProgressSummary>,
        inProgress: List<ChapterProgressSummary>,
        notStarted: List<ChapterProgressSummary>
    ) {
        val chaptersToDisplay = if (_showAllChapters.value) {
            allChapters
        } else {
            // Show first 4 in-progress chapters
            val selected = inProgress.take(4).toMutableList()
            // If less than 4 in-progress, fill with not started
            if (selected.size < 4) {
                val remaining = 4 - selected.size
                selected.addAll(notStarted.take(remaining))
            }
            selected
        }

        _chaptersToShow.value = chaptersToDisplay
        _hasMoreChapters.value = allChapters.size > chaptersToDisplay.size
    }

    /**
     * Toggle show all chapters state
     */
    fun toggleShowAllChapters() {
        _showAllChapters.value = !_showAllChapters.value
        categorizeChapters(_chapterProgressSummary.value)
    }

    /**
     * Calculate progress bar height for weekly activity
     * Returns percentage of max value (minimum 4% for visibility)
     */
    fun calculateBarHeight(count: Int): Float {
        val maxValue = _maxWeeklyValue.value
        return (count.toFloat() / maxValue * 100).coerceAtLeast(4f)
    }

    /**
     * Get progress color based on percentage
     */
    fun getProgressColor(percentage: Float): ProgressColorType {
        return when {
            percentage >= 100 -> ProgressColorType.COMPLETED
            percentage >= 80 -> ProgressColorType.HIGH_PROGRESS
            percentage >= 50 -> ProgressColorType.MEDIUM_PROGRESS
            percentage > 0 -> ProgressColorType.STARTED
            else -> ProgressColorType.NOT_STARTED
        }
    }

    /**
     * Capitalize first letter of string (for subject names)
     */
    fun capitalizeFirstLetter(text: String): String {
        return text.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase()
            else it.toString()
        }
    }

    /**
     * Get show more/less button text
     */
    fun getShowMoreButtonText(): String {
        val hiddenCount = _chapterProgressSummary.value.size - _chaptersToShow.value.size
        return if (_showAllChapters.value) {
            "show_less"
        } else {
            "show_more_count"  // Will be formatted with count in UI using string resource
        }
    }

    /**
     * Get hidden chapters count
     */
    fun getHiddenChaptersCount(): Int {
        return _chapterProgressSummary.value.size - _chaptersToShow.value.size
    }

    /**
     * Calculate seven days ago timestamp
     * Logic moved from WeeklyProgressUtils to ViewModel
     */
    fun getSevenDaysAgoInMillis(): Long {
        val today = LocalDate.now()
        val sevenDaysAgo = today.minusDays(7)
        return sevenDaysAgo.toEpochDay() * 24 * 60 * 60 * 1000
    }
}

/**
 * Data model for daily progress
 * Moved from WeeklyActivitySection to ViewModel
 */
data class DayProgress(
    val dayLabel: String,
    val count: Int
)

/**
 * Enum for progress color types
 * Allows UI to map to actual colors without ViewModel knowing about colors
 */
enum class ProgressColorType {
    COMPLETED,
    HIGH_PROGRESS,
    MEDIUM_PROGRESS,
    STARTED,
    NOT_STARTED
}