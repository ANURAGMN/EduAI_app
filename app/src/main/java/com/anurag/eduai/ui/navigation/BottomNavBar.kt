package com.anurag.eduai.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.anurag.eduai.ui.screens.home.HomeScreen
import com.anurag.eduai.ui.screens.progess.ProgressScreen
import com.anurag.eduai.ui.screens.setting.SettingScreen

@Composable
fun BottomNavBar() {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Progress,
        BottomNavItem.Setting
    )
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onNavigateToLearning = {
                        navController.navigate("learning")
                    }
                )
            }
            composable(BottomNavItem.Progress.route) { ProgressScreen() }
            composable(BottomNavItem.Setting.route) { SettingScreen() }
            composable("learning") {
                LearningNavigator(
                    onBackToHome = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}