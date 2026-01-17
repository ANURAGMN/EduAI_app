package com.anurag.eduai.ui.screens.conceptscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.anurag.eduai.R
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextOnPrimary

/**
 * Header component specifically for ConceptScreen
 * Customizable for concept listing screen with progress tracking
 * Layout: "Class X - Subject" on first line, "Chapter Name" on second line
 */
@Composable
fun ConceptScreenHeader(
    classId: String,
    subjectName: String,
    chapterName: String,
    onBackClick: () -> Unit = {},
    onGoHome: () -> Unit = {},
    onGoSetting: () -> Unit = {},
    completed: Int,
    total: Int,
) {
    val dimens = LocalDimensions.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color= HeaderGradientStart,
                shape = RoundedCornerShape(
                    bottomStart = dimens.cornerRadiusLarge,
                    bottomEnd = dimens.cornerRadiusLarge
                )
            )
            .padding(dimens.spaceSmall)
    ) {
        Column(
        modifier = Modifier.fillMaxWidth()
        ){
        // Top row: Back button, Title/Subtitle, Action buttons
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
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                // First line: Class X - Subject
                Text(
                    text = "$classId - $subjectName",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = TextOnPrimary.copy(alpha = 0.9f)
                )
                // Second line: Chapter Name (bold and larger)
                Text(
                    text = chapterName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary
                )
            }

            // Action buttons (Home & Settings)
            Row {
                IconButton(
                    onClick = {
                        DebugLogger.debugLog("ConceptScreenHeader", "Home button clicked")
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
                        DebugLogger.debugLog("ConceptScreenHeader", "Settings button clicked")
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

        // Progress card below header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spaceSmall)
        ) {
            ChapterProgressCardOnHeader(
                completed = completed,
                total = total
            )
        }

    }
    }
}

