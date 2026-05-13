package com.ncert7.aitutorandlab.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.ui.screens.login.LoginScreen
import com.ncert7.aitutorandlab.ui.screens.login.UserDetailEntryScreen
import com.ncert7.aitutorandlab.ui.screens.login.viewmodel.UserViewModel
import androidx.compose.ui.platform.LocalContext
import com.ncert7.aitutorandlab.debug.DebugLogger

@Composable
fun LoginNavigator() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferenceUtils = SharedPreferenceUtils(context)
    var isLoggedIn: Boolean = sharedPreferenceUtils.isLoggedIn()

    // Create ViewModel using Hilt
    val userViewModel: UserViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "main" else "login"
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }
        composable("userDetailEntry") {
            UserDetailEntryScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }
        composable("main") {
            BottomNavBar(
                onLogout = {
                    DebugLogger.debugLog("LoginNavigator", "User logged out, navigating to login screen")
                    // Navigate back to login and clear the back stack completely
                    navController.navigate("login") {
                        // Clear entire back stack
                        popUpTo(0) { inclusive = true }
                    }
                    // Reset user ViewModel state
                    userViewModel.resetUserState()
                }
            )
        }
    }
}