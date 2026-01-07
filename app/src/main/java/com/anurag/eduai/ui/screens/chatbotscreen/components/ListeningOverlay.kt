package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.IconPrimary
import com.anurag.eduai.ui.theme.TextPrimary

/**
 * A composable overlay that
 * indicates the app is listening for voice input.

 */
@Composable
fun ListeningOverlay(
    text: String,
    onStopClick: () -> Unit
) {

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "drift"
    )

    val glow = Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to HeaderGradientStart.copy(alpha = 0.4f* pulse),
            0.5f to HeaderGradientEnd.copy(alpha = 0.3f),
            1.0f to Color.Transparent
        ),
        center = Offset(x = drift, y = 50f),
        radius = 600f * pulse
    )



    // Auto-scroll to bottom when new text arrives
    LaunchedEffect(text) {
        if (text.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }



    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(glow),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // blurry gradient line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Spans most of the width
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(glow)
                    .blur(radiusX = 10.dp, radiusY = 10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp)) // Space between the line and the content

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if(text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.listening),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }

                IconButton(
                    onClick = onStopClick,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Stop listening",
                        tint = IconPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))

            val maxLines = 4
            val lineHeight = 20.sp
            val verticalPadding = 16.dp
            val totalHeight = with(density) {
                (lineHeight.toPx() * maxLines + verticalPadding.toPx() * 2).toDp()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .heightIn(max = totalHeight) // Approximate height for 4 lines
                    .padding(horizontal = 12.dp)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .fillMaxWidth()
                ) {
                    if (text.isNotEmpty()) {
                        Text(
                            text = text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }


        }
    }
}

@Preview
@Composable
fun ListeningOverlayPreview() {
    ListeningOverlay(
        text = "This is a sample transcribed text that the app is listening to. It can be quite long to demonstrate scrolling behavior.",
        onStopClick = {}
    )
}