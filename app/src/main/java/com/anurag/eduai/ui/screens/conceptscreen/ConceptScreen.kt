package com.anurag.eduai.ui.screens.conceptscreen

import androidx.compose.foundation.background
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.anurag.eduai.R
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.conceptscreen.components.ConceptScreenHeader
import com.anurag.eduai.ui.screens.conceptscreen.components.ConceptCard
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.viewModel.ConceptViewModel
import com.anurag.eduai.utils.StreakManager

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
    viewModel: ConceptViewModel = hiltViewModel()
) {
    TrackScreenEvent(screenName = ScreenName.CONCEPT)

    val dimens = LocalDimensions.current
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // streak update
    val streakManager = remember { StreakManager(context) }

    // updating streak on concept opening
    LaunchedEffect(Unit) {
        streakManager.onConceptOpened()
    }
    LaunchedEffect(chapterId, state.type) {
        viewModel.loadConcepts(chapterId, state.type)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        ConceptScreenHeader(
            classId = state.classLevel,
            subjectName = state.subjectName,
            chapterName = state.chapterName,
            progress = state.progressUiModel,
            onBackClick = onBackClick,
            onGoHome = onGoHome,
            onGoSetting = onGoSetting
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            DebugLogger.errorLog("ConceptScreen", "Error loading concepts: ${state.error}")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.unable_to_load_concepts), color = TextPrimary)
            }
        } else {
            Column(
                modifier = Modifier.padding(dimens.spaceMedium),
            ) {
                Text(
                    text = if (state.type == stringResource(R.string.simulation))
                        stringResource(R.string.simulations_to_explore)
                    else
                        stringResource(R.string.lessons_to_master),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )

                Spacer(modifier = Modifier.height(dimens.spaceSmall))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.spaceSmall),
                ) {
                    items(state.concepts, key = { it.id }) { conceptUiModel ->
                        ConceptCard(
                            concept = conceptUiModel,
                            onClick = {
                                DebugLogger.debugLog("ConceptScreen", "Concept clicked: ${conceptUiModel.id}")
                                onConceptClick(conceptUiModel.id)
                            }
                        )
                    }
                }
            }
        }
    }
}