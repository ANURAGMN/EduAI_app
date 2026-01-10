package com.anurag.eduai.ui.screens.progess.component

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.data.local.dao.ChapterProgressSummary
import com.anurag.eduai.data.local.entities.SubjectEntity
import com.anurag.eduai.ui.theme.BackgroundSecondary
import com.anurag.eduai.ui.theme.Black
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.theme.White
import java.util.Locale
import java.util.Locale.getDefault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsProgressSection(
    subjects: List<SubjectEntity>,
    selectedSubject: SubjectEntity?,
    chapterProgress: List<ChapterProgressSummary>,
    onSubjectSelected: (SubjectEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        // Header with dropdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subject Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Spacer(modifier = Modifier.width(25.dp))
            // Subject Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedSubject?.subjectName?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            getDefault()
                        ) else it.toString()
                    } ?: "Select Subject",
                    onValueChange = {},
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BackgroundSecondary,
                        unfocusedContainerColor = BackgroundSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = TextSecondary,
                        unfocusedLabelColor = ColorHint
                    ),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(BackgroundSecondary)
                ) {
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = subject.subjectName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                            },
                            onClick = {
                                onSubjectSelected(subject)
                                expanded = false
                            }
                        )
                        // adds a divider between each item except the last one
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            thickness = 1.dp,
                            color = ColorHint
                        )
                    }
                }
            }
        }

        // Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                if (chapterProgress.isEmpty()) {
                    Text(
                        text = "No progress data available",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    chapterProgress.forEach { chapter ->
                        ChapterProgressBar(
                            chapterName = chapter.chapterName,
                            progress = chapter.completionPercentage.toInt(),
                            completedConcepts = chapter.completedConcepts,
                            totalConcepts = chapter.totalConcepts,
                            color = getProgressColor(chapter.completionPercentage)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterProgressBar(
    chapterName: String,
    progress: Int,
    completedConcepts: Int,
    totalConcepts: Int,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chapterName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Black,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$completedConcepts/$totalConcepts",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp),
                color = color,
                trackColor = Color(0xFFE0E0E0)
            )
            Text(
                text = "$progress%",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

private fun getProgressColor(percentage: Float): Color {
    return when {
        percentage >= 80 -> Color(0xFF4CAF50) // Green
        percentage >= 50 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFE91E63) // Pink/Red
    }
}