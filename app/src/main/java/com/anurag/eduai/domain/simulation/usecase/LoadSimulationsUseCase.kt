package com.anurag.eduai.domain.simulation.usecase

import com.anurag.eduai.data.remote.AgenticAIClient
import com.anurag.eduai.debug.DebugLogger
import javax.inject.Inject

/**
 * Data class for simulation information
 */
data class SimulationInfo(
    val id: String,
    val title: String,
    val description: String
)

/**
 * Use case for managing available simulations
 */
class LoadSimulationsUseCase @Inject constructor(
    private val agenticAIClient: AgenticAIClient
) {
    private val tag = "LoadSimulationsUseCase"

    /**
     * Load all available simulations from the API
     * Fallback to default simulations if API call fails
     */
    suspend fun loadSimulations(): Result<List<SimulationInfo>> {
        return try {
            DebugLogger.debugLog(tag, "Loading available simulations...")

            val result = agenticAIClient.getAvailableSimulations()
            if (result.isSuccess) {
                val response = result.getOrNull()!!
                val simulations = response.simulations.map { sim ->
                    SimulationInfo(
                        id = sim.id,
                        title = sim.title,
                        description = sim.description
                    )
                }

                DebugLogger.debugLog(tag, "Loaded ${simulations.size} simulations")
                simulations.forEach {
                    DebugLogger.debugLog(tag, "  - ${it.title} (${it.id})")
                }
                Result.success(simulations)
            } else {
                throw result.exceptionOrNull() ?: Exception("Failed to load simulations")
            }

        } catch (e: Exception) {
            DebugLogger.errorLog(tag, "Failed to load simulations: ${e.message}")
            // Fallback to default simulations
            val defaultSimulations = listOf(
                SimulationInfo("simple_pendulum", "Simple Pendulum", ""),
                SimulationInfo("earth_rotation_revolution", "Earth Rotation & Revolution", ""),
                SimulationInfo("light_shadows", "Light & Shadows", "")
            )
            Result.success(defaultSimulations)
        }
    }
}
