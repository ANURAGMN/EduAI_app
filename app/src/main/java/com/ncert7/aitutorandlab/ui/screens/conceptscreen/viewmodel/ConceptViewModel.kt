package com.ncert7.aitutorandlab.ui.screens.conceptscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.data.local.entities.ProgressEntity
import com.ncert7.aitutorandlab.domain.progress.model.ProgressStatus
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.repository.ChapterRepository
import com.ncert7.aitutorandlab.repository.ConceptRepository
import com.ncert7.aitutorandlab.repository.StudentLocalRepository
import com.ncert7.aitutorandlab.repository.SubjectRepository
import com.ncert7.aitutorandlab.ui.models.ConceptUiModel
import com.ncert7.aitutorandlab.ui.screens.conceptscreen.dataclass.ConceptScreenState
import com.ncert7.aitutorandlab.utils.getLocalizedName
import com.ncert7.aitutorandlab.utils.isKannada
import com.ncert7.aitutorandlab.domain.progress.buildProgressUiModel
import com.ncert7.aitutorandlab.domain.progress.ChapterProgressService
import com.ncert7.aitutorandlab.data.local.dao.ProgressDao
import com.ncert7.aitutorandlab.config.AppConfig
import com.ncert7.aitutorandlab.domain.progress.ProgressEventTracker
import com.ncert7.aitutorandlab.domain.progress.model.ProgressStatus as DomainProgressStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingNavigation(
    val route: String,
    val isDirect: Boolean = false,
    val simulationUrl: String? = null,
    val simulationTitle: String? = null,
    val conceptId: String? = null
)

