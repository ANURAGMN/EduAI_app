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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anurag.eduai.ui.screens.chapterscreen.ChapterScreen
import com.anurag.eduai.ui.screens.chatbotscreen.ChatbotScreen
import com.anurag.eduai.ui.screens.conceptscreen.ConceptScreen
import com.anurag.eduai.ui.screens.conceptscreen.components.ConceptSimulationViewer
import com.anurag.eduai.ui.screens.home.HomeScreen
import com.anurag.eduai.ui.screens.progess.ProgressScreen
import com.anurag.eduai.ui.screens.setting.SettingScreen
import com.anurag.eduai.ui.screens.simulation_agent.SimulationAgentScreen
import com.anurag.eduai.ui.screens.simulationscreen.SimulationViewerScreen
import com.anurag.eduai.ui.screens.subjectscreen.SubjectScreen
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary

@Composable
fun BottomNavBar(onLogout: () -> Unit = {}) {
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
                        navController.navigate("chatbot?conceptId=$conceptId")
                    },
                    onSimulationClick = { simulationId ->
                        navController.navigate("simulation_agent/$simulationId")
                    },
                    onSimulationUrlClick = { title, url ->
                        // Encoded the URL to prevent navigation crashes due to '/'
                        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                        navController.navigate("concept_sim_view/$encodedUrl/$title")
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
            composable(BottomNavItem.Setting.route) {
                SettingScreen(
                    navController = navController,
                    onNavigateBack = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            restoreState = true
                        }
                    },
                    onLogout = onLogout
                )
            }
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
                            launchSingleTop =  true
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
                    onStudyClick = { chapterId, type ->
                        navController.navigate("concepts/$chapterId/$type")
                    },
                    onSimulationClick = { chapterId, type ->
                        navController.navigate("concepts/$chapterId/$type")
                    },
                    onRevisionClick = { chapterName ->
                        com.anurag.eduai.debug.DebugLogger.debugLog("BottomNavBar", "Navigating to revision with chapter: $chapterName")
                        val encodedChapter = java.net.URLEncoder.encode(chapterName, "UTF-8")
                        com.anurag.eduai.debug.DebugLogger.debugLog("BottomNavBar", "Encoded chapter name: $encodedChapter")
                        navController.navigate("revision/$encodedChapter")
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
            composable("concepts/{chapterId}/{type}") { backStackEntry ->
                val chapterId =
                    backStackEntry.arguments?.getString("chapterId") ?: return@composable
                val type =
                    backStackEntry.arguments?.getString("type") ?: return@composable
                ConceptScreen(
                    chapterId = chapterId,
                    type = type,
                    onBackClick = { navController.popBackStack() },
                    onConceptClick = { conceptId ->
                        navController.navigate("chatbot?conceptId=$conceptId")
                    },
                    onGoHome = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            restoreState = true
                        }
                    },
                    onSimulationAgentClick = { simulationId ->
                        navController.navigate("simulation_agent/$simulationId")
                    },
                    onSimulationClick = { title, url ->
                        //  Encode the URL to prevent navigation crashes due to '/'
                        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                        navController.navigate("concept_sim_view/$encodedUrl/$title")
                    },
                    onGoSetting = {
                        navController.navigate("setting") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            restoreState = true
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
            composable(
                route = "chatbot?conceptId={conceptId}",
                arguments = listOf(navArgument("conceptId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val conceptId = backStackEntry.arguments?.getString("conceptId")
                ChatbotScreen(conceptId = conceptId)
            }

            composable("revision/{chapterName}") { backStackEntry ->
                val encodedChapterName = backStackEntry.arguments?.getString("chapterName") ?: return@composable
                val chapterName = java.net.URLDecoder.decode(encodedChapterName, "UTF-8")
                com.anurag.eduai.debug.DebugLogger.debugLog("BottomNavBar", "Revision route - Encoded: $encodedChapterName, Decoded: $chapterName")
                com.anurag.eduai.ui.screens.revisionscreen.RevisionScreen(
                    chapterName = chapterName,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(route = "simulation_agent/{simulationId}") { backStackEntry ->
                val simulationId = backStackEntry.arguments?.getString("simulationId")!!
                SimulationAgentScreen(
                    simulationId = simulationId,
                    onNavigateBack = { navController.popBackStack() }
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

            composable(
                route = "concept_sim_view/{url}/{title}",
                arguments = listOf(
                    navArgument("url") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val url = backStackEntry.arguments?.getString("url") ?: ""
                val title = backStackEntry.arguments?.getString("title") ?: "Simulation"

                ConceptSimulationViewer(
                    simulationUrl = url,
                    simulationTitle = title,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}