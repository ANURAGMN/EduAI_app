package com.anurag.eduai.ui.screens.setting.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.AccentGreen
import com.anurag.eduai.ui.theme.AiMessageBackground
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.BackgroundSecondary
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.CardBackground
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextOnAccent
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary

@Composable
fun ContactSupportCard(
    emailAddress: String,
    whatsappNumber: String,
    websiteUrl: String,
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    emailButtonText: String,
    onClose:() -> Unit
) {
    val dimens = LocalDimensions.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundSecondary)
            .padding(dimens.screenPadding, dimens.spaceExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.spaceLarge)
    ) {
        // Email Section Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimens.cornerRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = dimens.cardElevation)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.cardPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.spaceMedium)
            ) {
                // Email Icon
                Box(
                    modifier = Modifier
                        .size(dimens.boxSizeSmall)
                        .background(
                            color = AiMessageBackground,
                            shape = RoundedCornerShape(dimens.cornerRadiusMedium)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        modifier = Modifier.size(dimens.iconLarge),
                        tint = BrandPrimary
                    )
                }

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                // Subtitle
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                // Open Email Button
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$emailAddress")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.buttonHeightLarge),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue
                    ),
                    shape = RoundedCornerShape(dimens.cornerRadiusMedium)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(dimens.iconMedium)
                    )
                    Spacer(modifier = Modifier.width(dimens.spaceSmall))
                    Text(
                        text = emailButtonText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextOnAccent
                    )
                }

                // Email Address
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.spaceExtraSmall)
                ) {
                    Text(
                        text = "Email:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = emailAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Other Ways to Reach Us Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceMedium)
        ) {
            Text(
                text = "Other Ways to Reach Us",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // WhatsApp Support
            ContactMethodItem(
                icon = Icons.Outlined.Message,
                iconTint = AccentGreen,
                title = "WhatsApp Support",
                subtitle = whatsappNumber,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/${whatsappNumber.replace("+", "").replace(" ", "")}")
                    }
                    context.startActivity(intent)
                }
            )

            // Website
            ContactMethodItem(
                icon = Icons.Outlined.Language,
                iconTint = AccentBlue,
                title = "Website",
                subtitle = websiteUrl,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(if (websiteUrl.startsWith("http")) websiteUrl else "https://$websiteUrl")
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun ContactMethodItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val dimens = LocalDimensions.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(dimens.cornerRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = BackgroundSecondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundPrimary)
                .padding(dimens.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(dimens.iconExtraLarge + dimens.spaceSmall)
                    .background(
                        color = CardBackground,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(dimens.iconMedium),
                    tint = iconTint
                )
            }

            // Text Content
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceExtraSmall)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}