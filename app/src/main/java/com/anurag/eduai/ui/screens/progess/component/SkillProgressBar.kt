package com.anurag.eduai.ui.screens.progess.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.anurag.eduai.ui.theme.LightGray
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary

@Composable
fun SkillProgressBar(skillName: String, percentage: Int, color: Color) {
    val dimes = LocalDimensions.current
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = skillName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(dimes.spaceSmall))

        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(dimes.spaceSmall)
                    .background(LightGray, RoundedCornerShape(dimes.cornerRadiusSmall))
        ) {
            Box(
                modifier =
                    Modifier.fillMaxWidth(percentage / 100f)
                        .height(dimes.spaceSmall)
                        .background(color, RoundedCornerShape(dimes.cornerRadiusSmall))
            )
        }
    }
}
