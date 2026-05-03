package com.anurag.eduai.utils

import android.content.Context
import android.widget.Toast
import com.anurag.eduai.BuildConfig
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.UnknownHostException

object TokenManager {

    private val refreshMutex = Mutex()

    // ──────────────────────────── Save / Get / Clear ────────────────────────────

    /**
     * @param isRefresh  Pass true when called from refreshTokenSilently()
     *                   so the new token gets logged for verification.
     */
    fun saveIdToken(context: Context, idToken: String, isRefresh: Boolean = false) {
        val prefs = SharedPreferenceUtils(context)
        prefs.setIdToken(idToken)
        prefs.setTokenExpiryTime(System.currentTimeMillis() + 60 * 60 * 1000L)

        if (isRefresh) {
            // Short preview — safe to share in logs (last 20 chars only)
            val preview = if (idToken.length > 20) "...${idToken.takeLast(20)}" else idToken
            DebugLogger.debugLog("TokenManager", " REFRESHED token preview (last 20): $preview")
            // Full token — remove this line before production release
            DebugLogger.debugLog("TokenManager", " REFRESHED full token: $idToken")
        } else {
            DebugLogger.debugLog("TokenManager", "Token saved (login), expires in 60 min")
        }
    }

    fun getIdToken(context: Context): String? {
        return SharedPreferenceUtils(context).getIdToken()
    }

    fun isTokenExpiredOrExpiring(context: Context): Boolean {
        return SharedPreferenceUtils(context).isTokenExpiredOrExpiring()
    }

    fun clearAllTokens(context: Context) {
        val prefs = SharedPreferenceUtils(context)
        prefs.clearIdToken()
        prefs.setTokenExpiryTime(0L)
        DebugLogger.debugLog("TokenManager", "All tokens cleared")
    }

    // ──────────────────────────── Silent Refresh ─────────────────────────────────

    suspend fun refreshTokenSilently(context: Context): Boolean = withContext(Dispatchers.IO) {
        refreshMutex.withLock {
            if (!isTokenExpiredOrExpiring(context)) {
                DebugLogger.debugLog("TokenManager", "Token still valid after acquiring lock, skip refresh")
                return@withLock true
            }

            DebugLogger.debugLog("TokenManager", "Starting silent token refresh")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.AUTH_KEY)
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, gso)

            try {
                val account = withTimeoutOrNull(15_000L) {
                    googleSignInClient.silentSignIn().await()
                }

                if (account == null) {
                    DebugLogger.errorLog("TokenManager", "Silent sign-in timed out")
                    showToastOnMain(context, "Could not refresh session. Please check your connection.")
                    return@withLock false
                }

                val newToken = account.idToken
                if (newToken.isNullOrEmpty()) {
                    DebugLogger.errorLog("TokenManager", "Silent sign-in succeeded but idToken is null")
                    return@withLock false
                }

                //  isRefresh=true → logs the new token for verification
                saveIdToken(context, newToken, isRefresh = true)
                DebugLogger.debugLog("TokenManager", "Token refreshed successfully via silent sign-in")
                return@withLock true

            } catch (e: UnknownHostException) {
                DebugLogger.debugLog("TokenManager", "Offline: cannot refresh token")
                showToastOnMain(context, "Connect to the internet to refresh your session.")
                return@withLock false

            } catch (e: Exception) {
                val msg = e.message ?: e.javaClass.simpleName
                DebugLogger.errorLog("TokenManager", "Silent sign-in failed: $msg")
                if (isNetworkException(e)) {
                    showToastOnMain(context, "Connect to the internet to refresh your session.")
                }
                return@withLock false
            }
        }
    }

    // ──────────────────────────── Helpers ────────────────────────────────────────

    private fun isNetworkException(e: Exception): Boolean {
        val msg = e.message?.lowercase() ?: ""
        return e is java.io.IOException ||
                msg.contains("network") ||
                msg.contains("timeout") ||
                msg.contains("unable to resolve") ||
                msg.contains("failed to connect")
    }

    private suspend fun showToastOnMain(context: Context, message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }
}