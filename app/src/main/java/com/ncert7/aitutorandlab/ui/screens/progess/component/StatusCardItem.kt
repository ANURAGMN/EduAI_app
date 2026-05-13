package com.ncert7.aitutorandlab.ui.screens.progess.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import com.ncert7.aitutorandlab.ui.theme.CardBackground
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.TextPrimary
import com.ncert7.aitutorandlab.ui.theme.TextSecondary

/**
 * Status Card Item Component
 * Pure UI component - displays a single status card with icon, value, and title
 * NO hardcoded dimensions/colors
 */
@Composable
fun StatusCardItem(
    icon: Painter,
    value: Int,
    title: String,
    iconColor: Color,
    iconContentDescription: String,
    modifier: Modifier = Modifier
) {
    val dimes = LocalDimensions.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = dimes.cardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(dimes.cardPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = icon,
                contentDescription = iconContentDescription,
                tint = iconColor
            )

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
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
        }
    }
}