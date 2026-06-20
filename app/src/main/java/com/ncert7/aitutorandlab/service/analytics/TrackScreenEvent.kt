package com.ncert7.aitutorandlab.service.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect

/**
 * Tracks screen entry and exit events for analytics purposes.
 */
@Composable
fun TrackScreenEvent(
    screenName: ScreenName,
    conceptId: String? = null
) {
    // Track entry when screen appears
    LaunchedEffect(screenName, conceptId) {
        SessionManager.trackScreenEntry(screenName, conceptId)
    }

    // Track exit when screen disappears
    DisposableEffect(screenName) {
        onDispose {
            SessionManager.trackScreenExitImmediate(screenName)
        }
    }
}