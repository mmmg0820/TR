package com.softcat.mystictarot

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import kotlin.math.ceil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomActionSpecTest {
    @Test
    fun approvedActionControlDimensionsStayStable() {
        assertEquals(44.dp, HoscatBottomCtaVisualHeight)
        assertEquals(48.dp, HoscatBottomCtaMinTouchHeight)
        assertEquals(8.dp, HoscatActionControlRadius)
        assertEquals(40.dp, HoscatStandardActionVisualHeight)
        assertEquals(8.dp, HoscatStandardActionRadius)
        assertEquals(4.dp, HoscatStandardActionTouchVerticalInset)
        assertEquals(38.dp, HoscatCompactActionVisualHeight)
        assertEquals(8.dp, HoscatCompactActionRadius)
        assertEquals(5.dp, HoscatCompactActionTouchVerticalInset)
        assertEquals(0.dp, HoscatActionControlVisualOffset)
        assertEquals(2.dp, HoscatActionControlVisualBottomInset)
        assertEquals(6.dp, HoscatActionStackBottomGap)
        assertEquals(4.dp, HoscatBottomActionLayoutSpacing)
    }

    @Test
    fun visualControlFitsInsideTouchArea() {
        assertTrue(HoscatBottomCtaVisualHeight <= HoscatBottomCtaMinTouchHeight)
        assertTrue(HoscatActionControlRadius * 2 <= HoscatBottomCtaVisualHeight)
    }

    @Test
    fun visibleActionGapsRemainEightDp() {
        val surfaceToSurfaceGap =
            HoscatBottomCtaTouchVerticalInset +
                HoscatBottomActionLayoutSpacing +
                HoscatBottomCtaTouchVerticalInset
        val lastSurfaceToDockGap = HoscatBottomCtaTouchVerticalInset + HoscatActionStackBottomGap

        assertEquals(8.dp, surfaceToSurfaceGap)
        assertEquals(8.dp, lastSurfaceToDockGap)
    }

    @Test
    fun pixelRoundingCannotReduceVisibleGapBelowEightDp() {
        for (dpi in listOf(160, 240, 320, 400, 420, 440, 480, 560, 640)) {
            with(Density(dpi / 160f)) {
                for (inset in listOf(0.dp, 24.dp, 48.dp)) {
                    val dockReserve = inset.roundToPx() +
                        HoscatBottomNavVisualHeight.roundToPx() +
                        HoscatBottomNavOuterVerticalPadding.roundToPx()
                    val actualGap = hoscatActionStackBottomPaddingPx(inset) -
                        dockReserve + HoscatBottomCtaTouchVerticalInset.roundToPx()
                    val requiredGap = ceil(8.dp.toPx()).toInt()
                    assertEquals("dpi=$dpi inset=$inset", requiredGap, actualGap)
                    assertEquals(requiredGap, hoscatActionStackSpacingPx() +
                        HoscatBottomCtaTouchVerticalInset.roundToPx() * 2)
                }
            }
        }
    }
}
