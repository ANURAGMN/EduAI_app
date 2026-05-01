package com.anurag.eduai.data.remote

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.anurag.eduai.BuildConfig
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

/**
 * OPTIMIZED: Handles token refresh with timeout protection and single mutex.
 *
 * Improvements:
 * - 10-second timeout on credential refresh (prevents hangs)
 * - Single mutex (no nested locks)
 * - Refresh result cached to avoid repeated attempts
 * - 5-second cooldown between refresh attempts
 * - Efficient error handling
 */
class GoogleCredentialManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private val sharedPrefs = SharedPreferenceUtils(context)

    companion object {
        private val refreshMutex = Mutex()
        private var lastRefreshTime = 0L
        private const val REFRESH_COOLDOWN_MS = 5000L
        private const val REFRESH_TIMEOUT_MS = 10000L // 10-second timeout to prevent hangs
        private var lastRefreshResult: Boolean? = null
        private var lastRefreshResultTime = 0L
    }

    /**
     * Ensures token is valid. If expired, attempts to refresh it.
     * Returns true if token is valid (either was already valid or successfully refreshed).
     * Uses timeout to prevent hangs.
     */
    suspend fun ensureValidToken(): Boolean = withContext(Dispatchers.IO) {
        // Token is still valid
        if (!sharedPrefs.isTokenExpiredOrExpiring()) {
            return@withContext true
        }

        return@withContext refreshMutex.withLock {
            // Double-check after acquiring lock
            if (!sharedPrefs.isTokenExpiredOrExpiring()) {
                return@withLock true
            }

            // Check if we recently tried to refresh (avoid repeated attempts)
            val now = System.currentTimeMillis()
            if (now - lastRefreshTime < REFRESH_COOLDOWN_MS) {
                // Return cached result if available
                if (lastRefreshResult != null && now - lastRefreshResultTime < REFRESH_COOLDOWN_MS) {
                    DebugLogger.debugLog(
                        "GoogleCredentialManager",
                        "Using cached refresh result: ${lastRefreshResult}"
                    )
                    return@withLock lastRefreshResult ?: false
                }
            }

            // Attempt refresh with timeout
            val refreshed = try {
                withTimeoutOrNull(REFRESH_TIMEOUT_MS) {
                    performTokenRefresh()
                } ?: false
            } catch (e: Exception) {
                DebugLogger.debugLog(
                    "GoogleCredentialManager",
                    "Refresh exception: ${e.message}"
                )
                false
            }

            lastRefreshTime = now
            lastRefreshResult = refreshed
            lastRefreshResultTime = now

            if (!refreshed) {
                clearAllTokens()
            }

            refreshed
        }
    }

    /**
     * Performs the actual token refresh from Credential Manager.
     * Must be called within a timeout context.
     */
    private suspend fun performTokenRefresh(): Boolean = withContext(Dispatchers.IO) {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.AUTH_KEY)
                .setFilterByAuthorizedAccounts(true)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == "com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN") {

                val idToken = credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")

                if (idToken != null && idToken.isNotEmpty()) {
                    val now = System.currentTimeMillis()
                    val expiryTimeMs = now + (60 * 60 * 1000)
                    sharedPrefs.setIdToken(idToken)
                    sharedPrefs.setTokenExpiryTime(expiryTimeMs)

                    DebugLogger.debugLog(
                        "GoogleCredentialManager",
                        "Token refreshed successfully"
                    )
                    return@withContext true
                }
            }

            DebugLogger.debugLog(
                "GoogleCredentialManager",
                "Credential refresh returned invalid token"
            )
            false

        } catch (e: NoCredentialException) {
            DebugLogger.debugLog(
                "GoogleCredentialManager",
                "No credential available: ${e.message}"
            )
            false
        } catch (e: GetCredentialException) {
            DebugLogger.debugLog(
                "GoogleCredentialManager",
                "Credential exception: ${e.message}"
            )
            false
        } catch (e: Exception) {
            DebugLogger.debugLog(
                "GoogleCredentialManager",
                "Unexpected error during refresh: ${e.message}"
            )
            false
        }
    }

    fun clearAllTokens() {
        sharedPrefs.clearIdToken()
        sharedPrefs.setTokenExpiryTime(0L)
    }

    fun getValidToken(): String? = sharedPrefs.getIdToken()
}

