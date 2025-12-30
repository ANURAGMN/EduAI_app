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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.AccentGreen
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.White

@Composable
fun TodayProgressCard() {

    val concept1: ConceptEntity
    val concept2: ConceptEntity

    val lockedConcept = listOf(
        "Irrational Number",
        "Properties of Real Number"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundPrimary),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 15.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
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
                ProgressCard(
                    cardColors = AccentBlue.copy(alpha = 0.3f),
                    title = stringResource(R.string.concept),
                    score = "1/3",
                    scoreColor = AccentBlue,
                    modifier = Modifier.weight(0.5f)
                )
                Spacer(modifier = Modifier.padding(20.dp))
                ProgressCard(
                    cardColors = AccentGreen.copy(alpha = 0.3f),
                    title = stringResource(R.string.simulation),
                    score = "0/3",
                    scoreColor = AccentGreen,
                    modifier = Modifier.weight(0.5f)
                )
            }

            LessonStatusCard(
                title = "What are Real Numbers?",
                subtitle = "Score: 95%",
                iconColor = AccentBlue,
                backgroundColor = AccentBlue.copy(alpha = 0.1f),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = White
                    )
                },
                status = "completed",
            )
            Spacer(modifier = Modifier.padding(5.dp))
            LessonStatusCard(
                title = "Ration Number",
                subtitle = "Score: 95%",
                iconColor = AccentBlue,
                backgroundColor = AccentBlue.copy(alpha = 0.1f),
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.LibraryBooks,
                        contentDescription = null,
                        tint = White
                    )
                },
                status = "pending",
            )

            Spacer(modifier = Modifier.padding(5.dp))

            lockedConcept.forEach { concept ->
                LockedLessons(title = concept)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}