package com.anurag.eduai.ui.theme

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

    // Icon sizes
    val iconSmall: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,

    // Corner radius
    val cornerRadiusSmall: Dp,
    val cornerRadiusMedium: Dp,
    val cornerRadiusLarge: Dp,
    val cornerRadiusRound: Dp,
) {
    companion object {
        val Compact = Dimensions(
            spaceExtraSmall = 4.dp,
            spaceSmall = 8.dp,
            spaceMedium = 16.dp,
            spaceLarge = 24.dp,
            spaceExtraLarge = 32.dp,
            messagePadding = 12.dp,
            messageHorizontalPadding = 8.dp,
            messageVerticalPadding = 8.dp,
            messageMaxWidth = 0.85f,
            userMessageMaxWidth = 280.dp,
            avatarSize = 36.dp,
            avatarIconSize = 20.dp,
            iconSmall = 16.dp,
            iconMedium = 20.dp,
            iconLarge = 28.dp,
            cornerRadiusSmall = 4.dp,
            cornerRadiusMedium = 12.dp,
            cornerRadiusLarge = 16.dp,
            cornerRadiusRound = 18.dp,
        )

        val Medium = Dimensions(
            spaceExtraSmall = 6.dp,
            spaceSmall = 12.dp,
            spaceMedium = 20.dp,
            spaceLarge = 28.dp,
            spaceExtraLarge = 36.dp,
            messagePadding = 16.dp,
            messageHorizontalPadding = 12.dp,
            messageVerticalPadding = 10.dp,
            messageMaxWidth = 0.75f,
            userMessageMaxWidth = 320.dp,
            avatarSize = 44.dp,
            avatarIconSize = 26.dp,
            iconSmall = 18.dp,
            iconMedium = 24.dp,
            iconLarge = 32.dp,
            cornerRadiusSmall = 6.dp,
            cornerRadiusMedium = 14.dp,
            cornerRadiusLarge = 18.dp,
            cornerRadiusRound = 22.dp,
        )

        val Expanded = Dimensions(
            spaceExtraSmall = 8.dp,
            spaceSmall = 16.dp,
            spaceMedium = 24.dp,
            spaceLarge = 32.dp,
            spaceExtraLarge = 48.dp,
            messagePadding = 20.dp,
            messageHorizontalPadding = 16.dp,
            messageVerticalPadding = 12.dp,
            messageMaxWidth = 0.65f,
            userMessageMaxWidth = 400.dp,
            avatarSize = 48.dp,
            avatarIconSize = 28.dp,
            iconSmall = 20.dp,
            iconMedium = 28.dp,
            iconLarge = 36.dp,
            cornerRadiusSmall = 8.dp,
            cornerRadiusMedium = 16.dp,
            cornerRadiusLarge = 20.dp,
            cornerRadiusRound = 24.dp,
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