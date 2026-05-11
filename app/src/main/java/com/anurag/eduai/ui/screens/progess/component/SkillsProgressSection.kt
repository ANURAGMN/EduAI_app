package com.anurag.eduai.ui.screens.progess.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.anurag.eduai.R
import com.anurag.eduai.data.local.dao.ChapterProgressSummary
import com.anurag.eduai.data.local.entities.SubjectEntity
import com.anurag.eduai.ui.theme.*
import com.anurag.eduai.ui.screens.progess.viewmodel.ProgressColorType
import com.anurag.eduai.utils.getLocalizedName
import com.anurag.eduai.utils.isKannada

/**
 * Skills Progress Section Component
 * Pure UI component - displays subject dropdown and chapter progress
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsProgressSection(
    subjects: List<SubjectEntity>,
    selectedSubject: SubjectEntity?,
    chaptersToShow: List<ChapterProgressSummary>,
    showAllChapters: Boolean,
    hasMoreChapters: Boolean,
    hiddenChaptersCount: Int,
    onSubjectSelected: (SubjectEntity) -> Unit,
    onToggleShowAll: () -> Unit,
    getProgressColor: (Float) -> ProgressColorType,
    capitalizeSubjectName: (String) -> String
) {
    val dimes = LocalDimensions.current
    val isKannadaMode = isKannada()
    var expanded by remember { mutableStateOf(false) }

    Column {
        // Header with dropdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimes.spaceMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.subject_progress),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.width(dimes.spaceLarge))

            // Subject Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedSubject?.let { capitalizeSubjectName(it.getLocalizedName()) }
                        ?: stringResource(R.string.select_subject),
                    onValueChange = {},
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .height(dimes.buttonHeightLarge),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DropdownBackgroundColor,
                        unfocusedContainerColor = DropdownBackgroundColor,
                        focusedTextColor = DropdownTextColor,
                        unfocusedTextColor = DropdownTextColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = TextSecondary,
                        unfocusedLabelColor = DropdownHintColor
                    ),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(DropdownBackgroundColor)
                ) {
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = subject.getLocalizedName(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DropdownTextColor
                                )
                            },
                            onClick = {
                                onSubjectSelected(subject)
                                expanded = false
                            }
                        )
                        // Divider between items except last
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = dimes.spaceSmall),
                            thickness = dimes.dividerThickness,
                            color = DropdownDividerColor
                        )
                    }
                }
            }
        }

        // Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = dimes.cardElevation),
            shape = RoundedCornerShape(dimes.cornerRadiusMedium)
        ) {
            Column(modifier = Modifier.padding(dimes.cardPadding)) {
                if (chaptersToShow.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_progress_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorHint,
                        modifier = Modifier.padding(vertical = dimes.spaceLarge)
                    )
                } else {
                    // Chapter progress bars
                    Column(verticalArrangement = Arrangement.spacedBy(dimes.spaceMedium)) {
                        chaptersToShow.forEach { chapter ->
                            // Use the localized chapter name when app is in Kannada
                            val displayName = if (isKannadaMode && chapter.chapterNameKannada.isNotBlank()) {
                                chapter.chapterNameKannada
                            } else {
                                chapter.chapterName
                            }
                            ChapterProgressBar(
                                chapterName = displayName,
                                progress = chapter.completionPercentage.toInt(),
                                completedConcepts = chapter.completedConcepts,
                                totalConcepts = chapter.totalConcepts,
                                colorType = getProgressColor(chapter.completionPercentage)
                            )
                        }
                    }

                    // Show More/Less Button
                    if (hasMoreChapters) {
                        Spacer(modifier = Modifier.height(dimes.spaceSmall))

                        TextButton(
                            onClick = onToggleShowAll,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (showAllChapters) {
                                    stringResource(R.string.show_less)
                                } else {
                                    stringResource(R.string.show_more_count, hiddenChaptersCount)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(dimes.spaceMedium))
                            Icon(
                                imageVector = if (showAllChapters) {
                                    Icons.Default.KeyboardArrowUp
                                } else {
                                    Icons.Default.KeyboardArrowDown
                                },
                                contentDescription = if (showAllChapters) {
                                    stringResource(R.string.show_less_icon)
                                } else {
                                    stringResource(R.string.show_more_icon)
                                },
                                modifier = Modifier.size(dimes.iconSmall)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chapter Progress Bar Component
 * Pure UI component - displays individual chapter progress
 * NO hardcoded values
 */
@Composable
private fun ChapterProgressBar(
    chapterName: String,
    progress: Int,
    completedConcepts: Int,
    totalConcepts: Int,
    colorType: ProgressColorType
) {
    val dimes = LocalDimensions.current

    // Map color type to actual color
    val color = when (colorType) {
        ProgressColorType.COMPLETED -> StatusGreen
        ProgressColorType.HIGH_PROGRESS -> StatusGreen
        ProgressColorType.MEDIUM_PROGRESS -> StatusOrange
        ProgressColorType.STARTED -> StatusBlue
        ProgressColorType.NOT_STARTED -> StatusGray
    }

    Column(verticalArrangement = Arrangement.spacedBy(dimes.spaceSmall)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chapterName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
//            Text(
//                text = stringResource(
//                    R.string.completed_concepts_format,
//                    completedConcepts,
//                    totalConcepts
//                ),
//                style = MaterialTheme.typography.labelSmall,
//                color = ColorHint
//            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimes.spaceSmall)
        ) {
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier
                    .weight(1f)
                    .height(dimes.spaceSmall),
                color = color,
                trackColor = ProgressTrackColor
            )
            Text(
                text = stringResource(R.string.percentage_format, progress),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}