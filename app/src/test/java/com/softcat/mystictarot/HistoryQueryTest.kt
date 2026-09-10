package com.softcat.mystictarot

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryQueryTest {
    private fun reading(
        id: Long,
        savedAt: Long,
        question: String,
        spread: String,
        card: String,
        pinned: Boolean = false
    ) = SavedReading(
        id = id,
        savedAt = savedAt,
        title = "기록 $id",
        spreadTitle = spread,
        question = question,
        isPinned = pinned,
        cards = listOf(SavedReadingCard(1, card, "Card $id", "정방향"))
    )

    private val readings = listOf(
        reading(1, 100, "이직을 해도 될까요", "3장 배열", "태양"),
        reading(2, 200, "관계의 흐름", "미니 켈틱", "달", pinned = true),
        reading(3, 300, "오늘의 선택", "1장 배열", "별")
    )

    @Test
    fun `pinned readings sort first then by newest`() {
        assertEquals(listOf(2L, 3L, 1L), savedReadingsForDisplay(readings, "", false).map { it.id })
    }

    @Test
    fun `search covers questions spreads and card names`() {
        assertEquals(listOf(1L), savedReadingsForDisplay(readings, "이직", false).map { it.id })
        assertEquals(listOf(2L), savedReadingsForDisplay(readings, "켈틱", false).map { it.id })
        assertEquals(listOf(3L), savedReadingsForDisplay(readings, "별", false).map { it.id })
    }

    @Test
    fun `pinned filter excludes unpinned results`() {
        assertEquals(listOf(2L), savedReadingsForDisplay(readings, "", true).map { it.id })
        assertEquals(emptyList<Long>(), savedReadingsForDisplay(readings, "이직", true).map { it.id })
    }
}
