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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anurag.eduai.ui.theme.Black
import com.anurag.eduai.ui.theme.LocalDimensions

@Composable
fun BottomPopupCard(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dimensions = LocalDimensions.current

    Box(modifier = Modifier.fillMaxSize()) {

        // Background scrim
        if (visible) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Black.copy(alpha = 0.4f))
                    .clickable { onDismiss() }
            )
        }

        // Animated card
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .height(400.dp),
                shape = RoundedCornerShape(topStart = dimensions.cornerRadiusMedium, topEnd = dimensions.cornerRadiusMedium),
                elevation = CardDefaults.cardElevation(dimensions.cardElevation)
            ) {
                content()
            }
        }
    }
}
