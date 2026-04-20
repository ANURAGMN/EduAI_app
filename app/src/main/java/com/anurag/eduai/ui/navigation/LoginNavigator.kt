package com.anurag.eduai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.ui.screens.login.LoginScreen
import com.anurag.eduai.ui.screens.login.UserDetailEntryScreen
import com.anurag.eduai.ui.viewModel.UserViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginNavigator() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferenceUtils = SharedPreferenceUtils(context)
    val isLoggedIn: Boolean = sharedPreferenceUtils.isLoggedIn()

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
            BottomNavBar()
        }
    }
}