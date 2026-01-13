package com.anurag.eduai.ui.screens.conceptscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.anurag.eduai.R
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.components.ScreenWithHeader
import com.anurag.eduai.ui.screens.conceptscreen.components.ConceptCard
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.viewModel.ConceptViewModel
import com.anurag.eduai.utils.StreakManager

enum class ConceptStatus {
    COMPLETED,
    IN_PROGRESS,
    NOT_STARTED

}

data class Concept(
    val id: String,
    val name: String,
    val order: Int,
    val status: ConceptStatus = ConceptStatus.NOT_STARTED
)

/**
 * Composable screen to display concepts of a chapter.
 * chapterId: ID of the chapter whose concepts are to be displayed.
 * onBackClick: Lambda function to handle back navigation.
 * onConceptClick: Lambda function to handle concept item clicks.
 *
 * loads concepts from the database using ConceptViewModel and displays them in a list.
 */
@Composable
fun ConceptScreen(
    chapterId: String,
    onBackClick: () -> Unit = {},
    onConceptClick: (String) -> Unit = {},
    onGoHome:() -> Unit = {},
    onGoSetting:() -> Unit = {},
) {
    TrackScreenEvent(screenName = ScreenName.CONCEPT)

    val dimens = LocalDimensions.current
    val context = LocalContext.current
    val db = remember { EduAiDatabase.getInstance(context) }
    val conceptDao = db.conceptDao()
    val chapterDao = db.chapterDao()
    val progressDao = db.progressDao()
    val sharedPrefs = remember { SharedPreferenceUtils(context) }


    // streak update
    val streakManager = StreakManager(context)

    val viewModel = remember {
        ConceptViewModel(conceptDao, chapterDao, progressDao, sharedPrefs)
    }
    val state by viewModel.state.collectAsState()

    // updating streak on concept opening
    LaunchedEffect(Unit) {
        streakManager.onConceptOpened()
    }
    LaunchedEffect(chapterId) {
        viewModel.loadConcepts(chapterId)
    }

    ScreenWithHeader(
        title = state.chapter?.chapterName ?: "Concepts",
        onBackClick = onBackClick,
        onGoHome = onGoHome,
        onGoSetting = onGoSetting
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${state.error}", color = TextPrimary)
            }
        } else {
            Column(
                modifier = Modifier.padding(dimens.spaceMedium),
            ) {
                Text(
                    text = stringResource(R.string.concepts_to_master),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )

                Spacer(modifier = Modifier.height(dimens.spaceSmall))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.spaceSmall),
                ) {
                    items(state.concepts) { conceptProgress ->
                        ConceptCard(
                            concept = Concept(
                                id = conceptProgress.concept.conceptId,
                                order = conceptProgress.concept.orderIndex,
                                name = conceptProgress.concept.conceptName,
                                status = when (conceptProgress.status) {
                                    "COMPLETED" -> ConceptStatus.COMPLETED
                                    "IN_PROGRESS", "STARTED" -> ConceptStatus.IN_PROGRESS
                                    else -> ConceptStatus.NOT_STARTED
                                }
                            ),
                            onClick = {
                                DebugLogger.debugLog("ConceptScreen", "Concept clicked: ${conceptProgress.concept.conceptId}")
                                onConceptClick(conceptProgress.concept.conceptId)
                            }
                        )
                    }
                }
            }
        }
    }
}