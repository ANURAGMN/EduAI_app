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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    // Using a simple Map or List of Pairs for UI to consume
    var progressConcepts = MutableStateFlow<List<Pair<ProgressEntity?, ConceptEntity?>>>(emptyList())
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
        viewModelScope.launch {

            getStreak()


            progressDao.getHomeScreenConcepts(userId, "CONCEPT")
                .collect { progressList ->

                    // all completed (latest first)
                    val completedList =
                        progressDao.getAllProgressSync(userId, "CONCEPT")
                            .filter { it.status == "COMPLETED" }
                            .sortedByDescending { it.completedAt ?: 0L }

                    // all in-progress (most recent first)
                    val inProgressList =
                        progressList
                            .filter { it.status == "IN_PROGRESS" }
                            .sortedByDescending { it.lastAccessedAt }

                    // build final list: 1 completed + up to 3 in-progress
                    val curatedProgress = mutableListOf<ProgressEntity>()

                    completedList.firstOrNull()?.let { curatedProgress.add(it) }

                    curatedProgress.addAll(
                        inProgressList.take(3)
                    )

                    // if less than 4, fill remaining with more COMPLETED
                    if (curatedProgress.size < 4) {
                        val remaining =
                            completedList
                                .drop(1) // skip the first completed already added
                                .take(4 - curatedProgress.size)

                        curatedProgress.addAll(remaining)
                    }

                    // FIRST LOGIN FALLBACK: No progress at all
                    if (curatedProgress.isEmpty()) {

                        val firstUnitConcepts =
                            conceptDao.getFirstConceptsOfChapter("1", 4)

                        // Show concepts without progress entries
                        val combined = firstUnitConcepts.map { concept ->
                            null to concept
                        }

                        progressConcepts.value = combined
                        return@collect
                    }

                    // Normal path: fetch concepts for progress entries
                    val conceptIds = curatedProgress.map { it.itemId }

                    conceptDao.getConceptsByIds(conceptIds)
                        .collect { concepts ->

                            val combined = curatedProgress.map { progress ->
                                val concept = concepts.find { it.conceptId == progress.itemId }
                                progress to concept
                            }

                            progressConcepts.value = combined
                        }
                }
        }
    }


    fun getStreak() {
        val result = streakManager.getCurrentStreak()
        _streakCount.value = result
    }

    fun getTodayCompletedConcept(){
        viewModelScope.launch {
            val result = progressDao.getTodayCompletedConceptCount(userId, startOfDay, endOfDay)
            _todayConceptCount.value = result
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

    fun getTodayCompletedSimulation(){
        viewModelScope.launch {
            val result = progressDao.getTodayCompletedSimulationCount(userId, startOfDay, endOfDay)
            _todaySimulationCount.value = result
        }
    }
    fun getStudent(){
        viewModelScope.launch {
            val result = studentDao.getStudentSync(userId)
            _student.value = result
        }
    }
}