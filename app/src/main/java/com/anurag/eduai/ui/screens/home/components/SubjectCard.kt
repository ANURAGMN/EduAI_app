package com.anurag.eduai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.ui.theme.White

@Composable
fun SubjectCard(
    subject: String,
    onChangeClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF7F63FF),
                        Color(0xFF9B4DFF),
                        Color(0xFFB03BFE)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onChangeClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Current Subject",
                fontSize = 12.sp,
                color = White.copy(alpha = 0.7f)
            )
            Text(
                text = subject,
                fontSize = 20.sp,
                color = White
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Change", color = White.copy(alpha = 0.7f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = White.copy(alpha = 0.7f)
            )
        }
    }
}
