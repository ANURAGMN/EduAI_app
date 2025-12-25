package com.anurag.eduai.service

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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
                    DebugLogger.errorLog("GoogleSignIn", "No credentials found $e")
                    launcher?.launch(getIntent())
                    onLoginFailed(e)

                } catch (e: GetCredentialException) {
                    DebugLogger.errorLog("GoogleSignIn", "Credential exception $e")
                    onLoginFailed(e)

                } catch (e: Exception) {
                    DebugLogger.errorLog("GoogleSignIn", "Unexpected exception $e")
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
    }
}