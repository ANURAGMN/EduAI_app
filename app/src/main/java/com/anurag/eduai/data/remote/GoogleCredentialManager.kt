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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Handles silent token refresh using Android Credential Manager.
 * Prevents concurrent refresh attempts and UI popups using Mutex.
 */
class GoogleCredentialManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private val sharedPrefs = SharedPreferenceUtils(context)

    companion object {
        private val refreshMutex = Mutex()
        private var lastRefreshTime = 0L
        private const val REFRESH_COOLDOWN_MS = 5000
    }

    suspend fun silentRefreshToken(): Boolean = withContext(Dispatchers.IO) {
        // Check cooldown to avoid too frequent refresh attempts
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime < REFRESH_COOLDOWN_MS) {
            return@withContext false
        }

        return@withContext refreshMutex.withLock {
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
                        val expiryTimeMs = now + (60 * 60 * 1000)
                        sharedPrefs.setIdToken(idToken)
                        sharedPrefs.setTokenExpiryTime(expiryTimeMs)
                        lastRefreshTime = now
                        return@withLock true
                    }
                }

                lastRefreshTime = now
                false

            } catch (e: NoCredentialException) {
                lastRefreshTime = now
                false
            } catch (e: GetCredentialException) {
                lastRefreshTime = now
                false
            } catch (e: Exception) {
                lastRefreshTime = now
                false
            }
        }
    }

    suspend fun ensureValidToken(): Boolean = withContext(Dispatchers.IO) {
        if (!sharedPrefs.isTokenExpiredOrExpiring()) {
            return@withContext true
        }

        return@withContext refreshMutex.withLock {
            val refreshed = silentRefreshToken()
            if (!refreshed) {
                clearAllTokens()
            }
            refreshed
        }
    }

    fun clearAllTokens() {
        sharedPrefs.clearIdToken()
        sharedPrefs.setTokenExpiryTime(0L)
    }

    fun getValidToken(): String? = sharedPrefs.getIdToken()
}

