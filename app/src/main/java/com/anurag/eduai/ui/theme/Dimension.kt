package com.anurag.eduai.ui.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Window size breakpoints based on actual screen width
enum class WindowSize { COMPACT, MEDIUM, EXPANDED }

/**
 * Determines window size based on actual available width
 * COMPACT: < 600.dp (phones)
 * MEDIUM: 600.dp - 840.dp (tablets in portrait)
 * EXPANDED: >= 840.dp (tablets in landscape, desktops)
 */
fun calculateWindowSizeFromWidth(width: Dp): WindowSize {
    return when {
        width < 600.dp -> WindowSize.COMPACT
        width < 840.dp -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }
}

/**
 * CompositionLocal to provide dimensions throughout the app
 * Access with: LocalDimensions.current
 */
val LocalDimensions = staticCompositionLocalOf<ResponsiveDimensions> {
    error("LocalDimensions not provided")
}

/**
 * Root-level composable wrapper - use this in your Activity level
 * Wrap entire app with this once, and all screens can access dimensions
 */
@Composable
fun AppDimensionProvider(
    content: @Composable () -> Unit
) {
    BoxWithConstraints {
        val windowSize = remember(maxWidth) {
            calculateWindowSizeFromWidth(maxWidth)
        }

        val dimensions = remember(windowSize) {
            ResponsiveDimensions.fromWindowSize(windowSize)
        }

        CompositionLocalProvider(LocalDimensions provides dimensions) {
            content()
        }
    }
}

/**
 * Alternative wrapper for individual screens - if you don't want to wrap entire app
 */
@Composable
fun WithDimensions(
    content: @Composable (ResponsiveDimensions) -> Unit
) {
    BoxWithConstraints {
        val windowSize = remember(maxWidth) {
            calculateWindowSizeFromWidth(maxWidth)
        }

        val dimensions = remember(windowSize) {
            ResponsiveDimensions.fromWindowSize(windowSize)
        }

        content(dimensions)
    }
}

/**
 * Holds all responsive dimension values
 */
data class ResponsiveDimensions(
    val windowSize: WindowSize,
    val spacing: Spacing,
    val radius: Radius,
    val componentSizes: ComponentSizes
) {
    companion object {
        fun fromWindowSize(windowSize: WindowSize): ResponsiveDimensions {
            return when (windowSize) {
                WindowSize.COMPACT -> ResponsiveDimensions(
                    windowSize = windowSize,
                    spacing = Spacing.Compact,
                    radius = Radius.Compact,
                    componentSizes = ComponentSizes.Compact
                )
                WindowSize.MEDIUM -> ResponsiveDimensions(
                    windowSize = windowSize,
                    spacing = Spacing.Medium,
                    radius = Radius.Medium,
                    componentSizes = ComponentSizes.Medium
                )
                WindowSize.EXPANDED -> ResponsiveDimensions(
                    windowSize = windowSize,
                    spacing = Spacing.Expanded,
                    radius = Radius.Expanded,
                    componentSizes = ComponentSizes.Expanded
                )
            }
        }
    }
}

/**
 * Padding/Spacing values that scale based on screen size
 */
data class Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 20.dp,
    val xxLarge: Dp = 24.dp,
    val xxxLarge: Dp = 32.dp
) {
    companion object {
        val Compact = Spacing(
            extraSmall = 4.dp,
            small = 8.dp,
            medium = 12.dp,
            large = 16.dp,
            extraLarge = 20.dp,
            xxLarge = 24.dp,
            xxxLarge = 32.dp
        )

        val Medium = Spacing(
            extraSmall = 4.dp,
            small = 8.dp,
            medium = 12.dp,
            large = 16.dp,
            extraLarge = 20.dp,
            xxLarge = 28.dp,
            xxxLarge = 36.dp
        )

        val Expanded = Spacing(
            extraSmall = 6.dp,
            small = 12.dp,
            medium = 16.dp,
            large = 24.dp,
            extraLarge = 28.dp,
            xxLarge = 32.dp,
            xxxLarge = 40.dp
        )
    }
}

