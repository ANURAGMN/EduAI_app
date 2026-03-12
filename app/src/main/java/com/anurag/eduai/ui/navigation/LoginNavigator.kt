package com.anurag.eduai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.ui.screens.login.LoginScreen
import com.anurag.eduai.ui.screens.login.UserDetailEntryScreen
import com.anurag.eduai.ui.viewModel.UserViewModel
import com.anurag.eduai.ui.viewmodel_factory.UserViewModelFactory

@Composable
fun LoginNavigator() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferenceUtils = SharedPreferenceUtils(context)
    val isLoggedIn: Boolean = sharedPreferenceUtils.isLoggedIn()

    // Create ViewModel using factory
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(context = context)
    )

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