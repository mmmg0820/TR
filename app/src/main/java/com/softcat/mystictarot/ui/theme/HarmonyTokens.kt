package com.softcat.mystictarot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeChoice(val label: String, val subtitle: String, val isDark: Boolean = false) {
    HoscatSignature("호스캣 시그니처", "화이트 · 잉크 · 버건디"),
    FamiliarOneUiLight("가장 익숙한 테마", "갤럭시 사용자는 다 아는 그 색감"),
    ClassicLight("클래식 라이트", "Android 기본 감각의 라이트"),
    CrimsonMirror("홍염의 거울", "정유 모티프 · 절제된 홍염"),
    BlackForestWave("흑림의 물결", "임인 모티프 · 깊은 숲과 물", isDark = true);

    companion object {
        fun fromStored(value: String): ThemeChoice = when (value) {
            "System" -> HoscatSignature
            "Light" -> ClassicLight
            "Dark" -> BlackForestWave
            else -> runCatching { valueOf(value) }.getOrDefault(HoscatSignature)
        }
    }
}

@Immutable
data class HarmonyColors(
    val appBackground: Color,
    val surfacePrimary: Color,
    val surfaceSecondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val primaryBlue: Color,
    val warningRed: Color,
    val successGreen: Color,
    val cardBackTop: Color,
    val cardBackBottom: Color,
    val cardBackLine: Color,
    val cardBackLogo: Color
)

val HoscatSignatureColors = HarmonyColors(
    appBackground = Color(0xFFFAFBFA),
    surfacePrimary = Color(0xFFFFFFFF),
    surfaceSecondary = Color(0xFFF0F3F1),
    textPrimary = Color(0xFF151B18),
    textSecondary = Color(0xFF536159),
    textTertiary = Color(0xFF6E7872),
    divider = Color(0xFFDCE2DE),
    primaryBlue = Color(0xFF7B2345),
    warningRed = Color(0xFFAE2638),
    successGreen = Color(0xFF226349),
    cardBackTop = Color(0xFF111615),
    cardBackBottom = Color(0xFF2C493D),
    cardBackLine = Color(0xFFD7E6DF),
    cardBackLogo = Color(0xFFDFC6CD)
)

val ClassicLightColors = HarmonyColors(
    appBackground = Color(0xFFF8F9FA),
    surfacePrimary = Color(0xFFFFFFFF),
    surfaceSecondary = Color(0xFFECEFF3),
    textPrimary = Color(0xFF1F2328),
    textSecondary = Color(0xFF3E4C59),
    textTertiary = Color(0xFF6B7280),
    divider = Color(0x26000000),
    primaryBlue = Color(0xFF4B6584),
    warningRed = Color(0xFFD14343),
    successGreen = Color(0xFF2D6A4F),
    cardBackTop = Color(0xFF263238),
    cardBackBottom = Color(0xFF455A64),
    cardBackLine = Color(0xFFCFD8DC),
    cardBackLogo = Color(0xFFF8F9FA)
)

val FamiliarOneUiLightColors = HarmonyColors(
    appBackground = Color(0xFFF7F7FA),
    surfacePrimary = Color(0xFFFFFFFF),
    surfaceSecondary = Color(0xFFF1F2F6),
    textPrimary = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF6F737A),
    textTertiary = Color(0xFF9AA0A8),
    divider = Color(0xFFE4E6EB),
    primaryBlue = Color(0xFF0381FE),
    warningRed = Color(0xFFFF3B30),
    successGreen = Color(0xFF34C759),
    cardBackTop = Color(0xFF1A1A1A),
    cardBackBottom = Color(0xFF30343A),
    cardBackLine = Color(0xFFE4E6EB),
    cardBackLogo = Color(0xFFFFFFFF)
)

val CrimsonMirrorColors = HarmonyColors(
    appBackground = Color(0xFFF8F2ED),
    surfacePrimary = Color(0xFFFFFAF5),
    surfaceSecondary = Color(0xFFF0E4DE),
    textPrimary = Color(0xFF241C1B),
    textSecondary = Color(0xFF5B463D),
    textTertiary = Color(0xFF7B6B63),
    divider = Color(0x33B88A76),
    primaryBlue = Color(0xFF9E5148),
    warningRed = Color(0xFFA83E3E),
    successGreen = Color(0xFF77604B),
    cardBackTop = Color(0xFF4A202B),
    cardBackBottom = Color(0xFF8E5547),
    cardBackLine = Color(0xFFD8B774),
    cardBackLogo = Color(0xFFFFF1DF)
)

val BlackForestWaveColors = HarmonyColors(
    appBackground = Color(0xFF101820),
    surfacePrimary = Color(0xFF17221F),
    surfaceSecondary = Color(0xFF223A32),
    textPrimary = Color(0xFFEAF3EC),
    textSecondary = Color(0xFFB7D2C3),
    textTertiary = Color(0xFF93A99F),
    divider = Color(0x334E7668),
    primaryBlue = Color(0xFF78B99A),
    warningRed = Color(0xFFC46A55),
    successGreen = Color(0xFF5CA08A),
    cardBackTop = Color(0xFF071116),
    cardBackBottom = Color(0xFF1B3D36),
    cardBackLine = Color(0xFF78B99A),
    cardBackLogo = Color(0xFFE7F4EB)
)

val LocalHarmonyColors = staticCompositionLocalOf { HoscatSignatureColors }

@Composable
fun MyangTarotTheme(
    themeChoice: ThemeChoice,
    content: @Composable () -> Unit
) {
    val harmonyColors = when (themeChoice) {
        ThemeChoice.HoscatSignature -> HoscatSignatureColors
        ThemeChoice.FamiliarOneUiLight -> FamiliarOneUiLightColors
        ThemeChoice.ClassicLight -> ClassicLightColors
        ThemeChoice.CrimsonMirror -> CrimsonMirrorColors
        ThemeChoice.BlackForestWave -> BlackForestWaveColors
    }
    val materialColors = if (themeChoice.isDark) {
        darkColorScheme(
            primary = harmonyColors.primaryBlue,
            background = harmonyColors.appBackground,
            surface = harmonyColors.surfacePrimary,
            onPrimary = Color.White,
            onBackground = harmonyColors.textPrimary,
            onSurface = harmonyColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = harmonyColors.primaryBlue,
            background = harmonyColors.appBackground,
            surface = harmonyColors.surfacePrimary,
            onPrimary = Color.White,
            onBackground = harmonyColors.textPrimary,
            onSurface = harmonyColors.textPrimary
        )
    }

    CompositionLocalProvider(LocalHarmonyColors provides harmonyColors) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}
