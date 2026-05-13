package com.ncert7.aitutorandlab.ui.screens.login.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ncert7.aitutorandlab.R
import com.ncert7.aitutorandlab.ui.theme.BackgroundPrimary
import com.ncert7.aitutorandlab.ui.theme.BrandPrimary
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions
import com.ncert7.aitutorandlab.ui.theme.TextPrimary

@Composable
fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val dimens = LocalDimensions.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundPrimary),
        elevation = CardDefaults.cardElevation(dimens.cardElevation + dimens.cardElevation),
        shape = RoundedCornerShape(dimens.cornerRadiusLarge)
    ) {
        Column(
            modifier = Modifier.padding(dimens.spaceMedium)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint = BrandPrimary
                )
                Spacer(modifier = Modifier.width(dimens.spaceSmall))
                Text(
                    text = stringResource(R.string.choose_your_language),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(dimens.spaceSmall + dimens.spaceExtraSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
            ) {
                LanguageChip(
                    text = stringResource(R.string.language_english),
                    isSelected = selectedLanguage == "en",
                    onClick = { onLanguageSelected("en") },
                    modifier = Modifier.weight(1f)
                )
                LanguageChip(
                    text = stringResource(R.string.language_kannada),
                    isSelected = selectedLanguage == "kn",
                    onClick = { onLanguageSelected("kn") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}