/**
 * Corner radius values that scale based on screen size
 */
data class Radius(
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val full: Dp = 24.dp,
    val extraLarge: Dp = 28.dp
) {
    companion object {
        val Compact = Radius(
            small = 8.dp,
            medium = 12.dp,
            large = 16.dp,
            full = 24.dp,
            extraLarge = 28.dp
        )

        val Medium = Radius(
            small = 8.dp,
            medium = 12.dp,
            large = 18.dp,
            full = 28.dp,
            extraLarge = 32.dp
        )

        val Expanded = Radius(
            small = 12.dp,
            medium = 16.dp,
            large = 20.dp,
            full = 32.dp,
            extraLarge = 36.dp
        )
    }
}

/**
 * Component-specific sizes that scale based on screen size
 */
data class ComponentSizes(
    val messageBubbleMaxWidth: Float = 0.85f,
    val messageIconSize: Dp = 16.dp,
    val buttonHeight: Dp = 40.dp,
    val buttonMinWidth: Dp = 80.dp,
    val inputFieldHeight: Dp = 48.dp,
    val iconSize: Dp = 24.dp,
    val smallIconSize: Dp = 16.dp,
    val largeIconSize: Dp = 32.dp,

    // Avatar specific
    val avatarInitialSize: Dp = 180.dp,
    val avatarConversationSize: Dp = 100.dp,
    val avatarPaddingLarge: Dp = 20.dp,

    // Card and container sizes
    val cardElevation: Dp = 8.dp,
    val cardMinHeight: Dp = 100.dp
) {
    companion object {
        val Compact = ComponentSizes(
            messageBubbleMaxWidth = 0.85f,
            messageIconSize = 16.dp,
            buttonHeight = 40.dp,
            buttonMinWidth = 80.dp,
            inputFieldHeight = 48.dp,
            iconSize = 24.dp,
            smallIconSize = 16.dp,
            largeIconSize = 32.dp,
            avatarInitialSize = 180.dp,
            avatarConversationSize = 100.dp,
            avatarPaddingLarge = 20.dp,
            cardElevation = 8.dp,
            cardMinHeight = 100.dp
        )

        val Medium = ComponentSizes(
            messageBubbleMaxWidth = 0.75f,
            messageIconSize = 18.dp,
            buttonHeight = 44.dp,
            buttonMinWidth = 88.dp,
            inputFieldHeight = 52.dp,
            iconSize = 28.dp,
            smallIconSize = 18.dp,
            largeIconSize = 36.dp,
            avatarInitialSize = 200.dp,
            avatarConversationSize = 120.dp,
            avatarPaddingLarge = 24.dp,
            cardElevation = 10.dp,
            cardMinHeight = 120.dp
        )

        val Expanded = ComponentSizes(
            messageBubbleMaxWidth = 0.60f,
            messageIconSize = 20.dp,
            buttonHeight = 48.dp,
            buttonMinWidth = 96.dp,
            inputFieldHeight = 56.dp,
            iconSize = 32.dp,
            smallIconSize = 20.dp,
            largeIconSize = 40.dp,
            avatarInitialSize = 220.dp,
            avatarConversationSize = 140.dp,
            avatarPaddingLarge = 28.dp,
            cardElevation = 12.dp,
            cardMinHeight = 140.dp
        )
    }
}

/**
 * Singleton object for static dimension values (non-responsive)
 * Use these when you don't need responsive behavior (rare cases)
 */
object StaticDimensions {
    object Spacing {
        val extraSmall = 4.dp
        val small = 8.dp
        val medium = 12.dp
        val large = 16.dp
        val extraLarge = 20.dp
        val xxLarge = 24.dp
        val xxxLarge = 32.dp
    }

    object Radius {
        val small = 8.dp
        val medium = 12.dp
        val large = 16.dp
        val full = 24.dp
        val extraLarge = 28.dp
    }

    object Typography {
        val bodyLarge = 16.dp
        val bodyMedium = 14.dp
        val bodySmall = 12.dp
        val headlineSmall = 18.dp
    }
}