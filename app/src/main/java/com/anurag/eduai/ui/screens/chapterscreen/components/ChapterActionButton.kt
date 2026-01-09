package com.anurag.eduai.ui.screens.chapterscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.anurag.eduai.ui.theme.ChipBackground
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary

/**
 * A customizable button used in the chapter screen for actions like starting a chapter or taking a quiz.
 *
 * @param label The text label displayed on the button.
 * @param icon The icon displayed alongside the label (can be an emoji or text).
 * @param modifier Optional [Modifier] for styling the button.
 * @param onClick Lambda function to be invoked when the button is clicked.
 */
@Composable
fun ChapterActionButton(
    label: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val dimens = LocalDimensions.current
    Box(
        modifier =  modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(dimens.cornerRadiusMedium)
            )
            .clickable(onClick = onClick)
            .border(dimens.dividerThickness, ChipBackground,
                RoundedCornerShape(dimens.cornerRadiusMedium))
            .padding(dimens.buttonPadding
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$icon $label",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
@Preview
@Composable
fun ChapterActionButtonPreview() {
    ChapterActionButton(
        label = "videos",
        icon = "▶️",
    )
}