package com.ncert7.aitutorandlab.ui.screens.chapterscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.ncert7.aitutorandlab.R
import com.ncert7.aitutorandlab.ui.theme.HeaderGradientStart
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.TextOnPrimary

/**
 * Header component specifically for ChapterScreen
 */
@Composable
fun ChapterScreenHeader(
    modifier: Modifier = Modifier,
    classLevel: Int,
    subjectName: String,
    onBackClick: () -> Unit = {},
    onGoHome: () -> Unit = {},
    onGoSetting: () -> Unit = {},
    onProgressClick: () -> Unit = {},
) {
    val dimens = LocalDimensions.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = HeaderGradientStart,
                shape = RoundedCornerShape(
                    bottomStart = dimens.cornerRadiusLarge,
                    bottomEnd = dimens.cornerRadiusLarge
                )
            )
            .padding(dimens.spaceSmall)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top row: Back button and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Back button
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = TextOnPrimary,
                        modifier = Modifier.size(dimens.iconMedium)
                    )
                }

                // Title and subtitle in the center
                Column(
                    modifier = Modifier.weight(0.7f)
                ) {
                    Text(
                        text = stringResource(R.string.class_and_subject, classLevel, subjectName),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextOnPrimary
                    )
                    Text(
                        text = stringResource(R.string.ncert),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnPrimary.copy(alpha = 0.9f)
                    )
                }

                // Action buttons (Home & Settings)
                Row {
                    IconButton(
                        onClick = onGoHome
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = stringResource(R.string.home),
                            tint = TextOnPrimary,
                            modifier = Modifier.size(dimens.iconMedium)
                        )
                    }
                    IconButton(
                        onClick = onGoSetting
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = TextOnPrimary,
                            modifier = Modifier.size(dimens.iconMedium)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.spaceSmall))

            // My Progress chip
            AssistChip(
                onClick = onProgressClick,
                label = {
                    Text(
                        text = stringResource(R.string.my_progress),
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = stringResource(R.string.my_progress),
                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = HeaderGradientStart,
                    labelColor = TextOnPrimary,
                    leadingIconContentColor = TextOnPrimary
                ),
                border = BorderStroke(
                    width = dimens.inputBorderWidth,
                    color = TextOnPrimary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(dimens.cornerRadiusMedium),
                modifier = Modifier.padding(start = dimens.spaceSmall)
            )
        }
    }
}

@Preview
@Composable
fun ChapterScreenHeaderPreview() {
    ChapterScreenHeader(
        classLevel = 10,
        subjectName = "Mathematics"
    )
}
