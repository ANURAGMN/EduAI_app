package com.ncert7.aitutorandlab.service.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClickAdPolicyTest {

    @Test
    fun firstFiveClicksPerDay_areAdFree() {
        for (clicks in 0 until ClickAdPolicy.FREE_CLICKS_PER_DAY) {
            assertFalse(ClickAdPolicy.shouldShowAd(clicks))
        }
    }

    @Test
    fun sixthClickOnward_showsAd() {
        assertTrue(ClickAdPolicy.shouldShowAd(ClickAdPolicy.FREE_CLICKS_PER_DAY))
        assertTrue(ClickAdPolicy.shouldShowAd(10))
    }
}
