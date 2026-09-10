package com.softcat.mystictarot

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Text
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Animatable
import kotlinx.coroutines.launch
import kotlin.math.ceil

val HoscatHomeBarReservedPadding = 112.dp
val HoscatHomeBarCtaGap = 8.dp
val HoscatHomeBarCtaMinimumGap = 8.dp
val HoscatBottomNavVisualHeight = 54.dp
val HoscatBottomNavOuterVerticalPadding = 12.dp
val HoscatBottomNavCtaVisualInset = 10.dp
val HoscatBottomCtaVisualHeight = 44.dp
val HoscatBottomCtaMinTouchHeight = 48.dp
val HoscatActionControlRadius = 8.dp
val HoscatBottomCtaMaxVisualHeight = 80.dp
val HoscatBottomCtaTouchVerticalInset = 2.dp
val HoscatStandardActionVisualHeight = 40.dp
val HoscatStandardActionRadius = 8.dp
val HoscatStandardActionMaxVisualHeight = 76.dp
val HoscatStandardActionTouchVerticalInset = 4.dp
val HoscatCompactActionVisualHeight = 38.dp
val HoscatCompactActionRadius = 8.dp
val HoscatCompactActionMaxVisualHeight = 72.dp
val HoscatCompactActionTouchVerticalInset = 5.dp
val HoscatActionControlVisualOffset = 0.dp
val HoscatActionControlVisualBottomInset =
    (HoscatBottomCtaMinTouchHeight - HoscatBottomCtaVisualHeight) / 2
val MyangControlHeight = 48.dp
val HoscatBottomActionSpacing = 8.dp
val HoscatBottomActionLayoutSpacing = 4.dp
val HoscatContentToActionGap = 18.dp
val HoscatActionStackBottomGap = HoscatHomeBarCtaGap - HoscatActionControlVisualBottomInset
val HoscatContentBottomClearance = 16.dp
val MyangSheetBottomClearance = 62.dp
const val MyangSheetDismissDistanceFraction = 0.10f
const val MyangSheetDismissVelocityThreshold = 620f

/*
 * The parameter historically named bottomPadding is the system navigation-bar
 * bottom inset supplied by HarmonyShell. A zero inset does not mean the floating
 * bottom nav is absent; gesture navigation and density/window modes can report
 * 0.dp while the app tab bar is still drawn. Always reserve the app tab bar
 * before applying the CTA gap so bottom actions cannot slide behind it.
 */
@Composable
fun hoscatBottomCtaPadding(navigationBarBottomInset: Dp): Dp =
    hoscatActionStackBottomPadding(navigationBarBottomInset)

internal fun Density.hoscatActionStackBottomPaddingPx(navigationBarBottomInset: Dp): Int =
    navigationBarBottomInset.roundToPx() +
        HoscatBottomNavVisualHeight.roundToPx() +
        HoscatBottomNavOuterVerticalPadding.roundToPx() +
        ceil(HoscatHomeBarCtaGap.toPx()).toInt() -
        HoscatBottomCtaTouchVerticalInset.roundToPx()

internal fun Density.hoscatActionStackSpacingPx(): Int =
    ceil(HoscatBottomActionSpacing.toPx()).toInt() -
        HoscatBottomCtaTouchVerticalInset.roundToPx() * 2

@Composable
fun hoscatActionStackBottomPadding(navigationBarBottomInset: Dp): Dp =
    with(LocalDensity.current) {
        // Dock modifiers round separately. Match those edges, then ceil the visible gap.
        hoscatActionStackBottomPaddingPx(navigationBarBottomInset).toDp()
    }

fun hoscatContentBottomPadding(navigationBarBottomInset: Dp): Dp =
    navigationBarBottomInset +
        HoscatBottomNavVisualHeight +
        (HoscatBottomNavOuterVerticalPadding * 2) +
        HoscatContentBottomClearance

fun hoscatContentBottomPaddingForActionStack(
    actionStackHeight: Dp,
    actionStackBottomPadding: Dp,
    contentToActionGap: Dp = HoscatContentToActionGap
): Dp = actionStackHeight + actionStackBottomPadding + contentToActionGap

fun hoscatInitialActionStackHeight(actionRows: Int): Dp {
    if (actionRows <= 0) return 0.dp
    return HoscatBottomCtaMinTouchHeight * actionRows +
        HoscatBottomActionLayoutSpacing * (actionRows - 1)
}