@HiltViewModel
class ConceptViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val chapterRepository: ChapterRepository,
    private val subjectRepository: SubjectRepository,
    private val studentRepository: StudentLocalRepository,
    private val chapterProgressService: ChapterProgressService,
    private val progressEventTracker: ProgressEventTracker,
    private val progressDao: ProgressDao,
    private val sharedPrefs: SharedPreferenceUtils
) : ViewModel() {

    private val _state = MutableStateFlow(ConceptScreenState())
    val state: StateFlow<ConceptScreenState> = _state.asStateFlow()

    private val _pendingNavigation = MutableStateFlow<PendingNavigation?>(null)
    val pendingNavigation: StateFlow<PendingNavigation?> = _pendingNavigation.asStateFlow()

    fun onSimulationOpened(simId: String) {
        val count = sharedPrefs.getSimulationOpenCount()
        DebugLogger.debugLog("ConceptVM", "Simulation Agent Clicked. Current Count: $count")
        val nav = PendingNavigation(route = "simulation_agent", conceptId = simId)
        if (count >= 5) {
            _pendingNavigation.value = nav
        } else {
            sharedPrefs.incrementSimulationOpenCount()
            _pendingNavigation.value = nav.copy(isDirect = true)
        }
    }

    fun onSimulationUrlOpened(title: String, url: String, conceptId: String) {
        val count = sharedPrefs.getSimulationOpenCount()
        DebugLogger.debugLog("ConceptVM", "Simulation URL Clicked. Current Count: $count")
        val nav = PendingNavigation(
            route = "concept_sim_view",
            simulationUrl = url,
            simulationTitle = title,
            conceptId = conceptId
        )
        if (count >= 5) {
            _pendingNavigation.value = nav
        } else {
            sharedPrefs.incrementSimulationOpenCount()
            _pendingNavigation.value = nav.copy(isDirect = true)
        }
    }

    fun markAdShown() {
        _pendingNavigation.value?.let { nav ->
            DebugLogger.debugLog("ConceptVM", "Ad shown, marking as direct. Incrementing count.")
            sharedPrefs.incrementSimulationOpenCount()
            _pendingNavigation.value = nav.copy(isDirect = true)
        }
    }

    fun clearPendingNavigation() {
        _pendingNavigation.value = null
    }

    fun loadConcepts(chapterId: String, type: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val studentId = sharedPrefs.getUserId() ?: ""
                val language = if (isKannada()) "kn" else "en"
                val chapter = chapterRepository.getChapter(chapterId)
                val subject = chapter?.let { subjectRepository.getSubject(it.subjectId) }
                val classLevel = 7 // Force class 7 syllabus display

                DebugLogger.debugLog("ConceptVM", " Loading concepts: chapterId=$chapterId, type=$type, language=$language")

                // Load concepts based on type using specialized repository methods
                val concepts = when {
                    type.equals("SIMULATION", ignoreCase = true) -> {
                        val simConcepts = conceptRepository.getSimulationConceptsForChapter(chapterId, language)
                        DebugLogger.debugLog("ConceptVM", " Loaded SIMULATION concepts: ${simConcepts.size}")
                        simConcepts
                    }
                    type.equals("MATH PROBLEM", ignoreCase = true) -> {
                        val mathConcepts = conceptRepository.getMathProblemConceptsForChapter(chapterId)
                        DebugLogger.debugLog("ConceptVM", " Loaded MATH PROBLEM concepts: ${mathConcepts.size}")
                        mathConcepts
                    }
                    type.equals("STUDY", ignoreCase = true) -> {
                        val studyConcepts = conceptRepository.getStudyConceptsForChapter(chapterId)
                        DebugLogger.debugLog("ConceptVM", " Loaded STUDY concepts: ${studyConcepts.size}")
                        studyConcepts
                    }
                    else -> {
                        DebugLogger.warnLog("ConceptVM", " Unknown type: $type, returning empty list")
                        emptyList()
                    }
                }

                if (concepts.isEmpty()) {
                    DebugLogger.warnLog("ConceptVM", " No concepts found for chapter=$chapterId, type=$type")
                }

                val subjectId = chapter?.subjectId ?: ""

                // Reactively collect progress changes for this chapter
                val progressFlow = progressDao.getAllProgress(studentId, AppConfig.APP_NAME)

                progressFlow.collect { allProgress ->
                    val conceptUiModels = concepts.mapIndexed { index, concept ->
                        // Determine display text based on concept type
                        val displayName = when {
                            type.equals("MATH PROBLEM", ignoreCase = true) -> {
                                // For Math Problems: Show problemTopicName (localized)
                                if (isKannada()) {
                                    concept.problemTopicNameKn.ifEmpty { concept.conceptName }
                                } else {
                                    concept.problemTopicName.ifEmpty { concept.conceptName }
                                }
                            }
                            else -> {
                                // For STUDY and SIMULATION: Show conceptName (localized)
                                concept.getLocalizedName()
                            }
                        }

                        // Get appropriate fields based on language and type
                        val simId = if (isKannada()) {
                            concept.simulationIdKannada
                        } else {
                            concept.simulationId
                        }

                        val simUrl = if (isKannada()) {
                            concept.simulationUrlKannada
                        } else {
                            concept.simulationUrl
                        }

                        val hasAgent = !simId.isNullOrBlank() && !simId.equals("null", ignoreCase = true)
                        val hasUrl = !simUrl.isNullOrBlank() && !simUrl.equals("null", ignoreCase = true)

                        // Determine concept status based on progress tracking
                        val status = when {
                            type.equals("SIMULATION", ignoreCase = true) -> {
                                determineSimulationStatus(
                                    allProgress, concept.conceptId, hasAgent, hasUrl, index, concepts
                                )
                            }
                            else -> {
                                // For STUDY and MATH PROBLEM types
                                val progress = allProgress.find { it.itemType == "CONCEPT" && it.itemId == concept.conceptId }
                                val prevStatus = if (index > 0) {
                                    allProgress.find { it.itemType == "CONCEPT" && it.itemId == concepts[index - 1].conceptId }?.status
                                } else null
                                determineConceptStatus(progress, index == 0, prevStatus)
                            }
                        }

                        ConceptUiModel(
                            id = concept.conceptId,
                            name = displayName,
                            order = concept.orderIndex,
                            status = when (status) {
                                ProgressStatus.COMPLETED.value -> DomainProgressStatus.COMPLETED
                                ProgressStatus.IN_PROGRESS.value, "STARTED" -> DomainProgressStatus.IN_PROGRESS
                                else -> DomainProgressStatus.NOT_STARTED
                            },
                            type = concept.type,
                            simulationUrl = simUrl ?: "",
                            simulationId = simId ?: "",
                            problemId = concept.problemId,
                            problemTopicName = if (isKannada()) concept.problemTopicNameKn else concept.problemTopicName
                        )
                    }

                    // Auto-unlock first concept if not started
                    if (conceptUiModels.isNotEmpty() && conceptUiModels[0].status == DomainProgressStatus.NOT_STARTED) {
                        unlockFirstConcept(studentId, conceptUiModels[0].id)
                    }

                    // Progress calculation based on loaded concepts
                    val completedLoadedConcepts = conceptUiModels.count { it.status == DomainProgressStatus.COMPLETED }
                    val progressUiModel = buildProgressUiModel(completedLoadedConcepts, conceptUiModels.size)

                    DebugLogger.debugLog(
                        "ConceptVM",
                        "Progress: completed=$completedLoadedConcepts of ${conceptUiModels.size} concepts (type=$type)"
                    )

                    _state.value = _state.value.copy(
                        concepts = conceptUiModels,
                        chapterName = chapter?.getLocalizedName() ?: "",
                        chapterId = chapterId,
                        type = type,
                        progressUiModel = progressUiModel,
                        subjectName = subject?.getLocalizedName() ?: "",
                        classLevel = classLevel,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("ConceptVM", "Error loading concepts: ${e.message}")
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /**
     * Determine the status for SIMULATION type concepts
     * Both Agent and URL must be completed for SIMULATION to be marked COMPLETED
     */
    private fun determineSimulationStatus(
        allProgress: List<ProgressEntity>,
        conceptId: String,
        hasAgent: Boolean,
        hasUrl: Boolean,
        index: Int,
        concepts: List<com.ncert7.aitutorandlab.data.local.entities.ConceptEntity>
    ): String {
        return when {
            hasAgent && hasUrl -> {
                val agentDone = allProgress.find { it.itemType == "SIMULATION_AGENT" && it.itemId == conceptId }
                    ?.status == ProgressStatus.COMPLETED.value
                val urlDone = allProgress.find { it.itemType == "SIMULATION" && it.itemId == conceptId }
                    ?.status == ProgressStatus.COMPLETED.value

                when {
                    agentDone && urlDone -> ProgressStatus.COMPLETED.value
                    agentDone || urlDone -> ProgressStatus.IN_PROGRESS.value
                    else -> {
                        val prevStatus = if (index > 0) {
                            allProgress.find { it.itemId == concepts[index - 1].conceptId }?.status
                        } else null
                        determineConceptStatus(null, index == 0, prevStatus)
                    }
                }
            }
            hasAgent -> {
                val agentDone = allProgress.find { it.itemType == "SIMULATION_AGENT" && it.itemId == conceptId }
                    ?.status == ProgressStatus.COMPLETED.value
                if (agentDone) ProgressStatus.COMPLETED.value else {
                    val prevStatus = if (index > 0) {
                        allProgress.find { it.itemId == concepts[index - 1].conceptId }?.status
                    } else null
                    determineConceptStatus(null, index == 0, prevStatus)
                }
            }
            hasUrl -> {
                val urlDone = allProgress.find { it.itemType == "SIMULATION" && it.itemId == conceptId }
                    ?.status == ProgressStatus.COMPLETED.value
                if (urlDone) ProgressStatus.COMPLETED.value else {
                    val prevStatus = if (index > 0) {
                        allProgress.find { it.itemId == concepts[index - 1].conceptId }?.status
                    } else null
                    determineConceptStatus(null, index == 0, prevStatus)
                }
            }
            else -> ProgressStatus.NOT_STARTED.value
        }
    }

    /**
     * Determine the status for STUDY and MATH PROBLEM type concepts
     * First concept is always unlocked
     * Subsequent concepts unlock only when previous is completed
     */
    private fun determineConceptStatus(
        progress: ProgressEntity?,
        isFirstConcept: Boolean,
        previousConceptStatus: String?
    ): String {
        if (progress != null) {
            return progress.status
        }

        if (isFirstConcept) {
            return ProgressStatus.IN_PROGRESS.value
        }

        if (previousConceptStatus == ProgressStatus.COMPLETED.value) {
            return ProgressStatus.IN_PROGRESS.value
        }

        return ProgressStatus.NOT_STARTED.value
    }



    private suspend fun unlockFirstConcept(studentId: String, conceptId: String) {
        try {
            val language = if (isKannada()) "kn" else "en"
            progressEventTracker.markStudyInProgress(studentId, conceptId, language)
            DebugLogger.debugLog("ConceptVM", "First concept unlocked: $conceptId")
        } catch (e: Exception) {
            DebugLogger.debugLog("ConceptVM", "Error unlocking first concept: ${e.message}")
        }
    }
}