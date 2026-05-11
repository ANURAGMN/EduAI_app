package com.anurag.eduai.service.sync

import android.content.Context
import android.widget.Toast
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.debug.DebugLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Simple notification for new simulations
 * Detects truly new simulations by checking if they exist in DB before insertion
 */
object NewSimulationNotifier {
    private const val TAG = "NewSimulationNotifier"

    /**
     * Check which simulations are new BEFORE inserting
     * Returns list of new SIMULATION type concepts
     */
    suspend fun getNewSimulations(
        incomingConcepts: List<ConceptEntity>,
        conceptDao: ConceptDao
    ): List<ConceptEntity> = withContext(Dispatchers.IO) {
        try {
            // Get existing concept IDs
            val existingConcepts = conceptDao.getAllConceptsSync()
            val existingIds = existingConcepts.map { it.conceptId }.toSet()

            // Filter new SIMULATION type concepts
            incomingConcepts.filter { concept ->
                concept.type.equals("SIMULATION", ignoreCase = true) &&
                !concept.simulationUrl.isNullOrBlank() &&
                concept.simulationUrl != "Not found" &&
                !existingIds.contains(concept.conceptId) // Only NEW ones
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error detecting new simulations: ${e.message}")
            emptyList()
        }
    }

    /**
     * Show toast for new simulations
     */
    fun showNotification(context: Context, newSimulations: List<ConceptEntity>) {
        if (newSimulations.isEmpty()) return

        try {
            val message = if (newSimulations.size == 1) {
                "New simulation added! 🎉"
            } else {
                "${newSimulations.size} new simulations added! 🎉"
            }

            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            DebugLogger.debugLog(TAG, "Toast: $message (${newSimulations.size} new)")

        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error showing toast: ${e.message}")
        }
    }
}
