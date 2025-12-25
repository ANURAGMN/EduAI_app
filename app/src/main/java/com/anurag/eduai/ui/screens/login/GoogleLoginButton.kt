package com.anurag.eduai.ui.screens.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.anurag.eduai.R
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.GoogleSignIn
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.ui.viewModel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun GoogleLoginButton(
    navController: NavController,
    userViewModel: UserViewModel,
) {
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    /**
     * Using rememberLauncherForActivityResult to keep the launcher alive and stable
     * Even if there is an UI update
     * It is useful because creating new launcher each time UI updates will break the result
     */
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        GoogleSignIn.doGoogleSignIn(
            context = context,
            scope = scope,
            launcher = null,
            onLoginSuccess = { },
            onLoginFailed = { }
        )
    }

    OutlinedButton(
        onClick = {
            if (!isLoading) {
                isLoading = true

                DebugLogger.debugLog("GoogleSignIn", "Google Sign In Button Clicked")

                GoogleSignIn.doGoogleSignIn(
                    context = context,
                    scope = scope,
                    launcher = launcher,
                    onLoginSuccess = {user ->
                        // TODO: store  user to local DB
                        DebugLogger.debugLog("GoogleSignIn", "User: \n $user")
                        scope.launch {
                            val userExists = userViewModel.handleGoogleLogin(user)

                            isLoading = false

                            if (userExists) {
                                // Existing user → go to home
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                // New user → pre-fill ViewModel data
                                userViewModel.updateId(user.id)
                                userViewModel.updateName(user.displayName)
                                userViewModel.updateEmail(user.email)
                                userViewModel.updateProfilePictureUri(user.profilePictureUri)

                                // Then navigate to detail entry screen
                                navController.navigate("userDetailEntry")
                            }
                        }
                    },
                    onLoginFailed = {error ->
                        DebugLogger.errorLog("GoogleSignIn", "Error:\n $error")
                        isLoading = false
                    }
                )
            }
        },
        enabled = !isLoading,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = White,
            contentColor = TextPrimary
        ),
        border = BorderStroke(1.dp, ColorHint),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isLoading) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.continue_with_google),
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}