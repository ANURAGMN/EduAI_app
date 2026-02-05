package com.anurag.eduai.ui.screens.chatbotscreen.components.inputSection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anurag.eduai.ui.theme.ChipBackground
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary

@Composable
fun AutoSuggestionChips(
    suggestions: List<String>,
    visible: Boolean,
    onSuggestionClick: (String) -> Unit
) {
    val dimens = LocalDimensions.current
    AnimatedVisibility(
        visible = visible && suggestions.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start=dimens.spaceMedium, end=dimens.spaceMedium),
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
        ) {
            suggestions.forEach { suggestion ->
                SuggestionChip(
                    onClick = { onSuggestionClick(suggestion) },
                    label = { Text(suggestion) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = ChipBackground,
                        labelColor = TextPrimary
                    )
                )
            }
        }
    }
}