package com.anurag.eduai.ui.screens.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.anurag.eduai.ui.theme.BackgroundSecondary
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.White

@Composable
fun LanguageChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current

    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BrandPrimary else BackgroundSecondary
        ),
        shape = RoundedCornerShape(dimens.cornerRadiusMedium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimens.spaceSmall + dimens.spaceExtraSmall),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                color = if (isSelected) White else TextPrimary
            )
        }
    }
}