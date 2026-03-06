package com.anurag.eduai.ui.screens.revisionscreen.components

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

/**
 * Settings menu for Revision screen
 * Shows chapter selection dropdown with chapters from backend
 */
@Composable
fun RevisionSettings(
    expanded: Boolean,
    onDismiss: () -> Unit,
    currentChapter: String,
    availableChapters: List<String>,
    isLoadingChapters: Boolean,
    onChapterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current

    if (expanded) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(White)
                .border(width = 1.dp, color = BrandPrimary)
                .padding(dimens.screenPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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

                // Chapter Selection
                if (isLoadingChapters) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimens.spaceSmall),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(dimens.iconMedium),
                                color = BrandPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.height(dimens.spaceSmall))
                            Text(
                                text = stringResource(R.string.loading_chapters),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.select_chapter),
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(dimens.spaceSmall))

                    DropDownMenu(
                        label = stringResource(R.string.chapter),
                        options = availableChapters,
                        selectedValue = currentChapter,
                        onValueSelected = onChapterChange
                    )
                }
            }
        }
    }
}
