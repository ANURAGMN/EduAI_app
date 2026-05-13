package com.ncert7.aitutorandlab.ui.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Screen size classifications
 */
enum class WindowSize {
    COMPACT,  // Phone in portrait
    MEDIUM,   // Phone in landscape or small tablet
    EXPANDED  // Tablet or desktop
}

/**
 * Dimension system that adapts based on screen size
 */
data class Dimensions(
    // Spacing
    val spaceExtraSmall: Dp,
    val spaceSmall: Dp,
    val spaceMedium: Dp,
    val spaceLarge: Dp,
    val spaceExtraLarge: Dp,

    // Message bubble specific
    val messagePadding: Dp,
    val messageHorizontalPadding: Dp,
    val messageVerticalPadding: Dp,
    val messageMaxWidth: Float,
    val userMessageMaxWidth: Dp,

    // Avatar
    val avatarSize: Dp,
    val avatarIconSize: Dp,
    val avatarSizeSmall: Dp,
    val avatarSizeLarge: Dp,

    // Icon sizes
    val iconExtraSmall: Dp,
    val iconSmall: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,
    val iconExtraLarge: Dp,

    // Corner radius
    val cornerRadiusSmall: Dp,
    val cornerRadiusMedium: Dp,
    val cornerRadiusLarge: Dp,
    val cornerRadiusRound: Dp,

    // Buttons
    val buttonHeight: Dp,
    val buttonHeightSmall: Dp,
    val buttonHeightLarge: Dp,
    val buttonPadding: Dp,
    val buttonIconSize: Dp,

    // TextFields
    val inputHeight: Dp,
    val inputPadding: Dp,
    val inputHorizontalPadding: Dp,
    val inputBorderWidth: Dp,
    val inputRadius: Dp,

    // Cards
    val cardPadding: Dp,
    val cardElevation: Dp,
    val containerMinHeight: Dp,

    //box
    val boxSizeSmall: Dp,
    val boxSizeMedium: Dp,

    // dialogs
    val dialogPadding: Dp,
    val dialogTitleSize: Dp,
    val sheetPeekHeight: Dp,
    // Dropdowns
    val dropdownMaxWidth: Dp,
    val dropdownItemHeight: Dp,
    val dropdownPadding: Dp,

    //dividers
    val dividerThickness: Dp,
    val dividerHeight: Dp,

    // spacing around screen edges
    val screenPadding: Dp,
    val sectionSpacing: Dp,

    // status card height
    val statusCardHeight: Dp,

    val weeklyActivityCardHeight: Dp,

    // Progress Indicator
    val progressIndicatorStrokeWidth: Dp,
    val timerLength: Dp
) {
    companion object {
        val Compact = Dimensions(
            // Spacing
            spaceExtraSmall = 4.dp,
            spaceSmall = 8.dp,
            spaceMedium = 16.dp,
            spaceLarge = 24.dp,
            spaceExtraLarge = 32.dp,

            // Message bubbles
            messagePadding = 12.dp,
            messageHorizontalPadding = 8.dp,
            messageVerticalPadding = 8.dp,
            messageMaxWidth = 0.85f,
            userMessageMaxWidth = 280.dp,

            // Avatars
            avatarSize = 36.dp,
            avatarIconSize = 20.dp,
            avatarSizeSmall = 24.dp,
            avatarSizeLarge = 56.dp,

            // Icons
            iconExtraSmall = 12.dp,
            iconSmall = 16.dp,
            iconMedium = 20.dp,
            iconLarge = 28.dp,
            iconExtraLarge = 36.dp,

            // Corner radius
            cornerRadiusSmall = 4.dp,
            cornerRadiusMedium = 12.dp,
            cornerRadiusLarge = 16.dp,
            cornerRadiusRound = 18.dp,

            // Buttons
            buttonHeight = 44.dp,
            buttonHeightSmall = 36.dp,
            buttonHeightLarge = 52.dp,
            buttonPadding = 12.dp,
            buttonIconSize = 18.dp,

            // Input fields
            inputHeight = 44.dp,
            inputPadding = 12.dp,
            inputHorizontalPadding = 12.dp,
            inputBorderWidth = 2.dp,
            inputRadius = 24.dp,

            // Cards & containers
            cardPadding = 12.dp,
            cardElevation = 4.dp,
            containerMinHeight = 200.dp,

            // Dialogs & sheets
            dialogPadding = 20.dp,
            dialogTitleSize = 18.dp,
            sheetPeekHeight = 100.dp,

            // Dropdown & menus
            dropdownMaxWidth = 200.dp,
            dropdownItemHeight = 40.dp,
            dropdownPadding = 8.dp,

            // Dividers
            dividerThickness = 1.dp,
            dividerHeight = 16.dp,

            // Screen spacing
            screenPadding = 16.dp,
            sectionSpacing = 24.dp,

            //box
            boxSizeSmall= 60.dp,
            boxSizeMedium = 80.dp,

            statusCardHeight = 100.dp,

            weeklyActivityCardHeight = 120.dp,

            // Progress Indicator
            progressIndicatorStrokeWidth = 4.dp,
            timerLength= 60.dp
        )

        val Medium = Dimensions(
            // Spacing
            spaceExtraSmall = 6.dp,
            spaceSmall = 12.dp,
            spaceMedium = 20.dp,
            spaceLarge = 28.dp,
            spaceExtraLarge = 36.dp,

            // Message bubbles
            messagePadding = 16.dp,
            messageHorizontalPadding = 12.dp,
            messageVerticalPadding = 10.dp,
            messageMaxWidth = 0.75f,
            userMessageMaxWidth = 320.dp,

            // Avatars
            avatarSize = 44.dp,
            avatarIconSize = 26.dp,
            avatarSizeSmall = 32.dp,
            avatarSizeLarge = 64.dp,

            // Icons
            iconExtraSmall = 14.dp,
            iconSmall = 18.dp,
            iconMedium = 24.dp,
            iconLarge = 32.dp,
            iconExtraLarge = 40.dp,

            // Corner radius
            cornerRadiusSmall = 6.dp,
            cornerRadiusMedium = 14.dp,
            cornerRadiusLarge = 18.dp,
            cornerRadiusRound = 22.dp,

            // Buttons
            buttonHeight = 48.dp,
            buttonHeightSmall = 40.dp,
            buttonHeightLarge = 56.dp,
            buttonPadding = 14.dp,
            buttonIconSize = 20.dp,

            // Input fields
            inputHeight = 48.dp,
            inputPadding = 14.dp,
            inputHorizontalPadding = 14.dp,
            inputBorderWidth = 1.dp,
            inputRadius = 24.dp,

            // Cards & containers
            cardPadding = 14.dp,
            cardElevation = 6.dp,
            containerMinHeight = 240.dp,

            // Dialogs & sheets
            dialogPadding = 24.dp,
            dialogTitleSize = 20.dp,
            sheetPeekHeight = 120.dp,

            // Dropdown & menus
            dropdownMaxWidth = 240.dp,
            dropdownItemHeight = 44.dp,
            dropdownPadding = 10.dp,

            // Dividers
            dividerThickness = 1.dp,
            dividerHeight = 20.dp,

            // Screen spacing
            screenPadding = 20.dp,
            sectionSpacing = 28.dp,

            //box
            boxSizeSmall = 80.dp,
            boxSizeMedium = 100.dp,

            statusCardHeight = 120.dp,
            weeklyActivityCardHeight = 140.dp,
            // Progress Indicator
            progressIndicatorStrokeWidth = 6.dp,
            timerLength= 64.dp
        )

        val Expanded = Dimensions(
            // Spacing
            spaceExtraSmall = 8.dp,
            spaceSmall = 16.dp,
            spaceMedium = 24.dp,
            spaceLarge = 32.dp,
            spaceExtraLarge = 48.dp,

            // Message bubbles
            messagePadding = 20.dp,
            messageHorizontalPadding = 16.dp,
            messageVerticalPadding = 12.dp,
            messageMaxWidth = 0.65f,
            userMessageMaxWidth = 400.dp,

            // Avatars
            avatarSize = 48.dp,
            avatarIconSize = 28.dp,
            avatarSizeSmall = 40.dp,
            avatarSizeLarge = 72.dp,

            // Icons
            iconExtraSmall = 16.dp,
            iconSmall = 20.dp,
            iconMedium = 28.dp,
            iconLarge = 36.dp,
            iconExtraLarge = 48.dp,

            // Corner radius
            cornerRadiusSmall = 8.dp,
            cornerRadiusMedium = 16.dp,
            cornerRadiusLarge = 20.dp,
            cornerRadiusRound = 24.dp,

            // Buttons
            buttonHeight = 52.dp,
            buttonHeightSmall = 44.dp,
            buttonHeightLarge = 60.dp,
            buttonPadding = 16.dp,
            buttonIconSize = 24.dp,

            // Input fields
            inputHeight = 52.dp,
            inputPadding = 16.dp,
            inputHorizontalPadding = 16.dp,
            inputBorderWidth = 1.5.dp,
            inputRadius = 24.dp,

            // Cards & containers
            cardPadding = 16.dp,
            cardElevation = 8.dp,
            containerMinHeight = 280.dp,


            // Dialogs & sheets
            dialogPadding = 28.dp,
            dialogTitleSize = 22.dp,
            sheetPeekHeight = 140.dp,


            // Dropdown & menus
            dropdownMaxWidth = 280.dp,
            dropdownItemHeight = 48.dp,
            dropdownPadding = 12.dp,

            // Dividers
            dividerThickness = 1.dp,
            dividerHeight = 24.dp,

            // Screen spacing
            screenPadding = 24.dp,
            sectionSpacing = 32.dp,

            //box
            boxSizeSmall = 100.dp,
            boxSizeMedium = 120.dp,

            statusCardHeight = 140.dp,

            weeklyActivityCardHeight = 160.dp,
            //progress indicator
            progressIndicatorStrokeWidth = 8.dp,
            timerLength= 68.dp
        )
    }
}

