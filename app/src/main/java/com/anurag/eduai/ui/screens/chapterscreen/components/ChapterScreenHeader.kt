package com.anurag.eduai.ui.screens.chapterscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextOnPrimary

/**
 * Header component specifically for ChapterScreen
 */
@Composable
fun ChapterScreenHeader(
    classLevel: Int,
    subjectName: String,
    onBackClick: () -> Unit = {},
    onGoHome: () -> Unit = {},
    onGoSetting: () -> Unit = {},
    onProgressClick: () -> Unit = {}
) {
    val dimens = LocalDimensions.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = HeaderGradientStart,
                shape = RoundedCornerShape(
                    bottomStart = dimens.cornerRadiusLarge,
                    bottomEnd = dimens.cornerRadiusLarge
                )
            )
            .padding(dimens.spaceMedium)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top row: Back button and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Class $classLevel - $subjectName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextOnPrimary
                    )
                    Text(
                        text = "NCERT Curriculum",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnPrimary.copy(alpha = 0.9f)
                    )
                }

                // Action buttons (Home & Settings)
                Row {
                    IconButton(
                        onClick = {
                            DebugLogger.debugLog("ChapterScreenHeader", "Home button clicked")
                            onGoHome()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = stringResource(R.string.home),
                            tint = TextOnPrimary,
                            modifier = Modifier.size(dimens.iconMedium)
                        )
                    }
                    IconButton(
                        onClick = {
                            DebugLogger.debugLog("ChapterScreenHeader", "Settings button clicked")
                            onGoSetting()
                        }
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimens.cornerRadiusMedium))
                        .clickable {
                            onProgressClick()
                        }
                        .border(
                            width = 1.dp,
                            color = TextOnPrimary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(dimens.cornerRadiusMedium)
                        ),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "My Progress",
                            tint = TextOnPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "My Progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextOnPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

