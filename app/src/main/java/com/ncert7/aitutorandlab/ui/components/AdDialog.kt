package com.ncert7.aitutorandlab.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.ncert7.aitutorandlab.ui.theme.LocalDimensions

/**
 * Simple dialog that displays a banner ad
 */
@Composable
fun AdDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    val dimens = LocalDimensions.current

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(dimens.cornerRadiusSmall)
                )
                .padding(dimens.spaceMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Advertisement",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = dimens.spaceSmall)
            )

            Spacer(modifier = Modifier.height(dimens.spaceMedium))

            // Banner Ad
            BannerAdView(context = context)

            Spacer(modifier = Modifier.height(dimens.spaceMedium))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    }
}
