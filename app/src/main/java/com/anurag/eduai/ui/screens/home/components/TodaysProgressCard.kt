package com.anurag.eduai.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.FontScaling
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.data.local.entities.ProgressEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.AccentGreen
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.White
@Composable
fun TodayProgressCard(
    progressConcepts: List<Pair<ProgressEntity?, ConceptEntity?>>,
    onLessonClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundPrimary),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 15.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {

            if (progressConcepts.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_progress_msg),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(0.dp, 6.dp),
                    textAlign = TextAlign.Center,
                )
                return@Column
            }

            Text(
                text = stringResource(R.string.today_progress),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Completed Concepts out of 4
                ProgressCard(
                    cardColors = AccentBlue.copy(alpha = 0.3f),
                    title = stringResource(R.string.concept),
                    score =
                        "${progressConcepts.count { it.first?.status == "COMPLETED" }}/4",
                    scoreColor = AccentBlue,
                    modifier = Modifier.weight(0.5f)
                )

                Spacer(modifier = Modifier.padding(20.dp))

                ProgressCard(
                    cardColors = AccentGreen.copy(alpha = 0.3f),
                    title = stringResource(R.string.simulation),
                    score =
                        "${progressConcepts.count { it.first?.itemType == "SIMULATION" }}/4",
                    scoreColor = AccentGreen,
                    modifier = Modifier.weight(0.5f)
                )
            }

            // 🔑 DO NOT FILTER COMPLETED — render all curated items
            progressConcepts.forEach { (progress, concept) ->

                val status = progress?.status ?: "NOT_STARTED"
                val isLocked = status == "NOT_STARTED"
                val isCompleted = status == "COMPLETED"


                when {
                    isLocked -> {
                        LockedLessons(
                            title = concept?.conceptName ?: "Unknown Concept"
                        )
                    }

                    else -> {
                        LessonStatusCard(
                            title = concept?.conceptName ?: "Unknown Concept",
                            subtitle = "Status: $status",
                            iconColor = if (isCompleted) AccentGreen else AccentBlue,
                            backgroundColor =
                                if (isCompleted)
                                    AccentGreen.copy(alpha = 0.1f)
                                else
                                    AccentBlue.copy(alpha = 0.1f),
                            icon = {
                                Icon(
                                    imageVector =
                                        if (isCompleted)
                                            Icons.Outlined.CheckCircle
                                        else
                                            Icons.AutoMirrored.Outlined.LibraryBooks,
                                    contentDescription = null,
                                    tint = White
                                )
                            },
                            status =
                                when (status) {
                                    "IN_PROGRESS" -> "pending"
                                    "COMPLETED" -> "completed"
                                    else -> "locked"
                                },
                            onClick = {
                                DebugLogger.debugLog(
                                    "TodayProgressCard",
                                    "Concept Clicked id ${concept?.conceptId}"
                                )
                                concept?.let { onLessonClick(it.conceptId) }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(5.dp))
            }
        }
    }
}
