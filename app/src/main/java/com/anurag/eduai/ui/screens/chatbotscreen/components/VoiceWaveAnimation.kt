package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.HeaderGradientStart

/**
 * Simple smooth curved line animation like Google AI mode
 * - Gentle curve that animates up and down
 * - Responds to voice amplitude
 */
@Composable
fun VoiceWaveAnimation(
    amplitude: Float = 0f,
    isListening: Boolean = true,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(HeaderGradientStart, HeaderGradientEnd)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "line")

    // Noticeable up/down breathing animation
    val verticalOffset by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    // Smooth amplitude transitions
    val smoothAmplitude by animateFloatAsState(
        targetValue = if (isListening) amplitude else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "amplitude"
    )

    // Idle pulse
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)  // Increased from 8.dp for more visible animation
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Active amplitude or idle
        val activeAmplitude = if (smoothAmplitude > 0.05f) smoothAmplitude else idlePulse

        // More noticeable curve height based on voice
        val curveHeight = 10.dp.toPx() * activeAmplitude + verticalOffset  // Increased from 3.dp

        // Gradient for the line
        val lineBrush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                colors[0].copy(alpha = 0.6f),
                colors[1].copy(alpha = 0.9f),
                colors[0].copy(alpha = 0.6f),
                Color.Transparent
            )
        )

        // Create smooth curved line path
        val linePath = Path()
        linePath.moveTo(0f, centerY)

        // Simple gentle curve using quadratic bezier
        val controlY = centerY + curveHeight
        linePath.quadraticTo(
            width * 0.5f, controlY,  // Control point in the middle
            width, centerY            // End point
        )

        // Draw the smooth line
        drawPath(
            path = linePath,
            brush = lineBrush,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            ),
            alpha = 0.8f
        )

        // Add subtle glow under the line
        drawPath(
            path = linePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors[1].copy(alpha = 0.2f * activeAmplitude),
                    Color.Transparent
                ),
                startY = centerY - 4.dp.toPx(),
                endY = centerY + 8.dp.toPx()
            ),
            style = Stroke(
                width = 6.dp.toPx(),
                cap = StrokeCap.Round
            ),
            alpha = 0.4f
        )
    }
}

