package com.anurag.eduai.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary

@Composable
fun FooterCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundPrimary)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FeatureItem(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = stringResource(R.string.complete_ncert),
            subtitle = stringResource(R.string.language_number),
            modifier = Modifier.weight(1f)
        )

        FeatureItem(
            icon = Icons.Default.AutoAwesome,
            title = stringResource(R.string.ai_title),
            subtitle = stringResource(R.string.smart_learning),
            modifier = Modifier.weight(1f)
        )

        FeatureItem(
            icon = Icons.Default.People,
            title = stringResource(R.string.student_count),
            subtitle = stringResource(R.string.trusted_globally),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandPrimary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            color = TextPrimary,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}