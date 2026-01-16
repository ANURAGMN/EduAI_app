// EditProfileScreen.kt
package com.anurag.eduai.ui.screens.setting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.White

@Composable
fun EditProfileScreen(
    onClose:() -> Unit
) {
    val dimensions = LocalDimensions.current

    var userName by remember { mutableStateOf("") }
    var classValue by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(dimensions.spaceMedium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(dimensions.spaceMedium))

        // Profile Photo Section
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFB24CF3),
                            Color(0xFFD946EF)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person, // You'll need to add this icon
                contentDescription = stringResource(R.string.profile_photo),
                modifier = Modifier.size(60.dp),
                tint = Color(0xFF3B82F6)
            )
        }

        Spacer(modifier = Modifier.height(dimensions.spaceMedium))

        // Change Photo Button
        OutlinedButton(
            onClick = { /* Handle photo change */ },
            modifier = Modifier.height(dimensions.buttonHeightSmall),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1F2937)
            ),
            border = androidx.compose.foundation.BorderStroke(
                dimensions.inputBorderWidth,
                Color(0xFFE5E7EB)
            ),
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
        ) {
            Text(
                text = stringResource(R.string.change_photo),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(dimensions.spaceLarge))

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it},
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {Text(stringResource(R.string.enter_your_name))},
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedBorderColor = BrandPrimary,
                unfocusedBorderColor = ColorHint,
                focusedLabelColor = BrandPrimary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(dimensions.spaceMedium))

//        // Class Field
        OutlinedTextField(
            value = classValue,
            onValueChange = { classValue = it},
            label = { Text(stringResource(R.string.class_label)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {Text(stringResource(R.string.class_placeholder))},
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedBorderColor = BrandPrimary,
                unfocusedBorderColor = ColorHint,
                focusedLabelColor = BrandPrimary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(dimensions.spaceMedium))

        // School Field

        OutlinedTextField(
            value = school,
            onValueChange = { school = it},
            label = { Text(stringResource(R.string.school)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {Text(stringResource(R.string.enter_school_name))},
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedBorderColor = BrandPrimary,
                unfocusedBorderColor = ColorHint,
                focusedLabelColor = BrandPrimary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(dimensions.spaceMedium))

        // Phone Number Field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it},
            label = { Text(stringResource(R.string.phone_number)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {Text(stringResource(R.string.enter_phone_number))},
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedBorderColor = BrandPrimary,
                unfocusedBorderColor = ColorHint,
                focusedLabelColor = BrandPrimary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(dimensions.spaceLarge))

        // Save Changes Button
        Button(
            onClick = {

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1F2937)
            ),
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
        ) {
            Text(
                text = stringResource(R.string.save_changes),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(dimensions.spaceMedium))
    }
}