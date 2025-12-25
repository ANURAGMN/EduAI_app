package com.anurag.eduai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anurag.eduai.ui.screens.login.LoginScreen
import com.anurag.eduai.ui.viewModel.UserViewModel
import com.anurag.eduai.ui.screens.login.UserDetailEntryScreen
@Composable
fun LoginNavigator(
    userViewModel: UserViewModel
) {
    val navController = rememberNavController()

    NavHost(
    navController = navController,
    startDestination = "login"
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
