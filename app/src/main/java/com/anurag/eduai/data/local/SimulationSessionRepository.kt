package com.anurag.eduai.data.local

import android.content.Context
import androidx.core.content.edit
import com.anurag.eduai.debug.DebugLogger
import org.json.JSONObject

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Repository for managing simulation ID to session ID mappings
 * Used by SimulationAgent flow for session persistence
 *
 * Storage structure:
 * SharedPreferences: "simulation_session_map"
 * Key: "simulation_map"
 * Value: JSON object where each simulation ID maps to {session}
 *
 * Example:
 * {
 *   "malleability_kn": {
 *     "session": "shelarutika21@gmail.com-simulation-malleability_kn-english-thread-20260507-045817"
 *   },
 *   "simple_pendulum": {
 *     "session": "user@gmail.com-simulation-simple_pendulum-english-thread-20260507-120000"
 *   }
 * }
 */
class SimulationSessionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val simulationSessionMap = mutableMapOf<String, String?>()

    companion object {
        private const val PREFS_NAME = "simulation_session_map"
        private const val PREFS_KEY = "simulation_map"
    }

    /**
     * Save mapping of simulation ID to session ID
     * Saved in shared preferences as JSON object
     */
    fun saveMapping(simulationId: String, sessionId: String?) {
        if (simulationId.isBlank()) {
            DebugLogger.errorLog(
                "SimulationSessionRepository",
                "✗ Cannot save mapping with blank simulationId"
            )
            return
        }

        // Save to in-memory cache first
        simulationSessionMap[simulationId] = sessionId

        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(PREFS_KEY, "{}") ?: "{}"
            val json = JSONObject(raw)

            val item = JSONObject().apply {
                put("session", sessionId ?: "")
            }

            json.put(simulationId, item)
            prefs.edit { putString(PREFS_KEY, json.toString()) }

            DebugLogger.debugLog(
                "SimulationSessionRepository",
                "✓ Saved mapping for simulationId: '$simulationId' -> sessionId: '${sessionId ?: "null"}'"
            )
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "SimulationSessionRepository",
                "✗ saveMapping failed: ${e.message}\nStack: ${e.stackTraceToString()}"
            )
        }
    }

    /**
     * Load mapping of simulation ID to session ID
     * Returns sessionId or null if not found
     * Reads from in-memory cache first, then from shared preferences
     */
    fun loadMapping(simulationId: String): String? {
        if (simulationId.isBlank()) {
            DebugLogger.errorLog(
                "SimulationSessionRepository",
                "✗ Cannot load mapping with blank simulationId"
            )
            return null
        }

        // Check in-memory cache first
        simulationSessionMap[simulationId]?.let { sessionId ->
            DebugLogger.debugLog(
                "SimulationSessionRepository",
                "✓ Loaded from cache for '$simulationId': sessionId=$sessionId"
            )
            return sessionId
        }

        // Load from shared preferences
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(PREFS_KEY, null) ?: return null
            val json = JSONObject(raw)

            if (!json.has(simulationId)) {
                DebugLogger.debugLog(
                    "SimulationSessionRepository",
                    "✗ No mapping found for simulationId: '$simulationId'"
                )
                return null
            }

            val obj = json.getJSONObject(simulationId)
            val sessionValue = obj.opt("session")
            val session = when {
                sessionValue == null || sessionValue == JSONObject.NULL -> null
                sessionValue is String && sessionValue.isNotBlank() -> sessionValue
                else -> null
            }

            // Cache in-memory
            simulationSessionMap[simulationId] = session

            DebugLogger.debugLog(
                "SimulationSessionRepository",
                "✓ Loaded from SharedPrefs for '$simulationId': sessionId=$session"
            )
            session
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "SimulationSessionRepository",
                "✗ loadMapping failed: ${e.message}\nStack: ${e.stackTraceToString()}"
            )
            null
        }
    }

    /**
     * Delete mapping for a specific simulation
     * Used when starting a fresh session
     */
    fun deleteMapping(simulationId: String) {
        if (simulationId.isBlank()) return

        try {
            // Remove from in-memory cache
            simulationSessionMap.remove(simulationId)

            // Remove from SharedPreferences
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(PREFS_KEY, "{}") ?: "{}"
            val json = JSONObject(raw)

            if (json.has(simulationId)) {
                json.remove(simulationId)
                prefs.edit { putString(PREFS_KEY, json.toString()) }
                DebugLogger.debugLog(
                    "SimulationSessionRepository",
                    "✓ Deleted mapping for simulationId: '$simulationId'"
                )
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "SimulationSessionRepository",
                "✗ deleteMapping failed: ${e.message}\nStack: ${e.stackTraceToString()}"
            )
        }
    }

    /**
     * Clear all session mappings
     * Called on logout
     */
    fun clearAllMappings() {
        try {
            simulationSessionMap.clear()

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                remove(PREFS_KEY)
            }
            DebugLogger.debugLog(
                "SimulationSessionRepository",
                "✓ Cleared all session mappings"
            )
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "SimulationSessionRepository",
                "✗ clearAllMappings failed: ${e.message}\nStack: ${e.stackTraceToString()}"
            )
        }
    }

    /**
     * Check if a mapping exists for a simulation
     */
    fun hasMapping(simulationId: String): Boolean {
        if (simulationId.isBlank()) return false

        // Check in-memory first
        if (simulationSessionMap.containsKey(simulationId) && simulationSessionMap[simulationId] != null) return true

        // Check SharedPreferences
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(PREFS_KEY, null) ?: return false
            val json = JSONObject(raw)
            val hasKey = json.has(simulationId)

            if (hasKey) {
                val obj = json.getJSONObject(simulationId)
                val sessionValue = obj.opt("session")
                val hasSession = when {
                    sessionValue == null || sessionValue == JSONObject.NULL -> false
                    sessionValue is String && sessionValue.isNotBlank() -> true
                    else -> false
                }
                return hasSession
            }
            false
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "SimulationSessionRepository",
                " hasMapping check failed: ${e.message}"
            )
            false
        }
    }
}
