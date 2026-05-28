package com.ncert7.aitutorandlab.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ncert7.aitutorandlab.R
import com.ncert7.aitutorandlab.service.analytics.ScreenName
import com.ncert7.aitutorandlab.service.analytics.TrackScreenEvent
import com.ncert7.aitutorandlab.ui.screens.login.components.FooterCard
import com.ncert7.aitutorandlab.ui.screens.login.components.GoogleLoginButton
import com.ncert7.aitutorandlab.ui.screens.login.components.LanguageSelector
import com.ncert7.aitutorandlab.ui.screens.login.viewmodel.InAppUpdateViewModel
import com.ncert7.aitutorandlab.ui.theme.BackgroundPrimary
import com.ncert7.aitutorandlab.ui.theme.BackgroundSecondary
import com.ncert7.aitutorandlab.ui.theme.BrandPrimary
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.TextPrimary
import com.ncert7.aitutorandlab.ui.theme.TextSecondary
import com.ncert7.aitutorandlab.ui.screens.login.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val dimens = LocalDimensions.current
    val snackbarHostState = remember { SnackbarHostState() }
    val updateViewModel: InAppUpdateViewModel = hiltViewModel()
    val selectedLanguage by userViewModel.selectedLanguage.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.LOGIN)

    // Check for in-app updates when LoginScreen is displayed
    // Google's native in-app update UI will appear automatically if update is available
    LaunchedEffect(Unit) {
        val activity = navController.context as? androidx.activity.ComponentActivity
        activity?.let {
            updateViewModel.checkForUpdate(it)
        }
    }


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
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .background(BackgroundSecondary)
                    .padding(dimens.screenPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.screenPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .padding(
                                top = dimens.spaceMedium,
                                bottom = dimens.spaceMedium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = stringResource(R.string.app_logo_desc),
                            modifier = Modifier
                                .height(dimens.containerMinHeight - dimens.buttonHeight)
                                .clip(RoundedCornerShape(dimens.cornerRadiusMedium))
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                        elevation = CardDefaults.cardElevation(dimens.cardElevation),
                        colors = CardDefaults.cardColors(containerColor = BackgroundPrimary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimens.spaceLarge),
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
                }

                // Footer Card with fixed spacing
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.screenPadding)
                ) {
                    Spacer(modifier = Modifier.height(dimens.spaceMedium))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(dimens.cardElevation),
                        shape = RoundedCornerShape(dimens.cornerRadiusMedium),
                        colors = CardDefaults.cardColors(containerColor = BackgroundPrimary)
                    ) {
                        FooterCard()
                    }

                    Spacer(modifier = Modifier.height(dimens.spaceMedium))
                }
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