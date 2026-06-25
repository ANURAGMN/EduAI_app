package com.ncert7.aitutorandlab.utils

import com.ncert7.aitutorandlab.config.AppConfig
import com.ncert7.aitutorandlab.data.firebase.model.User
import com.ncert7.aitutorandlab.debug.DebugLogger
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
            DebugLogger.debugLog(
                "GoogleUserInfo",
                "JWT received (length=${userInfo.jwtToken.length})"
            )

            return userInfo
        }
    }
}