package com.softcat.mystictarot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.softcat.mystictarot.ui.theme.MyangTarotTheme
import com.softcat.mystictarot.ui.theme.ThemeChoice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.math.ceil
import kotlin.math.floor

class BottomActionLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun twoColumnSecondaryActionsShareExpandedVisualHeight() {
        composeRule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 2f)) {
                MyangTarotTheme(ThemeChoice.HoscatSignature) {
                    Box(Modifier.size(width = 280.dp, height = 200.dp)) {
                        HoscatBottomSecondaryActionRow(
                            first = {
                                HoscatBottomSecondaryAction("카드별 설명", {},
                                    Modifier.weight(1f).fillMaxHeight())
                            },
                            second = {
                                HoscatBottomSecondaryAction("AI 해석", {},
                                    Modifier.weight(1f).fillMaxHeight())
                            }
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        val surfaces = composeRule.onAllNodesWithTag("hoscat-action-surface", useUnmergedTree = true)
            .fetchSemanticsNodes().map { it.boundsInRoot }
        assertEquals(2, surfaces.size)
        assertEquals(surfaces[0].top, surfaces[1].top, 0f)
        assertEquals(surfaces[0].bottom, surfaces[1].bottom, 0f)
    }

    @Test
    fun adaptiveThreeActionStackKeepsVisualAndContentGaps() {
        var testDensity by mutableStateOf(2.5f)
        var testFontScale by mutableStateOf(1f)
        var actionRows by mutableStateOf(1)
        var navigationInset by mutableStateOf(0.dp)
        val report = mutableListOf("density,font_scale,rows,navigation_inset_px,surface_bottom,dock_top,gap_px,gap_dp,content_gap_dp")
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(testDensity, testFontScale)) {
                MyangTarotTheme(ThemeChoice.HoscatSignature) {
                    var measuredActionHeight by remember(actionRows) {
                        mutableStateOf(hoscatInitialActionStackHeight(actionRows))
                    }
                    val actionBottomPadding = hoscatActionStackBottomPadding(navigationInset)
                    Box(Modifier.size(width = 360.dp, height = 800.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    bottom = hoscatContentBottomPaddingForActionStack(
                                        actionStackHeight = measuredActionHeight,
                                        actionStackBottomPadding = actionBottomPadding
                                    )
                                )
                        ) {
                            Spacer(
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .testTag("last-content")
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(bottom = navigationInset)
                                .padding(bottom = HoscatBottomNavOuterVerticalPadding)
                        ) {
                            Spacer(
                                Modifier
                                    .fillMaxWidth()
                                    .height(HoscatBottomNavVisualHeight)
                                    .testTag("bottom-dock")
                            )
                        }
                        HoscatBottomActionStack(
                            bottomPadding = navigationInset,
                            onMeasuredHeightChanged = { measuredActionHeight = it }
                        ) {
                            repeat(actionRows) { index ->
                                HoscatBottomCta("${index + 1}번째 긴 하단 작업 버튼", onClick = {})
                            }
                        }
                    }
                }
            }
        }

        val evidenceDirectory = File(
            InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null),
            "action-geometry-audit"
        ).apply { mkdirs() }
        for (density in listOf(2.5f, 2.625f, 3f, 3.5f)) {
          for (fontScale in listOf(1f, 2f)) {
           for (inset in listOf(0.dp, 24.dp)) {
            for (rows in 1..3) {
        composeRule.runOnIdle {
            testDensity = density
            testFontScale = fontScale
            navigationInset = inset
            actionRows = rows
        }
        composeRule.waitForIdle()
        val surfaces = composeRule.onAllNodesWithTag("hoscat-action-surface", useUnmergedTree = true)
            .fetchSemanticsNodes()
            .map { it.boundsInRoot }
            .sortedBy { it.top }
        assertEquals(rows, surfaces.size)
        surfaces.zipWithNext().forEach { (upper, lower) ->
            assertGap(lower.top - upper.bottom, density, "stack")
        }

        val dock = composeRule.onNodeWithTag("bottom-dock").fetchSemanticsNode().boundsInRoot
        val gap = dock.top - surfaces.last().bottom
        assertGap(gap, density, "dock")

        val lastContent = composeRule.onNodeWithTag("last-content").fetchSemanticsNode().boundsInRoot
        val contentGap = (surfaces.first().top - lastContent.bottom) / density
        assertTrue("content gap was $contentGap dp", contentGap >= HoscatContentToActionGap.value)
        report += listOf(density, fontScale, rows, inset.value * density,
            surfaces.last().bottom, dock.top, gap, gap / density, contentGap).joinToString(",")
        if (rows == 3 && inset == 24.dp) {
            File(evidenceDirectory, "stack-density-$density-font-$fontScale.png").outputStream().use {
                composeRule.onRoot(useUnmergedTree = true).captureToImage().asAndroidBitmap()
                    .compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
            }
           }
          }
        }
        File(evidenceDirectory, "bounds.csv").writeText(report.joinToString("\n"))
    }

    private fun assertGap(gapPx: Float, density: Float, label: String) {
        val minimum = ceil(8f * density)
        val maximum = floor(10f * density)
        assertTrue("$label gap=$gapPx px density=$density min=$minimum",
            gapPx >= minimum)
        assertTrue("$label gap=$gapPx px density=$density max=$maximum",
            gapPx <= maximum)
    }
}
