package com.ncert7.aitutorandlab.service.ads

import com.google.android.gms.ads.RequestConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileAdsInitializerTest {

    @Test
    fun buildRequestConfiguration_usesChildSafeGlobalSettings() {
        val config = MobileAdsInitializer.buildRequestConfiguration()

        assertEquals(
            RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE,
            config.tagForChildDirectedTreatment
        )
        assertEquals(
            RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE,
            config.tagForUnderAgeOfConsent
        )
        assertEquals(
            RequestConfiguration.MAX_AD_CONTENT_RATING_G,
            config.maxAdContentRating
        )
        assertTrue(config.testDeviceIds.isEmpty())
    }
}
