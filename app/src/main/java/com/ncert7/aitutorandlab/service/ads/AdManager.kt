package com.ncert7.aitutorandlab.service.ads

import android.content.Context
import android.view.ViewGroup
import com.ncert7.aitutorandlab.BuildConfig
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlin.let

/**
 * AdManager handles Google Mobile Ads banner ad lifecycle and loading.
 * Uses ad unit ID from BuildConfig for security.
 */
class AdManager(private val context: Context) {
    companion object {
        private const val TAG = "AdManager"
    }

    /**
     * Creates a new AdView with ad unit ID from BuildConfig and adaptive banner size
     */
    fun createBannerAd(): AdView {
        return try {
            val adView = AdView(context)
            adView.adUnitId = BuildConfig.BANNER_AD_UNIT_ID

            // Set adaptive banner ad size
            val adSize = AdSize.BANNER
            adView.setAdSize(adSize)

            DebugLogger.debugLog(TAG, "Banner ad created successfully")
            adView
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error creating banner ad: ${e.message}")
            AdView(context)
        }
    }

    /**
     * Loads an ad into the provided AdView
     */
    fun loadBannerAd(adView: AdView) {
        try {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            DebugLogger.debugLog(TAG, "Ad load request sent")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error loading banner ad: ${e.message}")
        }
    }

    /**
     * Properly destroys the banner ad and releases resources
     */
    fun destroyBannerAd(adView: AdView?) {
        try {
            adView?.let {
                val parentView = it.parent as? ViewGroup
                parentView?.removeView(it)
                it.destroy()
                DebugLogger.debugLog(TAG, "Banner ad destroyed successfully")
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error destroying banner ad: ${e.message}")
        }
    }

    /**
     * Set ad listener for lifecycle callbacks
     */
    fun setAdListener(adView: AdView, listener: AdListener) {
        try {
            adView.adListener = listener
            DebugLogger.debugLog(TAG, "Ad listener attached")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error setting ad listener: ${e.message}")
        }
    }
}
