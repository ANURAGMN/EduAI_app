package com.anurag.eduai.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.BackgroundSecondary
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedLanguage by remember { mutableStateOf("English") }

    // Login/SignUp state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSecondary)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language Selector Card
            LanguageSelector(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { selectedLanguage = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Login/SignUp Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Welcome Header
                    Text(
                        text = "👋",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // Google Sign in
                    GoogleLoginButton()

                    Spacer(modifier = Modifier.height(16.dp))

                    // OR Divider
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Divider(modifier = Modifier.weight(1f), color = ColorHint)
//                        Text(
//                            text = "OR",
//                            modifier = Modifier.padding(horizontal = 16.dp),
//                            color = TextSecondary,
//                            fontSize = 12.sp
//                        )
//                        Divider(modifier = Modifier.weight(1f), color = ColorHint)
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Login/SignUp Tabs
//                    LoginSignUpTabs(
//                        selectedIndex = selectedTab,
//                        onSelected = { selectedTab = it }
//                    )
//
//                    Spacer(modifier = Modifier.height(20.dp))
//
//                    // Form Fields based on selected tab
//                    if (selectedTab == 1) {
//                        // Sign Up Form
//                        SignUpForm(
//                            fullName = fullName,
//                            onFullNameChange = { fullName = it },
//                            email = email,
//                            onEmailChange = { email = it },
//                            password = password,
//                            onPasswordChange = { password = it },
//                            confirmPassword = confirmPassword,
//                            onConfirmPasswordChange = { confirmPassword = it },
//                            passwordVisible = passwordVisible,
//                            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
//                            confirmPasswordVisible = confirmPasswordVisible,
//                            onConfirmPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
//                        )
//                    } else {
//                        // Login Form
//                        LoginForm(
//                            email = email,
//                            onEmailChange = { email = it },
//                            password = password,
//                            onPasswordChange = { password = it },
//                            passwordVisible = passwordVisible,
//                            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible }
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(20.dp))
//
//                    // Action Button
//                    Button(
//                        onClick = { /* Handle login/signup */ },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(50.dp),
//                        shape = RoundedCornerShape(12.dp),
//                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
//                    ) {
//                        Text(
//                            text = if (selectedTab == 0) stringResource(R.string.sign_in)
//                                else stringResource(R.string.create_account),
//                            fontSize = 16.sp
//                        )
//                    }
//
//                    // Forgot Password (only for login)
//                    if (selectedTab == 0) {
//                        TextButton(
//                            onClick = { /* Handle forgot password */ },
//                            modifier = Modifier.padding(top = 8.dp)
//                        ) {
//                            Text(
//                                text = stringResource(R.string.forget_password),
//                                color = BrandPrimary,
//                                fontSize = 14.sp
//                            )
//                        }
//                    }

                    // Terms and Privacy (only for signup)
                    if (selectedTab == 1) {
                        Text(
                            text = stringResource(R.string.policy_msg),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 12.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Card
            Card(
                elevation = CardDefaults.cardElevation(10.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                FooterCard()
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
