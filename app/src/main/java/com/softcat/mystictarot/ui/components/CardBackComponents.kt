package com.softcat.mystictarot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softcat.mystictarot.CardBackStyle
import com.softcat.mystictarot.harmonyBlue
import com.softcat.mystictarot.harmonyCardBackBottom
import com.softcat.mystictarot.harmonyCardBackLine
import com.softcat.mystictarot.harmonyCardBackLogo
import com.softcat.mystictarot.harmonyCardBackTop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HoscatMark(
    modifier: Modifier,
    color: Color,
    detailColor: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val mark = Path().apply {
            moveTo(w * 0.18f, h * 0.12f)
            lineTo(w * 0.42f, h * 0.44f)
            lineTo(w * 0.58f, h * 0.44f)
            lineTo(w * 0.82f, h * 0.12f)
            lineTo(w * 0.82f, h * 0.66f)
            lineTo(w * 0.62f, h * 0.88f)
            lineTo(w * 0.38f, h * 0.88f)
            lineTo(w * 0.18f, h * 0.66f)
            close()
        }
        drawPath(mark, color)
        val cut = Stroke(width = w * 0.055f, cap = StrokeCap.Round)
        drawLine(detailColor, Offset(w * 0.25f, h * 0.42f), Offset(w * 0.37f, h * 0.34f), strokeWidth = cut.width, cap = StrokeCap.Round)
        drawLine(detailColor, Offset(w * 0.75f, h * 0.42f), Offset(w * 0.63f, h * 0.34f), strokeWidth = cut.width, cap = StrokeCap.Round)
        rotate(16f, pivot = Offset(w * 0.36f, h * 0.62f)) {
            drawOval(detailColor, topLeft = Offset(w * 0.28f, h * 0.57f), size = Size(w * 0.18f, h * 0.09f))
        }
        rotate(-16f, pivot = Offset(w * 0.64f, h * 0.62f)) {
            drawOval(detailColor, topLeft = Offset(w * 0.54f, h * 0.57f), size = Size(w * 0.18f, h * 0.09f))
        }
    }
}

@Composable
fun CompactCardBack(
    modifier: Modifier,
    cardBackStyle: CardBackStyle,
    customCardBackUri: String?,
    selectedIndex: Int?,
    onClick: () -> Unit
) {
    val selected = selectedIndex != null
    val palette = cardBackPalette(cardBackStyle)
    val context = LocalContext.current
    val customBitmap by produceState<ImageBitmap?>(
        initialValue = null,
        cardBackStyle,
        customCardBackUri
    ) {
        value = if (cardBackStyle == CardBackStyle.CustomImage && customCardBackUri != null) {
            withContext(Dispatchers.IO) {
                sampledImageBitmapFromUri(
                    context = context,
                    uriString = customCardBackUri,
                    maxTextureSize = 512
                )
            }
        } else {
            null
        }
    }
    val accent = harmonyBlue
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(palette.top, palette.bottom)))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) accent else palette.line.copy(alpha = 0.34f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        customBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "개인 카드 뒷면",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (selected) 0.08f else 0.02f))
            )
        } ?: run {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(7.dp)
            ) {
                val line = Stroke(width = size.minDimension * 0.025f, cap = StrokeCap.Round)
                drawRoundRect(
                    color = palette.line.copy(alpha = if (selected) 0.48f else 0.28f),
                    topLeft = Offset(size.width * 0.10f, size.height * 0.08f),
                    size = Size(size.width * 0.80f, size.height * 0.84f),
                    cornerRadius = CornerRadius(size.width * 0.08f, size.width * 0.08f),
                    style = line
                )
                drawLine(palette.line.copy(alpha = 0.24f), Offset(size.width * 0.50f, size.height * 0.16f), Offset(size.width * 0.50f, size.height * 0.28f), strokeWidth = line.width, cap = StrokeCap.Round)
                drawLine(palette.line.copy(alpha = 0.24f), Offset(size.width * 0.50f, size.height * 0.72f), Offset(size.width * 0.50f, size.height * 0.84f), strokeWidth = line.width, cap = StrokeCap.Round)
            }
            HoscatMark(
                modifier = Modifier.size(26.dp),
                color = palette.logo,
                detailColor = palette.top
            )
        }
        if (selectedIndex != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accent)
                    .border(1.dp, Color.White.copy(alpha = 0.75f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    selectedIndex.toString(),
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private data class CardBackPalette(
    val top: Color,
    val bottom: Color,
    val line: Color,
    val logo: Color
)

@Composable
private fun cardBackPalette(style: CardBackStyle): CardBackPalette {
    return when (style) {
        CardBackStyle.ThemeDefault,
        CardBackStyle.CustomImage -> CardBackPalette(
            top = harmonyCardBackTop,
            bottom = harmonyCardBackBottom,
            line = harmonyCardBackLine,
            logo = harmonyCardBackLogo
        )
        CardBackStyle.HoscatInk -> CardBackPalette(
            top = Color(0xFF111214),
            bottom = Color(0xFF252A25),
            line = Color(0xFFE9E1D5),
            logo = Color(0xFFF7F1EC)
        )
        CardBackStyle.ClassicSlate -> CardBackPalette(
            top = Color(0xFF263238),
            bottom = Color(0xFF4B5D67),
            line = Color(0xFFDDE3E8),
            logo = Color(0xFFF8F9FA)
        )
        CardBackStyle.CrimsonSigil -> CardBackPalette(
            top = Color(0xFF421B24),
            bottom = Color(0xFF8B4B42),
            line = Color(0xFFE1C08A),
            logo = Color(0xFFFFF1DF)
        )
        CardBackStyle.ForestWave -> CardBackPalette(
            top = Color(0xFF071116),
            bottom = Color(0xFF1B3D36),
            line = Color(0xFF78B99A),
            logo = Color(0xFFE7F4EB)
        )
    }
}
