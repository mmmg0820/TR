package com.softcat.mystictarot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugReadingFixturesTest {
    private val deck = TarotDeck(
        id = "standard",
        name = "test",
        cards = (0 until 12).map { id ->
            TarotCard(id, "Card $id", "카드 $id", "Test", "meaning")
        }
    )
    private val spread = SpreadOption(
        key = "three",
        title = "3장",
        subtitle = "",
        cardCount = 3,
        layoutId = "three_row",
        layoutTitle = "3장",
        layoutSubtitle = "",
        positionPresetTitle = "흐름",
        positionLabels = listOf("과거", "현재", "미래")
    )

    @Test
    fun `release build exposes no fixtures`() {
        assertTrue(createDebugReadingFixtures(false, deck, spread, now = 1_000L).isEmpty())
    }

    @Test
    fun `debug fixtures are deterministic and restorable`() {
        val fixtures = createDebugReadingFixtures(true, deck, spread, now = 1_000L)

        assertEquals(DebugReadingFixtureId.entries, fixtures.map { it.id })
        assertEquals(240, fixtures.first().draft.question.length)
        assertEquals(2, fixtures[1].draft.selectedCardIds.size)
        assertEquals(3, fixtures.last().draft.drawnCards.size)
        fixtures.forEach { fixture ->
            assertTrue(fixture.draft.isRestorable(deck, now = 1_000L))
        }
    }
}
