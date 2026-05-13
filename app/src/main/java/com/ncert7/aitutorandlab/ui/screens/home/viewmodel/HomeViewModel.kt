package com.ncert7.aitutorandlab.ui.screens.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncert7.aitutorandlab.config.AppConfig
import com.ncert7.aitutorandlab.data.local.dao.ConceptDao
import com.ncert7.aitutorandlab.data.local.dao.ProgressDao
import com.ncert7.aitutorandlab.data.local.dao.StudentDao
import com.ncert7.aitutorandlab.data.local.entities.ConceptEntity
import com.ncert7.aitutorandlab.data.local.entities.ProgressEntity
import com.ncert7.aitutorandlab.data.local.entities.StudentEntity
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.repository.StreakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val conceptDao: ConceptDao,
    private val progressDao: ProgressDao,
    private val studentDao: StudentDao,
    private val streakRepository: StreakRepository,
    private val sharedPrefs: com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
) : ViewModel(){

    private val userId: String
        get() = sharedPrefs.getUserId() ?: ""


    // Pair of ProgressEntity and its corresponding ConceptEntity
    var progressConcepts =
        MutableStateFlow<List<Pair<ProgressEntity?, ConceptEntity?>>>(emptyList())
    var progressSimulations =
        MutableStateFlow<List<Pair<ProgressEntity?, ConceptEntity?>>>(emptyList())

    private val _streakCount = MutableStateFlow(0)
    val streakCount: StateFlow<Int> = _streakCount

    private val _todayConceptCount = MutableStateFlow(0)
    val todayConceptCount: StateFlow<Int> = _todayConceptCount

    private val _todaySimulationCount = MutableStateFlow(0)
    val todaySimulationCount: StateFlow<Int> = _todaySimulationCount

    private val _student = MutableStateFlow<StudentEntity?>(null)
    val student: StateFlow<StudentEntity?> = _student

    private val _greeting = MutableStateFlow("")
    val greeting: StateFlow<String> = _greeting

    // Trigger for language changes - incrementing this will cause UI to recompose
    private val _languageChangeTrigger = MutableStateFlow(0)
    val languageChangeTrigger: StateFlow<Int> = _languageChangeTrigger

    val startOfDay = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val endOfDay = LocalDate.now()
        .plusDays(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli() - 1

    init {
        getStudent()
        getGreeting()
        observeStreak()
        observeTodayProgress()

        // Load CONCEPTS
        viewModelScope.launch {
            progressDao.getAllProgress(userId, AppConfig.APP_NAME )
                .collectLatest { allProgressList ->
                    // Filter by CONCEPT type in memory
                    val allProgress = allProgressList.filter { it.itemType == "CONCEPT" }

                    // Separate by status
                    val completedList = allProgress
                        .filter { it.status == "COMPLETED" }
                        .sortedByDescending { it.completedAt ?: 0L }

                    val inProgressList = allProgress
                        .filter { it.status == "IN_PROGRESS" }
                        .sortedByDescending { it.lastAccessedAt }

                    // Build curated list for display
                    val curatedProgress = mutableListOf<ProgressEntity>()

                    // Strategy: Show ALL in-progress concepts first (these need attention)
                    curatedProgress.addAll(inProgressList)

                    // Then add most recent completed concepts to fill up to 4 items
                    val remainingSlots = (4 - curatedProgress.size).coerceAtLeast(0)
                    if (remainingSlots > 0) {
                        curatedProgress.addAll(
                            completedList.take(remainingSlots)
                        )
                    }

                    //  No progress at all
                    if (curatedProgress.isEmpty()) {
                        val firstUnitConcepts = conceptDao.getFirstConceptsOfChapter("1", "STUDY", 4)

                        // Show concepts without progress entries
                        val combined = firstUnitConcepts.map { concept ->
                            null to concept
                        }

                        progressConcepts.value = combined
                        DebugLogger.debugLog("HomeViewModel", "First login - showing ${combined.size} default concepts")
                        return@collectLatest
                    }

                    // Normal path: fetch concepts for progress entries
                    val conceptIds = curatedProgress.map { it.itemId }

                    // Fetch concepts once for this snapshot to avoid nested long-lived collectors
                    val concepts = conceptDao.getConceptsByIds(conceptIds).first()

                    val combined = curatedProgress.map { progress ->
                        val concept = concepts.find { it.conceptId == progress.itemId }
                        progress to concept
                    }

                    progressConcepts.value = combined
                    DebugLogger.debugLog(
                        "HomeViewModel",
                        "Loaded ${combined.size} concepts: ${inProgressList.size} in-progress, ${completedList.size} completed"
                    )
                }
        }

        // Load SIMULATIONS
        viewModelScope.launch {
            progressDao.getAllProgress(userId, AppConfig.APP_NAME)
                .collectLatest { allProgressList ->
                    // Filter by SIMULATION type in memory
                    val allProgress = allProgressList.filter { it.itemType == "SIMULATION" }

                    val completedList = allProgress
                        .filter { it.status == "COMPLETED" }
                        .sortedByDescending { it.completedAt ?: 0L }

                    val inProgressList = allProgress
                        .filter { it.status == "IN_PROGRESS" }
                        .sortedByDescending { it.lastAccessedAt }

                    val curatedProgress = mutableListOf<ProgressEntity>()
                    curatedProgress.addAll(inProgressList)

                    val remainingSlots = (4 - curatedProgress.size).coerceAtLeast(0)
                    if (remainingSlots > 0) {
                        curatedProgress.addAll(completedList.take(remainingSlots))
                    }

                    // FIRST LOGIN FALLBACK: No progress at all
                    if (curatedProgress.isEmpty()) {
                        val firstUnitSimulations = conceptDao.getFirstConceptsOfChapter("1", "SIMULATION", 4)

                        val combined = firstUnitSimulations.map { concept ->
                            null to concept
                        }

                        progressSimulations.value = combined
                        DebugLogger.debugLog("HomeViewModel", "First login - showing ${combined.size} default simulations")
                        return@collectLatest
                    }

                    // fetch simulations for progress entries
                    val conceptIds = curatedProgress.map { it.itemId }

                    val concepts = conceptDao.getConceptsByIds(conceptIds).first()

                    val combined = curatedProgress.map { progress ->
                        val concept = concepts.find { it.conceptId == progress.itemId }
                        progress to concept
                    }

                    progressSimulations.value = combined
                    DebugLogger.debugLog(
                        "HomeViewModel",
                        "Loaded ${combined.size} simulations: ${inProgressList.size} in-progress, ${completedList.size} completed"
                    )
                }
        }
    }
    private fun observeStreak() {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            streakRepository.getStreakFlow(userId).collectLatest { streak ->
                // Default to 1 as requested for a better new user experience
                _streakCount.value = streak?.streakCount ?: 1
            }
        }
    }


    private fun observeTodayProgress() {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            
            // Observe today's concept count
            launch {
                progressDao.getTodayCompletedConceptCountFlow(userId, startOfDay, endOfDay, AppConfig.APP_NAME)
                    .collectLatest { count ->
                        _todayConceptCount.value = count
                        DebugLogger.debugLog("HomeViewModel", "Today's concept count updated: $count")
                    }
            }

            // Observe today's simulation count
            launch {
                progressDao.getTodayCompletedSimulationCountFlow(userId, startOfDay, endOfDay, AppConfig.APP_NAME)
                    .collectLatest { count ->
                        _todaySimulationCount.value = count
                        DebugLogger.debugLog("HomeViewModel", "Today's simulation count updated: $count")
                    }
            }
        }
    }

    /**
     * Returns appropriate greeting based on current time
     * 5-11: Good Morning
     * 12-16: Good Afternoon
     * 17-21: Good Evening
     * 22-4: Good Night
     */
    fun getGreeting() {
        val hour = LocalTime.now().hour

        _greeting.value = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }


    fun getStudent() {
        viewModelScope.launch {
            val result = studentDao.getStudentSync(userId)
            _student.value = result
            DebugLogger.debugLog("HomeViewModel", "Student loaded: ${result?.studentName}")
        }
    }

    /**
     * Called when app language changes to trigger UI recomposition with new localized names
     */
    fun onLanguageChanged() {
        _languageChangeTrigger.value += 1
    }
}