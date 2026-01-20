package com.anurag.eduai.ui.screens.setting.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anurag.eduai.ui.theme.Black
import com.anurag.eduai.ui.theme.LocalDimensions

@Composable
fun CenterPopupCard(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!visible) return
    val dimensions = LocalDimensions.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },   // tap outside to dismiss
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut()
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight()
                    .clickable(enabled = false) {}, // prevents dismiss when tapping card
                shape = RoundedCornerShape(dimensions.cornerRadiusRound),
                elevation = CardDefaults.cardElevation(dimensions.cardElevation)
            ) {
                content()
            }
        }
    }
}
