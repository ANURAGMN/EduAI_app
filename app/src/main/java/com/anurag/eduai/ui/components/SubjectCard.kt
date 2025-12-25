package com.anurag.eduai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.ui.screens.home.Subject
import com.anurag.eduai.ui.theme.TextOnAccent
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
@Composable
fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = subject.color,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = subject.color,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject.name.first().toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = subject.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subject.conceptCount,
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(36.dp),
            colors = ButtonDefaults.buttonColors(containerColor = subject.color),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Start Learning",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextOnAccent
            )
        }
    }
}
@Preview
@Composable
fun SubjectCardPreview() {
    SubjectCard(
        subject = Subject(
            id = "1",
            name = "Mathematics",
            color = Color(0xFF3B82F6),
            conceptCount = "12 Concepts"
        )
    )
}