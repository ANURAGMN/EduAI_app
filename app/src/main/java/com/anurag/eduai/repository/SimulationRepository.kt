package com.anurag.eduai.repository

import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.ui.models.SimulationUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.typeOf

/**
 * Repository for managing simulation data
 * Currently hardcoded for Unit 8, can be extended to database later
 */
class SimulationRepository(
    private val conceptDao: ConceptDao
) {

    /**
     * Get all simulations for a specific chapter
     * @param chapterId The chapter ID (e.g., "8")
     * @return List of simulations for the chapter
     */
    suspend fun getSimulationsForChapter(chapterId: String): List<ConceptEntity> {
        return conceptDao.getConceptsForChapterSync(chapterId = chapterId, type = "SIMULATION")
    }

}