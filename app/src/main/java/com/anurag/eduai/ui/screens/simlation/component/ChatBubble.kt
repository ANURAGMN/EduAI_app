package com.anurag.eduai.ui.screens.simlation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Simple chat message data class for UI
 * Separate from API models for clean separation of concerns
 */
data class SimChatMessage(
    val text: String,
    val isFromTeacher: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/** Chat bubble composable for displaying messages */
@Composable
fun SimChatBubble(message: SimChatMessage, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isFromTeacher) {
            Arrangement.Start
        } else {
            Arrangement.End
        }
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromTeacher) 4.dp else 16.dp,
                bottomEnd = if (message.isFromTeacher) 16.dp else 4.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromTeacher) {
                    Color(0xFFE3F2FD) // Light blue for teacher
                } else {
                    Color(0xFFF1F8E9) // Light green for student
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Sender label
                Text(
                    text = if (message.isFromTeacher) "Teacher 👨‍🏫" else "You 👤",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (message.isFromTeacher) {
                        Color(0xFF1976D2)
                    } else {
                        Color(0xFF558B2F)
                    },
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Message text
                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    color = Color(0xFF212121),
                    lineHeight = 20.sp
                )
            }
        }
    }
}