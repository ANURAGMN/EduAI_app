package com.ncert7.aitutorandlab.ui.screens.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncert7.aitutorandlab.config.AppConfig
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.data.local.dao.ConceptDao
import com.ncert7.aitutorandlab.data.local.dao.ProgressDao
import com.ncert7.aitutorandlab.data.local.dao.StudentDao
import com.ncert7.aitutorandlab.data.local.entities.ConceptEntity
import com.ncert7.aitutorandlab.data.local.entities.ProgressEntity
import com.ncert7.aitutorandlab.data.local.entities.StudentEntity
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.repository.StreakRepository
import com.ncert7.aitutorandlab.repository.SubjectRepository
import com.ncert7.aitutorandlab.utils.getLocalizedName
import com.ncert7.aitutorandlab.utils.isKannada
import com.ncert7.aitutorandlab.utils.normalizeLanguageCode
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
    private val sharedPrefs: SharedPreferenceUtils,
    private val subjectRepository: SubjectRepository
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

    // All-time totals — same queries used by ProgressScreenViewModel for consistency
    private val _totalCompletedConcept = MutableStateFlow(0)
    val totalCompletedConcept: StateFlow<Int> = _totalCompletedConcept

    private val _totalCompletedSimulation = MutableStateFlow(0)
    val totalCompletedSimulation: StateFlow<Int> = _totalCompletedSimulation

    private val _student = MutableStateFlow<StudentEntity?>(null)
    val student: StateFlow<StudentEntity?> = _student

    private val _studentLoaded = MutableStateFlow(false)
    val studentLoaded: StateFlow<Boolean> = _studentLoaded

    private val _greeting = MutableStateFlow("")
    val greeting: StateFlow<String> = _greeting

    // Trigger for language changes - incrementing this will cause UI to recompose
    private val _languageChangeTrigger = MutableStateFlow(0)
    val languageChangeTrigger: StateFlow<Int> = _languageChangeTrigger

    private val _currentLanguage = MutableStateFlow(if (isKannada()) "kn" else "en")
    val currentLanguage: StateFlow<String> = _currentLanguage

    private val _selectedSubjectName = MutableStateFlow("")
    val selectedSubjectName: StateFlow<String> = _selectedSubjectName

    fun setLanguage(lang: String) {
        val normalized = normalizeLanguageCode(lang)
        if (_currentLanguage.value != normalized) {
            _currentLanguage.value = normalized
            DebugLogger.debugLog("HomeViewModel", "Language dynamically changed to: $normalized")
        }
    }

    fun refreshSelectedSubjectName() {
        viewModelScope.launch {
            val language = _currentLanguage.value
            val subjectId = sharedPrefs.getSubjectSelectionId()
            val subject = subjectRepository.getSubject(subjectId)
            _selectedSubjectName.value = subject?.getLocalizedName(language) ?: ""
        }
    }

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
        observeStreak()
        observeTodayProgress()
        observeTotalCounts()
        observeProgressConceptsAndSimulations()
        observeSelectedSubjectName()
    }

    private fun observeSelectedSubjectName() {
        viewModelScope.launch {
            _currentLanguage.collectLatest {
                refreshSelectedSubjectName()
            }
        }
    }

    private fun observeProgressConceptsAndSimulations() {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            _currentLanguage.collectLatest { language ->
                kotlinx.coroutines.coroutineScope {
                    // Observe concepts
                    launch {
                        progressDao.getAllProgress(userId, AppConfig.APP_NAME)
                            .collectLatest { allProgressList ->
                                // Filter by CONCEPT type and LANGUAGE
                                val allProgress = allProgressList.filter { 
                                    it.itemType == "CONCEPT" && it.language == language 
                                }

                                // Separate by status
                                val completedList = allProgress
                                    .filter { it.status == "COMPLETED" }
                                    .sortedByDescending { it.completedAt ?: 0L }

                                val inProgressList = allProgress
                                    .filter { it.status == "IN_PROGRESS" }
                                    .sortedByDescending { it.lastAccessedAt }

                                // Build curated list for display
                                val curatedProgress = mutableListOf<ProgressEntity>()

                                // Strategy: Show ALL in-progress concepts first
                                curatedProgress.addAll(inProgressList)

                                // Then add most recent completed concepts to fill up to 4 items
                                val remainingSlots = (4 - curatedProgress.size).coerceAtLeast(0)
                                if (remainingSlots > 0) {
                                    curatedProgress.addAll(
                                        completedList.take(remainingSlots)
                                    )
                                }

                                // No progress at all
                                if (curatedProgress.isEmpty()) {
                                    val firstUnitConcepts = conceptDao.getFirstConceptsOfChapter("1", "STUDY", 4)
                                    val combined = firstUnitConcepts.map { concept ->
                                        null to concept
                                    }
                                    progressConcepts.value = combined
                                    DebugLogger.debugLog("HomeViewModel", "First login/no progress ($language) - showing ${combined.size} default concepts")
                                } else {
                                    val conceptIds = curatedProgress.map { it.itemId }
                                    val concepts = conceptDao.getConceptsByIds(conceptIds).first()
                                    val combined = curatedProgress.map { progress ->
                                        val concept = concepts.find { it.conceptId == progress.itemId }
                                        progress to concept
                                    }
                                    progressConcepts.value = combined
                                    DebugLogger.debugLog("HomeViewModel", "Loaded ${combined.size} concepts for $language")
                                }
                            }
                    }

                    // Observe simulations
                    launch {
                        progressDao.getAllProgress(userId, AppConfig.APP_NAME)
                            .collectLatest { allProgressList ->
                                // Filter by SIMULATION or SIMULATION_AGENT type and LANGUAGE
                                val allProgress = allProgressList.filter { 
                                    (it.itemType == "SIMULATION" || it.itemType == "SIMULATION_AGENT") && it.language == language 
                                }

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

                                if (curatedProgress.isEmpty()) {
                                    val firstUnitSimulations = conceptDao.getFirstConceptsOfChapter("1", "SIMULATION", 4)
                                    val combined = firstUnitSimulations.map { concept ->
                                        null to concept
                                    }
                                    progressSimulations.value = combined
                                    DebugLogger.debugLog("HomeViewModel", "First login/no simulations ($language) - showing ${combined.size} default simulations")
                                } else {
                                    val conceptIds = curatedProgress.map { it.itemId }
                                    val concepts = conceptDao.getConceptsByIds(conceptIds).first()
                                    val combined = curatedProgress.map { progress ->
                                        val concept = concepts.find { it.conceptId == progress.itemId }
                                        progress to concept
                                    }
                                    progressSimulations.value = combined
                                    DebugLogger.debugLog("HomeViewModel", "Loaded ${combined.size} simulations for $language")
                                }
                            }
                    }
                }
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
            _currentLanguage.collectLatest { language ->
                kotlinx.coroutines.coroutineScope {
                    // Observe today's concept count
                    launch {
                        progressDao.getTodayCompletedConceptCountFlow(userId, language, startOfDay, endOfDay, AppConfig.APP_NAME)
                            .collectLatest { count ->
                                _todayConceptCount.value = count
                                DebugLogger.debugLog("HomeViewModel", "Today's concept count updated: $count ($language)")
                            }
                    }

                    // Observe today's simulation count
                    launch {
                        progressDao.getTodayCompletedSimulationCountFlow(userId, language, startOfDay, endOfDay, AppConfig.APP_NAME)
                            .collectLatest { count ->
                                _todaySimulationCount.value = count
                                DebugLogger.debugLog("HomeViewModel", "Today's simulation count updated: $count ($language)")
                            }
                    }
                }
            }
        }
    }

    /**
     * Observes all-time total completed concept and simulation counts.
     * Uses the same queries as ProgressScreenViewModel so both screens show the same numbers.
     */
    private fun observeTotalCounts() {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            _currentLanguage.collectLatest { language ->
                kotlinx.coroutines.coroutineScope {
                    launch {
                        progressDao.getTotalCompletedConceptsFlow(userId, language, AppConfig.APP_NAME)
                            .collectLatest { count ->
                                _totalCompletedConcept.value = count
                                DebugLogger.debugLog("HomeViewModel", "Total completed concepts: $count ($language)")
                            }
                    }

                    launch {
                        progressDao.getTotalCompletedSimulationsFlow(userId, language, AppConfig.APP_NAME)
                            .collectLatest { count ->
                                _totalCompletedSimulation.value = count
                                DebugLogger.debugLog("HomeViewModel", "Total completed simulations: $count ($language)")
                            }
                    }
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
            _studentLoaded.value = true
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