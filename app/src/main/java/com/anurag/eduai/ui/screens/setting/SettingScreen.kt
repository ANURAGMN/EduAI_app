package com.anurag.eduai.ui.screens.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.repository.FirebaseRepository
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.setting.components.CenterPopupCard
import com.anurag.eduai.ui.screens.setting.components.EditProfileScreen

sealed class PopupScreen {
    object EditProfile : PopupScreen()
//    object Settings : PopupScreen()
//    object Help : PopupScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen() {
    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.SETTINGS)

    var selectedLanguage by remember { mutableStateOf("English") }

    var activeScreen by remember { mutableStateOf<PopupScreen?>(null) }

    val context = LocalContext.current

    val sharedPref = SharedPreferenceUtils(context)
    val userId = sharedPref.getUserId().toString()

    val firebaseRepository = FirebaseRepository()
    val db = remember { EduAiDatabase.getInstance(context) }
    val studentDao = db.studentDao()

    var student by remember { mutableStateOf<StudentEntity?>(null) }

    LaunchedEffect(userId) {
        student = studentDao.getStudentSync(userId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { /* Navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2C3E50)
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Learning Language Section
                SettingsSection(title = "Learning Language") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LanguageButton(
                            text = "English",
                            isSelected = selectedLanguage == "English",
                            onClick = { selectedLanguage = "English" },
                            modifier = Modifier.weight(1f)
                        )
                        LanguageButton(
                            text = "తెలుగు",
                            isSelected = selectedLanguage == "తెలుగు",
                            onClick = { selectedLanguage = "తెలుగు" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Account Section
                SettingsSection(title = "Account") {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        iconTint = Color(0xFF2196F3),
                        title = "Edit Profile",
                        onClick = {
                            activeScreen = PopupScreen.EditProfile
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        iconTint = Color(0xFFFFC107),
                        title = "Notifications",
                        onClick = { /* Navigate to Notifications */ }
                    )
                }

                // Support Section
                SettingsSection(title = "Support") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        iconTint = Color(0xFFE91E63),
                        title = "Help",
                        onClick = { /* Navigate to Help */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Email,
                        iconTint = Color(0xFF2196F3),
                        title = "Contact Us",
                        onClick = { /* Navigate to Contact */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Description,
                        iconTint = Color(0xFF9C27B0),
                        title = "Terms & Conditions",
                        onClick = { /* Navigate to Terms */ }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Logout Button
                Button(
                    onClick = { /* Handle Logout */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Logout",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

            }
        }
        CenterPopupCard(
            visible = activeScreen != null,
            onDismiss = { activeScreen = null }
        ) {
            when (activeScreen) {
                PopupScreen.EditProfile -> EditProfileScreen(
                    firebaseRepository = firebaseRepository,
                    studentDao = studentDao,
                    userId = userId,
                    student = student
                ) { activeScreen = null }
                null -> {}
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun LanguageButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF2196F3) else Color.White,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Text(text = text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}