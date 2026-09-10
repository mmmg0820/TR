package com.softcat.mystictarot

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingDraftTest {
    private val deck = TarotDeck(
        id = "standard",
        name = "테스트 덱",
        cards = List(10) { id ->
            TarotCard(id, "Card $id", "카드 $id", "Test", "Meaning $id")
        }
    )
    private val spread = SpreadOption(
        key = "three",
        title = "3장 배열",
        subtitle = "",
        cardCount = 3,
        layoutId = "three_cards",
        layoutTitle = "3장 배열",
        layoutSubtitle = "",
        positionPresetTitle = "흐름",
        positionLabels = listOf("과거", "현재", "미래")
    )

    @Test
    fun validQuestionDraftIsRestorable() {
        assertTrue(validDraft().isRestorable(deck, now = 10_000L))
    }

    @Test
    fun expiredOrUnknownCardDraftIsRejected() {
        assertFalse(validDraft(updatedAt = 1L).isRestorable(deck, now = READING_DRAFT_MAX_AGE_MILLIS + 2L))
        assertFalse(validDraft(selectedCardIds = listOf(999)).isRestorable(deck, now = 10_000L))
    }

    @Test
    fun resultDraftRequiresEverySpreadCard() {
        val incomplete = validDraft(
            screen = AppScreen.SpreadResult,
            drawnCards = listOf(ReadingDraftCard(0, CardDirection.Upright, 1))
        )

        assertFalse(incomplete.isRestorable(deck, now = 10_000L))
        assertTrue(
            incomplete.copy(
                drawnCards = listOf(
                    ReadingDraftCard(0, CardDirection.Upright, 1),
                    ReadingDraftCard(1, CardDirection.Reversed, 2),
                    ReadingDraftCard(2, CardDirection.Upright, 3)
                )
            ).isRestorable(deck, now = 10_000L)
        )
    }

    private fun validDraft(
        updatedAt: Long = 9_000L,
        screen: AppScreen = AppScreen.Question,
        selectedCardIds: List<Int> = emptyList(),
        drawnCards: List<ReadingDraftCard> = emptyList()
    ) = ReadingDraft(
        updatedAt = updatedAt,
        screen = screen,
        deckId = deck.id,
        spread = spread,
        question = "앞으로의 흐름은 어떤가요?",
        shuffledCardIds = deck.cards.map { it.id },
        selectedCardIds = selectedCardIds,
        finalCandidateCardIds = emptyList(),
        finalOneSecondStep = false,
        drawnCards = drawnCards
    )
}
