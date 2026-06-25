package com.ncert7.aitutorandlab.service.auth

import com.ncert7.aitutorandlab.BuildConfig

/**
 * In-app email + password sign-in for @padaams.in accounts (Play review / institutional).
 * Does not use Google OAuth — avoids Google account OTP challenges for reviewers.
 */
object PadaamsEmailAuth {

    private const val ALLOWED_DOMAIN = "@padaams.in"

    fun isEnabled(): Boolean = BuildConfig.PADAAMS_SIGNIN_PASSWORD.isNotBlank()

    fun isPadaamsEmail(email: String): Boolean {
        val normalized = email.trim().lowercase()
        return normalized.endsWith(ALLOWED_DOMAIN) && normalized.length > ALLOWED_DOMAIN.length
    }

    fun validateCredentials(email: String, password: String): Boolean {
        if (!isEnabled()) return false
        if (!isPadaamsEmail(email)) return false
        if (password.isBlank()) return false
        return constantTimeEquals(password, BuildConfig.PADAAMS_SIGNIN_PASSWORD)
    }

    fun displayNameFor(email: String): String {
        val local = email.trim().lowercase().substringBefore("@")
        return local.replace(".", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            .ifBlank { "Reviewer" }
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        val aBytes = a.toByteArray(Charsets.UTF_8)
        val bBytes = b.toByteArray(Charsets.UTF_8)
        if (aBytes.size != bBytes.size) return false
        var result = 0
        for (i in aBytes.indices) {
            result = result or (aBytes[i].toInt() xor bBytes[i].toInt())
        }
        return result == 0
    }
}
