package com.anurag.eduai.utils

import com.anurag.eduai.data.firebase.User
import com.anurag.eduai.debug.DebugLogger
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential


class GoogleInfoExtractor {

    companion object {

        fun extractUserInfo(googleIdTokenCredential: GoogleIdTokenCredential): User {
            return User(
                id = googleIdTokenCredential.id,
                email = googleIdTokenCredential.id, // id is mostly email address
                displayName = googleIdTokenCredential.displayName,
                profilePictureUri = googleIdTokenCredential.profilePictureUri?.toString(),
                schoolName = "",
                phoneNumber = "",
                studentClass = 7, // default value
                jwtToken = googleIdTokenCredential.idToken // Extract JWT token
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