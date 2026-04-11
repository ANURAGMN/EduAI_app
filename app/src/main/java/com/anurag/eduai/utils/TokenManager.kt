package com.anurag.eduai.utils

import android.content.Context
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger

object TokenManager {
    private const val TAG = "TokenManager"

    fun saveToken(context: Context, token: String) {
        val prefs = SharedPreferenceUtils(context)
        prefs.setJwtToken(token)
        DebugLogger.debugLog(TAG, "JWT token stored. Token=$token")
    }

    fun getToken(context: Context): String? {
        val prefs = SharedPreferenceUtils(context)
        val token = prefs.getJwtToken()
        if (token != null) {
            DebugLogger.debugLog(TAG, "JWT token retrieved from storage. Token=$token")
        } else {
            DebugLogger.debugLog(TAG, "No JWT token found in storage")
        }
        return token
    }

    fun clearToken(context: Context) {
        SharedPreferenceUtils(context).clearJwtToken()
        DebugLogger.debugLog(TAG, "JWT token cleared from storage")
    }
}
