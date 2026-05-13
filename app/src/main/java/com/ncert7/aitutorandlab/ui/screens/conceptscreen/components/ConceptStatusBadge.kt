package com.ncert7.aitutorandlab.ui.screens.conceptscreen.components

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
import androidx.compose.ui.res.stringResource
import com.ncert7.aitutorandlab.R
import com.ncert7.aitutorandlab.domain.progress.model.ProgressStatus
import com.ncert7.aitutorandlab.ui.theme.AccentGreen
import com.ncert7.aitutorandlab.ui.theme.InProgressIconBackground
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.NotStartedTextColor
import com.ncert7.aitutorandlab.ui.theme.White

/**
 * Composable function to display a badge indicating the status of a concept.
 * if(status == ProgressStatus.COMPLETED) show check icon
 * if(status == ProgressStatus.IN_PROGRESS) show concept number with white text
 * if(status == ProgressStatus.NOT_STARTED) show concept number with gray text
 *
 * @param conceptOrder The order of the concept to display.
 * @param status The status of the concept (COMPLETED, IN_PROGRESS, NOT_STARTED).
 */
@Composable
fun ConceptStatusBadge(
    conceptOrder: String,
    status: ProgressStatus
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
            ProgressStatus.COMPLETED -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.completed),
                    tint = AccentGreen,
                    modifier = Modifier.size(dimens.iconMedium)
                )
            }
            ProgressStatus.IN_PROGRESS -> {
                Text(
                    text = conceptOrder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
            }
            ProgressStatus.NOT_STARTED -> {
                Text(
                    text = conceptOrder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
            }
            ProgressStatus.LOCKED -> {
                Text(
                    text = conceptOrder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NotStartedTextColor
                )
            }
        }
    }
}