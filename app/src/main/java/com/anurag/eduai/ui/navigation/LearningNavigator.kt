package com.anurag.eduai.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anurag.eduai.ui.screens.chapterscreen.ChapterScreen
import com.anurag.eduai.ui.screens.conceptdetailscreen.ConceptDetailScreen
import com.anurag.eduai.ui.screens.conceptscreen.ConceptScreen
import com.anurag.eduai.ui.screens.home.HomeScreen
import com.anurag.eduai.ui.screens.subjectscreen.SubjectScreen

object LearningRoutes {
    const val HOME = "home"
    const val SUBJECTS = "subjects"
    const val CHAPTERS = "chapters/{subjectId}"
    const val CONCEPTS = "concepts/{chapterId}"
    const val CONCEPT_DETAIL = "concept_detail/{conceptId}"
}

@Composable
fun LearningNavigator(
    navController: NavHostController = rememberNavController(),
    onBackToHome: () -> Unit
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
                onSubjectClick = { subject ->
                    navController.navigate("chapters/${subject.id}")
                }
            )
        }

        composable(LearningRoutes.CHAPTERS) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: return@composable
            ChapterScreen(
                subjectId = subjectId,
                onBackClick = { navController.popBackStack() },
                onChapterClick = { chapterId ->
                    navController.navigate("concepts/$chapterId")
                }
            )
        }

        composable(LearningRoutes.CONCEPTS) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: return@composable
            ConceptScreen(
                chapterId = chapterId,
                onBackClick = { navController.popBackStack() },
                onConceptClick = { conceptId ->
                    navController.navigate("concept_detail/$conceptId")
                }
            )
        }

        composable(LearningRoutes.CONCEPT_DETAIL) { backStackEntry ->
            val conceptId = backStackEntry.arguments?.getString("conceptId") ?: return@composable
            ConceptDetailScreen(
                conceptId = conceptId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}