package com.softcat.mystictarot

import android.animation.ValueAnimator
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.softcat.mystictarot.ui.components.HarmonyIcon
import com.softcat.mystictarot.ui.components.HarmonyIconButton
import com.softcat.mystictarot.ui.components.HarmonyIconGlyph
import com.softcat.mystictarot.ui.components.CompactCardBack
import com.softcat.mystictarot.ui.components.TarotImage
import com.softcat.mystictarot.ui.theme.ThemeChoice
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val SpreadResultBoardBoundaryGap = 8.dp

@Composable
private fun SpreadDetailsSheetV2(
    spread: SpreadOption,
    drawnCards: List<DrawnCard>,
    adsDisabled: Boolean,
    onCardClick: (DrawnCard) -> Unit,
    onClose: () -> Unit
) {
    AppBottomSheet(
        heightFraction = 0.60f,
        adsDisabled = adsDisabled,
        onClose = onClose
    ) {
        SheetHeader(
            title = "카드별 설명",
            subtitle = "${spread.layoutTitle} · ${spread.positionPresetTitle}",
            closeDescription = "카드 설명 닫기",
            onClose = onClose
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            drawnCards.forEach { drawnCard ->
                SpreadDetailRow(
                    spread = spread,
                    drawnCard = drawnCard,
                    onClick = { onCardClick(drawnCard) }
                )
            }
        }
    }
}

