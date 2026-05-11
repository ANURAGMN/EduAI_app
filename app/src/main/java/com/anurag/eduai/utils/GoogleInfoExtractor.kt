package com.anurag.eduai.utils

import com.anurag.eduai.config.AppConfig
import com.anurag.eduai.data.firebase.model.User
import com.anurag.eduai.debug.DebugLogger
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential


class GoogleInfoExtractor {

    companion object {

        fun extractUserInfo(googleIdTokenCredential: GoogleIdTokenCredential): User {
            return User(
                id = googleIdTokenCredential.id, // Keep Google ID for document reference
                email = googleIdTokenCredential.id, // Will be set to actual email by caller
                displayName = googleIdTokenCredential.displayName,
                profilePictureUri = googleIdTokenCredential.profilePictureUri?.toString(),
                schoolName = "",
                phoneNumber = "",
                studentClass = 7, // default value
                jwtToken = googleIdTokenCredential.idToken, // Extract JWT token
                appName = AppConfig.APP_NAME // set app identifier
            )
        }

        fun extractAndLogUserInfo(googleIdTokenCredential: GoogleIdTokenCredential): User {
            val userInfo = extractUserInfo(googleIdTokenCredential)

            DebugLogger.debugLog("GoogleUserInfo", "User ID: ${userInfo.id}")
            DebugLogger.debugLog("GoogleUserInfo", "Email: ${userInfo.email}")
            DebugLogger.debugLog("GoogleUserInfo", "Display Name: ${userInfo.displayName}")
            DebugLogger.debugLog("GoogleUserInfo", "Profile Picture: ${userInfo.profilePictureUri}")
            DebugLogger.debugLog("GoogleUserInfo", "JWT Token: ${userInfo.jwtToken}")

            return userInfo
        }
    }
}