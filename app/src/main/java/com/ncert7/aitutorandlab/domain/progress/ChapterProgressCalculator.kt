package com.ncert7.aitutorandlab.domain.progress

import com.ncert7.aitutorandlab.data.local.entities.ConceptEntity
import com.ncert7.aitutorandlab.domain.progress.model.ProgressStatus
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.domain.revisionagent.usecase.RevisionUseCase
import com.ncert7.aitutorandlab.repository.ConceptRepository
import com.ncert7.aitutorandlab.repository.ProgressRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.round

/**
 * Chapter Progress Calculator — Unified progress computation engine
 *
 * LOGIC:
 *
 * STUDY (equal share):
 *   - Math subject (has MATH PROBLEM concepts): counts MATH_AGENT completions per concept
 *   - Other subjects: counts CONCEPT completions per STUDY-type concept
 *
 * SIMULATION (equal share): Average across simulation concepts filtered by language
 *   - Per concept with Agent + URL: each 50%  → 100% when both done
 *   - Per concept with only Agent OR only URL: that one = 100%
 *   - If neither agent nor URL → concept skipped (not in denominator)
 *
 * REVISION (equal share, only if chapter has a revision agent):
 *   - % of STUDY concepts with REVISION_AGENT = COMPLETED
 *
 * OVERALL = sum of available components / count of available components
 *   - Available means: study concepts exist, OR simulation concepts exist, OR revision agent exists
 *   - Dynamic divisor ensures each present component contributes equally
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
        private const val TAG = "ChapterProgressCalc"
        private const val MATH_SUBJECT_ID = "5c0a6b6d-7c6b-4f35-9d5b-9fd0fd8e8a01"
    }

    /**
     * Calculate overall chapter progress (0–100) and persist it to chapter_agent_progress.
     *
     * @param studentId  Student identifier
     * @param chapterId  Chapter identifier
     * @param language   "en" or "kn" — determines which simulation concepts to include
     * @return           Overall progress percentage (0–100)
     */
    suspend fun calculateChapterProgress(
        studentId: String,
        chapterId: String,
        language: String = "en"
    ): Int {
        return try {
            // 1. Determine subject type for this chapter
            val chapter = conceptRepository.getChapter(chapterId)
            val isMathSubject = chapter?.subjectId == MATH_SUBJECT_ID

            // 2. Load the correct study-type concepts
            val studyConcepts: List<ConceptEntity>
            val mathConcepts: List<ConceptEntity>

            if (isMathSubject) {
                studyConcepts = emptyList() // math uses mathConcepts instead
                mathConcepts = conceptRepository.getMathProblemConceptsForChapter(chapterId)
            } else {
                studyConcepts = conceptRepository.getStudyConceptsForChapter(chapterId)
                mathConcepts = emptyList()
            }

            // 3. Simulation concepts filtered by language
            val simulationConcepts = conceptRepository.getSimulationConceptsForChapter(chapterId, language)

            // 4. Check for revision agent
            val hasRevisionAgent = checkChapterHasRevisionAgent(chapterId)

            // 5. Calculate each component
            val study = if (isMathSubject) {
                calculateMathProgress(studentId, mathConcepts, language)
            } else {
                calculateStudyProgress(studentId, studyConcepts, language)
            }
            val simulation = calculateSimulationProgress(studentId, simulationConcepts, language)
            val revision = if (hasRevisionAgent) {
                // Revision is always based on STUDY concepts, not math concepts
                val revisionBase = if (isMathSubject) {
                    conceptRepository.getStudyConceptsForChapter(chapterId)
                } else {
                    studyConcepts
                }
                calculateRevisionProgress(studentId, revisionBase, language)
            } else {
                0
            }

            // 6. Dynamic divisor — only count components that actually exist for this chapter
            var divisor = 0
            var sum = 0

            val hasStudyComponent = if (isMathSubject) mathConcepts.isNotEmpty() else studyConcepts.isNotEmpty()
            if (hasStudyComponent) {
                divisor++
                sum += study
            }
            if (simulationConcepts.isNotEmpty()) {
                divisor++
                sum += simulation
            }
            if (hasRevisionAgent) {
                divisor++
                sum += revision
            }

            val overall = if (divisor > 0) {
                round(sum.toFloat() / divisor).toInt()
            } else {
                0
            }
            val finalProgress = overall.coerceIn(0, 100)

            // 7. Persist to chapter_agent_progress via ProgressRepository
            progressRepository.updateChapterAgentProgress(
                studentId            = studentId,
                chapterId            = chapterId,
                language             = language,
                studyPercentage      = study,
                simulationPercentage = simulation,
                revisionPercentage   = revision,
                overallPercentage    = finalProgress
            )

            DebugLogger.debugLog(
                TAG,
                "Chapter $chapterId [$language]: " +
                        "isMath=$isMathSubject " +
                        "Study=$study% (${if (isMathSubject) mathConcepts.size else studyConcepts.size}) " +
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

    /** Returns true if the chapter has a revision agent available */
    private suspend fun checkChapterHasRevisionAgent(chapterId: String): Boolean {
        return try {
            revisionUseCase.getAvailableChapters().contains(chapterId)
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error checking revision agent for $chapterId: ${e.message}")
            false
        }
    }

    /**
     * Study progress for non-math subjects.
     * Counts CONCEPT items with status = COMPLETED.
     */
    private suspend fun calculateStudyProgress(
        studentId: String,
        concepts: List<ConceptEntity>,
        language: String
    ): Int {
        if (concepts.isEmpty()) return 0
        val completed = concepts.count { concept ->
            progressRepository.getProgress(studentId, "CONCEPT", concept.conceptId, language)
                ?.status == ProgressStatus.COMPLETED.value
        }
        val pct = (completed * 100) / concepts.size
        DebugLogger.debugLog(TAG, "Study: $completed/${concepts.size} = $pct%")
        return pct.coerceIn(0, 100)
    }

    /**
     * Study progress for Math subject.
     * Counts MATH_AGENT items with status = COMPLETED.
     * A math concept is "done" when its MATH_AGENT row is COMPLETED
     * (markMathAgentCompleted also marks CONCEPT/COMPLETED, but we key on MATH_AGENT here
     *  to stay consistent with what the Math screen actually tracks).
     */
    private suspend fun calculateMathProgress(
        studentId: String,
        concepts: List<ConceptEntity>,
        language: String
    ): Int {
        if (concepts.isEmpty()) return 0
        val completed = concepts.count { concept ->
            // Accept either MATH_AGENT or CONCEPT completion — whichever was written
            val mathAgentDone = progressRepository.getProgress(studentId, "MATH_AGENT", concept.conceptId, language)
                ?.status == ProgressStatus.COMPLETED.value
            val conceptDone = progressRepository.getProgress(studentId, "CONCEPT", concept.conceptId, language)
                ?.status == ProgressStatus.COMPLETED.value
            mathAgentDone || conceptDone
        }
        val pct = (completed * 100) / concepts.size
        DebugLogger.debugLog(TAG, "Math study: $completed/${concepts.size} = $pct%")
        return pct.coerceIn(0, 100)
    }

    /**
     * Average progress across SIMULATION concepts.
     * Per-concept logic:
     *   - Both Agent & URL exist: 50% each.
     *   - Only one exists: 100% for that one.
     *   - Neither: skip.
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
            val simId = if (language.equals("kn", ignoreCase = true)) concept.simulationIdKannada else concept.simulationId
            val simUrl = if (language.equals("kn", ignoreCase = true)) concept.simulationUrlKannada else concept.simulationUrl

            val hasAgent = !simId.isNullOrBlank() && simId != "null"
            val hasUrl = !simUrl.isNullOrBlank() && simUrl != "null"

            if (!hasAgent && !hasUrl) continue

            val agentDone = progressRepository.getProgress(studentId, "SIMULATION_AGENT", concept.conceptId, language)
                ?.status == ProgressStatus.COMPLETED.value
            val urlDone = progressRepository.getProgress(studentId, "SIMULATION", concept.conceptId, language)
                ?.status == ProgressStatus.COMPLETED.value

            val conceptScore = when {
                hasAgent && hasUrl -> {
                    var score = 0f
                    if (agentDone) score += 50f
                    if (urlDone) score += 50f
                    score
                }
                hasAgent -> if (agentDone) 100f else 0f
                else     -> if (urlDone)  100f else 0f
            }

            totalScore += conceptScore
            counted++
        }

        val pct = if (counted > 0) round(totalScore / counted).toInt() else 0
        DebugLogger.debugLog(TAG, "Simulation: $totalScore/$counted concepts = $pct%")
        return pct.coerceIn(0, 100)
    }

    /** % of STUDY concepts that have REVISION_AGENT = COMPLETED */
    private suspend fun calculateRevisionProgress(
        studentId: String,
        concepts: List<ConceptEntity>,
        language: String
    ): Int {
        if (concepts.isEmpty()) return 0
        val revised = concepts.count { concept ->
            progressRepository.getProgress(studentId, "REVISION_AGENT", concept.conceptId, language)
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
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            val startOfDay = cal.timeInMillis
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
            cal.set(java.util.Calendar.MINUTE, 59)
            cal.set(java.util.Calendar.SECOND, 59)
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