@Composable
private fun LlmPromptSheetV2(
    prompt: String,
    adsDisabled: Boolean,
    onClose: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val promptPreviewScrollState = rememberScrollState()
    AppBottomSheet(
        heightFraction = 0.82f,
        adsDisabled = adsDisabled,
        onClose = onClose
    ) {
        SheetHeader(
            title = "AI 해석",
            subtitle = "요청문을 확인한 뒤 복사하거나 AI 앱에서 열 수 있습니다",
            closeDescription = "AI 해석 닫기",
            onClose = onClose
        )
        Text(
            prompt,
            modifier = Modifier
                .fillMaxWidth()
                .height(122.dp)
                .verticalScroll(promptPreviewScrollState)
                .clip(RoundedCornerShape(18.dp))
                .background(harmonySecondaryPanel)
                .padding(14.dp),
            color = harmonyInk,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        HoscatBottomSheetActionStack {
            HoscatBottomCta(
                label = "해석 요청문 복사",
                onClick = {
                    runCatching {
                        clipboard.setText(AnnotatedString(prompt))
                        Toast.makeText(context, "요청문을 복사했습니다. AI 앱에서 붙여넣어 주세요.", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(context, "요청문을 복사하지 못했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        Text(
            "다른 AI 앱에서 열기",
            color = harmonyInk,
            fontSize = 14.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        SystemPanel {
            AiAppLaunchRow("Gemini", "Google Gemini 앱 또는 웹으로 열기", "com.google.android.apps.bard", "https://gemini.google.com/app", prompt)
            SystemDivider()
            AiAppLaunchRow("ChatGPT", "ChatGPT 앱 또는 웹으로 열기", "com.openai.chatgpt", "https://chatgpt.com/", prompt)
            SystemDivider()
            AiAppLaunchRow("Claude", "Claude 앱 또는 웹으로 열기", "com.anthropic.claude", "https://claude.ai/new", prompt)
        }
    }
}

@Composable
fun DeckSelectSheet(
    decks: List<TarotDeck>,
    selectedDeckId: String,
    onAddDeck: () -> Unit,
    onSelect: (TarotDeck) -> Unit,
    onToggleDeck: (TarotDeck, Boolean) -> Unit,
    onOpenDeckSettings: (TarotDeck) -> Unit,
    onDeleteDeck: (TarotDeck) -> Unit,
    onClose: () -> Unit
) {
    var pendingDeleteDeck by remember { mutableStateOf<TarotDeck?>(null) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val sheetOffsetY = remember { Animatable(0f) }
    BackHandler(enabled = true) {
        onClose()
    }
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val dismissDistance = with(density) { maxHeight.toPx() * MyangSheetDismissDistanceFraction }
        val maxDismissOffset = with(density) { maxHeight.toPx() }
        val dragProgress = (sheetOffsetY.value / dismissDistance).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(myangBlackCat.copy(alpha = 0.46f * (1f - dragProgress)))
                .clickable { onClose() }
        )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MyangSheetBottomClearance)
                    .heightIn(max = maxHeight * 0.60f)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                .offset { IntOffset(0, sheetOffsetY.value.roundToInt()) }
                .clip(RoundedCornerShape(28.dp))
                .background(harmonyPanel)
                .myangSheetNestedDismiss(
                    sheetOffsetY = sheetOffsetY,
                    maxDismissOffset = maxDismissOffset,
                    dismissDistance = dismissDistance,
                    onClose = onClose
                )
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            sheetOffsetY.snapTo((sheetOffsetY.value + delta).coerceAtLeast(0f))
                        }
                    },
                    onDragStopped = { velocity ->
                        scope.launch {
                            if (sheetOffsetY.value > dismissDistance || velocity > MyangSheetDismissVelocityThreshold) {
                                sheetOffsetY.animateTo(maxDismissOffset, tween(220, easing = FastOutSlowInEasing))
                                onClose()
                            } else {
                                sheetOffsetY.animateTo(0f, tween(210, easing = FastOutSlowInEasing))
                            }
                        }
                    }
                )
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MyangSheetGrabber(onClose)
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("덱 선택 및 관리", color = harmonyInk, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
                    Text("사용할 덱 선택 · 활성화 · 세부 설정", color = harmonySub, fontSize = 12.sp, lineHeight = 15.sp)
                }
                HarmonyIconButton(HarmonyIcon.Close, "덱 선택 닫기", onClose)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeckAction("덱 추가", "이미지를 여러 장 선택해 개인 덱 만들기", onClick = onAddDeck)
                decks.forEach { deck ->
                    DeckChoiceRow(
                        deck = deck,
                        selected = deck.id == selectedDeckId,
                        canDelete = deck.id != "standard",
                        onClick = {
                            onSelect(deck)
                        },
                        onToggle = { enabled ->
                            onToggleDeck(deck, enabled)
                        },
                        onSettings = {
                            onOpenDeckSettings(deck)
                        },
                        onDelete = {
                            pendingDeleteDeck = deck
                        }
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
    }
    pendingDeleteDeck?.let { deck ->
        DeleteDeckDialog(
            deck = deck,
            isSelected = deck.id == selectedDeckId,
            onCancel = { pendingDeleteDeck = null },
            onDelete = {
                pendingDeleteDeck = null
                onDeleteDeck(deck)
            }
        )
    }
}

@Composable
fun DeckManagementScreen(
    decks: List<TarotDeck>,
    selectedDeckId: String,
    onBack: () -> Unit,
    onAddDeck: () -> Unit,
    onSelect: (TarotDeck) -> Unit,
    onToggleDeck: (TarotDeck, Boolean) -> Unit,
    onOpenDeckSettings: (TarotDeck) -> Unit,
    onDeleteDeck: (TarotDeck) -> Unit,
    bottomPadding: Dp
) {
    var pendingDeleteDeck by remember { mutableStateOf<TarotDeck?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .padding(bottom = hoscatContentBottomPadding(bottomPadding)),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CompactTopBar(
            title = "덱",
            subtitle = "",
            onBack = onBack,
            action = null
        )

        HarmonyCard(padding = PaddingValues(14.dp)) {
            Text("덱 선택 및 관리", color = harmonyInk, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("사용할 덱을 고르고, 개인 덱의 활성화와 세부 설정을 관리합니다.", color = harmonySub, fontSize = 13.sp, lineHeight = 18.sp)
        }

        DeckAction("덱 추가", "이미지를 여러 장 선택해 개인 덱 만들기", onClick = onAddDeck)

        decks.forEach { deck ->
            DeckManagementRow(
                deck = deck,
                selected = deck.id == selectedDeckId,
                canDelete = deck.id != "standard",
                onClick = { onSelect(deck) },
                onToggle = { enabled -> onToggleDeck(deck, enabled) },
                onSettings = { onOpenDeckSettings(deck) },
                onDelete = { pendingDeleteDeck = deck }
            )
        }
    }

    pendingDeleteDeck?.let { deck ->
        DeleteDeckDialog(
            deck = deck,
            isSelected = deck.id == selectedDeckId,
            onCancel = { pendingDeleteDeck = null },
            onDelete = {
                pendingDeleteDeck = null
                onDeleteDeck(deck)
            }
        )
    }
}

@Composable
private fun DeckManagementRow(
    deck: TarotDeck,
    selected: Boolean,
    canDelete: Boolean,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onSettings: () -> Unit,
    onDelete: () -> Unit
) {
    val enabled = deck.enabled
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                when {
                    selected -> harmonyBlue.copy(alpha = 0.08f)
                    !enabled -> harmonySecondaryPanel.copy(alpha = 0.38f)
                    else -> harmonySecondaryPanel.copy(alpha = 0.72f)
                }
            )
            .border(
                1.dp,
                when {
                    selected -> harmonyBlue.copy(alpha = 0.48f)
                    !enabled -> harmonyDivider.copy(alpha = 0.42f)
                    else -> harmonyDivider.copy(alpha = 0.62f)
                },
                RoundedCornerShape(20.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MyangControlHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background((if (selected) harmonyBlue else harmonySub).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (selected) "✓" else deck.cards.size.toString(),
                    color = if (selected) harmonyBlue else harmonySub.copy(alpha = if (enabled) 1f else 0.55f),
                    fontSize = 15.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    deck.name,
                    color = harmonyInk.copy(alpha = if (enabled) 1f else 0.55f),
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    when {
                        !enabled -> "${deck.cards.size}장 · 비활성화됨"
                        canDelete -> "${deck.cards.size}장 · 개인 덱"
                        else -> "${deck.cards.size}장 · 기본 덱"
                    },
                    color = harmonySub,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (canDelete) {
                MiniDeckToggle(enabled = enabled, onClick = { onToggle(!enabled) })
            } else {
                DeckStatePill("항상 사용")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeckManagementButton(
                label = if (selected) "선택됨" else "이 덱 사용",
                modifier = Modifier.weight(1f),
                enabled = enabled,
                selected = selected,
                onClick = onClick
            )
            if (canDelete) {
                DeckManagementButton(
                    label = "세부 설정",
                    modifier = Modifier.weight(1f),
                    enabled = true,
                    onClick = onSettings
                )
                DeckManagementButton(
                    label = "삭제",
                    modifier = Modifier.weight(1f),
                    enabled = true,
                    destructive = true,
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
private fun DeckStatePill(label: String) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(harmonySecondaryPanel.copy(alpha = 0.82f))
            .border(1.dp, harmonyDivider.copy(alpha = 0.54f), RoundedCornerShape(15.dp))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = harmonySub,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DeckManagementButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    HoscatActionControlSurface(
        label = label,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = when {
            !enabled -> harmonySecondaryPanel.copy(alpha = 0.46f)
            destructive -> harmonyRose.copy(alpha = 0.92f)
            selected -> harmonyBlue.copy(alpha = 0.12f)
            else -> harmonyPanel
        },
        visualHeight = HoscatStandardActionVisualHeight,
        maxVisualHeight = HoscatStandardActionMaxVisualHeight,
        touchVerticalInset = HoscatStandardActionTouchVerticalInset,
        radius = HoscatStandardActionRadius
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = when {
                !enabled -> harmonyTertiary
                destructive -> Color.White
                selected -> harmonyBlue
                else -> harmonyInk
            },
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DeleteDeckDialog(
    deck: TarotDeck,
    isSelected: Boolean,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(harmonyPanel)
                .border(1.dp, harmonyDivider.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("덱 삭제", color = harmonyInk, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                "${deck.name} · ${deck.cards.size}장\n${if (isSelected) "삭제하면 기본 78장 덱으로 전환됩니다." else "이 개인 덱을 삭제합니다."}",
                color = harmonySub,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HoscatBottomCta(
                    label = "취소",
                    modifier = Modifier.weight(1f),
                    onClick = onCancel,
                    containerColor = harmonySecondaryPanel,
                    contentColor = harmonyInk
                ) {
                    Text("취소", color = harmonyInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                HoscatBottomCta(
                    label = "삭제",
                    modifier = Modifier.weight(1f),
                    onClick = onDelete,
                    containerColor = harmonyRose
                ) {
                    HarmonyIconGlyph(HarmonyIcon.Delete, Color.White, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("삭제", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DeckChoiceRow(
    deck: TarotDeck,
    selected: Boolean,
    canDelete: Boolean,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onSettings: () -> Unit,
    onDelete: () -> Unit
) {
    val enabled = deck.enabled
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 66.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(
                when {
                    selected -> harmonyBlue.copy(alpha = 0.08f)
                    !enabled -> harmonySecondaryPanel.copy(alpha = 0.38f)
                    else -> harmonySecondaryPanel.copy(alpha = 0.72f)
                }
            )
            .pointerInput(deck.id, canDelete) {
                detectTapGestures(
                    onTap = { if (enabled) onClick() }
                )
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background((if (selected) harmonyBlue else harmonySub).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (selected) "✓" else deck.cards.size.toString(),
                    color = if (selected) harmonyBlue else harmonySub.copy(alpha = if (enabled) 1f else 0.55f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(deck.name, color = harmonyInk.copy(alpha = if (enabled) 1f else 0.55f), fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    when {
                        !enabled -> "${deck.cards.size}장 · 비활성화됨"
                        canDelete -> "${deck.cards.size}장 · 활성화/세부 설정 가능"
                        else -> "${deck.cards.size}장 · 기본 덱"
                    },
                    color = harmonySub,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
            if (canDelete) {
                Spacer(Modifier.width(8.dp))
                MiniDeckToggle(enabled = enabled, onClick = { onToggle(!enabled) })
            }
        }
        if (canDelete) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeckMiniActionButton(
                    label = if (enabled) "활성화됨" else "비활성",
                    modifier = Modifier.weight(1f),
                    onClick = { onToggle(!enabled) }
                )
                DeckMiniActionButton(
                    label = "세부 설정",
                    modifier = Modifier.weight(1f),
                    onClick = onSettings
                )
                DeckMiniActionButton(
                    label = "삭제",
                    modifier = Modifier.weight(1f),
                    destructive = true,
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
private fun DeckMiniActionButton(
    label: String,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    HoscatActionControlSurface(
        label = label,
        onClick = onClick,
        modifier = modifier,
        containerColor = if (destructive) harmonyRose.copy(alpha = 0.92f) else harmonySecondaryPanel,
        visualHeight = HoscatCompactActionVisualHeight,
        maxVisualHeight = HoscatCompactActionMaxVisualHeight,
        touchVerticalInset = HoscatCompactActionTouchVerticalInset,
        radius = HoscatCompactActionRadius
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = if (destructive) Color.White else harmonyInk,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MiniDeckToggle(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .semantics {
                contentDescription = "덱 활성화"
                stateDescription = if (enabled) "활성화됨" else "비활성화됨"
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (enabled) harmonyBlue.copy(alpha = 0.22f) else harmonyDivider.copy(alpha = 0.52f))
                .border(1.dp, if (enabled) harmonyBlue.copy(alpha = 0.58f) else harmonyDivider, RoundedCornerShape(13.dp))
                .padding(3.dp),
            contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (enabled) harmonyBlue else harmonyTertiary)
            )
        }
    }
}

@Composable
fun DeckSettingsSheet(
    deck: TarotDeck,
    onSaveDeck: (TarotDeck) -> Unit,
    onClose: () -> Unit
) {
    var deckName by remember(deck.id) { mutableStateOf(deck.name) }
    var aiPrompt by remember(deck.id) { mutableStateOf(deck.aiPrompt) }
    var cards by remember(deck.id) { mutableStateOf(deck.cards) }
    var selectedCardIndex by remember(deck.id) { mutableIntStateOf(0) }
    AppBottomSheet(
        heightFraction = 0.88f,
        adsDisabled = true,
        onClose = onClose
    ) {
        SheetHeader(
            title = "덱 세부 설정",
            subtitle = deck.name,
            closeDescription = "덱 세부 설정 닫기",
            onClose = onClose
        )

        HarmonyCard(padding = PaddingValues(14.dp)) {
            Text("덱 정보", color = harmonyInk, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = deckName,
                onValueChange = { deckName = it.take(40) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("덱 이름") }
            )
        }

        HarmonyCard(padding = PaddingValues(14.dp)) {
            Text("카드별 의미", color = harmonyInk, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("카드를 하나 선택해 이름과 의미를 수정합니다.", color = harmonySub, fontSize = 12.sp, lineHeight = 16.sp)
            Spacer(Modifier.height(10.dp))
            if (cards.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cards.chunked(6).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { card ->
                                val index = cards.indexOf(card)
                                val selected = index == selectedCardIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .sizeIn(minHeight = 48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) harmonyBlue else harmonySecondaryPanel)
                                        .border(1.dp, if (selected) harmonyBlue else harmonyDivider, RoundedCornerShape(12.dp))
                                        .semantics {
                                            contentDescription = "${index + 1}번 카드 설정"
                                            stateDescription = if (selected) "선택됨" else "선택 안 됨"
                                        }
                                        .clickable { selectedCardIndex = index },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${index + 1}",
                                        color = if (selected) Color.White else harmonyInk,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            repeat(6 - row.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                val selectedCard = cards[selectedCardIndex.coerceIn(0, cards.lastIndex)]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(harmonySecondaryPanel.copy(alpha = 0.46f))
                        .border(1.dp, harmonyDivider.copy(alpha = 0.56f), RoundedCornerShape(18.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${selectedCardIndex + 1}번 카드", color = harmonySub, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = selectedCard.nameKr,
                        onValueChange = { value ->
                            cards = cards.toMutableList().also { list ->
                                list[selectedCardIndex] = selectedCard.copy(nameKr = value.take(24))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true,
                        label = { Text("카드 이름") }
                    )
                    OutlinedTextField(
                        value = selectedCard.basicMeaning,
                        onValueChange = { value ->
                            cards = cards.toMutableList().also { list ->
                                list[selectedCardIndex] = selectedCard.copy(basicMeaning = value.take(360))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                        label = { Text("카드 의미") },
                        maxLines = 4
                    )
                }
            }
        }

        HarmonyCard(padding = PaddingValues(14.dp)) {
            Text("덱 전용 AI 프롬프트", color = harmonyInk, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            BottomSheetAdSlot(adsDisabled = false)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = aiPrompt,
                onValueChange = { aiPrompt = it.take(1800) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                placeholder = { Text("예: 이 덱은 색감과 이미지 분위기를 먼저 읽고, 카드 이름보다 사용자가 입력한 의미를 우선 반영해 주세요.") },
                maxLines = 8
            )
        }

        HoscatBottomCta(
            label = "덱 설정 저장",
            onClick = {
                onSaveDeck(
                    deck.copy(
                        name = deckName.ifBlank { deck.name },
                        cards = cards,
                        aiPrompt = aiPrompt
                    )
                )
            }
        ) {
            HarmonyIconGlyph(HarmonyIcon.Check, Color.White, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("저장", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun DeckPickScreen(
    deck: List<TarotCard>,
    selectedCards: List<TarotCard>,
    spread: SpreadOption,
    cardBackStyle: CardBackStyle,
    customCardBackUri: String?,
    targetCountOverride: Int? = null,
    stageLabel: String? = null,
    onBack: () -> Unit,
    onCardClick: (TarotCard) -> Unit,
    shuffleDeck: () -> Unit,
    onConfirm: () -> Unit,
    bottomPadding: Dp
) {
    val targetCount = targetCountOverride ?: min(spread.cardCount, deck.size)
    val headerTitle = spread.layoutTitle
    val headerSubtitle = stageLabel?.let { "$it · ${selectedCards.size}/${targetCount}장" }
        ?: "${selectedCards.size}/${targetCount}장"
    var actionStackHeight by remember { mutableStateOf(hoscatInitialActionStackHeight(1)) }
    val actionBottomPadding = hoscatActionStackBottomPadding(bottomPadding)
    val contentBottomPadding = hoscatContentBottomPaddingForActionStack(
        actionStackHeight = actionStackHeight,
        actionStackBottomPadding = actionBottomPadding,
        contentToActionGap = SpreadResultBoardBoundaryGap
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 14.dp, bottom = contentBottomPadding)
        ) {
            Box(Modifier.padding(horizontal = 20.dp)) {
                CompactTopBar(
                    title = headerTitle,
                    subtitle = headerSubtitle,
                    onBack = onBack,
                    action = {
                        ShuffleDeckChip(onClick = shuffleDeck)
                    }
                )
            }

            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CompactDeckAllInOneScreen(
                    deck = deck,
                    selectedCards = selectedCards,
                    cardBackStyle = cardBackStyle,
                    customCardBackUri = customCardBackUri,
                    onCardClick = onCardClick
                )
            }
        }

        HoscatBottomActionStack(
            bottomPadding = bottomPadding,
            horizontalPadding = 20.dp,
            onMeasuredHeightChanged = { actionStackHeight = it }
        ) {
            HoscatBottomCta(
                label = "결과 보기",
                enabled = selectedCards.size == targetCount,
                onClick = onConfirm
            )
        }
    }
}

@Composable
private fun ShuffleDeckChip(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .sizeIn(minHeight = 48.dp)
            .semantics { contentDescription = "덱 다시 섞기" }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(harmonySecondaryPanel.copy(alpha = 0.92f))
                .border(1.dp, harmonyDivider.copy(alpha = 0.68f), RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HarmonyIconGlyph(
                icon = HarmonyIcon.Refresh,
                color = harmonyBlue,
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "다시 섞기",
                color = harmonyBlue,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CompactDeckAllInOneScreen(
    deck: List<TarotCard>,
    selectedCards: List<TarotCard>,
    cardBackStyle: CardBackStyle,
    customCardBackUri: String?,
    onCardClick: (TarotCard) -> Unit
) {
    if (deck.isEmpty()) return
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        val count = deck.size
        val aspect = 0.62f
        val gap = when {
            count <= 22 -> 7.dp
            count <= 78 -> 5.dp
            else -> 6.dp
        }
        val edgePadding = if (count == 78) 13.dp else 8.dp
        val verticalGuard = 0.dp
        var bestColumns = 1
        var bestCardW = 0.dp
        var bestCardH = 0.dp
        val fixedRowSizes = if (count == 78) {
            listOf(8, 8, 7, 8, 8, 8, 8, 7, 8, 8)
        } else {
            null
        }

        val candidateColumns = fixedRowSizes?.let { listOf(it.maxOrNull() ?: 1) } ?: (1..count).toList()
        for (columns in candidateColumns) {
            val rowCount = fixedRowSizes?.size ?: ((count + columns - 1) / columns)
            val availableW = maxWidth - edgePadding * 2 - gap * (columns - 1)
            val availableH = maxHeight - verticalGuard - gap * (rowCount - 1)
            if (availableW <= 0.dp || availableH <= 0.dp) continue
            val byWidth = availableW / columns
            val byHeight = availableH / rowCount * aspect
            val cardW = if (byWidth < byHeight) byWidth else byHeight
            val cardH = cardW / aspect
            if (cardW > bestCardW) {
                bestColumns = columns
                bestCardW = cardW
                bestCardH = cardH
            }
        }

        val rows = fixedRowSizes?.let { sizes ->
            val mutableDeck = deck.toMutableList()
            sizes.map { size ->
                mutableDeck.take(size).also { rowCards ->
                    repeat(rowCards.size) { mutableDeck.removeAt(0) }
                }
            }
        } ?: deck.chunked(bestColumns)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = edgePadding),
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalAlignment = Alignment.Start
        ) {
            rows.forEachIndexed { rowIndex, row ->
                val rowStartIndex = rows.take(rowIndex).sumOf { it.size }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (row.size > 1) {
                        Arrangement.SpaceBetween
                    } else {
                        Arrangement.Start
                    }
                ) {
                    row.forEachIndexed { rowCardIndex, card ->
                        val cardIndex = rowStartIndex + rowCardIndex
                        val selectedIndex = selectedCards.indexOf(card).takeIf { it >= 0 }?.plus(1)
                        DealtDeckCard(
                            modifier = Modifier
                                .width(bestCardW)
                                .height(bestCardH)
                                .semantics {
                                    contentDescription = if (selectedIndex != null) {
                                        "${cardIndex + 1}번째 카드, 선택됨, ${selectedIndex}번째 선택"
                                    } else {
                                        "${cardIndex + 1}번째 카드, 선택 안 됨"
                                    }
                                    stateDescription = if (selectedIndex != null) "선택됨" else "선택 안 됨"
                                },
                            dealIndex = cardIndex,
                            animateEntrance = count <= 22,
                            cardBackStyle = cardBackStyle,
                            customCardBackUri = customCardBackUri,
                            selectedIndex = selectedIndex,
                            onClick = { onCardClick(card) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DealtDeckCard(
    modifier: Modifier,
    dealIndex: Int,
    animateEntrance: Boolean,
    cardBackStyle: CardBackStyle,
    customCardBackUri: String?,
    selectedIndex: Int?,
    onClick: () -> Unit
) {
    val animationsEnabled = remember { ValueAnimator.areAnimatorsEnabled() }
    if (!animateEntrance || !animationsEnabled) {
        CompactCardBack(
            modifier = modifier,
            cardBackStyle = cardBackStyle,
            customCardBackUri = customCardBackUri,
            selectedIndex = selectedIndex,
            onClick = onClick
        )
        return
    }
    val progress = remember(dealIndex) { Animatable(0f) }
    LaunchedEffect(dealIndex) {
        progress.snapTo(0f)
        delay((dealIndex * 7L).coerceAtMost(520L))
        progress.animateTo(1f, tween(180, easing = FastOutSlowInEasing))
    }
    CompactCardBack(
        modifier = modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 18f
            scaleX = 0.985f + progress.value * 0.015f
            scaleY = 0.985f + progress.value * 0.015f
        },
        cardBackStyle = cardBackStyle,
        customCardBackUri = customCardBackUri,
        selectedIndex = selectedIndex,
        onClick = onClick
    )
}

@Composable
fun SpreadResultScreen(
    spread: SpreadOption,
    question: String,
    drawnCards: List<DrawnCard>,
    expandedCard: DrawnCard?,
    showSpreadDetails: Boolean,
    showLlmPrompt: Boolean,
    readingSaved: Boolean,
    showLlmAction: Boolean,
    showCardDetailsAction: Boolean,
    deckAiPrompt: String,
    adsDisabled: Boolean,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onCardClick: (DrawnCard) -> Unit,
    onShowDetails: () -> Unit,
    onCloseDetails: () -> Unit,
    onShowLlmPrompt: () -> Unit,
    onCloseLlmPrompt: () -> Unit,
    onSaveReading: () -> Unit,
    onCloseExpanded: () -> Unit
) {
    val hasSecondaryResultActions = showLlmAction || showCardDetailsAction
    val resultActionRows = 1 + if (hasSecondaryResultActions) 1 else 0
    val fallbackResultActionHeight = hoscatInitialActionStackHeight(resultActionRows)
    var resultActionHeight by remember(resultActionRows) { mutableStateOf(fallbackResultActionHeight) }
    val resultActionBottomPadding = hoscatActionStackBottomPadding(bottomPadding)
    val resultContentBottomPadding = hoscatContentBottomPaddingForActionStack(
        actionStackHeight = resultActionHeight,
        actionStackBottomPadding = resultActionBottomPadding,
        contentToActionGap = SpreadResultBoardBoundaryGap
    )
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .padding(bottom = resultContentBottomPadding)
        ) {
            CompactTopBar(
                title = "리딩 결과",
                subtitle = "",
                onBack = onBack,
                action = { HarmonyIconButton(HarmonyIcon.Refresh, "리딩 다시 시작", onRestart) }
            )
            Spacer(Modifier.height(8.dp))
            InfoSummaryCard(
                title = spread.layoutTitle,
                subtitle = if (question.isBlank()) {
                    ""
                } else {
                    "질문: ${question.trim()}"
                }
            )
            Spacer(Modifier.height(SpreadResultBoardBoundaryGap))
            VariableSpreadLayout(
                modifier = Modifier.weight(1f),
                spread = spread,
                drawnCards = drawnCards,
                onCardClick = onCardClick
            )
        }

        HoscatBottomActionStack(
            bottomPadding = bottomPadding,
            horizontalPadding = 20.dp,
            onMeasuredHeightChanged = { resultActionHeight = it }
        ) {
            if (hasSecondaryResultActions) {
                HoscatBottomSecondaryActionRow(
                    first = if (showCardDetailsAction) {
                        {
                            HoscatBottomSecondaryAction(
                                label = "카드별 설명",
                                onClick = onShowDetails,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    } else {
                        null
                    },
                    second = if (showLlmAction) {
                        {
                            HoscatBottomSecondaryAction(
                                label = "AI 해석",
                                onClick = onShowLlmPrompt,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    } else {
                        null
                    }
                )
            }
            HoscatBottomCta(
                label = if (readingSaved) "저장됨" else "저장",
                onClick = onSaveReading,
                modifier = Modifier
                    .fillMaxWidth(),
                containerColor = if (readingSaved) harmonySecondaryPanel else harmonyBlue,
                contentColor = if (readingSaved) harmonyInk else Color.White
            ) {
                if (readingSaved) {
                    HarmonyIconGlyph(HarmonyIcon.Check, harmonyBlue, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    if (readingSaved) "저장됨" else "저장",
                    color = if (readingSaved) harmonyInk else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (showSpreadDetails) {
            SpreadDetailsSheetV2(
                spread = spread,
                drawnCards = drawnCards,
                adsDisabled = adsDisabled,
                onCardClick = onCardClick,
                onClose = onCloseDetails
            )
        }

        if (showLlmPrompt) {
            val llmPrompt = remember(spread, question, drawnCards, deckAiPrompt) {
                runCatching {
                    buildSpreadLlmPrompt(spread, question, drawnCards, deckAiPrompt)
                }.getOrElse {
                    "선택한 카드와 스프레드 정보를 바탕으로 타로 리딩을 도와주세요."
                }
            }
            LlmPromptSheetV2(
                prompt = llmPrompt,
                adsDisabled = adsDisabled,
                onClose = onCloseLlmPrompt
            )
        }

        if (expandedCard != null) {
            ExpandedCardOverlay(
                drawnCard = expandedCard,
                adsDisabled = adsDisabled,
                onClose = onCloseExpanded
            )
        }
    }
}

@Composable
private fun VariableSpreadLayout(
    modifier: Modifier,
    spread: SpreadOption,
    drawnCards: List<DrawnCard>,
    onCardClick: (DrawnCard) -> Unit
) {
    SpreadBoardLayout(
        modifier = modifier,
        spread = spread,
        drawnCards = drawnCards,
        onCardClick = onCardClick
    )
}

@Composable
internal fun SpreadBoardLayout(
    modifier: Modifier,
    spread: SpreadOption,
    drawnCards: List<DrawnCard>,
    onCardClick: (DrawnCard) -> Unit
) {
    if (spread.layoutId == "mini_celtic" && drawnCards.size >= 6) {
        MiniCelticSpreadBoard(
            modifier = modifier,
            spread = spread,
            drawnCards = drawnCards,
            onCardClick = onCardClick
        )
        return
    }
    if (spread.layoutId == "celtic_cross" && drawnCards.size >= 10) {
        CelticCrossSpreadBoard(
            modifier = modifier,
            spread = spread,
            drawnCards = drawnCards,
            onCardClick = onCardClick
        )
        return
    }
    val slots = spreadSlots(spread, drawnCards.size)
    if (drawnCards.isEmpty() || slots.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("선택된 카드가 없습니다", color = harmonySub, fontSize = 14.sp)
        }
        return
    }
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val maxX = slots.maxOf { it.x }
        val maxY = slots.maxOf { it.y }
        val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
        val profile = spreadBoardProfile(spread.layoutId, fontScale)
        val labelH = profile.labelHeight
        val cardLabelGap = profile.cardLabelGap
        val slotGapX = profile.slotGapX
        val slotGapY = profile.slotGapY
        val labelExtraW = profile.labelExtraWidth
        val duplicateLabelGap = 2.dp
        val duplicateCounts = slots.groupingBy { "${it.x}:${it.y}" }.eachCount()
        val maxDuplicateLabels = duplicateCounts.values.maxOrNull() ?: 1
        val slotLabelH = labelH * maxDuplicateLabels + duplicateLabelGap * (maxDuplicateLabels - 1).coerceAtLeast(0)
        val aspectRatio = 0.62f
        val inverseAspectRatio = 1f / aspectRatio
        val rotationWidthFactor = slots.maxOf { slot ->
            val radians = Math.toRadians(abs(slot.rotation).toDouble())
            (cos(radians) + inverseAspectRatio * sin(radians)).toFloat()
        }
        val rotationHeightFactor = slots.maxOf { slot ->
            val radians = Math.toRadians(abs(slot.rotation).toDouble())
            (inverseAspectRatio * cos(radians) + sin(radians)).toFloat()
        }
        val visualOffsetReserve = slots.maxOf { slot ->
            relationshipClearingVisualOffset(spread.layoutId, slot.y)
        }
        val slotWFromWidth = (maxWidth - slotGapX * maxX) / (maxX + 1f)
        val cardWFromWidth = minOf(
            (slotWFromWidth - labelExtraW).coerceAtLeast(32.dp),
            slotWFromWidth / rotationWidthFactor
        )
        val slotHFromHeight =
            (maxHeight - visualOffsetReserve - slotGapY * maxY) / (maxY + 1f)
        val cardWFromHeight =
            ((slotHFromHeight - cardLabelGap - slotLabelH) / rotationHeightFactor).coerceAtLeast(28.dp)
        val baseCardW = minOf(cardWFromWidth, cardWFromHeight)
        val cardW = (baseCardW * profile.cardScale).coerceAtMost(minOf(cardWFromWidth, cardWFromHeight))
        val cardH = cardW / 0.62f
        val visualCardW = cardW * rotationWidthFactor
        val visualCardH = cardW * rotationHeightFactor
        val labelW = maxOf(cardW + labelExtraW, visualCardW).coerceAtMost(slotWFromWidth)
        val slotW = labelW + slotGapX
        val slotH = visualCardH + cardLabelGap + slotLabelH + slotGapY
        val boardW = labelW + slotW * maxX
        val boardH = visualCardH + cardLabelGap + slotLabelH + slotH * maxY + visualOffsetReserve
        val duplicateSeen = mutableMapOf<String, Int>()

        Box(
            modifier = Modifier
                .width(boardW)
                .height(boardH)
                .offset(y = profile.boardOffsetY)
                .align(Alignment.Center)
        ) {
            drawnCards.take(slots.size).forEachIndexed { index, card ->
                val slot = slots[index]
                val duplicateKey = "${slot.x}:${slot.y}"
                val duplicateIndex = duplicateSeen.getOrDefault(duplicateKey, 0)
                duplicateSeen[duplicateKey] = duplicateIndex + 1
                Column(
                    modifier = Modifier
                        .width(labelW)
                        .offset(
                            x = slotW * slot.x,
                            y = slotH * slot.y + relationshipClearingVisualOffset(spread.layoutId, slot.y)
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(visualCardH),
                        contentAlignment = Alignment.Center
                    ) {
                        TarotArtCard(
                            modifier = Modifier
                                .width(cardW)
                                .height(cardH)
                                .graphicsLayer { rotationZ = slot.rotation },
                            drawnCard = card,
                            large = drawnCards.size <= 3,
                            onClick = { onCardClick(card) }
                        )
                    }
                    Spacer(Modifier.height(cardLabelGap + (labelH + duplicateLabelGap) * duplicateIndex))
                    Text(
                        positionMeaning(spread, card.order),
                        color = harmonySub,
                        fontSize = when {
                            drawnCards.size <= 3 -> 12.sp
                            profile.protectsLabels -> 9.sp
                            else -> 10.sp
                        },
                        lineHeight = when {
                            drawnCards.size <= 3 -> 15.sp
                            profile.protectsLabels -> 11.sp
                            else -> 13.sp
                        },
                        maxLines = profile.labelMaxLines,
                        overflow = profile.labelOverflow,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(labelH)
                            .semantics { contentDescription = "위치 의미: ${positionMeaning(spread, card.order)}" }
                    )
                }
            }
        }
    }
}

private val protectedLabelSpreadLayoutIds = setOf(
    "horseshoe",
    "relationship_clearing",
    "tarot_v",
    "crow_seven",
    "crow_eight"
)

private val loweredSpecialSpreadLayoutIds = setOf(
    "tarot_v",
    "crow_seven",
    "crow_eight"
)

private data class SpreadBoardProfile(
    val protectsLabels: Boolean,
    val allowsTwoLineLabels: Boolean,
    val labelHeight: Dp,
    val cardLabelGap: Dp,
    val slotGapX: Dp,
    val slotGapY: Dp,
    val labelExtraWidth: Dp,
    val boardOffsetY: Dp,
    val cardScale: Float,
    val labelMaxLines: Int,
    val labelOverflow: TextOverflow
)

private fun spreadBoardProfile(layoutId: String, fontScale: Float): SpreadBoardProfile {
    val isDenseMeaningSpread = layoutId == "celtic_cross" || layoutId == "mini_celtic"
    val protectsLabels = layoutId in protectedLabelSpreadLayoutIds
    val lowerAndSlightlyEnlargeCards = layoutId in loweredSpecialSpreadLayoutIds
    val isRelationshipClearing = layoutId == "relationship_clearing"
    return SpreadBoardProfile(
        protectsLabels = protectsLabels,
        allowsTwoLineLabels = isDenseMeaningSpread || protectsLabels,
        labelHeight = (when {
            isRelationshipClearing -> 38.dp
            isDenseMeaningSpread || protectsLabels -> 36.dp
            else -> 28.dp
        }) * fontScale,
        cardLabelGap = when {
            isRelationshipClearing -> 6.dp
            isDenseMeaningSpread || protectsLabels -> 4.dp
            else -> 4.dp
        },
        slotGapX = if (protectsLabels || isDenseMeaningSpread) 8.dp else 6.dp,
        slotGapY = if (protectsLabels) 12.dp else if (isDenseMeaningSpread) 8.dp else 6.dp,
        labelExtraWidth = if (isRelationshipClearing) 24.dp else 0.dp,
        boardOffsetY = if (lowerAndSlightlyEnlargeCards) 6.dp else 0.dp,
        cardScale = if (lowerAndSlightlyEnlargeCards) 1.03f else 1f,
        labelMaxLines = when {
            isRelationshipClearing -> 3
            isDenseMeaningSpread || protectsLabels -> 2
            else -> 1
        },
        labelOverflow = if (isRelationshipClearing) TextOverflow.Clip else TextOverflow.Ellipsis
    )
}

private fun relationshipClearingVisualOffset(layoutId: String, slotY: Float): Dp {
    if (layoutId != "relationship_clearing") return 0.dp
    return if (slotY >= 2.5f) 6.dp else 10.dp
}

private fun Dp.limitTo(min: Dp, max: Dp): Dp = when {
    this < min -> min
    this > max -> max
    else -> this
}

@Composable
private fun MiniCelticSpreadBoard(
    modifier: Modifier,
    spread: SpreadOption,
    drawnCards: List<DrawnCard>,
    onCardClick: (DrawnCard) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val sideGap = 18.dp
        val rowGap = 3.dp
        val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
        val labelH = 28.dp * fontScale
        val cardLabelGap = 4.dp
        val centerBoxAspectWidth = 1f / 0.62f
        val cardWByWidth = (maxWidth - sideGap * 2) / (2f + centerBoxAspectWidth)
        val cardWByHeight = ((maxHeight - rowGap * 2) / 3f - cardLabelGap - labelH) * 0.62f
        val cardW = (if (cardWByWidth < cardWByHeight) cardWByWidth else cardWByHeight)
            .limitTo(34.dp, 82.dp)
        val cardH = cardW / 0.62f
        val crossW = cardH
        val crossH = cardH

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(rowGap)
        ) {
                SpreadCardWithLabel(
                    card = drawnCards[4],
                    spread = spread,
                    cardW = cardW,
                    cardH = cardH,
                    labelH = labelH,
                    cardLabelGap = cardLabelGap,
                    onCardClick = onCardClick
                )
            Row(
                horizontalArrangement = Arrangement.spacedBy(sideGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpreadCardWithLabel(
                    card = drawnCards[3],
                    spread = spread,
                    cardW = cardW,
                    cardH = cardH,
                    labelH = labelH,
                    cardLabelGap = cardLabelGap,
                    onCardClick = onCardClick
                )
                CelticCrossCenterPair(
                    first = drawnCards[0],
                    second = drawnCards[1],
                    spread = spread,
                    cardW = cardW,
                    cardH = cardH,
                    boxW = crossW,
                    boxH = crossH,
                    labelH = labelH,
                    cardLabelGap = cardLabelGap,
                    onCardClick = onCardClick
                )
                SpreadCardWithLabel(
                    card = drawnCards[5],
                    spread = spread,
                    cardW = cardW,
                    cardH = cardH,
                    labelH = labelH,
                    cardLabelGap = cardLabelGap,
                    onCardClick = onCardClick
                )
            }
            SpreadCardWithLabel(
                card = drawnCards[2],
                spread = spread,
                cardW = cardW,
                cardH = cardH,
                labelH = labelH,
                cardLabelGap = cardLabelGap,
                onCardClick = onCardClick
            )
        }
    }
}

@Composable
private fun CelticCrossSpreadBoard(
    modifier: Modifier,
    spread: SpreadOption,
    drawnCards: List<DrawnCard>,
    onCardClick: (DrawnCard) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
        val labelH = 28.dp * fontScale
        val crossGap = 3.dp
        val rowGap = 6.dp
        val sideGap = 24.dp
        val cardLabelGap = 4.dp
        val heightBoundCardW =
            ((maxHeight - crossGap * 3 - (labelH + cardLabelGap) * 4) / 4f * 0.62f)
                .coerceAtLeast(30.dp)
        val fixedHorizontalSpace = sideGap * 2 + 18.dp + 34.dp
        val widthBoundCardW =
            ((maxWidth - fixedHorizontalSpace) / (3f + (1f / 0.62f))).coerceAtLeast(30.dp)
        val cardW = (if (widthBoundCardW < heightBoundCardW) widthBoundCardW else heightBoundCardW)
            .limitTo(30.dp, 56.dp)
        val cardH = cardW / 0.62f
        val crossBox = cardH

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(rowGap)
            ) {
                SpreadCardWithLabel(
                    card = drawnCards[2],
                    spread = spread,
                    cardW = cardW,
                    cardH = cardH,
                    labelH = labelH,
                    cardLabelGap = cardLabelGap,
                    onCardClick = onCardClick
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(sideGap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpreadCardWithLabel(
                        card = drawnCards[4],
                        spread = spread,
                        cardW = cardW,
                        cardH = cardH,
                        labelH = labelH,
                        cardLabelGap = cardLabelGap,
                        onCardClick = onCardClick
                    )
                    CelticCrossCenterPair(
                        first = drawnCards[0],
                        second = drawnCards[1],
                        spread = spread,
                        cardW = cardW,
                        cardH = cardH,
                        boxW = crossBox,
                        boxH = crossBox,
                        labelH = labelH,
                        cardLabelGap = cardLabelGap,
                        onCardClick = onCardClick
                    )
                    SpreadCardWithLabel(
                        card = drawnCards[5],
                        spread = spread,
                        cardW = cardW,
                        cardH = cardH,
                        labelH = labelH,
                        cardLabelGap = cardLabelGap,
                        onCardClick = onCardClick
                    )
                }
                SpreadCardWithLabel(
                    card = drawnCards[3],
                    spread = spread,
                    cardW = cardW,
                    cardH = cardH,
                    labelH = labelH,
                    cardLabelGap = cardLabelGap,
                    onCardClick = onCardClick
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(crossGap)
            ) {
                listOf(9, 8, 7, 6).forEach { index ->
                    SpreadCardWithLabel(
                        card = drawnCards[index],
                        spread = spread,
                        cardW = cardW,
                        cardH = cardH,
                        labelW = cardW + 34.dp,
                        labelH = labelH,
                        cardLabelGap = cardLabelGap,
                        onCardClick = onCardClick
                    )
                }
            }
        }
    }
}

@Composable
private fun CelticCrossCenterPair(
    first: DrawnCard,
    second: DrawnCard,
    spread: SpreadOption,
    cardW: Dp,
    cardH: Dp,
    boxW: Dp,
    boxH: Dp,
    labelH: Dp,
    cardLabelGap: Dp = 6.dp,
    onCardClick: (DrawnCard) -> Unit
) {
    Column(
        modifier = Modifier.width(boxW),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(boxW)
                .height(boxH),
            contentAlignment = Alignment.Center
        ) {
            TarotArtCard(
                modifier = Modifier
                    .width(cardW)
                    .height(cardH),
                drawnCard = first,
                large = false,
                onClick = { onCardClick(first) }
            )
            TarotArtCard(
                modifier = Modifier
                    .width(cardW)
                    .height(cardH)
                    .graphicsLayer { rotationZ = 90f },
                drawnCard = second,
                large = false,
                onClick = { onCardClick(second) }
            )
        }
        Spacer(Modifier.height(cardLabelGap))
        Text(
            "세로: ${positionMeaning(spread, first.order)}",
            color = harmonySub,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(labelH / 2)
                .semantics { contentDescription = "세로 위치 의미: ${positionMeaning(spread, first.order)}" }
        )
        Text(
            "가로: ${positionMeaning(spread, second.order)}",
            color = harmonySub,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(labelH / 2)
                .semantics { contentDescription = "가로 위치 의미: ${positionMeaning(spread, second.order)}" }
        )
    }
}

@Composable
private fun SpreadCardWithLabel(
    card: DrawnCard,
    spread: SpreadOption,
    cardW: Dp,
    cardH: Dp,
    labelW: Dp = cardW,
    labelH: Dp,
    cardLabelGap: Dp = 6.dp,
    onCardClick: (DrawnCard) -> Unit
) {
    Column(
        modifier = Modifier.width(labelW),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TarotArtCard(
            modifier = Modifier
                .width(cardW)
                .height(cardH),
            drawnCard = card,
            large = false,
            onClick = { onCardClick(card) }
        )
        Spacer(Modifier.height(cardLabelGap))
        Text(
            positionMeaning(spread, card.order),
            color = harmonySub,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(labelH)
                .semantics { contentDescription = "위치 의미: ${positionMeaning(spread, card.order)}" }
        )
    }
}

@Composable
private fun SpreadDetailRow(
    spread: SpreadOption,
    drawnCard: DrawnCard,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(harmonySecondaryPanel.copy(alpha = 0.74f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(42.dp)
                .aspectRatio(0.62f)
                .clip(RoundedCornerShape(7.dp))
                .background(harmonyPanel)
                .border(1.dp, harmonyDivider, RoundedCornerShape(7.dp))
        ) {
            TarotImage(
                card = drawnCard.card,
                isReversed = drawnCard.direction == CardDirection.Reversed,
                maxTextureSize = 384
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "${drawnCard.order.toString().padStart(2, '0')}  ${drawnCard.card.nameKr}",
                color = harmonyInk,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${drawnCard.direction.label} · ${positionMeaning(spread, drawnCard.order)}",
                color = harmonySub,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                directionalMeaningSnapshot(drawnCard.card, drawnCard.direction),
                color = harmonySub,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ExpandedCardOverlay(
    drawnCard: DrawnCard,
    adsDisabled: Boolean,
    onClose: () -> Unit
) {
    val displayMeaning = directionalMeaningSnapshot(drawnCard.card, drawnCard.direction)
    val hasDescription = displayMeaning.isNotBlank()
    AppBottomSheet(
        heightFraction = if (hasDescription) 0.82f else 0.72f,
        adsDisabled = adsDisabled,
        onClose = onClose
    ) {
        SheetHeader(
            title = drawnCard.card.nameKr,
            subtitle = "${drawnCard.card.nameEn} · ${drawnCard.direction.label}",
            closeDescription = "카드 상세 닫기",
            onClose = onClose
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HarmonyCard(
                modifier = Modifier.fillMaxWidth(),
                padding = PaddingValues(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(430.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TarotArtCard(
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .aspectRatio(0.62f),
                        drawnCard = drawnCard,
                        large = true,
                        onClick = onClose
                    )
                }
            }
            if (hasDescription) {
                HarmonyCard(padding = PaddingValues(14.dp)) {
                    Text(
                        "${drawnCard.order.toString().padStart(2, '0')}  ${drawnCard.card.nameKr}",
                        color = harmonyInk,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        drawnCard.direction.label,
                        color = harmonySub,
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(displayMeaning, color = harmonyInk, fontSize = 17.sp, lineHeight = 27.sp)
                }
            }
        }
    }
}

@Composable
private fun TarotArtCard(
    modifier: Modifier,
    drawnCard: DrawnCard,
    large: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (large) 10.dp else 4.dp,
                shape = RoundedCornerShape(if (large) 18.dp else 10.dp),
                ambientColor = Color(0x16000000),
                spotColor = Color(0x12000000)
            )
            .clip(RoundedCornerShape(if (large) 18.dp else 10.dp))
            .background(harmonyPanel)
            .border(1.dp, harmonyDivider.copy(alpha = 0.8f), RoundedCornerShape(if (large) 18.dp else 10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        TarotImage(
            card = drawnCard.card,
            isReversed = drawnCard.direction == CardDirection.Reversed,
            maxTextureSize = if (large) 960 else 512,
            accessibilityDescription = "${drawnCard.card.nameKr}, ${drawnCard.direction.label}"
        )
    }
}
