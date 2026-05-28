package com.ncert7.aitutorandlab.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.ui.screens.login.LoginScreen
import com.ncert7.aitutorandlab.ui.screens.login.UserDetailEntryScreen
import com.ncert7.aitutorandlab.ui.screens.login.viewmodel.UserViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginNavigator() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferenceUtils = SharedPreferenceUtils(context)
    val logoutTriggered = remember { mutableStateOf(false) }
    // Read login status - recompose when logout happens
    var isLoggedIn by remember {
        mutableStateOf(sharedPreferenceUtils.isLoggedIn())
    }

    // Create ViewModel using factory
    val userViewModel: UserViewModel = hiltViewModel()

    // When logout is triggered, update the login status
    if (logoutTriggered.value) {
        isLoggedIn = sharedPreferenceUtils.isLoggedIn()
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn && !logoutTriggered.value) "main" else "login"
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
                    logoutTriggered.value = true
                    // Reset user ViewModel state
                    userViewModel.resetLoginState()
                    userViewModel.resetUserSaveState()

                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
}