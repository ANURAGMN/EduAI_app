package com.anurag.eduai.repository

import com.anurag.eduai.ui.models.SimulationUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing simulation data
 * Currently hardcoded for Unit 8, can be extended to database later
 */
class SimulationRepository {

    /**
     * Get all simulations for a specific chapter
     * @param chapterId The chapter ID (e.g., "8")
     * @return List of simulations for the chapter
     */
    suspend fun getSimulationsForChapter(chapterId: String): List<SimulationUiModel> {
        return withContext(Dispatchers.IO) {
            when (chapterId) {
                "8" -> getUnit8Simulations()
                else -> emptyList()
            }
        }
    }

    /**
     * Get a specific simulation by ID
     * @param simulationId The simulation ID (e.g., "unit_8_1")
     * @return SimulationUiModel or null if not found
     */
    suspend fun getSimulation(simulationId: String): SimulationUiModel? {
        return withContext(Dispatchers.IO) {
            getAllSimulations().find { it.id == simulationId }
        }
    }

    /**
     * Get all available simulations across all chapters
     */
    private fun getAllSimulations(): List<SimulationUiModel> {
        return getUnit8Simulations()
    }

    /**
     * Hardcoded simulations for Unit 8
     */
    private fun getUnit8Simulations(): List<SimulationUiModel> {
        return listOf(
            SimulationUiModel(
                id = "unit_8_1",
                title = "unit_8_1.html",
                htmlFileName = "unit_8_1.html",
                chapterId = "8"
            ),
            SimulationUiModel(
                id = "unit_8_2",
                title = "unit_8_2.html",
                htmlFileName = "unit_8_2.html",
                chapterId = "8"
            )
        )
    }
}