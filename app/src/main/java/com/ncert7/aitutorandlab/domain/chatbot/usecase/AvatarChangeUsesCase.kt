package com.ncert7.aitutorandlab.domain.chatbot.usecase

import com.ncert7.aitutorandlab.ui.viewModel.TextToSpeech
import javax.inject.Inject

/**
 * Use case to handle avatar changes in a reliable way.
 * This separates the avatar change logic from the ViewModel and uses
 * enum-based avatar codes instead of string comparison which is unreliable
 * across different languages.
 */
class AvatarChangeUseCase @Inject constructor() {

    /**
     * Avatar types supported in the app
     */
    enum class AvatarType(val code: String) {
        BOY("boy"),
        GIRL("girl"),
        DISABLE("disable");

        companion object {
            fun fromCode(code: String): AvatarType {
                return values().find { it.code.equals(code, ignoreCase = true) } ?: DISABLE
            }
        }
    }

    /**
     * Handle avatar change with proper voice and character switching
     * @param avatarCode The avatar code ("boy", "girl", "disable")
     * @param ttsController The TextToSpeech controller
     * @param currentLanguage The current language code
     * @return The normalized avatar code
     */
    fun changeAvatar(
        avatarCode: String,
        ttsController: TextToSpeech,
        currentLanguage: String
    ): String {
        val avatarType = AvatarType.fromCode(avatarCode)
        val normalizedCode = avatarType.code

        // Switch the character in the webview
        ttsController.switchCharacter(normalizedCode)

        // Apply defaults if not disabled
        if (avatarType != AvatarType.DISABLE) {
            ttsController.applyDefaultsForAvatarLanguage(normalizedCode, currentLanguage)
        } else {
            // Stop speaking if disabling avatar
            if (ttsController.state.value.isSpeaking) {
                ttsController.stop()
            }
        }

        return normalizedCode
    }

    /**
     * Get the avatar type from a display name (localized string)
     * This is more reliable than string comparison
     * @param displayName The localized display name
     * @param boyDisplayName The localized "boy" string
     * @param girlDisplayName The localized "girl" string
     * @return The avatar code
     */
    fun getAvatarCodeFromDisplayName(
        displayName: String,
        boyDisplayName: String,
        girlDisplayName: String
    ): String {
        return when {
            displayName.equals(boyDisplayName, ignoreCase = false) -> AvatarType.BOY.code
            displayName.equals(girlDisplayName, ignoreCase = false) -> AvatarType.GIRL.code
            else -> AvatarType.DISABLE.code
        }
    }

    /**
     * Get display name from avatar code
     * @param avatarCode The avatar code
     * @param boyDisplayName The localized "boy" string
     * @param girlDisplayName The localized "girl" string
     * @param disableDisplayName The localized "disable" string
     * @return The localized display name
     */
    fun getDisplayNameFromCode(
        avatarCode: String,
        boyDisplayName: String,
        girlDisplayName: String,
        disableDisplayName: String
    ): String {
        val avatarType = AvatarType.fromCode(avatarCode)
        return when (avatarType) {
            AvatarType.BOY -> boyDisplayName
            AvatarType.GIRL -> girlDisplayName
            AvatarType.DISABLE -> disableDisplayName
        }
    }
}