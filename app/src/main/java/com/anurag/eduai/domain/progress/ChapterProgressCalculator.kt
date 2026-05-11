package com.anurag.eduai.domain.progress

import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.domain.progress.model.ProgressStatus
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.domain.revisionagent.usecase.RevisionUseCase
import com.anurag.eduai.repository.ConceptRepository
import com.anurag.eduai.repository.ProgressRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chapter Progress Calculator - Unified progress computation engine
 *
 * LOGIC:
 *
 * STUDY (50% or 33.33%): % of STUDY-type concepts completed
 *   - Only concepts with type = "STUDY" loaded for the chapter
 *
 * SIMULATION (50% or 33.33%): Average of simulation concepts (filtered by language & visibility)
 *   - Per concept: Agent (50%) + URL (50%) = 100% when both completed
 *   - Per concept: Agent OR URL alone = 100%
 *   - If a concept has neither agent nor URL row → not counted
 *
 * REVISION (33.33% or 0%): % of STUDY concepts with REVISION_AGENT = COMPLETED
 *   - Only if chapter has a revision agent (checked via RevisionUseCase)
 *   - With revision agent: divisor = 3, Without: divisor = 2
 *
 * OVERALL:
 *   - With revision: (STUDY + SIMULATION + REVISION) / 3
 *   - Without revision: (STUDY + SIMULATION) / 2
 *
 * NOTE: Uses ConceptRepository and ProgressRepository — no direct DAO access.
 */
