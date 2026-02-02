package com.anurag.eduai.ui.screens.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.anurag.eduai.R
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.auth.GoogleSignIn
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.ui.viewModel.LoginState
import com.anurag.eduai.ui.viewModel.UserViewModel
import com.anurag.eduai.ui.viewmodel_factory.UserViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun GoogleLoginButton(
    selectedLanguage: String,
    navController: NavController
) {
    val context = LocalContext.current
    val dimens = LocalDimensions.current
    val scope = rememberCoroutineScope()

    // Get ViewModel using factory
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory()
    )

    var isLoading by remember { mutableStateOf(false) }
    val loginState by userViewModel.loginState.collectAsStateWithLifecycle()

    // Handle login state changes
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.ExistingUser -> {
                DebugLogger.debugLog("GoogleLoginButton", "Existing user detected: ${state.user.email}")
                isLoading = true

                // Save user data locally and sync content
                userViewModel.saveExistingUserLocally(context) { success ->
                    isLoading = false
                    if (success) {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        DebugLogger.debugLog("GoogleLoginButton", "Failed to save existing user")
                    }
                }
            }
            is LoginState.NewUser -> {
                isLoading = false
                DebugLogger.debugLog("GoogleLoginButton", "New user detected, navigating to detail entry")
                navController.navigate("userDetailEntry")
            }
            is LoginState.Error -> {
                isLoading = false
                DebugLogger.debugLog("GoogleLoginButton", "Login error: ${state.exception.message}")
            }
            is LoginState.Loading -> {
                isLoading = true
            }
            is LoginState.Idle -> {
                isLoading = false
            }
        }
    }

    /**
     * Using rememberLauncherForActivityResult to keep the launcher alive and stable
     * Even if there is an UI update
     */
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // The actual result handling is done in GoogleSignIn.doGoogleSignIn
        DebugLogger.debugLog("GoogleLoginButton", "Activity result received: ${result.resultCode}")
    }

    OutlinedButton(
        onClick = {
            if (!isLoading) {
                GoogleSignIn.doGoogleSignIn(
                    context = context,
                    scope = scope,
                    launcher = launcher,
                    onLoginSuccess = { firebaseUser ->
                        // Handle login with ViewModel
                        scope.launch {
                            userViewModel.handleGoogleLogin(firebaseUser, selectedLanguage)
                        }
                        DebugLogger.debugLog("GoogleLoginButton", "Google sign-in successful: ${firebaseUser.email}")
                    },
                    onLoginFailed = { error ->
                        isLoading = false
                        DebugLogger.debugLog("GoogleLoginButton", "Google sign-in failed: $error")
                    }
                )
            }
        },
        enabled = !isLoading,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = White,
            contentColor = TextPrimary
        ),
        border = BorderStroke(dimens.inputBorderWidth, ColorHint),
        shape = RoundedCornerShape(dimens.cornerRadiusMedium),
        modifier = Modifier
            .fillMaxWidth()
            .size(height = dimens.buttonHeight, width = dimens.buttonHeight),
        contentPadding = PaddingValues(horizontal = dimens.buttonPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isLoading) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = stringResource(R.string.google_icon_desc),
                    modifier = Modifier.size(dimens.iconMedium)
                )
                Spacer(Modifier.width(dimens.spaceSmall + dimens.spaceExtraSmall))
                Text(
                    text = stringResource(R.string.continue_with_google),
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimens.iconMedium),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}