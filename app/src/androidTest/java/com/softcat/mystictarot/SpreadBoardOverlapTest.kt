package com.softcat.mystictarot

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.softcat.mystictarot.ui.theme.MyangTarotTheme
import com.softcat.mystictarot.ui.theme.ThemeChoice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class SpreadBoardOverlapTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun everySelectableSpreadKeepsCardsAndLabelsSeparate() {
        val evidenceDirectory = File(
            InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null),
            "spread-overlap-audit"
        ).apply {
            deleteRecursively()
            mkdirs()
        }
        val boundsReport = mutableListOf(
            "spread_key,font_scale,type,index,left,top,right,bottom,overlap_result"
        )
        var activeSpread by mutableStateOf(selectableSpreadOptions.first())
        var activeFontScale by mutableFloatStateOf(1f)
        composeRule.setContent {
            val deviceDensity = LocalDensity.current.density
            val cards = standardTarot78.take(activeSpread.cardCount).mapIndexed { index, card ->
                DrawnCard(card, CardDirection.Upright, index + 1)
            }
            CompositionLocalProvider(LocalDensity provides Density(deviceDensity, activeFontScale)) {
                MyangTarotTheme(ThemeChoice.HoscatSignature) {
                    Box(Modifier.size(width = 328.dp, height = 400.dp)) {
                        SpreadBoardLayout(
                            modifier = Modifier.matchParentSize(),
                            spread = activeSpread,
                            drawnCards = cards,
                            onCardClick = {}
                        )
                    }
                }
            }
        }
        listOf(1f, 1.3f, 1.5f, 2f).forEach { fontScale ->
            selectableSpreadOptions.forEach { spread ->
                composeRule.runOnIdle {
                    activeSpread = spread
                    activeFontScale = fontScale
                }
                verifySpread(spread, fontScale, evidenceDirectory, boundsReport)
            }
        }
        File(evidenceDirectory, "bounds.csv").writeText(boundsReport.joinToString("\n"))
        File(evidenceDirectory, "summary.txt").writeText(
            "PASS: ${selectableSpreadOptions.size} selectable spreads x 4 font scales; card-label and label-label overlap 0\n"
        )
    }

    private fun verifySpread(
        spread: SpreadOption,
        fontScale: Float,
        evidenceDirectory: File,
        boundsReport: MutableList<String>
    ) {
        val cards = standardTarot78.take(spread.cardCount).mapIndexed { index, card ->
            DrawnCard(card, CardDirection.Upright, index + 1)
        }
        composeRule.waitForIdle()

        val cardBounds = cards.map { drawnCard ->
            val expectedPrefix = "${drawnCard.card.nameKr}, ${drawnCard.direction.label}"
            composeRule.onAllNodesWithContentDescription(
                expectedPrefix,
                substring = true,
                useUnmergedTree = true
            ).fetchSemanticsNodes().single { node ->
                node.config[SemanticsProperties.ContentDescription].any { description ->
                    description == expectedPrefix || description.startsWith("$expectedPrefix,")
                }
            }.boundsInRoot
        }
        val labelBounds = cards.map { drawnCard ->
            val description = when {
                spread.layoutId in setOf("mini_celtic", "celtic_cross") && drawnCard.order == 1 ->
                    "세로 위치 의미: ${positionMeaning(spread, drawnCard.order)}"
                spread.layoutId in setOf("mini_celtic", "celtic_cross") && drawnCard.order == 2 ->
                    "가로 위치 의미: ${positionMeaning(spread, drawnCard.order)}"
                else -> "위치 의미: ${positionMeaning(spread, drawnCard.order)}"
            }
            composeRule.onNodeWithContentDescription(description, useUnmergedTree = true)
                .fetchSemanticsNode().boundsInRoot
        }

        cardBounds.forEachIndexed { cardIndex, cardRect ->
            labelBounds.forEachIndexed { labelIndex, labelRect ->
                assertEquals(
                    "${spread.key} font=$fontScale card=${cardIndex + 1} label=${labelIndex + 1}",
                    0f,
                    overlapArea(cardRect, labelRect),
                    0.01f
                )
            }
            boundsReport += spreadBoundsLine(spread, fontScale, "card", cardIndex, cardRect)
        }
        labelBounds.forEachIndexed { firstIndex, first ->
            labelBounds.drop(firstIndex + 1).forEachIndexed { offset, second ->
                assertEquals(
                    "${spread.key} font=$fontScale labels=${firstIndex + 1}/${firstIndex + offset + 2}",
                    0f,
                    overlapArea(first, second),
                    0.01f
                )
            }
            boundsReport += spreadBoundsLine(spread, fontScale, "label", firstIndex, first)
        }
        cardBounds.forEachIndexed { index, bounds ->
            assertTrue("${spread.key} font=$fontScale card=${index + 1} width", bounds.width > 0f)
            assertTrue("${spread.key} font=$fontScale card=${index + 1} height", bounds.height > 0f)
        }
        if (fontScale == 1f) {
            val fileName = spread.key.replace(':', '_') + ".png"
            FileOutputStream(File(evidenceDirectory, fileName)).use { output ->
                composeRule.onRoot(useUnmergedTree = true).captureToImage().asAndroidBitmap()
                    .compress(Bitmap.CompressFormat.PNG, 100, output)
            }
        }
    }

    private fun spreadBoundsLine(
        spread: SpreadOption,
        fontScale: Float,
        type: String,
        index: Int,
        bounds: Rect
    ): String = listOf(
        spread.key,
        fontScale,
        type,
        index + 1,
        bounds.left,
        bounds.top,
        bounds.right,
        bounds.bottom,
        "PASS"
    ).joinToString(",")

    private fun overlapArea(first: Rect, second: Rect): Float {
        val width = (minOf(first.right, second.right) - maxOf(first.left, second.left)).coerceAtLeast(0f)
        val height = (minOf(first.bottom, second.bottom) - maxOf(first.top, second.top)).coerceAtLeast(0f)
        return width * height
    }
}
