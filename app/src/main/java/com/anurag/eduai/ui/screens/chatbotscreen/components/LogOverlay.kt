package com.anurag.eduai.ui.screens.chatbotscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.data.remote.SessionMetadata
import com.anurag.eduai.ui.theme.TextPrimary

/**
 * Simple debug overlay showing current node transition
 */
@Composable
fun LogOverlay(
    modifier: Modifier = Modifier,
    metadata: SessionMetadata?,
    conceptMapStatus: String? = null,
) {
    val transitions = metadata?.nodeTransitions ?: emptyList()
    if (transitions.isEmpty()) return

    val latest = transitions.lastOrNull() ?: return
    val fromNode = latest["from_node"]?.toString() ?: latest["fromNode"]?.toString() ?: "?"
    val toNode = latest["to_node"]?.toString() ?: latest["toNode"]?.toString() ?: "?"

    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Column {
            Text("From node: $fromNode", color = TextPrimary, fontSize = 10.sp)
            Text("To node: $toNode", color =TextPrimary, fontSize = 10.sp)
            Text("imager url: ${metadata?.imageUrl ?: "Null"}", color =TextPrimary, fontSize = 10.sp)
            // Show concept map status if available
            conceptMapStatus?.let {
                Text("Concept Map: $it", color = Color.Red, fontSize = 10.sp)
            }
        }
    }
}

