package com.softcat.mystictarot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PrivacySafeDiagnosticsTest {
    @Test
    fun `diagnostic JSON contains only approved keys`() {
        val report = PrivacySafeDiagnostics(
            generatedAt = 1L,
            packageName = "com.softcat.mystictarot",
            versionName = "1.0",
            versionCode = 1L,
            debuggable = true,
            sdkInt = 35,
            manufacturer = "manufacturer",
            model = "model",
            densityDpi = 420,
            screenWidthDp = 411,
            screenHeightDp = 891,
            fontScale = 1.3f,
            languageTag = "ko-KR",
            screen = AppScreen.Info.name,
            theme = "HoscatSignature",
            useReversed = true,
            hapticsEnabled = true,
            adsDisabled = false,
            cardBackStyle = CardBackStyle.ThemeDefault.name,
            hasCustomCardBack = true,
            customDeckCount = 2,
            enabledDeckCount = 2,
            savedReadingCount = 9,
            savedSpreadCount = 3,
            hasReadingDraft = true
        )

        val fields = report.toSafeFieldMap()
        assertEquals(PRIVACY_SAFE_DIAGNOSTIC_KEYS, fields.keys)
        val rendered = fields.toString()
        listOf("question", "cardId", "cardName", "imageUri", "aiPrompt", "purchaseCode").forEach {
            assertFalse(rendered.contains(it, ignoreCase = true))
        }
    }
}
