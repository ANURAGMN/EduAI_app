package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.HeaderGradientStart

/**
 * A composable overlay that
 * indicates the app is listening for voice input.

 */
@Composable
fun ListeningOverlay(
    text: String,
    onStopClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(vertical = 16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // blurry gradient line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Spans most of the width
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        brush = Brush.horizontalGradient(

                            colors = listOf(
                                Color.Transparent,
                                HeaderGradientStart,
                                HeaderGradientEnd,
                                Color.Transparent
                            )
                        )
                    )
                    .blur(radiusX = 10.dp, radiusY = 10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp)) // Space between the line and the content

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Listening...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                TextButton(onClick = onStopClick) {
                    Text("Stop", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
