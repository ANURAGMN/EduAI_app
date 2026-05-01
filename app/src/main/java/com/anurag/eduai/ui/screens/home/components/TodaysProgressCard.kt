package com.anurag.eduai.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.data.local.entities.ProgressEntity
import com.anurag.eduai.data.model.ProgressStatus
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.AccentGreen
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.utils.getLocalizedName

@Composable
fun TodayProgressCard(
    progressConcepts: List<Pair<ProgressEntity?, ConceptEntity?>>,
    todayCompletedConcept: Int,
    todayCompletedSimulation: Int,
    onLessonClick: (String) -> Unit,
    onShowAllChapters: () -> Unit = {}
) {

    val dimes = LocalDimensions.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundPrimary),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = dimes.cardElevation)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimes.screenPadding),
            verticalArrangement = Arrangement.Center
        ) {
            if (progressConcepts.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_progress_msg),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontStyle = FontStyle.Italic,
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(dimes.containerMinHeight)
                            .padding(0.dp, dimes.spaceExtraSmall),
                    textAlign = TextAlign.Center,
                )
                return@Column
            }

            Text(
                text = stringResource(R.string.today_progress),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(0.dp, dimes.spaceExtraSmall)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(0.dp, dimes.screenPadding),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Completed Concepts today
                ProgressCard(
                    cardColors = AccentBlue.copy(alpha = 0.3f),
                    title = stringResource(R.string.concept),
                    score = todayCompletedConcept,
                    scoreColor = AccentBlue,
                    modifier = Modifier.weight(0.5f)
                )

                Spacer(modifier = Modifier.padding(dimes.spaceSmall))

                ProgressCard(
                    cardColors = AccentGreen.copy(alpha = 0.3f),
                    title = stringResource(R.string.simulation),
                    score = todayCompletedSimulation,
                    scoreColor = AccentGreen,
                    modifier = Modifier.weight(0.5f)
                )
            }

            /** Button to view all chapter */
            OutlinedButton(
                onClick = onShowAllChapters,
                shape = RoundedCornerShape(dimes.cornerRadiusMedium),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(dimes.dividerThickness, ColorHint),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\uD83D\uDCD6", // 📖 icon
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.padding(dimes.spaceExtraSmall))
                    Text(
                        text = stringResource(R.string.view_ll_chapter),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.padding(dimes.spaceSmall))

            progressConcepts.forEach { (progress, concept) ->
                val progressStatus = ProgressStatus.fromString(progress?.status ?: "NOT_STARTED")
                val isCompleted = progressStatus == ProgressStatus.COMPLETED
                val isInProgress = progressStatus == ProgressStatus.IN_PROGRESS

                val progressPercentage = progress?.progressPercentage ?: 0
                LessonStatusCard(
                    title = concept?.getLocalizedName()
                        ?: stringResource(R.string.unknown_concept),
                    subtitle = stringResource(R.string.status_label, progressStatus.value),
                    iconColor = when (progressStatus) {
                        ProgressStatus.COMPLETED -> AccentGreen
                        ProgressStatus.IN_PROGRESS -> AccentBlue
                        else -> AccentBlue
                    },
                    backgroundColor = when (progressStatus) {
                        ProgressStatus.COMPLETED -> AccentGreen.copy(alpha = 0.1f)
                        ProgressStatus.IN_PROGRESS -> AccentBlue.copy(alpha = 0.1f)
                        else -> AccentBlue.copy(alpha = 0.1f)
                    },
                    icon = {
                        Icon(
                            imageVector = when (progressStatus) {
                                ProgressStatus.COMPLETED -> Icons.Outlined.CheckCircle
                                else -> Icons.AutoMirrored.Outlined.LibraryBooks
                            },
                            contentDescription = null,
                            tint = White
                        )
                    },
                    progressStatus = progressStatus,
                    progressPercentage = progressPercentage,
                    onClick = {
                        DebugLogger.debugLog("TodayProgressCard", "Concept Clicked id ${concept?.conceptId}")
                        concept?.let { onLessonClick(it.conceptId) }
                    })
                Spacer(modifier = Modifier.padding(dimes.spaceSmall))
            }
        }
    }
}