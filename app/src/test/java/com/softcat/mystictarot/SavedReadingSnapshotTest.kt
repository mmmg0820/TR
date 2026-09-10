package com.softcat.mystictarot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedReadingSnapshotTest {
    @Test
    fun newReadingKeepsSpreadIdentityAndGeometry() {
        val spread = spreadOptions.first { it.layoutId == "mini_celtic" }
        val cards = standardTarot78.take(spread.cardCount).mapIndexed { index, card ->
            DrawnCard(card = card, direction = CardDirection.Upright, order = index + 1)
        }

        val saved = buildSavedReading(
            spread = spread,
            question = "지금의 흐름은 어떤가요?",
            drawnCards = cards,
            deck = TarotDeck(id = "standard", name = "유니버셜 타로", cards = standardTarot78)
        )

        assertEquals(SAVED_READING_SCHEMA_VERSION, saved.schemaVersion)
        assertEquals(spread.key, saved.spreadKeySnapshot)
        assertEquals(spread.layoutId, saved.layoutIdSnapshot)
        assertEquals(spread.drawMode, saved.drawModeSnapshot)
        assertEquals(cards.size, saved.spreadSlotsSnapshot.size)
        assertTrue(saved.spreadSlotsSnapshot.any { it.rotation != 0f })
    }

    @Test
    fun legacyReadingDefaultsKeepFallbackAvailable() {
        val reading = SavedReading(
            id = 1L,
            savedAt = 1L,
            title = "기존 기록",
            spreadTitle = "3장 배열",
            cards = emptyList()
        )

        assertEquals("", reading.spreadKeySnapshot)
        assertEquals("", reading.layoutIdSnapshot)
        assertEquals(SpreadDrawMode.Normal, reading.drawModeSnapshot)
        assertTrue(reading.spreadSlotsSnapshot.isEmpty())
    }
}
