package com.softcat.mystictarot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TarotCatalogIntegrityTest {
    @Test
    fun standardDeckHas78UniqueStableIds() {
        assertEquals(78, standardTarot78.size)
        assertEquals((0 until 78).toList(), standardTarot78.map { it.id })
        assertEquals(78, standardTarot78.map { it.id }.distinct().size)
    }

    @Test
    fun standardDeckHasExpectedArcanaCounts() {
        assertEquals(22, standardTarot78.count { it.arcana == "Major Arcana" })
        assertEquals(56, standardTarot78.count { it.arcana == "Minor Arcana" })
    }

    @Test
    fun everyMinorSuitHas14Cards() {
        listOf("완드", "컵", "소드", "펜타클").forEach { suit ->
            assertEquals(suit, 14, standardTarot78.count { it.nameKr.startsWith("$suit ") })
        }
    }

    @Test
    fun everyCardHasNamesAndMeaning() {
        standardTarot78.forEach { card ->
            assertTrue("English name for ${card.id}", card.nameEn.isNotBlank())
            assertTrue("Korean name for ${card.id}", card.nameKr.isNotBlank())
            assertTrue("Meaning for ${card.id}", card.basicMeaning.isNotBlank())
        }
    }

    @Test
    fun spreadDefinitionsHaveConsistentCardContracts() {
        assertEquals(spreadOptions.size, spreadOptions.map { it.key }.distinct().size)
        spreadOptions.forEach { spread ->
            assertTrue("Positive card count for ${spread.key}", spread.cardCount > 0)
            assertEquals("Position labels for ${spread.key}", spread.cardCount, spread.positionLabels.size)
            assertTrue("Slots for ${spread.key}", spreadSlots(spread, spread.cardCount).size >= spread.cardCount)
            assertFalse("Blank label for ${spread.key}", spread.positionLabels.any { it.isBlank() })
        }
    }
}
