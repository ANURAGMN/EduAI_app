package com.ncert7.aitutorandlab.service.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.ncert7.aitutorandlab.BuildConfig
import com.ncert7.aitutorandlab.debug.DebugLogger

object MobileAdsInitializer {

    private const val TAG = "MobileAdsInitializer"

    /** Google sample IDs — safe for debug only. */
    private val TEST_APP_ID_SUFFIX = "3940256099942544"

    fun initialize(context: Context) {
        configureTestDevices()
        MobileAds.initialize(context)
        logAdConfiguration()
    }

    private fun configureTestDevices() {
        val testDeviceIds = buildList {
            if (BuildConfig.ADMOB_TEST_DEVICE_ID.isNotBlank()) {
                add(BuildConfig.ADMOB_TEST_DEVICE_ID)
            }
        }.distinct()

        if (testDeviceIds.isNotEmpty()) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds)
                    .build()
            )
            DebugLogger.debugLog(TAG, "Test device IDs configured: $testDeviceIds")
        }
    }

    private fun logAdConfiguration() {
        val usingTestIds = BuildConfig.ADMOB_APP_ID.contains(TEST_APP_ID_SUFFIX)
        DebugLogger.debugLog(
            TAG,
            "Mobile Ads initialized | testIds=$usingTestIds | debug=${BuildConfig.DEBUG}"
        )
        if (!BuildConfig.DEBUG && usingTestIds) {
            DebugLogger.errorLog(
                TAG,
                "Release build is using Google sample AdMob IDs — replace ADMOB_APP_ID and BANNER_AD_UNIT_ID in local.properties"
            )
        }
    }

    fun isUsingTestAdIds(): Boolean =
        BuildConfig.ADMOB_APP_ID.contains(TEST_APP_ID_SUFFIX) ||
            BuildConfig.BANNER_AD_UNIT_ID.contains(TEST_APP_ID_SUFFIX)
}