@Singleton
class ChapterProgressCalculator @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val progressRepository: ProgressRepository,
    private val revisionUseCase: RevisionUseCase
) {
    companion object {
        private const val TAG = "ChapterProgressCalculator"
    }

    /**
     * Calculate overall chapter progress (0-100) and persist it to chapter_agent_progress.
     *
     * @param studentId   Student identifier
     * @param chapterId   Chapter identifier
     * @param language    "en" or "kn" — determines which simulation concepts to include
     * @return            Overall progress percentage (0–100)
     */
    suspend fun calculateChapterProgress(
        studentId: String,
        chapterId: String,
        language: String = "en"
    ): Int {
        return try {
            // 1. Get STUDY concepts for this chapter
            val studyConcepts = conceptRepository.getConceptsForChapter(chapterId, "STUDY")

            // 2. Get SIMULATION concepts filtered by language
            val simulationConcepts = if (language.equals("kn", ignoreCase = true)) {
                conceptRepository.getSimulationConceptsKannada(chapterId)
            } else {
                conceptRepository.getSimulationConceptsEnglish(chapterId)
            }

            // 3. Check for revision agent
            val hasRevisionAgent = checkChapterHasRevisionAgent(chapterId)

            // 4. Calculate each component
            val study     = calculateStudyProgress(studentId, studyConcepts)
            val simulation = calculateSimulationProgress(studentId, simulationConcepts, language)
            val revision  = if (hasRevisionAgent) calculateRevisionProgress(studentId, studyConcepts) else 0

            // 5. Compute overall with correct dynamic divisor
            var divisor = 0
            if (studyConcepts.isNotEmpty()) divisor++
            if (simulationConcepts.isNotEmpty()) divisor++
            if (hasRevisionAgent) divisor++

            val overall = if (divisor > 0) {
                (study + simulation + revision) / divisor
            } else {
                0
            }
            val finalProgress = overall.coerceIn(0, 100)

            // 6. Persist to chapter_agent_progress via ProgressRepository
            progressRepository.updateChapterAgentProgress(
                studentId     = studentId,
                chapterId     = chapterId,
                language      = language,
                studyPercentage      = study,
                simulationPercentage = simulation,
                revisionPercentage   = revision,
                overallPercentage    = finalProgress
            )

            DebugLogger.debugLog(
                TAG,
                "Chapter $chapterId [$language]: " +
                "Study=$study% (${studyConcepts.size}) " +
                "Sim=$simulation% (${simulationConcepts.size}) " +
                "Rev=$revision% (hasAgent=$hasRevisionAgent) " +
                "Divisor=$divisor Overall=$finalProgress%"
            )
            finalProgress
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error calculating chapter progress for $chapterId: ${e.message}")
            0
        }
    }

    // ===== PRIVATE HELPERS =====

    /** Returns true if the chapter has a revision agent available via RevisionUseCase */
    private suspend fun checkChapterHasRevisionAgent(chapterId: String): Boolean {
        return try {
            val availableChapters = revisionUseCase.getAvailableChapters()
            availableChapters.contains(chapterId).also {
                DebugLogger.debugLog(TAG, "Chapter $chapterId revision agent: $it")
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error checking revision agent for $chapterId: ${e.message}")
            false
        }
    }

    /** % of STUDY concepts with status = COMPLETED in the progress table */
    private suspend fun calculateStudyProgress(
        studentId: String,
        concepts: List<ConceptEntity>
    ): Int {
        if (concepts.isEmpty()) return 0
        val completed = concepts.count { concept ->
            progressRepository.getProgress(studentId, "CONCEPT", concept.conceptId)
                ?.status == ProgressStatus.COMPLETED.value
        }
        val pct = (completed * 100) / concepts.size
        DebugLogger.debugLog(TAG, "Study: $completed/${concepts.size} = $pct%")
        return pct.coerceIn(0, 100)
    }

    /**
     * Average progress across SIMULATION concepts.
     * Per-concept logic:
     * - If both Agent & URL exist: 50% each.
     * - If only one exists: 100% for that one.
     */
    private suspend fun calculateSimulationProgress(
        studentId: String,
        concepts: List<ConceptEntity>,
        language: String
    ): Int {
        if (concepts.isEmpty()) return 0

        var totalScore = 0f
        var counted = 0

        for (concept in concepts) {
            // Determine available components based on language
            val simId = if (language.equals("kn", ignoreCase = true)) concept.simulationIdKannada else concept.simulationId
            val simUrl = if (language.equals("kn", ignoreCase = true)) concept.simulationUrlKannada else concept.simulationUrl

            val hasAgent = !simId.isNullOrBlank() && !simId.equals("null", ignoreCase = true)
            val hasUrl = !simUrl.isNullOrBlank() && !simUrl.equals("null", ignoreCase = true)

            // Skip if no component exists for this language
            if (!hasAgent && !hasUrl) continue

            val agentDone = progressRepository.getProgress(studentId, "SIMULATION_AGENT", concept.conceptId)
                ?.status == ProgressStatus.COMPLETED.value
            val urlDone = progressRepository.getProgress(studentId, "SIMULATION", concept.conceptId)
                ?.status == ProgressStatus.COMPLETED.value

            val conceptScore = when {
                hasAgent && hasUrl -> {
                    var score = 0f
                    if (agentDone) score += 50f
                    if (urlDone) score += 50f
                    score
                }
                hasAgent -> if (agentDone) 100f else 0f
                hasUrl -> if (urlDone) 100f else 0f
                else -> 0f
            }

            totalScore += conceptScore
            counted++
        }

        val pct = if (counted > 0) (totalScore / counted).toInt() else 0
        DebugLogger.debugLog(TAG, "Simulation: $totalScore/$counted concepts = $pct%")
        return pct.coerceIn(0, 100)
    }

    /** % of STUDY concepts that have REVISION_AGENT = COMPLETED */
    private suspend fun calculateRevisionProgress(
        studentId: String,
        concepts: List<ConceptEntity>
    ): Int {
        if (concepts.isEmpty()) return 0
        val revised = concepts.count { concept ->
            progressRepository.getProgress(studentId, "REVISION_AGENT", concept.conceptId)
                ?.status == ProgressStatus.COMPLETED.value
        }
        val pct = (revised * 100) / concepts.size
        DebugLogger.debugLog(TAG, "Revision: $revised/${concepts.size} = $pct%")
        return pct.coerceIn(0, 100)
    }

    // ===== PUBLIC HELPERS =====

    /** Returns true if the chapter's stored progress is 100% COMPLETED */
    suspend fun isChapterFullyCompleted(
        studentId: String,
        chapterId: String,
        language: String = "en"
    ): Boolean {
        return try {
            progressRepository.getChapterAgentProgress(studentId, chapterId, language)
                ?.let { it.status == "COMPLETED" && it.overallPercentage >= 100 } ?: false
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error checking chapter completion: ${e.message}")
            false
        }
    }

    /** Count of all completed activities today (concepts + simulations) */
    suspend fun getTodayActivityCount(studentId: String): Int {
        return try {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0); cal.set(java.util.Calendar.MINUTE, 0); cal.set(java.util.Calendar.SECOND, 0)
            val startOfDay = cal.timeInMillis
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23); cal.set(java.util.Calendar.MINUTE, 59); cal.set(java.util.Calendar.SECOND, 59)
            val endOfDay = cal.timeInMillis

            progressRepository.getTodayCompletedConceptCount(studentId, startOfDay, endOfDay) +
            progressRepository.getTodayCompletedSimulationCount(studentId, startOfDay, endOfDay)
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error getting today activity count: ${e.message}")
            0
        }
    }

    /** 7-day activity summary for streak tracking */
    suspend fun getSevenDayActivitySummary(studentId: String): List<Pair<String, Int>> {
        return try {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
            val sevenDaysAgo = cal.timeInMillis
            progressRepository.getDailyCompletedActivityLast7Days(studentId, sevenDaysAgo)
                .map { Pair(it.date, it.count) }
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error getting 7-day activity summary: ${e.message}")
            emptyList()
        }
    }
}
