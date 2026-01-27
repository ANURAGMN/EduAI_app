package com.anurag.eduai.ui.screens.simlation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.LocalDimensions

data class SimChatMessage(
    val text: String,
    val isFromTeacher: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun SimChatBubble(
    message: SimChatMessage,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimens.messageHorizontalPadding,
                vertical = dimens.spaceExtraSmall
            ),
        horizontalArrangement = if (message.isFromTeacher) {
            Arrangement.Start
        } else {
            Arrangement.End
        }
    ) {
        Card(
            modifier = Modifier.widthIn(max = dimens.userMessageMaxWidth),
            shape = RoundedCornerShape(
                topStart = dimens.cornerRadiusMedium,
                topEnd = dimens.cornerRadiusMedium,
                bottomStart = if (message.isFromTeacher) {
                    dimens.spaceExtraSmall
                } else {
                    dimens.cornerRadiusMedium
                },
                bottomEnd = if (message.isFromTeacher) {
                    dimens.cornerRadiusMedium
                } else {
                    dimens.spaceExtraSmall
                }
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromTeacher) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = dimens.cardElevation
            )
        ) {
            Column(modifier = Modifier.padding(dimens.messagePadding)) {
                Text(
                    text = if (message.isFromTeacher) {
                        stringResource(R.string.sim_teacher_label)
                    } else {
                        stringResource(R.string.sim_you_label)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (message.isFromTeacher) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    modifier = Modifier.padding(bottom = dimens.spaceExtraSmall)
                )

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isFromTeacher) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }
    }
}