package com.softcat.mystictarot

const val READING_DRAFT_SCHEMA_VERSION = 1
internal const val READING_DRAFT_MAX_AGE_MILLIS = 7L * 24L * 60L * 60L * 1_000L

data class ReadingDraftCard(
    val cardId: Int,
    val direction: CardDirection,
    val order: Int
)

data class ReadingDraft(
    val schemaVersion: Int = READING_DRAFT_SCHEMA_VERSION,
    val updatedAt: Long,
    val screen: AppScreen,
    val deckId: String,
    val spread: SpreadOption,
    val question: String,
    val shuffledCardIds: List<Int>,
    val selectedCardIds: List<Int>,
    val finalCandidateCardIds: List<Int>,
    val finalOneSecondStep: Boolean,
    val drawnCards: List<ReadingDraftCard>
)

internal fun ReadingDraft.isRestorable(
    deck: TarotDeck,
    now: Long = System.currentTimeMillis()
): Boolean {
    if (schemaVersion != READING_DRAFT_SCHEMA_VERSION || deckId != deck.id) return false
    if (updatedAt > now + 5L * 60L * 1_000L || now - updatedAt > READING_DRAFT_MAX_AGE_MILLIS) return false
    if (screen !in setOf(AppScreen.Question, AppScreen.DeckPick, AppScreen.SpreadResult)) return false
    if (spread.cardCount <= 0 || spread.positionLabels.size < spread.cardCount) return false
    if (question.length > 240) return false

    val deckIds = deck.cards.mapTo(hashSetOf()) { it.id }
    fun List<Int>.containsOnlyKnownUniqueCards(): Boolean =
        size == distinct().size && all(deckIds::contains)

    if (!selectedCardIds.containsOnlyKnownUniqueCards()) return false
    if (!finalCandidateCardIds.containsOnlyKnownUniqueCards()) return false
    if (shuffledCardIds.isNotEmpty() && !shuffledCardIds.containsOnlyKnownUniqueCards()) return false
    if (drawnCards.map { it.cardId }.let { it.size != it.distinct().size || !it.all(deckIds::contains) }) return false
    if (drawnCards.map { it.order }.sorted() != (1..drawnCards.size).toList()) return false

    val targetCount = if (spread.drawMode == SpreadDrawMode.FinalOneFromTen && finalOneSecondStep) 1 else spread.cardCount
    if (selectedCardIds.size > targetCount) return false
    if (screen == AppScreen.SpreadResult && drawnCards.size != spread.cardCount) return false
    return true
}
