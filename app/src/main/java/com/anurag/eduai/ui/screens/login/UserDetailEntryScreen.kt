package com.anurag.eduai.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightClass
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.anurag.eduai.R
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.StudentLocalRepository
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.sync.FirebaseSyncManager
import com.anurag.eduai.ui.components.DropDownMenu
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.BackgroundSecondary
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.ColorError
import com.anurag.eduai.ui.theme.ColorHint
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.theme.White
import com.anurag.eduai.ui.viewModel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun UserDetailEntryScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val dimens = LocalDimensions.current

    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.USER_DETAIL_ENTRY)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedClass by remember { mutableIntStateOf(7) } // default Class 7
    var schoolName by remember { mutableStateOf("") }

    var phoneError by remember { mutableStateOf<String?>(null) }
    var schoolError by remember { mutableStateOf<String?>(null) }

    val classOptions = (1..10).map { context.getString(R.string.class_format, it) }

    // shared preference object
    val sharedPreference = SharedPreferenceUtils(context)
    // localDB instances
    val db = remember { EduAiDatabase.getInstance(context) }
    val studentDao = db.studentDao()
    val conceptDao = db.conceptDao()
    val localRepo = remember { StudentLocalRepository(studentDao) }

    val scrollState = rememberScrollState()
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .background(BackgroundSecondary)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(dimens.spaceMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(R.string.app_logo_desc),
                    modifier = Modifier.height(dimens.containerMinHeight - dimens.buttonHeight)
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimens.spaceMedium),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = dimens.spaceExtraSmall)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundPrimary)
                        .padding(dimens.spaceSmall + dimens.spaceExtraSmall),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.padding(dimens.spaceSmall + dimens.spaceExtraSmall))

                    Text(
                        text = stringResource(R.string.lets_go_to_know_you_message),
                        color = TextSecondary,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.padding(dimens.spaceSmall + dimens.spaceExtraSmall))

                    /**
                     * TextField to entry Full Name
                     */
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text(stringResource(R.string.full_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.full_name_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.person_icon_desc)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = ColorHint,
                            focusedLabelColor = BrandPrimary,
                            focusedLeadingIconColor = BrandPrimary,
                            unfocusedLeadingIconColor = ColorHint,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.padding(dimens.spaceSmall))

                    /**
                     * TextField to entry PhoneNumber
                     * On change it will update the mutable variable phoneNumber
                     */
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            // Dynamic validation logic
                            phoneError = when {
                                phoneNumber.isBlank() -> "Phone number cannot be empty"
                                phoneNumber.matches(Regex("^[0-5]")) -> "Phone number should start from 6 to 9"
                                !phoneNumber.matches(Regex("^(?:\\+91|91)?[6-9]\\d{9}$")) -> "Enter a valid 10-digit number"
                                else -> null
                            }
                        },
                        label = { Text(stringResource(R.string.phone_number_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.phone_number_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = stringResource(R.string.phone_icon_desc)
                            )
                        },
                        isError = phoneError != null,
                        supportingText = {
                            if (phoneError != null) {
                                Text(
                                    text = phoneError!!,
                                    color = ColorError,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = ColorHint,
                            focusedLabelColor = BrandPrimary,
                            focusedLeadingIconColor = BrandPrimary,
                            unfocusedLeadingIconColor = ColorHint,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            errorTextColor = ColorError
                        )
                    )

                    /**
                     * Drop down menu for class section
                     */
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = OutlinedTextFieldDefaults.shape,
                        border = BorderStroke(width = dimens.inputBorderWidth, color = ColorHint),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = dimens.spaceExtraSmall)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BackgroundPrimary),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightClass,
                                contentDescription = null,
                                tint = ColorHint,
                                modifier = Modifier
                                    .padding(start = dimens.spaceSmall + dimens.spaceExtraSmall, dimens.screenPadding - dimens.spaceExtraSmall)
                                    .alignByBaseline()
                            )
                            DropDownMenu(
                                label = stringResource(R.string.class_selection),
                                options =classOptions,
                                selectedValue = "Class $selectedClass",
                                onValueSelected = { selectedString ->
                                    selectedClass = selectedString.removePrefix("Class ").trim().toInt()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.padding(dimens.spaceSmall))

                    /**
                     * TextField to entry school name
                     * On change it will update the mutable variable schoolName
                     */
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = {
                            schoolName = it
                            // Dynamic validation logic
                            schoolError = when {
                                schoolName.isBlank() -> "School name can not be empty"
                                schoolName.length < 3 -> "School name must be at least 3 characters"
                                !schoolName.matches(Regex("^[a-zA-Z0-9 .,'-]{3,}$")) -> "School name should only contain alphabet"
                                else -> null
                            }
                        },
                        label = { Text(stringResource(R.string.school_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.school_name_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = stringResource(R.string.school_icon_desc)
                            )
                        },
                        isError = schoolError != null,
                        supportingText = {
                            if (schoolError != null) {
                                Text(
                                    text = schoolError!!,
                                    color = ColorError,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = ColorHint,
                            focusedLabelColor = BrandPrimary,
                            focusedLeadingIconColor = BrandPrimary,
                            unfocusedLeadingIconColor = ColorHint,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            errorTextColor = ColorError
                        )
                    )

                    Spacer(modifier = Modifier.padding(dimens.screenPadding - dimens.spaceExtraSmall))

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(dimens.spaceSmall),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimens.buttonHeight)
                            .padding(horizontal = dimens.spaceLarge - dimens.spaceExtraSmall),
                        enabled = phoneError == null && schoolError == null &&
                                phoneNumber.isNotBlank() && schoolName.isNotBlank(),
                        onClick = {
                            DebugLogger.debugLog("UserDetailEntryScreen", "Get Started Button Clicked")

                            userViewModel.updateName(fullName)
                            userViewModel.updateSchool(schoolName)
                            userViewModel.updateClass(selectedClass)
                            userViewModel.updatePhoneNumber(phoneNumber)
                            userViewModel.updateUpdatedAt(System.currentTimeMillis())
                            userViewModel.updateCreatedAt(System.currentTimeMillis())

                            scope.launch {
                                userViewModel.submit { success ->
                                    if (success) {
                                        scope.launch {
                                            val studentEntity = StudentEntity(
                                                studentId = userViewModel.user.value.id,
                                                studentName = userViewModel.user.value.displayName.toString(),
                                                email = userViewModel.user.value.email,
                                                phoneNumber = userViewModel.user.value.phoneNumber,
                                                studentSchool = userViewModel.user.value.schoolName,
                                                language = userViewModel.user.value.language,
                                                classLevel = userViewModel.user.value.studentClass,
                                                profilePhotoUrl = userViewModel.user.value.profilePictureUri,
                                                createdAt = userViewModel.user.value.createdAt,
                                                updatedAt = userViewModel.user.value.lastLogin,
                                                isSynced = true
                                            )
                                            localRepo.saveStudentLocally(studentEntity)

                                            val syncManager = FirebaseSyncManager(
                                                subjectDao = db.subjectDao(),
                                                chapterDao = db.chapterDao(),
                                                conceptDao = conceptDao
                                            )

                                            val result = syncManager.syncAllContent()
                                            DebugLogger.debugLog("LoginSync", result.message)
                                        }

                                        sharedPreference.setLoggedIn(true)
                                        sharedPreference.setLanguagePreference(userViewModel.user.value.language)
                                        sharedPreference.setUserId(userViewModel.user.value.id)

                                        navController.navigate("main") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            }
                        },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.get_started),
                                color = White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(dimens.spaceSmall))
                }
            }
            Spacer(modifier = Modifier.weight(1f))

            Card(
                elevation = CardDefaults.cardElevation(dimens.cardElevation + dimens.cardElevation),
                shape = RoundedCornerShape(dimens.spaceMedium)
            ) {
                FooterCard()
            }

            Spacer(modifier = Modifier.height(dimens.spaceMedium))
        }
    }
}