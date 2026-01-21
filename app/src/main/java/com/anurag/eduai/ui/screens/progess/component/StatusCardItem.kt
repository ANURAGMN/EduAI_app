package com.anurag.eduai.ui.screens.progess.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.theme.White

@Composable
fun StatusCardItem(
    icon: String,
    value: Int,
    title: String,
    modifier: Modifier = Modifier
) {
    val dimes = LocalDimensions.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(dimes.statusCardHeight),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = dimes.cardElevation)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(dimes.cardPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.padding(dimes.spaceExtraSmall))
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.padding(dimes.spaceExtraSmall))
            Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
            )
        }
    }
}
