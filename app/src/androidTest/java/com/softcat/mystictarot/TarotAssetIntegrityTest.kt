package com.softcat.mystictarot

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.softcat.mystictarot.ui.components.tarotDrawableResId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TarotAssetIntegrityTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun allStandardCardsHaveDecodableImages() {
        standardTarot78.forEach { card ->
            val resId = tarotDrawableResId(card.id)
            assertNotNull("Drawable mapping for ${card.id} ${card.nameEn}", resId)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, resId!!, bounds)
            assertTrue("Image width for ${card.id}", bounds.outWidth > 0)
            assertTrue("Image height for ${card.id}", bounds.outHeight > 0)
        }
    }

    @Test
    fun bundledCardJsonLoadsCompleteDeck() {
        val cards = MyangTarotRepository(context).loadStandardTarotCards(standardTarot78)

        assertEquals(78, cards.size)
        assertEquals((0 until 78).toList(), cards.map { it.id })
        assertTrue(cards.all { it.nameKr.isNotBlank() && it.nameEn.isNotBlank() })
        assertTrue(cards.all { it.basicMeaning.isNotBlank() })
    }
}
