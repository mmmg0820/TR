package com.softcat.mystictarot

import androidx.compose.ui.graphics.Color
import com.softcat.mystictarot.ui.theme.HoscatSignatureColors
import com.softcat.mystictarot.ui.theme.ThemeChoice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HarmonyThemeContractTest {
    @Test
    fun hoscatSignatureUsesApprovedLightPalette() {
        assertFalse(ThemeChoice.HoscatSignature.isDark)
        assertEquals(Color(0xFFFAFBFA), HoscatSignatureColors.appBackground)
        assertEquals(Color(0xFFFFFFFF), HoscatSignatureColors.surfacePrimary)
        assertEquals(Color(0xFFF0F3F1), HoscatSignatureColors.surfaceSecondary)
        assertEquals(Color(0xFF151B18), HoscatSignatureColors.textPrimary)
        assertEquals(Color(0xFF536159), HoscatSignatureColors.textSecondary)
        assertEquals(Color(0xFF6E7872), HoscatSignatureColors.textTertiary)
        assertEquals(Color(0xFFDCE2DE), HoscatSignatureColors.divider)
        assertEquals(Color(0xFF7B2345), HoscatSignatureColors.primaryBlue)
        assertEquals(Color(0xFFAE2638), HoscatSignatureColors.warningRed)
        assertEquals(Color(0xFF226349), HoscatSignatureColors.successGreen)
    }
}
