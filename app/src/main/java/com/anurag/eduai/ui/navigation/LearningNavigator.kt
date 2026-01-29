package com.anurag.eduai.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anurag.eduai.ui.screens.chapterscreen.ChapterScreen
import com.anurag.eduai.ui.screens.chatbotscreen.ChatbotScreen
import com.anurag.eduai.ui.screens.conceptdetailscreen.ConceptDetailScreen
import com.anurag.eduai.ui.screens.conceptscreen.ConceptScreen
import com.anurag.eduai.ui.screens.home.HomeScreen
import com.anurag.eduai.ui.screens.simulationscreen.SimulationListScreen
import com.anurag.eduai.ui.screens.simulationscreen.SimulationViewerScreen
import com.anurag.eduai.ui.screens.subjectscreen.SubjectScreen

object LearningRoutes {
    const val HOME = "home"
    const val SUBJECTS = "subjects"
    const val CHAPTERS = "chapters/{subjectId}"
    const val CONCEPTS = "concepts/{chapterId}"
    const val CONCEPT_DETAIL = "concept_detail/{conceptId}"
    const val CHATBOT = "chatbot"
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
                    navController.navigate("concept_detail/$conceptId")
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
                onChapterClick = { chapterId ->
                    navController.navigate("concepts/$chapterId")
                },
                onSimulationClick = { chapterId, classLevel, subjectName, chapterName ->
                    navController.navigate("simulation_list/$chapterId/$classLevel/$subjectName/$chapterName")
                },
                onGoHome = onGoHome,
                onGoSetting = onGoSetting
            )
        }

        composable(LearningRoutes.CONCEPTS) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: return@composable
            ConceptScreen(
                chapterId = chapterId,
                onBackClick = { navController.popBackStack() },
                onConceptClick = {
                    navController.navigate(LearningRoutes.CHATBOT)
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

        composable(LearningRoutes.CHATBOT) {
            ChatbotScreen()
        }

        // Simulation List Screen
        composable(LearningRoutes.SIMULATION_LIST) { backStackEntry ->
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
                onGoHome = onGoHome,
                onGoSetting = onGoSetting
            )
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
    }
}