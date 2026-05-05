package com.anurag.eduai.data.local

import android.content.Context
import androidx.core.content.edit
import com.anurag.eduai.debug.DebugLogger
import org.json.JSONObject

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Repository for managing problem ID to thread ID and session ID mappings
 * Used exclusively by MathAgent flow for session persistence
 *
 * Storage structure:
 * SharedPreferences: "problem_session_map"
 * Key: "problem_thread_map"
 * Value: JSON object where each problem ID maps to {thread, session}
 *
 * Example:
 * {
 *   "maths-simplifying-algebraic-expressions": {
 *     "thread": "thread_123456",
 *     "session": "session_789"
 *   },
 *   "physics-kinematics": {
 *     "thread": "thread_abcdef",
 *     "session": "session_xyz"
 *   }
 * }
 */
class ProblemSessionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val problemThreadMap = mutableMapOf<String, String>()
    private val problemSessionMap = mutableMapOf<String, String?>()

    companion object {
        private const val PREFS_NAME = "problem_session_map"
        private const val PREFS_KEY = "problem_thread_map"
    }

    /**
     * Save mapping of problem ID to thread ID and session ID
     * Saved in shared preferences as JSON object
     */
    fun saveMapping(problemId: String, threadId: String, sessionId: String?) {
        if (problemId.isBlank() || threadId.isBlank()) {
            DebugLogger.errorLog(
                "ProblemSessionRepository",
                "✗ Cannot save mapping with blank problemId or threadId. problemId='$problemId', threadId='$threadId'"
            )
            return
        }

        // Save to in-memory cache first
        problemThreadMap[problemId] = threadId
        problemSessionMap[problemId] = sessionId

        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(PREFS_KEY, "{}") ?: "{}"
            val json = JSONObject(raw)

            val item = JSONObject().apply {
                put("thread", threadId)
                put("session", sessionId ?: "")
            }

            json.put(problemId, item)
            prefs.edit { putString(PREFS_KEY, json.toString()) }

            DebugLogger.debugLog(
                "ProblemSessionRepository",
                "✓ Saved mapping for problemId: '$problemId' -> threadId: '$threadId', sessionId: '${sessionId ?: "null"}'"
            )
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "ProblemSessionRepository",
                "✗ saveMapping failed: ${e.message}\nStack: ${e.stackTraceToString()}"
            )
        }
    }

    /**
     * Load mapping of problem ID to thread ID and session ID
     * Returns Pair(threadId, sessionId) or null if not found
     * Reads from in-memory cache first, then from shared preferences
     */
    fun loadMapping(problemId: String): Pair<String, String?>? {
        if (problemId.isBlank()) {
            DebugLogger.errorLog(
                "ProblemSessionRepository",
                "✗ Cannot load mapping with blank problemId"
            )
            return null
        }

        // Check in-memory cache first
        problemThreadMap[problemId]?.let { threadId ->
            val sessionId = problemSessionMap[problemId]
            DebugLogger.debugLog(
                "ProblemSessionRepository",
                "✓ Loaded from memory cache for problemId='$problemId': threadId='$threadId', sessionId='$sessionId'"
            )
            return Pair(threadId, sessionId)
        }

        // Load from shared preferences
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(PREFS_KEY, null)

            if (raw == null) {
                DebugLogger.debugLog(
                    "ProblemSessionRepository",
                    "No mapping data found in SharedPreferences for problemId: '$problemId'"
                )
                return null
            }

            val json = JSONObject(raw)

            if (!json.has(problemId)) {
                DebugLogger.debugLog(
                    "ProblemSessionRepository",
                    "✗ No mapping found for problemId: '$problemId'. Available keys: ${json.keys().asSequence().joinToString(", ")}"
                )
                return null
            }

            val obj = json.getJSONObject(problemId)
            val thread = obj.optString("thread", "")

            if (thread.isBlank()) {
                DebugLogger.errorLog(
                    "ProblemSessionRepository",
                    "✗ Thread ID is blank for problemId: '$problemId'"
                )
                return null
            }

            val sessionValue = obj.opt("session")
            val session = when {
                sessionValue == null || sessionValue == JSONObject.NULL -> null
                sessionValue is String && sessionValue.isNotBlank() -> sessionValue
                else -> null
            }

            // Cache in-memory for future lookups
            problemThreadMap[problemId] = thread
            problemSessionMap[problemId] = session

            DebugLogger.debugLog(
                "ProblemSessionRepository",
                "✓ Loaded from SharedPreferences for problemId='$problemId': threadId='$thread', sessionId='$session'"
            )
            Pair(thread, session)
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "ProblemSessionRepository",
                "✗ loadMapping failed for problemId='$problemId': ${e.message}\nStack: ${e.stackTraceToString()}"
            )
            null
        }
    }

    /**
     * Delete mapping for a specific problem ID
     * Used when user chooses to start a fresh session
     */
    fun deleteMapping(problemId: String) {
        if (problemId.isBlank()) {
            DebugLogger.errorLog("ProblemSessionRepository", "Cannot delete mapping with blank problemId")
            return
        }

        try {
            // Remove from in-memory cache
            problemThreadMap.remove(problemId)
            problemSessionMap.remove(problemId)

            // Remove from SharedPreferences
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(PREFS_KEY, "{}") ?: "{}"
            val json = JSONObject(raw)

            if (json.has(problemId)) {
                json.remove(problemId)
                prefs.edit { putString(PREFS_KEY, json.toString()) }
                DebugLogger.debugLog(
                    "ProblemSessionRepository",
                    "✓ Deleted mapping for problemId: '$problemId'"
                )
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "ProblemSessionRepository",
                "✗ deleteMapping failed: ${e.message}"
            )
        }
    }

    /**
     * Check if a mapping exists for a problem ID
     */
    fun hasMapping(problemId: String): Boolean {
        if (problemId.isBlank()) {
            DebugLogger.debugLog("ProblemSessionRepository", "hasMapping: problemId is blank")
            return false
        }

        // Check in-memory first
        if (problemThreadMap.containsKey(problemId)) {
            DebugLogger.debugLog("ProblemSessionRepository", "✓ Mapping found in memory cache for problemId: '$problemId'")
            return true
        }

        // Check SharedPreferences
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(PREFS_KEY, null) ?: return false
            val json = JSONObject(raw)
            val exists = json.has(problemId)

            DebugLogger.debugLog(
                "ProblemSessionRepository",
                if (exists) "✓ Mapping found in SharedPrefs for problemId: '$problemId'"
                else "✗ No mapping found in SharedPrefs for problemId: '$problemId'"
            )
            exists
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "ProblemSessionRepository",
                "✗ hasMapping check failed: ${e.message}"
            )
            false
        }
    }

    /**
     * Clear all problem session mappings
     * Called on logout
     */
    fun clearAllMappings() {
        try {
            problemThreadMap.clear()
            problemSessionMap.clear()

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                remove(PREFS_KEY)
            }
            DebugLogger.debugLog(
                "ProblemSessionRepository",
                "✓ Cleared all problem session mappings"
            )
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "ProblemSessionRepository",
                "✗ clearAllMappings failed: ${e.message}"
            )
        }
    }

    /**
     * Get all stored problem IDs (useful for debugging)
     */
    fun getAllProblemIds(): List<String> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(PREFS_KEY, "{}") ?: "{}"
            val json = JSONObject(raw)
            json.keys().asSequence().toList()
        } catch (e: Exception) {
            DebugLogger.errorLog("ProblemSessionRepository", "✗ Failed to get all problem IDs: ${e.message}")
            emptyList()
        }
    }
}
