package com.anurag.eduai.service.auth

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.anurag.eduai.BuildConfig
import com.anurag.eduai.data.firebase.User
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.GoogleInfoExtractor
import com.anurag.eduai.utils.TokenManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class GoogleSignIn {

    companion object {
        fun doGoogleSignIn(
            context: Context,
            scope: CoroutineScope,
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
            onLoginSuccess: (user: User) -> Unit,
            onLoginFailed: (error: Throwable) -> Unit
        ) {
            val credentialManager = CredentialManager.create(context)

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getCredentialOptions(context))
                .build()

            scope.launch {
                try {
                    val result = credentialManager.getCredential(context, request)

                    when (result.credential) {
                        is CustomCredential -> {
                            if (result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                                val googleIdTokenCredential =
                                    GoogleIdTokenCredential.createFrom(result.credential.data)

                                val user: User =
                                    GoogleInfoExtractor.extractAndLogUserInfo(googleIdTokenCredential)

                                // Store JWT token
                                TokenManager.saveToken(context, user.jwtToken)
                                DebugLogger.debugLog("GoogleSignIn", "JWT token stored successfully")

                                onLoginSuccess(user)
                            } else {
                                val error = IllegalStateException("Unexpected credential type: ${result.credential.type}")
                                DebugLogger.errorLog("GoogleSignIn", error.message ?: "")
                                onLoginFailed(error)
                            }
                        }
                        else -> {
                            val error = IllegalStateException("Unknown credential object")
                            DebugLogger.errorLog("GoogleSignIn", error.message ?: "")
                            onLoginFailed(error)
                        }
                    }

                } catch (e: NoCredentialException) {
                    // This is not an error - user needs to add an account
                    // Launch account picker without calling onLoginFailed
                    DebugLogger.debugLog("GoogleSignIn", "No credentials found, launching account picker")
                    launcher?.launch(getIntent())

                } catch (e: GetCredentialException) {
                    // Check if it's a network-related error
                    val networkError = isNetworkError(e)
                    if (networkError) {
                        DebugLogger.errorLog("GoogleSignIn", "Network error during credential fetch: ${e.message}")
                        onLoginFailed(NetworkException("Network error. Please check your connection and try again.", e))
                    } else {
                        DebugLogger.errorLog("GoogleSignIn", "Credential exception: ${e.message}")
                        onLoginFailed(e)
                    }

                } catch (e: SocketTimeoutException) {
                    DebugLogger.errorLog("GoogleSignIn", "Connection timeout: ${e.message}")
                    onLoginFailed(NetworkException("Connection timeout. Please check your internet connection and try again.", e))

                } catch (e: UnknownHostException) {
                    DebugLogger.errorLog("GoogleSignIn", "Network unavailable: ${e.message}")
                    onLoginFailed(NetworkException("No internet connection. Please check your network and try again.", e))

                } catch (e: IOException) {
                    DebugLogger.errorLog("GoogleSignIn", "Network I/O error: ${e.message}")
                    onLoginFailed(NetworkException("Network error occurred. Please try again.", e))

                } catch (e: Exception) {
                    DebugLogger.errorLog("GoogleSignIn", "Unexpected exception: ${e.message}")
                    onLoginFailed(e)
                }
            }
        }

        private fun getIntent(): Intent {
            return Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            }
        }

        private fun getCredentialOptions(context: Context): CredentialOption {
            return GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(BuildConfig.AUTH_KEY)
                .build()
        }

        /**
         * Check if the exception is network-related
         */
        private fun isNetworkError(exception: Throwable): Boolean {
            val message = exception.message?.lowercase() ?: ""
            val cause = exception.cause

            return when {
                // Check exception message
                message.contains("network") -> true
                message.contains("timeout") -> true
                message.contains("connection") -> true
                message.contains("unable to resolve host") -> true
                message.contains("failed to connect") -> true

                // Check exception cause
                cause is SocketTimeoutException -> true
                cause is UnknownHostException -> true
                cause is IOException -> true

                else -> false
            }
        }
    }
}

/**
 * Custom exception for network-related errors
 */
class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)