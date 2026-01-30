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
                title = "Simulation 1",
                htmlFileName = "unit_8_1.html",
                chapterId = "8"
            ),
            SimulationUiModel(
                id = "unit_8_2",
                title = "Simulation 2",
                htmlFileName = "unit_8_2.html",
                chapterId = "8"
            ),
            SimulationUiModel(
                id = "unit_8_3",
                title = "Simulation 3",
                htmlFileName = "unit_8_3.html",
                chapterId = "8"
            ),
            SimulationUiModel(
                id = "unit_8_4",
                title = "Simulation 4",
                htmlFileName = "unit_8_4.html",
                chapterId = "8"
            ),
            SimulationUiModel(
                id = "unit_8_5",
                title = "Simulation 5",
                htmlFileName = "unit_8_5.html",
                chapterId = "8"
            ),
            SimulationUiModel(
                id = "unit_8_6",
                title = "Simulation 6",
                htmlFileName = "unit_8_6.html",
                chapterId = "8"
            ),
            SimulationUiModel(
                id = "unit_8_7",
                title = "Simulation 7",
                htmlFileName = "unit_8_7.html",
                chapterId = "8"
            ),
            SimulationUiModel(
                id = "unit_8_8",
                title = "Simulation 8",
                htmlFileName = "unit_8_8.html",
                chapterId = "8"
            ),
            SimulationUiModel(
                id = "unit_8_9",
                title = "Simulation 9",
                htmlFileName = "unit_8_9.html",
                chapterId = "8"
            ),
            SimulationUiModel(
                id = "unit_8_10",
                title = "Simulation 10",
                htmlFileName = "unit_8_10.html",
                chapterId = "8"
            )
        )
    }
}