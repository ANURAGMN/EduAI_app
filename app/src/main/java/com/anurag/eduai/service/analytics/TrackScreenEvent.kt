package com.anurag.eduai.service.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect

/**
 * Tracks screen entry and exit events for analytics purposes.
 */
@Composable
fun TrackScreenEvent(screenName: ScreenName) {
    // Track entry when screen appears
    LaunchedEffect(screenName) {
        SessionManager.trackScreenEntry(screenName)
    }

    // Track exit when screen disappears
    DisposableEffect(screenName) {
        onDispose {
            SessionManager.trackScreenExitImmediate(screenName)
        }
    }
}