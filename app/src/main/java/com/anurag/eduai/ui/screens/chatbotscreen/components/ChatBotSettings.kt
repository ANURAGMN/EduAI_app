package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.ui.components.DropDownMenu
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.IconPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.White

data class ChatBotSettingsState(
    val selectedAvatar: String = "disable",
    val selectedSpeed: String = "0.75x",
    val selectedStudentLevel: String = "medium",
    val voiceOptions: List<String> = emptyList(),
    val displayedVoiceName: String = "",
    val availableConcepts: List<String> = emptyList(),
    val selectedConcept: String? = null,
    val isLoadingConcepts: Boolean = false
)

@Composable
fun ChatBotSettings(
    expanded: Boolean,
    onDismiss: () -> Unit,
    state: ChatBotSettingsState,
    onAvatarChange: (String) -> Unit,
    onVoiceChange: (String) -> Unit,
    onConceptChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    onSpeedChange: (String) -> Unit
) {
    val dimens = LocalDimensions.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(White)
            .border(dimens.inputBorderWidth, BrandPrimary)
    ) {
        Column(
            modifier = Modifier
                .padding(dimens.cardPadding)
                .widthIn(max = dimens.dropdownMaxWidth)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.spaceSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.settings),
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                )

                IconButton(onClick = onDismiss, modifier = Modifier.size(dimens.iconLarge)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close_settings),
                        tint = IconPrimary
                    )
                }
            }

            Spacer(Modifier.height(dimens.spaceMedium))

            // Avatar
            Text(
                text = stringResource(R.string.select_avatar),
                color = TextPrimary,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(dimens.spaceSmall))
            DropDownMenu(
                label = stringResource(R.string.avatar),
                options = listOf(
                    stringResource(R.string.disable),
                    stringResource(R.string.boy),
                    stringResource(R.string.girl)
                ),
                selectedValue = when (state.selectedAvatar) {
                    "boy" -> stringResource(R.string.boy)
                    "girl" -> stringResource(R.string.girl)
                    else -> stringResource(R.string.disable)
                },
                onValueSelected = { displayName ->
                    val code = when (displayName) {
                        "boy" -> "boy"
                        "girl" -> "girl"
                        else -> "disable"
                    }
                    onAvatarChange(code)
                }
            )

            Spacer(Modifier.height(dimens.spaceMedium))

            // Voice
            Text(
                text = stringResource(R.string.select_voice),
                color = TextPrimary,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(dimens.spaceSmall))
            DropDownMenu(
                label = stringResource(R.string.voice),
                options = state.voiceOptions,
                selectedValue = state.displayedVoiceName,
                onValueSelected = onVoiceChange
            )

            Spacer(Modifier.height(dimens.spaceMedium))

            // Concept
            if (state.isLoadingConcepts) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = BrandPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.height(dimens.spaceSmall))
                        Text(
                            text = stringResource(R.string.loading_topics),
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.select_concepts),
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(dimens.spaceSmall))
                DropDownMenu(
                    label = stringResource(R.string.select_concepts),
                    options = state.availableConcepts,
                    selectedValue = state.selectedConcept ?: stringResource(R.string.tap_to_choose_topic),
                    onValueSelected = onConceptChange
                )
            }

            Spacer(Modifier.height(dimens.spaceMedium))

            // Student Level
            Text(
                text = stringResource(R.string.select_student_level),
                color = TextPrimary,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(dimens.spaceSmall))
            DropDownMenu(
                label = stringResource(R.string.student_level),
                options = listOf(
                    stringResource(R.string.level_low),
                    stringResource(R.string.level_medium),
                    stringResource(R.string.level_advanced)
                ),
                selectedValue = when (state.selectedStudentLevel) {
                    "low" -> stringResource(R.string.level_low)
                    "medium" -> stringResource(R.string.level_medium)
                    "advanced" -> stringResource(R.string.level_advanced)
                    else -> stringResource(R.string.level_medium)
                },
                onValueSelected = { displayName ->
                    val code = when (displayName) {
                        "low" -> "low"
                        "medium" -> "medium"
                        "advanced" -> "advanced"
                        else -> "medium"
                    }
                    onLevelChange(code)
                }
            )

            Spacer(Modifier.height(dimens.spaceMedium))

            // Speed
            Text(
                text = stringResource(R.string.select_speed),
                color = TextPrimary,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(dimens.spaceSmall))
            DropDownMenu(
                label = stringResource(R.string.speed),
                options = listOf("0.75x", "1.0x", "1.25x", "1.5x"),
                selectedValue = state.selectedSpeed,
                onValueSelected = onSpeedChange
            )
        }
    }
}