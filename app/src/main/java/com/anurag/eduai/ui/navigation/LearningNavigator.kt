package com.anurag.eduai.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anurag.eduai.ui.screens.chapterscreen.ChapterScreen
import com.anurag.eduai.ui.screens.chatbotscreen.ChatbotScreen
import com.anurag.eduai.ui.screens.conceptdetailscreen.ConceptDetailScreen
import com.anurag.eduai.ui.screens.conceptscreen.ConceptScreen
import com.anurag.eduai.ui.screens.conceptscreen.components.ConceptSimulationViewer
import com.anurag.eduai.ui.screens.home.HomeScreen
import com.anurag.eduai.ui.screens.simulation_agent.SimulationAgentScreen
import com.anurag.eduai.ui.screens.simulationscreen.SimulationViewerScreen
import com.anurag.eduai.ui.screens.subjectscreen.SubjectScreen

object LearningRoutes {
    const val HOME = "home"
    const val SUBJECTS = "subjects"
    const val CHAPTERS = "chapters/{subjectId}"
    const val CONCEPTS = "concepts/{chapterId}/{type}"
    const val CONCEPT_DETAIL = "concept_detail/{conceptId}"
    const val CHATBOT = "chatbot?conceptId={conceptId}"
    const val SIMULATION_LIST = "simulation_list/{chapterId}/{classLevel}/{subjectName}/{chapterName}"
    const val SIMULATION_VIEWER = "simulation_viewer/{simulationId}/{htmlFileName}/{simulationTitle}"
}

@Composable
fun LearningNavigator(
    navController: NavHostController = rememberNavController(),
    onBackToHome: () -> Unit,
    onGoHome: () -> Unit = {},
    onGoSetting: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = LearningRoutes.SUBJECTS,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(LearningRoutes.HOME) {
            HomeScreen(
                onLessonClick = { conceptId ->
                    navController.navigate("chatbot?conceptId=$conceptId")
                }
            )
        }

        composable(LearningRoutes.SUBJECTS) {
            SubjectScreen(
                onBackClick = onBackToHome,
                onSubjectClick = { subjectId ->
                    navController.navigate("chapters/${subjectId}")
                },
                onGoHome = onGoHome,
                onGoSetting = onGoSetting
            )
        }

        composable(LearningRoutes.CHAPTERS) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: return@composable
            ChapterScreen(
                subjectId = subjectId,
                onBackClick = { navController.popBackStack() },
                onStudyClick = { chapterId, type ->
                    navController.navigate("concepts/$chapterId/$type")
                },
                onSimulationClick = { chapterId, type ->
                    navController.navigate("concepts/$chapterId/$type")
                },
                onGoHome = onGoHome,
                onGoSetting = onGoSetting
            )
        }

        composable(LearningRoutes.CONCEPTS) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: return@composable
            val type = backStackEntry.arguments?.getString("type") ?: "STUDY"
            ConceptScreen(
                chapterId = chapterId,
                type = type,
                onBackClick = { navController.popBackStack() },
                onConceptClick = { conceptId ->
                    navController.navigate("chatbot?conceptId=$conceptId")
                },
                onSimulationAgentClick = {simulationId->
                    navController.navigate("simulation_agent/$simulationId")
                },
                onSimulationClick = { title, url ->
                    val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                    navController.navigate("concept_sim_view/$encodedUrl/$title")
                },
                onGoHome = onGoHome,
                onGoSetting = onGoSetting
            )
        }

        composable(LearningRoutes.CONCEPT_DETAIL) { backStackEntry ->
            val conceptId = backStackEntry.arguments?.getString("conceptId") ?: return@composable
            ConceptDetailScreen(
                conceptId = conceptId,
                onBackClick = { navController.popBackStack() },
                onGoHome = onGoHome,
                onGoSetting = onGoSetting
            )
        }

        composable(
            route = LearningRoutes.CHATBOT,
            arguments = listOf(navArgument("conceptId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val conceptId = backStackEntry.arguments?.getString("conceptId")
            ChatbotScreen(conceptId = conceptId)
        }

        // Simulation Viewer Screen
        composable(LearningRoutes.SIMULATION_VIEWER) { backStackEntry ->
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

        composable(route = "simulation_agent/{simulationId}") { backStackEntry ->
            val simulationId = backStackEntry.arguments?.getString("simulationId")!!
            SimulationAgentScreen(
                simulationId = simulationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}