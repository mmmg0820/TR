package com.softcat.mystictarot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedSpreadFactoryTest {
    private val base = SpreadOption(
        key = "three_cards:past_present_future",
        title = "과거 · 현재 · 미래",
        subtitle = "시간의 흐름",
        cardCount = 3,
        layoutId = "three_cards",
        layoutTitle = "3장 배열",
        layoutSubtitle = "가로 배열",
        positionPresetTitle = "과거 · 현재 · 미래",
        positionLabels = listOf("과거", "현재", "미래")
    )

    @Test
    fun defaultLabelsAreNotDuplicated() {
        assertNull(buildAutoSavedSpreadPreset(base, listOf(base), emptyList(), 100L))
    }

    @Test
    fun customLabelsCreateReusablePreset() {
        val custom = base.copy(positionLabels = listOf("마음", "행동", "결과"))

        val preset = buildAutoSavedSpreadPreset(custom, listOf(base), emptyList(), 100L)

        assertEquals(listOf("마음", "행동", "결과"), preset?.positionLabels)
        assertEquals(base.key, preset?.baseSpreadKey)
        assertTrue(preset?.id?.startsWith("auto-three_cards-") == true)
    }

    @Test
    fun matchingSavedPresetIsNotDuplicated() {
        val custom = base.copy(positionLabels = listOf("마음", "행동", "결과"))
        val existing = buildAutoSavedSpreadPreset(custom, listOf(base), emptyList(), 100L)!!

        assertNull(buildAutoSavedSpreadPreset(custom, listOf(base), listOf(existing), 200L))
    }
}
