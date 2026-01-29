package com.anurag.eduai.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.anurag.eduai.ui.screens.chapterscreen.ChapterScreen
import com.anurag.eduai.ui.screens.chatbotscreen.ChatbotScreen
import com.anurag.eduai.ui.screens.conceptdetailscreen.ConceptDetailScreen
import com.anurag.eduai.ui.screens.conceptscreen.ConceptScreen
import com.anurag.eduai.ui.screens.home.HomeScreen
import com.anurag.eduai.ui.screens.progess.ProgressScreen
import com.anurag.eduai.ui.screens.setting.SettingScreen
import com.anurag.eduai.ui.screens.simulation_agent.SimulationAgentScreen
import com.anurag.eduai.ui.screens.simulationscreen.SimulationListScreen
import com.anurag.eduai.ui.screens.simulationscreen.SimulationViewerScreen
import com.anurag.eduai.ui.screens.subjectscreen.SubjectScreen
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary

@Composable
fun BottomNavBar() {
    val items = listOf(BottomNavItem.Home, BottomNavItem.Progress, BottomNavItem.Setting)
    val navController = rememberNavController()

    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route

    val showBottomBar =
        currentRoute == BottomNavItem.Home.route || currentRoute == BottomNavItem.Progress.route || currentRoute == BottomNavItem.Setting.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = BackgroundPrimary, tonalElevation = 8.dp) {
                    items.forEach { item ->
                        val selected = currentRoute == item.route

                        NavigationBarItem(
                            selected = selected,
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) TextPrimary else TextSecondary
                                )
                            },
                            label = {
                                if (selected) {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary
                                    )
                                }
                            },
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors =
                                NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent
                                )
                        )
                    }
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
                    onNavigateToLearning = { navController.navigate("learning") },
                    onNavigateToChapters = { subjectId ->
                        navController.navigate("chapters/$subjectId")
                    },
                    onLessonClick = { conceptId ->
                        navController.navigate("concept_detail/$conceptId")
                    },
                    onSimulationClick = { simulationId ->
                        navController.navigate("simulation_agent/$simulationId")
                    }
                )
            }
            composable(BottomNavItem.Progress.route) {
                ProgressScreen(
                    onGoHome = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            restoreState = true
                        }
                    },
                    onGoSetting = {
                        navController.navigate("setting") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavItem.Setting.route) { SettingScreen() }
            composable("learning") {
                LearningNavigator(
                    onBackToHome = { navController.popBackStack() },
                    onGoHome = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            restoreState = true
                        }
                    },
                    onGoSetting = {
                        navController.navigate("setting") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable("chapters/{subjectId}") { backStackEntry ->
                val subjectId =
                    backStackEntry.arguments?.getString("subjectId") ?: return@composable
                ChapterScreen(
                    subjectId = subjectId,
                    onBackClick = { navController.popBackStack() },
                    onChapterClick = { chapterId ->
                        navController.navigate("concepts/$chapterId")
                    },
                    onSimulationClick = { chapterId, classLevel, subjectName, chapterName ->
                        navController.navigate("simulation_list/$chapterId/$classLevel/$subjectName/$chapterName")
                    },
                    onGoHome = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            restoreState = true
                        }
                    },
                    onGoSetting = {
                        navController.navigate("setting") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            restoreState = true
                        }
                    }
                )
            }
            composable("concepts/{chapterId}") { backStackEntry ->
                val chapterId =
                    backStackEntry.arguments?.getString("chapterId") ?: return@composable
                ConceptScreen(
                    chapterId = chapterId,
                    onBackClick = { navController.popBackStack() },
                    onConceptClick = { navController.navigate("chatbot") },
                    onGoHome = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            restoreState = true
                        }
                    },
                    onGoSetting = {
                        navController.navigate("setting") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            restoreState = true
                        }
                    }
                )
            }
            composable("concept_detail/{conceptId}") { backStackEntry ->
                val conceptId =
                    backStackEntry.arguments?.getString("conceptId") ?: return@composable
                ConceptDetailScreen(
                    conceptId = conceptId,
                    onBackClick = { navController.popBackStack() },
                    onGoHome = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onGoSetting = {
                        navController.navigate("setting") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("subjects") {
                SubjectScreen(
                    onBackClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onSubjectClick = { subject ->
                        navController.navigate("chapters/${subject}")
                    },
                    onGoHome = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onGoSetting = {
                        navController.navigate("setting") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("chatbot") { ChatbotScreen() }
            composable(route = "simulation_agent/{simulationId}") { backStackEntry ->
                val simulationId = backStackEntry.arguments?.getString("simulationId")!!
                SimulationAgentScreen(
                    simulationId = simulationId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // New Simulation Routes
            composable("simulation_list/{chapterId}/{classLevel}/{subjectName}/{chapterName}") { backStackEntry ->
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: return@composable
                val classLevel = backStackEntry.arguments?.getString("classLevel")?.toIntOrNull() ?: 7
                val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
                val chapterName = backStackEntry.arguments?.getString("chapterName") ?: ""

                SimulationListScreen(
                    chapterId = chapterId,
                    classLevel = classLevel,
                    subjectName = subjectName,
                    chapterName = chapterName,
                    onBackClick = { navController.popBackStack() },
                    onSimulationClick = { simulationId, htmlFileName, simulationTitle ->
                        navController.navigate("simulation_viewer/$simulationId/$htmlFileName/$simulationTitle")
                    },
                    onGoHome = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    onGoSetting = {
                        navController.navigate("setting") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                )
            }

            composable("simulation_viewer/{simulationId}/{htmlFileName}/{simulationTitle}") { backStackEntry ->
                val simulationId = backStackEntry.arguments?.getString("simulationId") ?: return@composable
                val htmlFileName = backStackEntry.arguments?.getString("htmlFileName") ?: return@composable
                val simulationTitle = backStackEntry.arguments?.getString("simulationTitle") ?: ""

                SimulationViewerScreen(
                    simulationId = simulationId,
                    htmlFileName = htmlFileName,
                    simulationTitle = simulationTitle,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}