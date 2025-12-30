package com.anurag.eduai.service.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * Tracks screen entry and exit events for analytics purposes.
 */
@Composable
fun TrackScreenEvent(screenName: ScreenName) {
    DisposableEffect(screenName) {
        SessionManager.trackScreenEntry(screenName)
        onDispose {
            SessionManager.trackScreenExit(screenName)
        }
    }
}