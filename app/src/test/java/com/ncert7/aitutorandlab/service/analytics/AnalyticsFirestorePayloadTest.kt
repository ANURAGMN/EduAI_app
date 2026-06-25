package com.ncert7.aitutorandlab.service.analytics

import com.ncert7.aitutorandlab.data.local.entities.AppAnalyticsEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class AnalyticsFirestorePayloadTest {

    @Test
    fun build_includesSimulationMetadataWhenPresent() {
        val entity = AppAnalyticsEntity(
            analyticsId = 42L,
            studentId = "student-1",
            sessionId = "session-1",
            screenName = "SIMULATION",
            eventType = "CLICK",
            entryTime = 1_000L,
            conceptId = "concept-abc",
            source = "HOME",
            interactionType = "URL",
            appName = "eduai_app"
        )

        val payload = AnalyticsFirestorePayload.build(entity, "student-1", syncedAt = 2_000L)

        assertEquals(42L, payload["analyticsId"])
        assertEquals("concept-abc", payload["conceptId"])
        assertEquals("HOME", payload["source"])
        assertEquals("URL", payload["interactionType"])
        assertEquals(2_000L, payload["syncedAt"])
    }

    @Test
    fun build_includesFunnelStepMetadata() {
        val entity = AppAnalyticsEntity(
            analyticsId = 99L,
            sessionId = "session-1",
            screenName = "FUNNEL",
            eventType = "FUNNEL",
            entryTime = 1_000L,
            conceptId = "gmail_tap",
            interactionType = "gmail_tap",
            appName = "eduai_app"
        )

        val payload = AnalyticsFirestorePayload.build(entity, "student-1")

        assertEquals("FUNNEL", payload["eventType"])
        assertEquals("gmail_tap", payload["interactionType"])
        assertEquals("gmail_tap", payload["conceptId"])
    }

    @Test
    fun build_omitsOptionalFieldsWhenNull() {
        val entity = AppAnalyticsEntity(
            analyticsId = 1L,
            sessionId = "session-1",
            screenName = "HOME",
            eventType = "ENTRY",
            entryTime = 1_000L
        )

        val payload = AnalyticsFirestorePayload.build(entity, "student-1")

        assertNull(payload["conceptId"])
        assertNull(payload["source"])
        assertNull(payload["interactionType"])
        assertFalse(payload.containsKey("conceptId"))
    }
}
