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
        timestamp: Long
    ) {
        progressDao.updateProgressStatus(studentId, itemType, itemId, newStatus, timestamp)
    }
}

