package com.ncert7.aitutorandlab.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.service.ads.AdManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError

/**
 * BannerAdView is a Composable that displays a Google Mobile Ads banner ad.
 * This is a reusable component for displaying test/production banner ads throughout the app.
 */
@Composable
fun BannerAdView(
    context: Context,
    modifier: Modifier = Modifier,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailedToLoad: ((String) -> Unit)? = null
) {
    val adManager = remember { AdManager(context) }
    val adView = remember { adManager.createBannerAd() }

    // Load ad only once
    remember {
        adManager.loadBannerAd(adView)
        adView
    }

    // Set ad listener for lifecycle events
    remember {
        val adListener = object : AdListener() {
            override fun onAdLoaded() {
                DebugLogger.debugLog("BannerAdView", " Ad loaded successfully")
                onAdLoaded?.invoke()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                DebugLogger.errorLog(
                    "BannerAdView",
                    " Ad failed to load: ${adError.message} (Code: ${adError.code})"
                )
                onAdFailedToLoad?.invoke(adError.message)
            }

            override fun onAdClicked() {
                DebugLogger.debugLog("BannerAdView", " Ad clicked")
            }

            override fun onAdOpened() {
                DebugLogger.debugLog("BannerAdView", " Ad opened (overlay shown)")
            }

            override fun onAdClosed() {
                DebugLogger.debugLog("BannerAdView", " Ad closed (returned to app)")
            }

            override fun onAdImpression() {
                DebugLogger.debugLog("BannerAdView", " Ad impression recorded")
            }
        }
        adManager.setAdListener(adView, adListener)
        adListener
    }

    // Clean up resources when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            DebugLogger.debugLog("BannerAdView", "Disposing banner ad resources")
            adManager.destroyBannerAd(adView)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { adView },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
    }
}
