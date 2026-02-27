package com.anurag.eduai.ui.screens.conceptscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.anurag.eduai.R
import com.anurag.eduai.ui.models.ConceptStatus
import com.anurag.eduai.ui.theme.AccentGreen
import com.anurag.eduai.ui.theme.CompleteIconBackground
import com.anurag.eduai.ui.theme.InProgressIconBackground
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.NotStartedIconBackground
import com.anurag.eduai.ui.theme.NotStartedTextColor
import com.anurag.eduai.ui.theme.White

/**
 * Composable function to display a badge indicating the status of a concept.
 *if(status == ConceptStatus.COMPLETED) show check icon
 * if(status == ConceptStatus.IN_PROGRESS) show concept number with white text
 * if(status == ConceptStatus.NOT_STARTED) show concept number with gray text
 *
 * @param conceptOrder The order of the concept to display.
 * @param status The status of the concept (COMPLETED, IN_PROGRESS, NOT_STARTED).
 */
@Composable
fun ConceptStatusBadge(
    conceptOrder: String,
    status: ConceptStatus
) {
    val dimens = LocalDimensions.current
    Box(
        modifier = Modifier
            .size(dimens.boxSizeSmall)
            .background(
                color =  InProgressIconBackground,
                shape = RoundedCornerShape(dimens.cornerRadiusRound)
            ),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            ConceptStatus.COMPLETED -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.completed),
                    tint = AccentGreen,
                    modifier = Modifier.size(dimens.iconMedium)
                )
            }
            ConceptStatus.IN_PROGRESS -> {
                Text(
                    text = conceptOrder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
            }
            ConceptStatus.NOT_STARTED -> {
                Text(
                    text = conceptOrder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
            }
        }
    }
}