/**
 * Get dimensions based on window size
 */
fun WindowSize.getDimensions(): Dimensions = when (this) {
    WindowSize.COMPACT -> Dimensions.Compact
    WindowSize.MEDIUM -> Dimensions.Medium
    WindowSize.EXPANDED -> Dimensions.Expanded
}

/**
 * Determine window size based on screen width
 * Using BoxWithConstraints for accurate window size measurement
 */
@Composable
fun rememberWindowSize(): WindowSize {
    var windowSize = WindowSize.COMPACT

    BoxWithConstraints {
        val screenWidthDp = maxWidth.value.toInt()

        windowSize = remember(screenWidthDp) {
            when {
                screenWidthDp < 600 -> WindowSize.COMPACT
                screenWidthDp < 840 -> WindowSize.MEDIUM
                else -> WindowSize.EXPANDED
            }
        }
    }

    return windowSize
}

/**
 * CompositionLocal for accessing dimensions throughout the app
 */
val LocalDimensions = compositionLocalOf { Dimensions.Compact }

/**
 * Theme wrapper that provides adaptive dimensions
 * Uses BoxWithConstraints to accurately measure available space
 */
@Composable
fun AdaptiveTheme(
    content: @Composable () -> Unit
) {
    BoxWithConstraints {
        val screenWidthDp = maxWidth.value.toInt()

        val windowSize = remember(screenWidthDp) {
            when {
                screenWidthDp < 600 -> WindowSize.COMPACT
                screenWidthDp < 840 -> WindowSize.MEDIUM
                else -> WindowSize.EXPANDED
            }
        }

        CompositionLocalProvider(
            LocalDimensions provides windowSize.getDimensions(),
            content = content
        )
    }
}