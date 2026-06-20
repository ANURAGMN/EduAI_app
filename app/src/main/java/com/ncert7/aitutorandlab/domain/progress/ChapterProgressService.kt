package com.ncert7.aitutorandlab.domain.progress

import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chapter Progress Service — High-level chapter progress interface
 *
 * Wraps ChapterProgressCalculator and ProgressRepository to provide:
 * - Chapter progress calculation + persistence
 * - Completion status checks
 * - Reactive progress Flows
 * - Progress breakdown (study/simulation/revision)
 *
 * Uses ProgressRepository — no direct DAO access.
 */
@Singleton
class ChapterProgressService @Inject constructor(
    private val calculator: ChapterProgressCalculator,
    private val progressRepository: ProgressRepository
) {
    companion object {
        private const val TAG = "ChapterProgressService"
    }

    /**
     * Recalculate chapter progress, persist it to the chapter_agent_progress table, and return it.
     * Must be called after any activity is marked complete.
     *
     * Critical: without persisting here, getChapterProgressFlow() always emits 0%
     * because it reads from chapter_agent_progress which is never written to otherwise.
     */
    suspend fun updateChapterProgress(
        studentId: String,
        chapterId: String,
        language: String = "en"
    ): Int {
        val result = calculator.calculateDetailedChapterProgress(studentId, chapterId, language)
        try {
            progressRepository.updateChapterAgentProgress(
                studentId            = studentId,
                chapterId            = chapterId,
                language             = language,
                studyPercentage      = result.studyPercentage,
                simulationPercentage = result.simulationPercentage,
                revisionPercentage   = result.revisionPercentage,
                overallPercentage    = result.overallPercentage
            )
            DebugLogger.debugLog(TAG, "Chapter $chapterId [$language] persisted: ${result.overallPercentage}% (Study=${result.studyPercentage}%, Sim=${result.simulationPercentage}%, Rev=${result.revisionPercentage}%)")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Failed to persist chapter progress for $chapterId: ${e.message}")
        }
        return result.overallPercentage
    }

    /** Returns true if chapter overall progress is 100% (status = COMPLETED) */
    suspend fun isChapterCompleted(
        studentId: String,
        chapterId: String,
        language: String = "en"
    ): Boolean = calculator.isChapterFullyCompleted(studentId, chapterId, language)

    /**
     * Reactive Flow of overall progress for a specific chapter.
     * Emits whenever the chapter_agent_progress row changes.
     */
    fun getChapterProgressFlow(
        studentId: String,
        chapterId: String,
        language: String = "en"
    ): Flow<Int> = progressRepository.getChapterProgressFlow(studentId, chapterId, language)
        .map { it?.overallPercentage ?: 0 }

    /**
     * Detailed breakdown of study/simulation/revision percentages for a chapter.
     */
    suspend fun getChapterProgressBreakdown(
        studentId: String,
        chapterId: String,
        language: String = "en"
    ): ChapterProgressBreakdown {
        return try {
            val progress = progressRepository.getChapterAgentProgress(studentId, chapterId, language)
                ?: return ChapterProgressBreakdown(chapterId = chapterId, language = language)

            ChapterProgressBreakdown(
                chapterId            = progress.chapterId,
                studyPercentage      = progress.studyPercentage,
                simulationPercentage = progress.simulationPercentage,
                revisionPercentage   = progress.revisionPercentage,
                overallPercentage    = progress.overallPercentage,
                language             = language,
                isComplete           = progress.overallPercentage >= 100
            )
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error getting breakdown: ${e.message}")
            ChapterProgressBreakdown(chapterId = chapterId, language = language)
        }
    }

    /**
     * Get all chapters for a subject with their overall progress.
     */
    suspend fun getSubjectChapters(
        studentId: String,
        subjectId: String,
        language: String = "en"
    ): List<ChapterProgressSummary> {
        return try {
            progressRepository.getChapterWiseProgressSummary(studentId, language, subjectId)
                .map { dto ->
                    ChapterProgressSummary(
                        chapterId            = dto.chapterId,
                        chapterName          = dto.chapterName,
                        completionPercentage = dto.completionPercentage,
                        status               = dto.status
                    )
                }
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "getSubjectChapters error: ${e.message}")
            emptyList()
        }
    }

    /** Number of chapters with status = COMPLETED */
    suspend fun getCompletedChaptersCount(studentId: String, language: String = "en"): Int =
        progressRepository.getCompletedChaptersCount(studentId, language)
}

// ===== Data Transfer Objects =====

data class ChapterProgressBreakdown(
    val chapterId: String,
    val studyPercentage: Int = 0,
    val simulationPercentage: Int = 0,
    val revisionPercentage: Int = 0,
    val overallPercentage: Int = 0,
    val language: String = "en",
    val isComplete: Boolean = false
)

data class ChapterProgressSummary(
    val chapterId: String,
    val chapterName: String,
    val completionPercentage: Int,
    val status: String
)
