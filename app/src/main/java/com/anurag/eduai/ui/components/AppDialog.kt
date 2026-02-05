package com.anurag.eduai.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.theme.White

/**
 * A reusable dialog component for the app.
 * show : Whether to show the dialog.
 * title : Optional title of the dialog.
 * message : Optional message body of the dialog.
 * confirmText : Text for the confirm button.
 * dismissText : Optional text for the dismiss button.
 * confirmColor : Background color for the confirm button.
 * dismissColor : Text color for the dismiss button.
 * onConfirm : Lambda to execute on confirm action.
 * onDismiss : Lambda to execute on dismiss action.
 * content : Optional composable content to include in the dialog body.
 *
 */
@Composable
fun AppDialog(
    show: Boolean,
    title: String? = null,
    message: String? = null,
    confirmText: String = "OK",
    dismissText: String? = null,
    confirmColor: Color = BrandPrimary,
    dismissColor: Color = BrandPrimary,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: (@Composable () -> Unit)? = null
) {
    val dimens = LocalDimensions.current
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(dimens.cornerRadiusMedium),
        containerColor = White,

        title = title?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
        },

        text = {
            Column {
                message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                content?.invoke()
            }
        },

        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmColor
                )
            ) {
                Text(confirmText, color = Color.White)
            }
        },

        dismissButton = dismissText?.let {
            {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = dismissColor
                    )
                ) {
                    Text(it)
                }
            }
        }
    )
}
