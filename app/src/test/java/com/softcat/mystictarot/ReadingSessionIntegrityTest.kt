package com.softcat.mystictarot

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingSessionIntegrityTest {
    private val cards = List(12) { id ->
        TarotCard(
            id = id,
            nameEn = "Card $id",
            nameKr = "카드 $id",
            arcana = "Test",
            basicMeaning = "Meaning $id"
        )
    }

    @Test
    fun shuffleKeepsEveryCardExactlyOnce() {
        val shuffled = shuffleDeckForReading(cards, Random(33))

        assertEquals(cards.size, shuffled.size)
        assertEquals(cards.map { it.id }.toSet(), shuffled.map { it.id }.toSet())
        assertEquals(cards.size, shuffled.map { it.id }.distinct().size)
    }

    @Test
    fun shuffleIsStableForInjectedSeed() {
        val first = shuffleDeckForReading(cards, Random(7)).map { it.id }
        val second = shuffleDeckForReading(cards, Random(7)).map { it.id }

        assertEquals(first, second)
    }

    @Test
    fun matchingResultIsReusedWithoutChangingDirections() {
        val selected = cards.take(3)
        val existing = lockDrawnCards(selected, emptyList(), true, Random(1))

        val locked = lockDrawnCards(selected, existing, true, Random(999))

        assertSame(existing, locked)
        assertEquals(existing.map { it.direction }, locked.map { it.direction })
    }

    @Test
    fun changedSelectionCreatesResultInSelectionOrder() {
        val existing = lockDrawnCards(cards.take(3), emptyList(), false, Random(1))
        val changedSelection = listOf(cards[2], cards[1], cards[4])

        val changed = lockDrawnCards(changedSelection, existing, false, Random(1))

        assertEquals(listOf(2, 1, 4), changed.map { it.card.id })
        assertEquals(listOf(1, 2, 3), changed.map { it.order })
        assertTrue(changed.all { it.direction == CardDirection.Upright })
    }

    @Test
    fun invalidStoredOrderIsNotReused() {
        val selected = cards.take(2)
        val malformed = listOf(
            DrawnCard(selected[0], CardDirection.Upright, 2),
            DrawnCard(selected[1], CardDirection.Reversed, 1)
        )

        val locked = lockDrawnCards(selected, malformed, false, Random(1))

        assertEquals(listOf(1, 2), locked.map { it.order })
        assertTrue(locked.all { it.direction == CardDirection.Upright })
    }
}
