package com.softcat.mystictarot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.softcat.mystictarot.ui.theme.LocalHarmonyColors

enum class HarmonyIcon {
    Home,
    Reading,
    Deck,
    History,
    Menu,
    Back,
    Close,
    Refresh,
    Settings,
    Theme,
    Add,
    Check,
    Delete,
    Ai,
    Info,
    Direction,
    Chevron
}

@Composable
fun HarmonyIconButton(
    icon: HarmonyIcon,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = LocalHarmonyColors.current
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .size(48.dp)
            .clip(CircleShape)
            .semantics { this.contentDescription = contentDescription }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        HarmonyIconGlyph(
            icon = icon,
            color = colors.textPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun HarmonyIconGlyph(
    icon: HarmonyIcon,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.092f, cap = StrokeCap.Round)
        val thin = Stroke(width = w * 0.072f, cap = StrokeCap.Round)
        when (icon) {
            HarmonyIcon.Home -> {
                drawLine(color, Offset(w * 0.18f, h * 0.52f), Offset(w * 0.50f, h * 0.22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.50f, h * 0.22f), Offset(w * 0.82f, h * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.28f, h * 0.48f), Offset(w * 0.28f, h * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.72f, h * 0.48f), Offset(w * 0.72f, h * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.28f, h * 0.78f), Offset(w * 0.72f, h * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.Reading -> {
                rotate(-8f, pivot = Offset(w * 0.42f, h * 0.54f)) {
                    drawRoundRect(color, topLeft = Offset(w * 0.18f, h * 0.24f), size = Size(w * 0.34f, h * 0.52f), style = thin)
                }
                rotate(8f, pivot = Offset(w * 0.58f, h * 0.54f)) {
                    drawRoundRect(color, topLeft = Offset(w * 0.48f, h * 0.24f), size = Size(w * 0.34f, h * 0.52f), style = thin)
                }
                drawLine(color, Offset(w * 0.50f, h * 0.38f), Offset(w * 0.50f, h * 0.62f), strokeWidth = thin.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.Deck -> {
                drawRoundRect(color, topLeft = Offset(w * 0.30f, h * 0.17f), size = Size(w * 0.44f, h * 0.62f), style = thin)
                drawLine(color, Offset(w * 0.20f, h * 0.30f), Offset(w * 0.20f, h * 0.86f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.20f, h * 0.86f), Offset(w * 0.62f, h * 0.86f), strokeWidth = thin.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.History -> {
                drawCircle(color, radius = w * 0.34f, center = Offset(w * 0.52f, h * 0.52f), style = thin)
                drawLine(color, Offset(w * 0.52f, h * 0.52f), Offset(w * 0.52f, h * 0.32f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.52f, h * 0.52f), Offset(w * 0.68f, h * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.Menu -> {
                listOf(0.30f, 0.50f, 0.70f).forEach { y ->
                    drawLine(color, Offset(w * 0.24f, h * y), Offset(w * 0.76f, h * y), strokeWidth = stroke.width, cap = StrokeCap.Round)
                }
            }
            HarmonyIcon.Back -> {
                drawLine(color, Offset(w * 0.66f, h * 0.20f), Offset(w * 0.34f, h * 0.50f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.34f, h * 0.50f), Offset(w * 0.66f, h * 0.80f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.Close -> {
                drawLine(color, Offset(w * 0.28f, h * 0.28f), Offset(w * 0.72f, h * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.72f, h * 0.28f), Offset(w * 0.28f, h * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.Refresh -> {
                drawArc(color, startAngle = 35f, sweepAngle = 285f, useCenter = false, topLeft = Offset(w * 0.22f, h * 0.22f), size = Size(w * 0.56f, h * 0.56f), style = thin)
                drawLine(color, Offset(w * 0.72f, h * 0.20f), Offset(w * 0.78f, h * 0.42f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.72f, h * 0.20f), Offset(w * 0.52f, h * 0.24f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.Settings -> {
                drawCircle(color, radius = w * 0.24f, center = Offset(w * 0.50f, h * 0.50f), style = thin)
                repeat(6) { i ->
                    rotate(i * 60f, pivot = Offset(w * 0.50f, h * 0.50f)) {
                        drawLine(color, Offset(w * 0.50f, h * 0.13f), Offset(w * 0.50f, h * 0.22f), strokeWidth = thin.width, cap = StrokeCap.Round)
                    }
                }
            }
            HarmonyIcon.Theme -> {
                drawCircle(color, radius = w * 0.30f, center = Offset(w * 0.46f, h * 0.50f), style = thin)
                drawCircle(color, radius = w * 0.045f, center = Offset(w * 0.35f, h * 0.40f))
                drawCircle(color, radius = w * 0.045f, center = Offset(w * 0.52f, h * 0.36f))
                drawCircle(color, radius = w * 0.045f, center = Offset(w * 0.56f, h * 0.56f))
                drawLine(color, Offset(w * 0.62f, h * 0.70f), Offset(w * 0.78f, h * 0.78f), strokeWidth = thin.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.Add -> {
                drawLine(color, Offset(w * 0.50f, h * 0.24f), Offset(w * 0.50f, h * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.24f, h * 0.50f), Offset(w * 0.76f, h * 0.50f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.Check -> {
                drawLine(color, Offset(w * 0.22f, h * 0.54f), Offset(w * 0.42f, h * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.42f, h * 0.72f), Offset(w * 0.78f, h * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.Delete -> {
                drawLine(color, Offset(w * 0.30f, h * 0.34f), Offset(w * 0.70f, h * 0.34f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.38f, h * 0.34f), Offset(w * 0.42f, h * 0.78f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.62f, h * 0.34f), Offset(w * 0.58f, h * 0.78f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.42f, h * 0.20f), Offset(w * 0.58f, h * 0.20f), strokeWidth = thin.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.Ai -> {
                drawLine(color, Offset(w * 0.50f, h * 0.18f), Offset(w * 0.50f, h * 0.82f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.18f, h * 0.50f), Offset(w * 0.82f, h * 0.50f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.28f, h * 0.28f), Offset(w * 0.72f, h * 0.72f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.72f, h * 0.28f), Offset(w * 0.28f, h * 0.72f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawCircle(color, radius = w * 0.075f, center = Offset(w * 0.50f, h * 0.50f))
            }
            HarmonyIcon.Info -> {
                drawCircle(color, radius = w * 0.32f, center = Offset(w * 0.50f, h * 0.50f), style = thin)
                drawCircle(color, radius = w * 0.035f, center = Offset(w * 0.50f, h * 0.34f))
                drawLine(color, Offset(w * 0.50f, h * 0.46f), Offset(w * 0.50f, h * 0.66f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.Direction -> {
                drawLine(color, Offset(w * 0.35f, h * 0.20f), Offset(w * 0.35f, h * 0.78f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.25f, h * 0.32f), Offset(w * 0.35f, h * 0.20f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.45f, h * 0.32f), Offset(w * 0.35f, h * 0.20f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.65f, h * 0.20f), Offset(w * 0.65f, h * 0.78f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.55f, h * 0.66f), Offset(w * 0.65f, h * 0.78f), strokeWidth = thin.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.75f, h * 0.66f), Offset(w * 0.65f, h * 0.78f), strokeWidth = thin.width, cap = StrokeCap.Round)
            }
            HarmonyIcon.Chevron -> {
                drawLine(color, Offset(w * 0.40f, h * 0.25f), Offset(w * 0.60f, h * 0.50f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.60f, h * 0.50f), Offset(w * 0.40f, h * 0.75f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
        }
    }
}
