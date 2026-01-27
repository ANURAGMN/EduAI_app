package com.anurag.eduai.ui.screens.simlation.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.LocalDimensions
import kotlin.math.abs

@Composable
fun SimVoiceWaveAnimation(
    modifier: Modifier = Modifier,
    amplitude: Float = 0f,
    isListening: Boolean = true,
    colors: List<Color> = listOf(HeaderGradientStart, HeaderGradientEnd),
    segmentCount: Int = 150
) {
    val dimens = LocalDimensions.current

    val animatedAmplitude by animateFloatAsState(
        targetValue = if (isListening) amplitude else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "amplitude_animation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "animations")
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle"
    )

    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_flow"
    )

    val activeAmplitude = if (animatedAmplitude > 0.05f) animatedAmplitude else idlePulse

    val parabolicOffsets = remember(segmentCount) {
        List(segmentCount) { index ->
            val normalizedPosition = index.toFloat() / (segmentCount - 1)
            val distanceFromCenter = normalizedPosition - 0.5f
            -(1f - (4f * distanceFromCenter * distanceFromCenter))
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(dimens.containerMinHeight / 2.5f)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2f
        val maxVerticalOffset = canvasHeight * 0.35f

        val curvePoints = List(segmentCount) { index ->
            val x = (index.toFloat() / (segmentCount - 1)) * canvasWidth
            val offset = parabolicOffsets[index] * activeAmplitude * maxVerticalOffset
            Offset(x, centerY + offset)
        }

        val gradientWidth = canvasWidth * 2.5f
        val gradientStartX = -gradientWidth + (gradientOffset * gradientWidth * 2.5f)

        val glowLineGradient = Brush.horizontalGradient(
            colors = listOf(colors[0], colors[1], colors[0], colors[1]),
            startX = gradientStartX,
            endX = gradientStartX + gradientWidth
        )

        val glowLayers = listOf(
            Triple((2f + (activeAmplitude * 1f)), 1f, glowLineGradient)
        )

        glowLayers.forEach { (strokeWidth, alpha, brush) ->
            for (i in 0 until segmentCount - 1) {
                val normalizedPosition = i.toFloat() / (segmentCount - 1)
                val distanceFromCenter = abs(normalizedPosition - 0.5f) * 2f
                val edgeFade = 1f - (distanceFromCenter * distanceFromCenter * 0.7f)

                drawLine(
                    brush = brush,
                    start = curvePoints[i],
                    end = curvePoints[i + 1],
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                    alpha = alpha * edgeFade
                )
            }
        }

        val glowHeight = canvasHeight * 0.4f
        for (i in 0 until segmentCount - 1) {
            val startPoint = curvePoints[i]
            val endPoint = curvePoints[i + 1]

            val segmentX = (startPoint.x + endPoint.x) / 2f
            val relativePosition = ((segmentX - gradientStartX) / gradientWidth).coerceIn(0f, 1f)
            val colorIndex = (relativePosition * 4f).toInt() % 2
            val currentColor = colors[colorIndex]

            val segmentGradient = Brush.verticalGradient(
                colors = listOf(
                    currentColor.copy(alpha = 0.6f),
                    currentColor.copy(alpha = 0.35f),
                    currentColor.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                startY = minOf(startPoint.y, endPoint.y),
                endY = minOf(startPoint.y, endPoint.y) + glowHeight
            )

            val path = Path().apply {
                moveTo(startPoint.x, startPoint.y)
                lineTo(endPoint.x, endPoint.y)
                lineTo(endPoint.x, endPoint.y + glowHeight)
                lineTo(startPoint.x, startPoint.y + glowHeight)
                close()
            }

            drawPath(
                path = path,
                brush = segmentGradient,
                alpha = 0.7f + (activeAmplitude * 0.3f)
            )
        }
    }
}