package com.anurag.eduai.repository

import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.data.local.entities.ProgressEntity

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
     * Retrieves a list of concepts for a given chapter.
     * returns List of ConceptEntity
     */
    suspend fun getConceptsForChapter(chapterId: String, type: String): List<ConceptEntity> {
        return conceptDao.getConceptsForChapterSync(chapterId , type )
    }

    /**
     * Retrieves a specific concept by its ID.
     * returns ConceptEntity or null if not found
     */
    suspend fun getConcept(conceptId: String): ConceptEntity? {
        return conceptDao.getConcept(conceptId)
    }

    /**
     * Retrieves the progress of a student for a specific item.
     * returns ProgressEntity or null if not found
     */
    suspend fun getProgress(studentId: String, itemType: String, itemId: String): ProgressEntity? {
        return progressDao.getProgress(studentId, itemType, itemId)
    }

    /**
     * Updates the progress status of a specific item for a student.
     */
    suspend fun updateProgressStatus(
        studentId: String,
        itemType: String,
        itemId: String,
        newStatus: String,
        progressPercentage: Int,
        timestamp: Long
    ) {
        progressDao.updateProgressStatus(studentId, itemType, itemId, newStatus, progressPercentage,timestamp)
    }

    /**
     * Retrieves simulation concepts for English (with valid simulationId or simulationUrl).
     * This filters at the database level to only get concepts that have simulation data.
     */
    suspend fun getSimulationConceptsEnglish(chapterId: String): List<ConceptEntity> {
        return conceptDao.getSimulationConceptsEnglish(chapterId)
    }

    /**
     * Retrieves simulation concepts for Kannada (with valid simulationIdKannada or simulationUrlKannada).
     * This filters at the database level to only get concepts that have Kannada simulation data.
     */
    suspend fun getSimulationConceptsKannada(chapterId: String): List<ConceptEntity> {
        return conceptDao.getSimulationConceptsKannada(chapterId)
    }
}

