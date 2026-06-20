package com.ncert7.aitutorandlab.service.ads

/**
 * Banner ads after [FREE_CLICKS_PER_DAY] tracked clicks per calendar day.
 * Counts all analytics CLICK events (content + simulation). The 6th click onward shows an ad.
 */
object ClickAdPolicy {
    const val FREE_CLICKS_PER_DAY = 5

    fun shouldShowAd(clicksAlreadyToday: Int): Boolean {
        return clicksAlreadyToday >= FREE_CLICKS_PER_DAY
    }
}

/** @deprecated Use [ClickAdPolicy] */
typealias SimulationAdPolicy = ClickAdPolicy
