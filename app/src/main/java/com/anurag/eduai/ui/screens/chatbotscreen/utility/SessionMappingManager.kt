package com.anurag.eduai.ui.screens.chatbotscreen.utility

import com.anurag.eduai.data.local.ConceptSessionRepository
import com.anurag.eduai.debug.DebugLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Session Mapping Manager
 */
@Singleton
class SessionMappingManager @Inject constructor(
    private val repository: ConceptSessionRepository
) {

    /**
     * Saves thread and session mapping for a concept
     *
     * @param concept Concept name
     * @param threadId Thread ID from backend
     * @param sessionId Session ID from backend
     * @param onMemoryUpdate Callback to update in-memory cache
     */
    suspend fun saveMapping(
        concept: String,
        threadId: String?,
        sessionId: String?,
        onMemoryUpdate: (threadId: String, sessionId: String?) -> Unit
    ) {
        if (threadId.isNullOrBlank()) return

        // Update in-memory cache
        onMemoryUpdate(threadId, sessionId)

        // Persist to storage
        withContext(Dispatchers.IO) {
            try {
                repository.saveMapping(concept, threadId, sessionId)
                DebugLogger.debugLog(
                    "SessionMappingManager",
                    "Saved mapping for concept: $concept (thread: $threadId, session: $sessionId)"
                )
            } catch (e: Exception) {
                DebugLogger.errorLog("SessionMappingManager", "saveMapping error: ${e.message}")
            }
        }
    }

    /**
     * Loads thread and session mapping for a concept
     *
     * @param concept Concept name
     * @param checkMemoryCache Check in-memory cache first
     * @param onMemoryUpdate Callback to update in-memory cache if loaded from storage
     * @return Pair of (threadId, sessionId) or null if not found
     */
    suspend fun loadMapping(
        concept: String,
        checkMemoryCache: () -> Pair<String, String?>?,
        onMemoryUpdate: (threadId: String, sessionId: String?) -> Unit
    ): Pair<String, String?>? {
        // Check in-memory cache first
        checkMemoryCache()?.let { return it }

        // Load from persistent storage
        return withContext(Dispatchers.IO) {
            try {
                repository.loadMapping(concept)?.also { (thread, session) ->
                    // Update in-memory cache
                    onMemoryUpdate(thread, session)
                    DebugLogger.debugLog(
                        "SessionMappingManager",
                        "Loaded mapping for concept: $concept from storage"
                    )
                }
            } catch (e: Exception) {
                DebugLogger.errorLog("SessionMappingManager", "loadMapping error: ${e.message}")
                null
            }
        }
    }

    /**
     * Checks if a session exists for a concept
     *
     * @param concept Concept name
     * @param checkMemoryCache Check in-memory cache
     * @return true if session exists, false otherwise
     */
    fun hasSession(
        concept: String,
        checkMemoryCache: () -> Boolean
    ): Boolean {
        // Check memory cache first
        if (checkMemoryCache()) return true

        // Check persistent storage
        return repository.loadMapping(concept) != null
    }

    /**
     * Deletes mapping for a concept
     *
     * @param concept Concept name
     */
    suspend fun deleteMapping(concept: String) {
        withContext(Dispatchers.IO) {
            try {
                repository.deleteMapping(concept)
                DebugLogger.debugLog("SessionMappingManager", "Deleted mapping for concept: $concept")
            } catch (e: Exception) {
                DebugLogger.errorLog("SessionMappingManager", "deleteMapping error: ${e.message}")
            }
        }
    }

    /**
     * Clears all mappings
     */
    suspend fun clearAllMappings() {
        withContext(Dispatchers.IO) {
            try {
                repository.clearAllMappings()
                DebugLogger.debugLog("SessionMappingManager", "Cleared all mappings")
            } catch (e: Exception) {
                DebugLogger.errorLog("SessionMappingManager", "clearAllMappings error: ${e.message}")
            }
        }
    }
}