@Composable
fun HoscatActionControlSurface(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color,
    border: BorderStroke? = null,
    visualHeight: Dp = HoscatBottomCtaVisualHeight,
    maxVisualHeight: Dp = HoscatBottomCtaMaxVisualHeight,
    touchVerticalInset: Dp = HoscatBottomCtaTouchVerticalInset,
    radius: Dp = HoscatActionControlRadius,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    val minimumTouchHeight = maxOf(HoscatBottomCtaMinTouchHeight, visualHeight + touchVerticalInset * 2)
    val maximumTouchHeight = maxVisualHeight + touchVerticalInset * 2
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minimumTouchHeight, max = maximumTouchHeight)
            .semantics(mergeDescendants = true) {
                contentDescription = label
            }
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = touchVerticalInset),
        contentAlignment = Alignment.Center,
        propagateMinConstraints = true
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = visualHeight, max = maxVisualHeight)
                .testTag("hoscat-action-surface")
                .offset(y = HoscatActionControlVisualOffset)
                .background(containerColor, shape)
                .then(if (border != null) Modifier.border(border, shape) else Modifier)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

@Composable
fun HoscatBottomCta(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = harmonyBlue,
    disabledContainerColor: Color = harmonySecondaryPanel,
    contentColor: Color = Color.White,
    disabledContentColor: Color = harmonyInk,
    border: BorderStroke? = null,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = 18.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    val visualColor = if (enabled) containerColor else disabledContainerColor
    val textColor = if (enabled) contentColor else disabledContentColor
    HoscatActionControlSurface(
        label = label,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = visualColor,
        border = border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (content != null) {
                content()
            } else {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    fontWeight = fontWeight,
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun HoscatBottomSecondaryAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    HoscatBottomCta(
        label = label,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = Color.Transparent,
        contentColor = harmonySub,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = harmonyTertiary,
        border = BorderStroke(1.dp, harmonyDivider)
    )
}

@Composable
fun HoscatBottomSecondaryActionRow(
    modifier: Modifier = Modifier,
    first: (@Composable RowScope.() -> Unit)?,
    second: (@Composable RowScope.() -> Unit)?
) {
    if (first == null && second == null) return
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(HoscatBottomActionSpacing)
    ) {
        if (first != null) first()
        if (second != null) second()
    }
}

@Composable
fun BoxScope.HoscatBottomActionStack(
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp,
    onMeasuredHeightChanged: (Dp) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(harmonyBgBottom)
            .padding(horizontal = horizontalPadding)
            .padding(bottom = hoscatActionStackBottomPadding(bottomPadding))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size ->
                    onMeasuredHeightChanged(with(density) { size.height.toDp() })
                },
            verticalArrangement = Arrangement.spacedBy(with(density) { hoscatActionStackSpacingPx().toDp() }),
            content = content
        )
    }
}

@Composable
fun ColumnScope.HoscatBottomSheetActionStack(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(with(density) { hoscatActionStackSpacingPx().toDp() }),
        content = content
    )
}

fun Modifier.myangSheetDismissHandle(onClose: () -> Unit): Modifier {
    return this
        .sizeIn(minWidth = 64.dp, minHeight = 28.dp)
        .clickable { onClose() }
        .pointerInput(onClose) {
            detectVerticalDragGestures { _, dragAmount ->
                if (dragAmount > 10f) {
                    onClose()
                }
            }
        }
}

@Composable
fun Modifier.myangSheetNestedDismiss(
    sheetOffsetY: Animatable<Float, *>,
    maxDismissOffset: Float,
    dismissDistance: Float,
    onClose: () -> Unit
): Modifier {
    val scope = rememberCoroutineScope()
    val connection = remember(sheetOffsetY, maxDismissOffset, dismissDistance, onClose) {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y <= 0f) return Offset.Zero
                scope.launch {
                    sheetOffsetY.snapTo((sheetOffsetY.value + available.y).coerceAtLeast(0f))
                }
                return Offset(x = 0f, y = available.y)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (sheetOffsetY.value > dismissDistance || available.y > MyangSheetDismissVelocityThreshold) {
                    sheetOffsetY.animateTo(maxDismissOffset)
                    onClose()
                    return available
                }
                return Velocity.Zero
            }
        }
    }
    return nestedScroll(connection)
}

@Composable
fun ColumnScope.MyangSheetGrabber(onClose: () -> Unit) {
    Box(
        Modifier
            .align(Alignment.CenterHorizontally)
            .myangSheetDismissHandle(onClose),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .width(42.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(harmonyDivider)
        )
    }
}
