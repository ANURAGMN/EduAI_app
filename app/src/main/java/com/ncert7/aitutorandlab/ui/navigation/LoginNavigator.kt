package com.ncert7.aitutorandlab.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val sharedPreferenceUtils = remember { SharedPreferenceUtils(context) }
    val logoutTriggered = remember { mutableStateOf(false) }
    var sessionChecked by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf("login") }

    val userViewModel: UserViewModel = hiltViewModel()

    LaunchedEffect(logoutTriggered.value) {
        sessionChecked = false
        startDestination = if (logoutTriggered.value || !userViewModel.hasValidLocalSession()) {
            "login"
        } else {
            "main"
        }
        sessionChecked = true
    }

    if (!sessionChecked) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
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