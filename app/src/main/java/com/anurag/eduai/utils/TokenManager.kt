package com.anurag.eduai.utils

import android.content.Context
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.remote.GoogleCredentialManager

/**
 * Simple wrapper for token operations.
 * Token is stored in SharedPreferences and refreshed silently via Credential Manager.
 */
object TokenManager {

    fun saveIdToken(context: Context, idToken: String) {
        val prefs = SharedPreferenceUtils(context)
        prefs.setIdToken(idToken)
        prefs.setTokenExpiryTime(System.currentTimeMillis() + (60 * 60 * 1000))
    }

    fun getIdToken(context: Context): String? {
        return SharedPreferenceUtils(context).getIdToken()
    }

    suspend fun refreshTokenSilently(context: Context): Boolean {
        return GoogleCredentialManager(context).ensureValidToken()
    }

    fun isTokenExpiredOrExpiring(context: Context): Boolean {
        return SharedPreferenceUtils(context).isTokenExpiredOrExpiring()
    }

    fun clearAllTokens(context: Context) {
        GoogleCredentialManager(context).clearAllTokens()
        SharedPreferenceUtils(context).clearAllAuthData()
    }
}
