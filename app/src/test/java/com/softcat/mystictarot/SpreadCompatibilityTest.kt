package com.softcat.mystictarot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpreadCompatibilityTest {
    @Test
    fun positionDropdownKeepsCurrentLayout() {
        val selected = spreadOptions.first { it.layoutId == "five_cross" }

        val compatible = compatiblePositionPresets(selected)

        assertTrue(compatible.isNotEmpty())
        assertTrue(compatible.all { it.layoutId == selected.layoutId })
        assertTrue(compatible.all { it.cardCount == selected.cardCount })
    }

    @Test
    fun hiddenSpreadsCannotReappearAsPositionPreset() {
        val selected = spreadOptions.first { it.layoutId == "five_cross" }
        val compatible = compatiblePositionPresets(selected)

        assertFalse(compatible.any { it.key in hiddenFromNewSpreadSelectionKeys })
    }

    @Test
    fun duplicateKeysAreCollapsed() {
        val selected = spreadOptions.first { it.layoutId == "three_cards" }

        val compatible = compatiblePositionPresets(selected, listOf(selected, selected))

        assertEquals(1, compatible.size)
    }

    @Test
    fun removedSpreadsAreAbsentFromNewSelection() {
        assertFalse(selectableSpreadOptions.any { it.key in hiddenFromNewSpreadSelectionKeys })
        assertTrue(hiddenFromNewSpreadSelectionKeys.all { hiddenKey ->
            spreadOptions.any { it.key == hiddenKey }
        })
    }
}
