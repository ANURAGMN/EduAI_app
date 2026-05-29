package com.ncert7.aitutorandlab.repository

import com.ncert7.aitutorandlab.config.AppConfig
import com.ncert7.aitutorandlab.data.local.dao.ConceptDao
import com.ncert7.aitutorandlab.data.local.dao.ProgressDao
import com.ncert7.aitutorandlab.data.local.entities.ChapterEntity
import com.ncert7.aitutorandlab.data.local.entities.ConceptEntity
import com.ncert7.aitutorandlab.data.local.entities.ProgressEntity
import com.ncert7.aitutorandlab.utils.DatabaseRetryHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository class for managing concepts and their progress.
 */
 class ConceptRepository(
    private val conceptDao: ConceptDao,
    private val progressDao: ProgressDao
) {
    /**
     * Retrieves all concepts from the database.
     * returns List of ConceptEntity
     */
    suspend fun getAllConcepts(): List<ConceptEntity> {
        return conceptDao.getAllConceptsSync()
    }

    /**
     * Retrieves a list of concepts suspend fun getConceptsForChapter(chapterId: String, type: String): List<ConceptEntity>or a given chapter.
     * returns List of ConceptEntity
     */
    suspend fun getConceptsForChapter(chapterId: String, type: String): List<ConceptEntity> {
        return conceptDao.getConceptsForChapterSync(chapterId , type )
    }

    /**
     * Retrieves ALL concepts for a given chapter regardless of type (for debugging).
     * returns List of ConceptEntity
     */
    suspend fun getAllConceptsForChapter(chapterId: String): List<ConceptEntity> {
        return conceptDao.getConceptsForChapter(chapterId).first() // Get first emission from Flow
    }

    /**
     * Retrieves the progress of a student for a specific item.
     * returns ProgressEntity or null if not found
     */
    suspend fun getProgress(studentId: String, itemType: String, itemId: String, language: String): ProgressEntity? {
        return progressDao.getProgress(studentId, itemType, itemId, language, AppConfig.APP_NAME)
    }

    /**
     * Updates the progress status of a specific item for a student.
     */
    suspend fun updateProgressStatus(
        studentId: String,
        itemType: String,
        itemId: String,
        language: String,
        newStatus: String,
        progressPercentage: Int,
        timestamp: Long
    ) {
        progressDao.updateProgressStatus(studentId, itemType, itemId, AppConfig.APP_NAME, language, newStatus, progressPercentage, timestamp)
    }

    /**
     * Load STUDY type concepts for a non-math chapter
     * Used when user taps "Study" button on any non-math subject chapter
     *
     * @param chapterId The chapter ID
     * @return List of STUDY type concepts ordered by orderIndex
     */
    suspend fun getStudyConceptsForChapter(chapterId: String): List<ConceptEntity> {
        return try {
            val concepts = conceptDao.getStudyConceptsForChapter(chapterId)
            concepts
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Load MATH PROBLEM type concepts for the Math subject chapter
     * Only loads concepts with valid problemId
     * Used when user taps "Study" button on Math subject chapter
     *
     * @param chapterId The chapter ID
     * @return List of MATH PROBLEM type concepts with valid problemId, ordered by orderIndex
     */
    suspend fun getMathProblemConceptsForChapter(chapterId: String): List<ConceptEntity> {
        return try {
            val concepts = conceptDao.getMathProblemConceptsForChapter(chapterId)
            concepts
        } catch (e: Exception) {
            emptyList()
        }
    }
    /**
     * Load SIMULATION type concepts filtered by language
     * Only loads concepts with valid simulationId/simulationUrl based on language
     * Used when user taps "Simulation" button on chapter card
     *
     * @param chapterId The chapter ID
     * @param language "en" for English, "kn" for Kannada
     * @return List of SIMULATION type concepts available for the language
     */
    suspend fun getSimulationConceptsForChapter(chapterId: String, language: String): List<ConceptEntity> {
        return try {
            val concepts = conceptDao.getSimulationConceptsForChapter(chapterId, language)
            concepts
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Check if a chapter has STUDY concepts (for non-math subjects)
     * Used by ChapterViewModel to determine if "Study" button should be enabled
     *
     * @param chapterId The chapter ID
     * @return Number of STUDY concepts available
     */
    suspend fun getStudyConceptCount(chapterId: String): Int {
        return try {
            conceptDao.getStudyConceptCount(chapterId)
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getMathProblemConceptCount(chapterId: String): Int {
        return try {
            conceptDao.getMathProblemConceptCount(chapterId)
        } catch (e: Exception) {
            0
        }
    }


    /**
     * Check if a chapter has SIMULATION concepts for a specific language
     * Used by ChapterViewModel to determine if "Simulation" button should be enabled
     *
     * @param chapterId The chapter ID
     * @param language "en" for English, "kn" for Kannada
     * @return Number of SIMULATION concepts available for the language
     */
    suspend fun getSimulationConceptCount(chapterId: String, language: String): Int {
        return try {
            conceptDao.getSimulationConceptCount(chapterId, language)
        } catch (e: Exception) {
            0
        }
    }
    /**
     * Gets the total number of simulations completed today by a student
     */
    suspend fun getTodayCompletedSimulations(studentId: String): Int {
        return DatabaseRetryHelper.retryIfFailsNullable(maxRetries = 3) {
            progressDao.getTodayCompletedSimulations(studentId)
        } ?: 0
    }

    fun getConceptsForChapterFlow(chapterId: String): Flow<List<ConceptEntity>> {
        return conceptDao.getConceptsForChapter(chapterId)
    }

    suspend fun getConcept(conceptId: String): ConceptEntity? {
        return conceptDao.getConcept(conceptId)
    }

    fun getConceptFlow(conceptId: String): Flow<ConceptEntity?> {
        return conceptDao.getConceptFlow(conceptId)
    }

    suspend fun insertConcepts(concepts: List<ConceptEntity>) {
        conceptDao.insertConcepts(concepts)
    }

    suspend fun insertConcept(concept: ConceptEntity) {
        conceptDao.insertConcept(concept)
    }

    suspend fun updateConcept(concept: ConceptEntity) {
        conceptDao.updateConcept(concept)
    }

    suspend fun deleteConcept(conceptId: String) {
        conceptDao.deleteConcept(conceptId)
    }

    suspend fun deleteConceptsForChapter(chapterId: String) {
        conceptDao.deleteConceptsForChapter(chapterId)
    }

    /**
     * Get a chapter by its ID
     */
    suspend fun getChapter(chapterId: String): ChapterEntity? {
        return conceptDao.getChapter(chapterId)
    }

    /**
     * Get the chapter that contains a specific concept.
     */
    suspend fun getChapterForConcept(conceptId: String): ChapterEntity? {
        return conceptDao.getChapterForConcept(conceptId)
    }
}
