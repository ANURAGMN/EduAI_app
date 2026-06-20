package com.ncert7.aitutorandlab.service.sync

import com.ncert7.aitutorandlab.config.AppConfig
import com.ncert7.aitutorandlab.data.local.entities.ProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreSyncUtilsTest {

    @Test
    fun studentAppDocId_prefixesAppName() {
        assertEquals(
            "${AppConfig.APP_NAME}_user@example.com",
            FirestoreSyncUtils.studentAppDocId("user@example.com")
        )
    }

    @Test
    fun progressRecordDocId_includesNormalizedLanguage() {
        assertEquals(
            "SIMULATION_concept123_en",
            FirestoreSyncUtils.progressRecordDocId("SIMULATION", "concept123", "English")
        )
        assertEquals(
            "SIMULATION_concept123_kn",
            FirestoreSyncUtils.progressRecordDocId("SIMULATION", "concept123", "Kannada")
        )
        assertEquals(
            "MATH_AGENT_prob1_en",
            FirestoreSyncUtils.progressRecordDocId("MATH_AGENT", "prob1", "en")
        )
    }

    @Test
    fun progressRecordDocId_enAndKnDoNotCollide() {
        val en = FirestoreSyncUtils.progressRecordDocId("SIMULATION", "c1", "en")
        val kn = FirestoreSyncUtils.progressRecordDocId("SIMULATION", "c1", "kn")
        assertTrue(en != kn)
    }

    @Test
    fun progressRecordPayload_includesLanguage() {
        val progress = ProgressEntity(
            studentId = "user@test.com",
            itemType = "SIMULATION",
            itemId = "sim1",
            status = "COMPLETED",
            progressPercentage = 100,
            language = "Kannada",
            appName = AppConfig.APP_NAME
        )
        val payload = FirestoreSyncUtils.progressRecordPayload(progress, syncedAt = 1L)
        assertEquals("kn", payload["language"])
        assertEquals(AppConfig.APP_NAME, payload["appName"])
        assertEquals("SIMULATION", payload["itemType"])
    }

    @Test
    fun shouldRestoreProgressRecord_respectsLookbackWindow() {
        val now = 1_000_000L
        val lookback = 10_000L

        assertTrue(
            FirestoreSyncUtils.shouldRestoreProgressRecord(
                lastAccessedAt = now - 5_000L,
                completedAt = null,
                updatedAt = 0L,
                now = now,
                lookbackMs = lookback
            )
        )
        assertTrue(
            FirestoreSyncUtils.shouldRestoreProgressRecord(
                lastAccessedAt = 0L,
                completedAt = now - 1_000L,
                updatedAt = 0L,
                now = now,
                lookbackMs = lookback
            )
        )
        assertFalse(
            FirestoreSyncUtils.shouldRestoreProgressRecord(
                lastAccessedAt = now - 20_000L,
                completedAt = now - 20_000L,
                updatedAt = now - 20_000L,
                now = now,
                lookbackMs = lookback
            )
        )
    }
}
