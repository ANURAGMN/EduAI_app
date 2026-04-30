package com.anurag.eduai.ui.screens.setting.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.R
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.ui.components.DropDownMenu
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.ButtonBorderLight
import com.anurag.eduai.ui.theme.ButtonContentDark
import com.anurag.eduai.ui.theme.ColorError
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.ColorWarning
import com.anurag.eduai.ui.theme.EditProfileBackground
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.ProfileAvatarGradientEnd
import com.anurag.eduai.ui.theme.ProfileAvatarGradientStart
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.ui.screens.setting.viewmodel.SettingViewModel
import com.anurag.eduai.ui.screens.setting.viewmodel.UpdateProfileState
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    userId: String,
    student: StudentEntity?,
    userViewModel: SettingViewModel,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    val dimensions = LocalDimensions.current
    val scrollState = rememberScrollState()

    var userName by remember {
        mutableStateOf(
            student?.studentName.orEmpty()
        )
    }
    var classValue by remember {
        mutableIntStateOf(
            student?.classLevel
                ?: 7
        )
    }
    var school by remember {
        mutableStateOf(
            student?.studentSchool.orEmpty()
        )
    }
    // TODO: it should be Integer but due to refactoring purpose it is left this way
    // Country code is not taken for future we can add country code
    var phoneNumber by remember {
        mutableStateOf(
            student?.phoneNumber.orEmpty()
        )
    }

    val classOptions = (1..10).map { "Class $it" }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var schoolError by remember { mutableStateOf<String?>(null) }

    // object of UpdateUserViewModel
    val updateState by userViewModel.updateState.collectAsState()

    LaunchedEffect(updateState) {
        when (updateState) {
            UpdateProfileState.Success -> {
                DebugLogger.debugLog("EditProfilePopUp", "Update success")
                onClose()
                userViewModel.resetState()
            }
            is UpdateProfileState.Error -> {
                DebugLogger.errorLog(
                    "EditProfilePopUp",
                    (updateState as UpdateProfileState.Error).message
                )
                userViewModel.resetState()
            }
            else -> Unit
        }
    }
    Column(
        modifier =
            modifier.fillMaxWidth()
                .background(EditProfileBackground)
                .verticalScroll(scrollState)
                .padding(dimensions.spaceMedium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(dimensions.spaceMedium))
        // Profile Photo Section
        Box(
            modifier = Modifier
                .size(dimensions.boxSizeMedium)
                .clip(CircleShape)
                .background(
                    brush = Brush
                        .linearGradient(
                            colors =
                                listOf(
                                    ProfileAvatarGradientStart,
                                    ProfileAvatarGradientEnd
                                )
                        )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person, // You'll need to add this icon
                contentDescription = stringResource(R.string.profile_photo),
                modifier = Modifier.size(dimensions.avatarSizeLarge),
                tint = AccentBlue
            )
        }

        Spacer(modifier = Modifier.height(dimensions.spaceMedium))
        // Change Photo Button
        OutlinedButton(
            onClick = { /* Handle photo change */},
            modifier = Modifier.height(dimensions.buttonHeightSmall),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = ButtonContentDark
                ),
            border = BorderStroke(dimensions.inputBorderWidth, ButtonBorderLight),
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
        ) {
            Text(
                text = stringResource(R.string.change_photo),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(dimensions.spaceLarge))
        // name field
        OutlinedTextField(
            value = userName,
            onValueChange = {
                userName = it
                nameError = when {
                    // TODO: remove hard coded error string from validation
                    userName.isBlank() -> "Name can not be empty"
                    userName.length < 5 ->
                        "Full Name must be at least 5 characters"
                    !userName.matches(
                        Regex("^[a-zA-Z0-9 .,'-]{3,}$")
                    ) -> "Name should only contain alphabet"
                    else -> null
                } },
            isError = nameError != null,
            supportingText = {
                if (nameError != null) {
                    Text(
                        text = nameError!!,
                        color = ColorError,
                        style = MaterialTheme.typography.bodySmall
                    )
                } },
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.enter_your_name)) },
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
        //      Class Field
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = OutlinedTextFieldDefaults.shape,
            border = BorderStroke(
                width = dimensions.inputBorderWidth,
                color = ColorHint
            ),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(BackgroundPrimary),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DropDownMenu(
                    label = stringResource(R.string.class_selection),
                    options = classOptions,
                    selectedValue = "Class $classValue",
                    onValueSelected = { selectedString ->
                        classValue = selectedString
                            .removePrefix("Class ")
                            .trim()
                            .toInt()
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(dimensions.spaceMedium))
        // School Field
        OutlinedTextField(
            value = school,
            onValueChange = {
                school = it
                // Dynamic validation logic
                schoolError = when {
                    school.isBlank() -> "School name can not be empty"
                    school.length < 3 ->
                        "School name must be at least 3 characters"
                    !school.matches(Regex("^[a-zA-Z0-9 .,'-]{3,}$")) ->
                        "School name should only contain alphabet"
                    else -> null
                } },
            isError = schoolError != null,
            supportingText = {
                if (schoolError != null) {
                    Text(
                        text = schoolError!!,
                        color = ColorError,
                        style = MaterialTheme.typography.bodySmall
                    )
                } },
            label = { Text(stringResource(R.string.school)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.enter_school_name)) },
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
            onValueChange = {
                phoneNumber = it
                // Dynamic validation logic
                phoneError = when {
                    phoneNumber.isBlank() ->
                        "Phone number cannot be empty"
                    phoneNumber.matches(Regex("^[0-5]")) ->
                        "Phone number should start from 6 to 9"
                    !phoneNumber.matches(
                        Regex("^[6-9]\\d{9}$")
                    ) -> "Enter a valid 10-digit number"
                    else -> null
                }},
            isError = phoneError != null,
            supportingText = {
                if (phoneError != null) {
                    Text(
                        text = phoneError!!,
                        color = ColorError,
                        style = MaterialTheme.typography.bodySmall
                    )
                } },
            label = { Text(stringResource(R.string.phone_number)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.enter_phone_number)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                userViewModel.viewModelScope.launch {
                    // TODO: move it to viewmodel
                    userViewModel.updateProfile(
                        updatedName = userName,
                        updatedPhone = phoneNumber,
                        updatedClass = classValue,
                        updatedSchool = school
                    )
                } },
            enabled = updateState !is UpdateProfileState.Loading,
            modifier = Modifier.fillMaxWidth().height(dimensions.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = HeaderGradientEnd,
                disabledContainerColor = ColorWarning
            ),
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
        ) {
            Text(
                text = if (updateState is UpdateProfileState.Loading) "Saving..."
                else "Save",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = White
            )
        }
        Spacer(modifier = Modifier.height(dimensions.spaceMedium))
    }
}
