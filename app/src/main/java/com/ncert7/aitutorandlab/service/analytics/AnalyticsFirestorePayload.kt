package com.ncert7.aitutorandlab.service.analytics

import com.ncert7.aitutorandlab.config.AppConfig
import com.ncert7.aitutorandlab.data.local.entities.AppAnalyticsEntity

/**
 * Builds Firestore payloads for analytics events.
 */
object AnalyticsFirestorePayload {

    fun build(
        analytics: AppAnalyticsEntity,
        studentId: String,
        syncedAt: Long = System.currentTimeMillis()
    ): Map<String, Any?> {
        val data = linkedMapOf<String, Any?>(
            "analyticsId" to analytics.analyticsId,
            "studentId" to studentId,
            "sessionId" to analytics.sessionId,
            "screenName" to analytics.screenName,
            "eventType" to analytics.eventType,
            "entryTime" to analytics.entryTime,
            "exitTime" to analytics.exitTime,
            "durationMillis" to analytics.durationMillis,
            "appName" to analytics.appName.ifBlank { AppConfig.APP_NAME },
            "syncedAt" to syncedAt
        )
        analytics.conceptId?.let { data["conceptId"] = it }
        analytics.source?.let { data["source"] = it }
        analytics.interactionType?.let { data["interactionType"] = it }
        return data
    }
}
