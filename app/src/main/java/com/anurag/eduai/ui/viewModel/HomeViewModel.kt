package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.entities.ProgressEntity
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.StreakManager
import com.anurag.eduai.utils.getLocalizedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class HomeViewModel(
    private val conceptDao: ConceptDao,
    private val progressDao: ProgressDao,
    private val studentDao: StudentDao,
    private val userId: String,
    private val streakManager: StreakManager
) : ViewModel(){

    // Pair of ProgressEntity and its corresponding ConceptEntity
    var progressConcepts = MutableStateFlow<List<Pair<ProgressEntity?, ConceptEntity?>>>(emptyList())
    var progressSimulations = MutableStateFlow<List<Pair<ProgressEntity?, ConceptEntity?>>>(emptyList())

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
        getTodayCompletedConcept()
        getTodayCompletedSimulation()
        getStudent()
        getGreeting()
        getStreak()

        // Load CONCEPTS
        viewModelScope.launch {
            progressDao.getHomeScreenConcepts(userId, "CONCEPT")
                .collectLatest {

                    // Get all progress for the user (synchronous snapshot)
                    val allProgress = progressDao.getAllProgressSync(userId, "CONCEPT")

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
                            // copy with localized name
                            val localizedConcept = concept.copy(
                                conceptName = concept.getLocalizedName()
                            )
                            null to localizedConcept
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
                        val localizedConcept = concept?.copy(
                            conceptName = concept.getLocalizedName()
                        )

                        progress to localizedConcept
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
            progressDao.getHomeScreenConcepts(userId, "SIMULATION")
                .collectLatest {
                    val allProgress = progressDao.getAllProgressSync(userId, "SIMULATION")

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
                            val localizedConcept = concept.copy(
                                conceptName = concept.getLocalizedName()
                            )
                            null to localizedConcept
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
                        val localizedConcept = concept?.copy(
                            conceptName = concept.getLocalizedName()
                        )

                        progress to localizedConcept
                    }

                    progressSimulations.value = combined
                    DebugLogger.debugLog(
                        "HomeViewModel",
                        "Loaded ${combined.size} simulations: ${inProgressList.size} in-progress, ${completedList.size} completed"
                    )
                }
        }
    }
    fun getStreak() {
        val result = streakManager.getCurrentStreak()
        _streakCount.value = result
        DebugLogger.debugLog("HomeViewModel", "Current streak: $result days")
    }

    fun getTodayCompletedConcept() {
        viewModelScope.launch {
            val result = progressDao.getTodayCompletedConceptCount(userId, startOfDay, endOfDay)
            _todayConceptCount.value = result
            DebugLogger.debugLog("HomeViewModel", "Today's completed concepts: $result")
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

    fun getTodayCompletedSimulation() {
        viewModelScope.launch {
            val result = progressDao.getTodayCompletedSimulationCount(userId, startOfDay, endOfDay)
            _todaySimulationCount.value = result
            DebugLogger.debugLog("HomeViewModel", "Today's completed simulations: $result")
        }
    }

    fun getStudent() {
        viewModelScope.launch {
            val result = studentDao.getStudentSync(userId)
            _student.value = result
            DebugLogger.debugLog("HomeViewModel", "Student loaded: ${result?.studentName}")
        }
    }
}