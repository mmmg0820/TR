package com.softcat.mystictarot

enum class DebugReadingFixtureId {
    LongQuestion,
    SelectionAlmostComplete,
    CompletedResult
}

data class DebugReadingFixture(
    val id: DebugReadingFixtureId,
    val label: String,
    val draft: ReadingDraft
)

fun createDebugReadingFixtures(
    debuggable: Boolean,
    deck: TarotDeck,
    spread: SpreadOption,
    now: Long = System.currentTimeMillis()
): List<DebugReadingFixture> {
    if (!debuggable || deck.cards.size < spread.cardCount || spread.cardCount <= 0) return emptyList()

    val shuffledIds = deck.cards.map { it.id }
    val selected = deck.cards.take(spread.cardCount)
    val longQuestionSeed = "이 선택이 앞으로의 관계와 일, 생활에 어떤 영향을 줄지 현실적으로 살펴보고 싶어요. "
    val longQuestion = buildString {
        while (length < 240) append(longQuestionSeed)
    }.take(240)

    fun draft(
        screen: AppScreen,
        question: String,
        selectedCards: List<TarotCard> = emptyList(),
        drawnCards: List<DrawnCard> = emptyList()
    ) = ReadingDraft(
        updatedAt = now,
        screen = screen,
        deckId = deck.id,
        spread = spread,
        question = question,
        shuffledCardIds = shuffledIds,
        selectedCardIds = selectedCards.map { it.id },
        finalCandidateCardIds = emptyList(),
        finalOneSecondStep = false,
        drawnCards = drawnCards.map { ReadingDraftCard(it.card.id, it.direction, it.order) }
    )

    return listOf(
        DebugReadingFixture(
            id = DebugReadingFixtureId.LongQuestion,
            label = "240자 질문 입력",
            draft = draft(AppScreen.Question, longQuestion)
        ),
        DebugReadingFixture(
            id = DebugReadingFixtureId.SelectionAlmostComplete,
            label = "카드 선택 직전",
            draft = draft(
                screen = AppScreen.DeckPick,
                question = "마지막 카드를 선택하기 전 상태를 확인합니다.",
                selectedCards = selected.dropLast(1)
            )
        ),
        DebugReadingFixture(
            id = DebugReadingFixtureId.CompletedResult,
            label = "결과와 저장 직전",
            draft = draft(
                screen = AppScreen.SpreadResult,
                question = "완료된 결과 화면과 저장 동작을 확인합니다.",
                drawnCards = selected.mapIndexed { index, card ->
                    DrawnCard(
                        card = card,
                        direction = if (index % 2 == 0) CardDirection.Upright else CardDirection.Reversed,
                        order = index + 1
                    )
                }
            )
        )
    )
}
