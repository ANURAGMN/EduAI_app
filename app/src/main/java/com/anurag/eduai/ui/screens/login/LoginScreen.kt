package com.anurag.eduai.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.anurag.eduai.R
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.login.components.FooterCard
import com.anurag.eduai.ui.screens.login.components.GoogleLoginButton
import com.anurag.eduai.ui.screens.login.components.LanguageSelector
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.BackgroundSecondary
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.screens.login.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val dimens = LocalDimensions.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.LOGIN)


    val selectedLanguage by userViewModel.selectedLanguage.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Show snackbar when error message is set
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            errorMessage = null // Clear after showing
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(BackgroundSecondary),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundSecondary)
                    .padding(dimens.screenPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(dimens.spaceLarge),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = stringResource(R.string.app_logo_desc),
                        modifier = Modifier.height(dimens.containerMinHeight - dimens.buttonHeight)
                    )
                }

                // Language Selector Card
                LanguageSelector(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { langCode ->
                        userViewModel.setLanguage(langCode)
                    }
                )

                Spacer(modifier = Modifier.height(dimens.spaceMedium))

                // Main Login/SignUp Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                    elevation = CardDefaults.cardElevation(dimens.cardElevation + dimens.spaceExtraSmall),
                    colors = CardDefaults.cardColors(containerColor = BackgroundPrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.spaceLarge - dimens.spaceExtraSmall),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Welcome Header
                        Text(
                            text = stringResource(R.string.welcome_emoji),
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(bottom = dimens.spaceSmall)
                        )
                        Text(
                            text = stringResource(R.string.welcome_message),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.welcome_message_secondary),
                            style = MaterialTheme.typography.titleSmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(dimens.spaceLarge - dimens.spaceExtraSmall))

                        // Google Sign in
                        GoogleLoginButton(
                            navController = navController,
                            userViewModel = userViewModel,
                            onError = { error ->
                                errorMessage = error
                            }
                        )

                        Spacer(modifier = Modifier.height(dimens.spaceMedium))

                        // Terms and Privacy
                        Text(
                            text = stringResource(R.string.policy_msg),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = dimens.spaceSmall + dimens.spaceExtraSmall),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer Card
                Card(
                    elevation = CardDefaults.cardElevation(dimens.cardElevation + dimens.cardElevation),
                    shape = RoundedCornerShape(dimens.spaceMedium)
                ) {
                    FooterCard()
                }

                Spacer(modifier = Modifier.height(dimens.spaceMedium))
            }
        }

        // Snackbar for error messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(dimens.spaceMedium)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                actionColor = BrandPrimary,
                shape = RoundedCornerShape(dimens.cornerRadiusMedium),
            )
        }
    }
}