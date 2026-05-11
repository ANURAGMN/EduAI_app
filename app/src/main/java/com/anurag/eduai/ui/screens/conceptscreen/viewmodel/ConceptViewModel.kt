package com.anurag.eduai.ui.screens.conceptscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.entities.ProgressEntity
import com.anurag.eduai.domain.progress.model.ProgressStatus
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.ChapterRepository
import com.anurag.eduai.repository.ConceptRepository
import com.anurag.eduai.repository.StudentLocalRepository
import com.anurag.eduai.repository.SubjectRepository
import com.anurag.eduai.ui.models.ConceptUiModel
import com.anurag.eduai.ui.screens.conceptscreen.dataclass.ConceptScreenState
import com.anurag.eduai.utils.getLocalizedName
import com.anurag.eduai.utils.isKannada
import com.anurag.eduai.domain.progress.buildProgressUiModel
import com.anurag.eduai.domain.progress.ChapterProgressService
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.config.AppConfig
import com.anurag.eduai.domain.progress.model.ProgressStatus as DomainProgressStatus
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
    private val progressEventTracker: com.anurag.eduai.domain.progress.ProgressEventTracker,
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

                // 1. Get base concepts
                val concepts = if (type.equals("SIMULATION", ignoreCase = true)) {
                    if (isKannada()) conceptRepository.getSimulationConceptsKannada(chapterId)
                    else conceptRepository.getSimulationConceptsEnglish(chapterId)
                } else {
                    conceptRepository.getConceptsForChapter(chapterId, type)
                }

                // 2. Reactively collect progress changes for this chapter
                // Combine individual progress and aggregated chapter progress
                val progressFlow = progressDao.getAllProgress(studentId, AppConfig.APP_NAME)
                val chapterFlow = chapterProgressService.getChapterProgressFlow(studentId, chapterId, language)

                kotlinx.coroutines.flow.combine(progressFlow, chapterFlow) { allProgress, overallPct ->
                    val conceptUiModels = concepts.mapIndexed { index, concept ->
                        val simId = if (isKannada()) concept.simulationIdKannada else concept.simulationId
                        val simUrl = if (isKannada()) concept.simulationUrlKannada else concept.simulationUrl

                        val hasAgent = !simId.isNullOrBlank() && !simId.equals("null", ignoreCase = true)
                        val hasUrl = !simUrl.isNullOrBlank() && !simUrl.equals("null", ignoreCase = true)

                        val status = if (concept.type.equals("SIMULATION", ignoreCase = true)) {
                            val agentDone = allProgress.find { it.itemType == "SIMULATION_AGENT" && it.itemId == concept.conceptId }
                                ?.status == ProgressStatus.COMPLETED.value
                            val urlDone = allProgress.find { it.itemType == "SIMULATION" && it.itemId == concept.conceptId }
                                ?.status == ProgressStatus.COMPLETED.value

                            when {
                                hasAgent && hasUrl -> {
                                    if (agentDone && urlDone) ProgressStatus.COMPLETED.value
                                    else if (agentDone || urlDone) ProgressStatus.IN_PROGRESS.value
                                    else {
                                        val prevStatus = if (index > 0) {
                                            val pc = concepts[index - 1]
                                            val pt = if (pc.type.equals("SIMULATION", ignoreCase = true)) "SIMULATION" else "CONCEPT"
                                            allProgress.find { (it.itemType == pt || it.itemType == "SIMULATION_AGENT") && it.itemId == pc.conceptId }?.status
                                        } else null
                                        determineConceptStatus(null, index == 0, prevStatus)
                                    }
                                }
                                hasAgent -> if (agentDone) ProgressStatus.COMPLETED.value else {
                                    val prevStatus = if (index > 0) {
                                        val pc = concepts[index - 1]
                                        val pt = if (pc.type.equals("SIMULATION", ignoreCase = true)) "SIMULATION" else "CONCEPT"
                                        allProgress.find { (it.itemType == pt || it.itemType == "SIMULATION_AGENT") && it.itemId == pc.conceptId }?.status
                                    } else null
                                    determineConceptStatus(null, index == 0, prevStatus)
                                }
                                hasUrl -> if (urlDone) ProgressStatus.COMPLETED.value else {
                                    val prevStatus = if (index > 0) {
                                        val pc = concepts[index - 1]
                                        val pt = if (pc.type.equals("SIMULATION", ignoreCase = true)) "SIMULATION" else "CONCEPT"
                                        allProgress.find { (it.itemType == pt || it.itemType == "SIMULATION_AGENT") && it.itemId == pc.conceptId }?.status
                                    } else null
                                    determineConceptStatus(null, index == 0, prevStatus)
                                }
                                else -> ProgressStatus.NOT_STARTED.value
                            }
                        } else {
                            val progress = allProgress.find { it.itemType == "CONCEPT" && it.itemId == concept.conceptId }
                            val prevStatus = if (index > 0) {
                                val pc = concepts[index - 1]
                                val pt = if (pc.type.equals("SIMULATION", ignoreCase = true)) "SIMULATION" else "CONCEPT"
                                allProgress.find { (it.itemType == pt || it.itemType == "SIMULATION_AGENT") && it.itemId == pc.conceptId }?.status
                            } else null

                            determineConceptStatus(
                                progress = progress,
                                isFirstConcept = index == 0,
                                previousConceptStatus = prevStatus
                            )
                        }

                        ConceptUiModel(
                            id = concept.conceptId,
                            name = concept.getLocalizedName(),
                            order = concept.orderIndex,
                            status = when (status) {
                                ProgressStatus.COMPLETED.value -> DomainProgressStatus.COMPLETED
                                ProgressStatus.IN_PROGRESS.value, "STARTED" -> DomainProgressStatus.IN_PROGRESS
                                else -> DomainProgressStatus.NOT_STARTED
                            },
                            type = concept.type,
                            simulationUrl = simUrl,
                            simulationId = simId
                        )
                    }

                    // Auto-unlock first concept if not started
                    if (conceptUiModels.isNotEmpty() && conceptUiModels[0].status == DomainProgressStatus.NOT_STARTED) {
                        unlockFirstConcept(studentId, conceptUiModels[0].id)
                    }

                    // Calculate header progress consistently with Chapter/Progress screens
                    val chapterTotalConcepts = if ((chapter?.totalConcepts ?: 0) > 0) chapter!!.totalConcepts else concepts.size
                    val derivedCompletedCount = if (chapterTotalConcepts > 0) {
                        ((overallPct.toFloat() / 100f) * chapterTotalConcepts).toInt()
                    } else 0

                    _state.value = _state.value.copy(
                        concepts = conceptUiModels,
                        chapterName = chapter?.getLocalizedName() ?: "",
                        chapterId = chapterId,
                        type = type,
                        progressUiModel = buildProgressUiModel(derivedCompletedCount, chapterTotalConcepts),
                        subjectName = subject?.getLocalizedName() ?: "",
                        classLevel = classLevel,
                        isLoading = false,
                        error = null
                    )
                }.collect()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun determineConceptStatus(
        progress: ProgressEntity?,
        isFirstConcept: Boolean,
        previousConceptStatus: String?
    ): String {
        // If progress exists, use its status
        if (progress != null) {
            return progress.status
        }

        // First concept is always unlocked (IN_PROGRESS)
        if (isFirstConcept) {
            return ProgressStatus.IN_PROGRESS.value
        }

        // Unlock next concept only if previous is completed
        if (previousConceptStatus == ProgressStatus.COMPLETED.value) {
            return ProgressStatus.IN_PROGRESS.value
        }

        // Otherwise, keep locked
        return ProgressStatus.NOT_STARTED.value
    }

    private suspend fun unlockFirstConcept(studentId: String, conceptId: String) {
        try {
            val language = if (isKannada()) "kn" else "en"
            progressEventTracker.markStudyInProgress(studentId, conceptId, language)
            DebugLogger.debugLog("ConceptViewModel", "First concept unlocked via tracker: $conceptId")
        } catch (e: Exception) {
            DebugLogger.debugLog("ConceptViewModel", "Error unlocking first concept: ${e.message}")
        }
    }
}