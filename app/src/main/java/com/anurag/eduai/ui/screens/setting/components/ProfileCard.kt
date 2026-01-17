package com.anurag.eduai.ui.screens.setting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileCard(
    profileImageUri: String?,
    name: String,
    email: String,
    phone: String,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(dimens.cornerRadiusLarge),
        elevation = CardDefaults.cardElevation(defaultElevation = dimens.cardElevation),
        colors = CardDefaults.cardColors(
            containerColor = BackgroundPrimary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(dimens.boxSizeMedium)
                    .clip(CircleShape)
                    .background(BackgroundPrimary),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null) {
                    GlideImage(
                        model = profileImageUri,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder icon if no image
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default profile",
                        modifier = Modifier.size(dimens.iconExtraLarge),
                        tint = AccentBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.spaceMedium))

            // Name
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(dimens.spaceSmall))
            Text(
                text = email,
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Text(
                text = phone,
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
        }
    }
}