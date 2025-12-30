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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.anurag.eduai.R
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.ConceptRepository
import com.anurag.eduai.repository.StudentLocalRepository
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.sync.FirebaseSyncManager
import com.anurag.eduai.ui.components.DropDownMenu
import com.anurag.eduai.ui.theme.*
import com.anurag.eduai.ui.viewModel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun UserDetailEntryScreen(
    navController: NavController,
    userViewModel: UserViewModel = UserViewModel()
) {

    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.USER_DETAIL_ENTRY)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf(7) } // default Class 7

    var schoolName by remember { mutableStateOf("") }

    var phoneError by remember { mutableStateOf<String?>(null) }
    var schoolError by remember { mutableStateOf<String?>(null) }

    val classOptions = (1..10).map { "Class $it" }

    // shared preference object
    val sharedPreference = SharedPreferenceUtils(context)
    // localDB instances
    val db = remember { EduAiDatabase.getInstance(context) }
    val studentDao = db.studentDao()
    val conceptDao = db.conceptDao()
    val localRepo = remember { StudentLocalRepository(studentDao) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSecondary)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(16.dp),
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
                    contentDescription = "Logo",
                    modifier = Modifier.height(150.dp)
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundPrimary)
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.padding(10.dp))

                    Text(
                        text = stringResource(R.string.lets_go_to_know_you_message),
                        color = TextSecondary,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.padding(10.dp))

                    /**
                     * TextField to entry Full Name
                     * ReadOnly
                     */
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                        },
                        label = { Text(stringResource(R.string.full_name_label)) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.full_name_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Person Icon"
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

                    Spacer(modifier = Modifier.padding(5.dp))
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
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.phone_number_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone Icon"
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
                    Spacer(modifier = Modifier.padding(0.dp))

                    /**
                     * Drop down menu for class section
                     */
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = OutlinedTextFieldDefaults.shape,
                        border = BorderStroke(width = 1.dp, color = ColorHint),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                    .padding(start = 10.dp, 15.dp)
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

                    Spacer(modifier = Modifier.padding(5.dp))
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
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.school_name_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "School Icon"
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

                    Spacer(modifier = Modifier.padding(15.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 20.dp),

                        //  the button is only enabled if phoneNumber and schoolName is filled
                        // Which makes the Name, Phone Number, and School Name as required and ambition as optional
                        enabled = phoneError == null && schoolError == null,
                        onClick = {
                            DebugLogger.debugLog(
                                "UserDetailEntryScreen",
                                "Get Started Button Clicked"
                            )
                            userViewModel.updateName(fullName)
                            userViewModel.updateSchool(schoolName)
                            userViewModel.updateClass(selectedClass)
                            userViewModel.updatePhoneNumber(phoneNumber)
                            userViewModel.updateUpdatedAt(System.currentTimeMillis())
                            userViewModel.updateCreatedAt(System.currentTimeMillis())

                            scope.launch {
                                userViewModel.submit { success ->
                                    if (success) {
                                        scope.launch { // new coroutine scope for saveStudentLocally() method
                                            val studentEntity = StudentEntity(
                                                studentId = userViewModel.user.value.id,
                                                studentName = userViewModel.user.value.displayName.toString(),
                                                email = userViewModel.user.value.email,
                                                phoneNumber = userViewModel.user.value.phoneNumber,
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
                                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
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
                    Spacer(modifier = Modifier.padding(5.dp))

                }
            }
            Spacer(modifier = Modifier.weight(1f))

            Card(
                elevation = CardDefaults.cardElevation(10.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                FooterCard()
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}