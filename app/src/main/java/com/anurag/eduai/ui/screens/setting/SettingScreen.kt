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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.anurag.eduai.R
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.setting.components.CenterPopupCard
import com.anurag.eduai.ui.screens.setting.components.ContactSupportCard
import com.anurag.eduai.ui.screens.setting.components.EditProfileScreen
import com.anurag.eduai.ui.screens.setting.components.ProfileCard
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.BackgroundSecondary
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.CardBackground
import com.anurag.eduai.ui.theme.ColorError
import com.anurag.eduai.ui.theme.ColorWarning
import com.anurag.eduai.ui.theme.IconSecondary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextOnPrimary
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.theme.TextSecondary
import com.anurag.eduai.ui.viewModel.SettingViewModel

sealed class PopupScreen {
    object EditProfile : PopupScreen()
    object ContactUs : PopupScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit = {}
) {
    TrackScreenEvent(screenName = ScreenName.SETTINGS)

    val dimens = LocalDimensions.current

    var activeScreen by remember { mutableStateOf<PopupScreen?>(null) }

    val viewModel: SettingViewModel = hiltViewModel()

    val student by viewModel.student.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings),
                        fontWeight = FontWeight.SemiBold,
                        color = TextOnPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            modifier = Modifier.size(dimens.iconMedium),
                            tint = TextOnPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandPrimary)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(BackgroundSecondary)
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .padding(dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceMedium)
            ) {
                // Learning Language Section
                SettingsSection(title = stringResource(R.string.language)) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = dimens.spaceSmall),
                        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
                    ) {
                        LanguageButton(
                            text = stringResource(R.string.language_english),
                            isSelected = selectedLanguage == "en",
                            onClick = { viewModel.setLanguage("en") },
                            modifier = Modifier.weight(1f)
                        )
                        LanguageButton(
                            text = stringResource(R.string.language_kannada),
                            isSelected = selectedLanguage == "kn",
                            onClick = { viewModel.setLanguage("kn") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.profile),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                )

                if (student == null) {
                    Text(
                        text = stringResource(R.string.loading_profile),
                        modifier = Modifier.padding(dimens.spaceMedium),
                        color = TextSecondary
                    )
                } else {
                    ProfileCard(
                        profileImageUri = student!!.localProfilePhotoUri
                            ?: student!!.profilePhotoUrl,
                        name = student!!.studentName,
                        email = student!!.email,
                        phone = student!!.phoneNumber,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Account Section
                SettingsSection(title = stringResource(R.string.account)) {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        iconTint = AccentBlue,
                        title = stringResource(R.string.edit_profile),
                        onClick = { activeScreen = PopupScreen.EditProfile }
                    )
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        iconTint = ColorWarning,
                        title = stringResource(R.string.notifications),
                        onClick = { /* Navigate to Notifications */ }
                    )
                }

                // Support Section
                SettingsSection(title = stringResource(R.string.support)) {
                    SettingsItem(
                        icon = Icons.Default.Email,
                        iconTint = AccentBlue,
                        title = stringResource(R.string.contact_us),
                        onClick = { activeScreen = PopupScreen.ContactUs }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Logout Button
                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.buttonHeightLarge),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = ColorError.copy(alpha = 0.1f)
                        ),
                    shape = RoundedCornerShape(dimens.cornerRadiusMedium)
                ) {
                    Text(
                        text = stringResource(R.string.logout),
                        color = ColorError,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        CenterPopupCard(visible = activeScreen != null, onDismiss = { activeScreen = null }) {
            when (activeScreen) {
                PopupScreen.EditProfile ->
                    EditProfileScreen(
                        userId = viewModel.userId,
                        student = student,
                        userViewModel = viewModel
                    ) { activeScreen = null }
                PopupScreen.ContactUs ->
                    ContactSupportCard(
                        emailAddress = stringResource(R.string.contact_email),
                        whatsappNumber = stringResource(R.string.contact_number),
                        websiteUrl = stringResource(R.string.contact_website),
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.send_us_mail_msg),
                        subtitle = stringResource(R.string.we_would_love_msg),
                        emailButtonText = stringResource(R.string.open_email_app_msg)
                    ) { activeScreen = null }
                null -> {}
            }
        }

        // Handle logout success
        if (logoutState) {
            onLogout()
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val dimens = LocalDimensions.current

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = dimens.spaceSmall)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimens.cornerRadiusMedium),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = dimens.cardElevation)
        ) {
            Column(
                modifier = Modifier.padding(dimens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceExtraSmall)
            ) { content() }
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
    val dimens = LocalDimensions.current

    Button(
        onClick = onClick,
        modifier = modifier.height(dimens.buttonHeight),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = if (isSelected) BrandPrimary else CardBackground,
                contentColor = if (isSelected) TextOnPrimary else TextPrimary
            ),
        shape = RoundedCornerShape(dimens.cornerRadiusMedium),
        elevation =
            ButtonDefaults.buttonElevation(
                defaultElevation =
                    if (isSelected) dimens.cardElevation
                    else dimens.cardElevation / 2
            )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SettingsItem(icon: ImageVector, iconTint: Color, title: String, onClick: () -> Unit) {
    val dimens = LocalDimensions.current

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = dimens.spaceSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(dimens.iconMedium)
            )
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = IconSecondary,
            modifier = Modifier.size(dimens.iconMedium)
        )
    }
}