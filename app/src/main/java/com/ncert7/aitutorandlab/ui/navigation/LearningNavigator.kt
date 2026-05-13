package com.ncert7.aitutorandlab.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.ui.screens.chapterscreen.ChapterScreen
import com.ncert7.aitutorandlab.ui.screens.chatbotscreen.ChatbotScreen
import com.ncert7.aitutorandlab.ui.screens.conceptscreen.ConceptScreen
import com.ncert7.aitutorandlab.ui.screens.conceptscreen.components.ConceptSimulationViewer
import com.ncert7.aitutorandlab.ui.screens.home.HomeScreen
import com.ncert7.aitutorandlab.ui.screens.mathagentscreen.MathAgentScreen
import com.ncert7.aitutorandlab.ui.screens.revisionscreen.RevisionScreen
import com.ncert7.aitutorandlab.ui.screens.simulation_agent.SimulationAgentScreen
import com.ncert7.aitutorandlab.ui.screens.subjectscreen.SubjectScreen

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
    onGoSetting: () -> Unit = {},
    onGoProgress: () -> Unit = {}
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
                onBackClick = onGoHome,
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
                onMathAgentClick = { chapterId, problemId ->
                    val problemIdParam = problemId ?: "null"
                    navController.navigate("math_agent?chapterId=$chapterId&problemId=$problemIdParam")
                },
                onRevisionClick = { chapterName ->
                    DebugLogger.debugLog("LearningNavigator", "Navigating to revision with chapter: $chapterName")
                    val encodedChapter = java.net.URLEncoder.encode(chapterName, "UTF-8")
                    DebugLogger.debugLog("LearningNavigator", "Encoded chapter name: $encodedChapter")
                    navController.navigate("revision/$encodedChapter")
                },
                onGoHome = onGoHome,
                onGoSetting = onGoSetting,
                onProgressClick = onGoProgress
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
                onSimulationClick = { title, url, conceptId ->
                    val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                    val encodedConceptId = java.net.URLEncoder.encode(conceptId, "UTF-8")
                    navController.navigate("concept_sim_view/$encodedUrl/$title/$encodedConceptId")
                },
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

        composable(
            route = "math_agent?chapterId={chapterId}&problemId={problemId}",
            arguments = listOf(
                navArgument("chapterId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("problemId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId")
            val problemId = backStackEntry.arguments?.getString("problemId")
            MathAgentScreen(
                problemId = problemId
            )
        }

        composable(
            route = "concept_sim_view/{url}/{title}/{conceptId}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("conceptId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: "Simulation"
            val conceptId = backStackEntry.arguments?.getString("conceptId") ?: ""

            ConceptSimulationViewer(
                simulationUrl = url,
                simulationTitle = title,
                conceptId = conceptId,
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

        composable("revision/{chapterName}") { backStackEntry ->
            val encodedChapterName = backStackEntry.arguments?.getString("chapterName") ?: return@composable
            val chapterName = java.net.URLDecoder.decode(encodedChapterName, "UTF-8")
            DebugLogger.debugLog("LearningNavigator", "Revision route - Encoded: $encodedChapterName, Decoded: $chapterName")
           RevisionScreen(
                chapterName = chapterName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

