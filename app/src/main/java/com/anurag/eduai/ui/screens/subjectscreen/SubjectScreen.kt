package com.anurag.eduai.ui.screens.subjectscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.R
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.SubjectRepository
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.subjectscreen.components.SubjectCard
import com.anurag.eduai.ui.screens.subjectscreen.components.SubjectScreenHeader
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.viewModel.SubjectViewModel
import com.anurag.eduai.ui.viewmodel_factory.SubjectViewModelFactory

@Composable
fun SubjectScreen(
    onBackClick: () -> Unit = {},
    onSubjectClick: (String) -> Unit = {},
    onGoHome: () -> Unit = {},
    onGoSetting: () -> Unit = {},
) {
    TrackScreenEvent(screenName = ScreenName.SUBJECT)
    val dimens = LocalDimensions.current

    val context = LocalContext.current
    val db = remember { EduAiDatabase.getInstance(context) }
    val sharedPref = remember { SharedPreferenceUtils(context) }

    val repository = remember { SubjectRepository(db.subjectDao()) }
    val factory = remember { SubjectViewModelFactory(repository, sharedPref) }
    val viewModel: SubjectViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        SubjectScreenHeader(
            title = stringResource(R.string.class_title, state.classLevel),
            subtitle = stringResource(R.string.ncert_curriculum),
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
            DebugLogger.errorLog("SubjectScreen", "Error loading subjects: ${state.error}")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.unable_to_load_subjects))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(dimens.spaceMedium),
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmall),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
            ) {
                items(state.subjects, key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        onClick = {
                            viewModel.onSubjectSelected(subject.id)
                            onSubjectClick(subject.id)
                        }
                    )
                }
            }
        }
    }
}
