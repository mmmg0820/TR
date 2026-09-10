package com.softcat.mystictarot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softcat.mystictarot.ui.components.HarmonyIcon
import com.softcat.mystictarot.ui.components.HarmonyIconGlyph
import kotlin.math.abs
import kotlin.math.min

@Composable
fun SpreadSelectScreen(
    selectedSpread: SpreadOption,
    recentSpread: SpreadOption?,
    useCounts: Map<String, Int>,
    onBack: () -> Unit,
    onSpreadClick: (SpreadOption) -> Unit,
    listState: LazyListState,
    bottomPadding: Dp
) {
    var selectedCategory by remember { mutableStateOf(SpreadCategory.Recent) }
    var stagedSpread by remember(selectedSpread.key) { mutableStateOf(selectedSpread) }
    val categories = SpreadCategory.entries.toList()
    val visibleSpreads = remember(selectedCategory, recentSpread, useCounts) {
        spreadsForCategory(selectedCategory, recentSpread, useCounts)
    }
    val visibleSpreadGroups = remember(visibleSpreads) {
        groupSpreadsByPreview(visibleSpreads)
    }
    var actionStackHeight by remember { mutableStateOf(hoscatInitialActionStackHeight(1)) }
    val actionBottomPadding = hoscatActionStackBottomPadding(bottomPadding)
    val contentBottomPadding = hoscatContentBottomPaddingForActionStack(
        actionStackHeight = actionStackHeight,
        actionStackBottomPadding = actionBottomPadding
    )

    fun moveCategory(delta: Int) {
        val currentIndex = categories.indexOf(selectedCategory)
        selectedCategory = categories[(currentIndex + delta).coerceIn(0, categories.lastIndex)]
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = contentBottomPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompactTopBar(
                title = "스프레드 선택",
                subtitle = "",
                onBack = onBack,
                action = null
            )

            SpreadCategorySelector(
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(selectedCategory) {
                        var dragX = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragX = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                dragX += dragAmount
                                if (abs(dragX) > 56f) {
                                    moveCategory(if (dragX < 0f) 1 else -1)
                                    dragX = 0f
                                    change.consume()
                                }
                            },
                            onDragEnd = { dragX = 0f },
                            onDragCancel = { dragX = 0f }
                        )
                    }
            ) {
                AnimatedContent(
                    targetState = selectedCategory,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                        (
                            fadeIn(tween(200, easing = FastOutSlowInEasing)) +
                                slideInHorizontally(tween(200, easing = FastOutSlowInEasing)) { direction * it / 5 }
                            ).togetherWith(
                            fadeOut(tween(160, easing = FastOutSlowInEasing)) +
                                slideOutHorizontally(tween(160, easing = FastOutSlowInEasing)) { -direction * it / 5 }
                        )
                    },
                    label = "spread-category"
                ) { category ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when {
                            visibleSpreads.isEmpty() -> item { EmptySpreadCategory(category) }
                            category == SpreadCategory.Count -> item {
                                CountSpreadGrid(
                                    spreads = visibleSpreads,
                                    selectedKey = stagedSpread.key,
                                    onSelect = { stagedSpread = it }
                                )
                            }
                            else -> {
                                items(visibleSpreadGroups) { group ->
                                    SpreadChoiceGroupRow(
                                        group = group,
                                        selectedKey = stagedSpread.key,
                                        onSelect = { stagedSpread = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        HoscatBottomActionStack(
            bottomPadding = bottomPadding,
            onMeasuredHeightChanged = { actionStackHeight = it }
        ) {
            HoscatBottomCta(
                label = "이 스프레드로 리딩",
                onClick = { onSpreadClick(stagedSpread) },
                fontSize = 16.sp
            )
        }
    }
}

private fun arrangementOptionLabel(option: SpreadOption, options: List<SpreadOption>): String {
    val title = option.positionPresetTitle
    val duplicated = options.count { it.positionPresetTitle == title } > 1
    if (!duplicated) return title
    val layoutHint = option.layoutTitle.takeIf { it.isNotBlank() && it != title }
    return listOfNotNull(title, layoutHint).joinToString(" · ")
}

@Composable
private fun QuestionExampleChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    HoscatActionControlSurface(
        label = "질문 예시: $label",
        onClick = onClick,
        modifier = modifier,
        containerColor = harmonySecondaryPanel.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, harmonyDivider.copy(alpha = 0.58f)),
        visualHeight = HoscatCompactActionVisualHeight,
        maxVisualHeight = HoscatCompactActionMaxVisualHeight,
        touchVerticalInset = HoscatCompactActionTouchVerticalInset,
        radius = 16.dp
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp),
            color = harmonyInk,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

internal enum class SpreadCategory(val label: String) {
    Recent("최근 사용"),
    Count("1~10장"),
    Theme("테마")
}

internal fun spreadsForCategory(
    category: SpreadCategory,
    recentSpread: SpreadOption?,
    useCounts: Map<String, Int>
): List<SpreadOption> {
    return when (category) {
        SpreadCategory.Recent -> {
            val frequent = useCounts.entries
                .sortedByDescending { it.value }
                .mapNotNull { entry -> selectableSpreadOptions.firstOrNull { it.key == entry.key } }
            (listOfNotNull(recentSpread?.takeIf { it.isSelectableSpreadOption() }) + frequent).distinctBy { it.key }.take(5)
        }
        SpreadCategory.Count -> selectableSpreadOptions
            .filter { it.key in countSpreadKeys }
            .sortedBy { countSpreadKeys.indexOf(it.key) }
        SpreadCategory.Theme -> selectableSpreadOptions
            .filter { it.key in themeSpreadKeys }
            .sortedBy { themeSpreadKeys.indexOf(it.key) }
    }
}

internal val themeSpreadKeys = listOf(
    spreadKey("celtic_cross", "classic"),
    spreadKey("mini_celtic", "mini_celtic_cross"),
    spreadKey("relationship_clearing", "release_flow"),
    spreadKey("horseshoe", "horseshoe_flow"),
    spreadKey("magic_seven", "magic_seven_flow"),
    spreadKey("wheel_of_fortune", "wheel_flow"),
    spreadKey("tarot_v", "v_flow"),
    spreadKey("crow_seven", "crow_seven_flow"),
    spreadKey("crow_eight", "crow_eight_flow"),
    spreadKey("either_or_five", "either_or_flow")
)

internal val countSpreadKeys = listOf(
    spreadKey("one_card", "daily"),
    spreadKey("two_cards", "now_next"),
    spreadKey("three_cards", "past_present_future"),
    spreadKey("four_cards", "situation_obstacle_advice_result"),
    spreadKey("five_cross", "core_flow"),
    spreadKey("six_cards", "career_path"),
    spreadKey("horseshoe", "horseshoe_flow"),
    spreadKey("eight_cards", "wide_flow"),
    spreadKey("nine_cards", "nine_grid"),
    spreadKey("ten_cards", "ten_step")
)

internal data class SpreadChoiceGroup(val spreads: List<SpreadOption>) {
    val representative: SpreadOption get() = spreads.first()
    val selectedKeys: Set<String> get() = spreads.map { it.key }.toSet()
}

internal fun groupSpreadsByPreview(spreads: List<SpreadOption>): List<SpreadChoiceGroup> {
    val groups = linkedMapOf<String, MutableList<SpreadOption>>()
    spreads.forEach { spread ->
        groups.getOrPut(previewSignature(spread)) { mutableListOf() }.add(spread)
    }
    return groups.values.map { SpreadChoiceGroup(it) }
}

internal fun previewSignature(spread: SpreadOption): String {
    return previewSlots(spread).joinToString("|") { slot ->
        "${slot.x}:${slot.y}:${slot.rotation}"
    }
}

@Composable
internal fun SpreadCategorySelector(selected: SpreadCategory, onSelect: (SpreadCategory) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SpreadCategory.entries.forEach { category ->
            val isSelected = category == selected
            HoscatActionControlSurface(
                label = category.label,
                onClick = { onSelect(category) },
                modifier = Modifier.weight(1f),
                containerColor = if (isSelected) harmonySecondaryPanel else Color.Transparent,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) harmonyBlue.copy(alpha = 0.55f) else harmonyDivider.copy(alpha = 0.7f)
                ),
                visualHeight = HoscatCompactActionVisualHeight,
                maxVisualHeight = HoscatCompactActionMaxVisualHeight,
                touchVerticalInset = HoscatCompactActionTouchVerticalInset,
                radius = HoscatCompactActionRadius
            ) {
                Text(
                    text = category.label,
                    modifier = Modifier.padding(horizontal = 6.dp),
                    color = if (isSelected) harmonyBlue else harmonyInk,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
internal fun CountSpreadGrid(
    spreads: List<SpreadOption>,
    selectedKey: String,
    onSelect: (SpreadOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        spreads.chunked(2).forEach { rowSpreads ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSpreads.forEach { spread ->
                    CountSpreadButton(
                        spread = spread,
                        label = countSpreadDisplayLabel(spread, spreads),
                        selected = spread.key == selectedKey,
                        onClick = { onSelect(spread) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowSpreads.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun CountSpreadButton(
    spread: SpreadOption,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) harmonySecondaryPanel else Color.Transparent)
            .border(
                1.dp,
                if (selected) harmonyBlue.copy(alpha = 0.55f) else harmonyDivider.copy(alpha = 0.62f),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = "$label 스프레드"
            }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpreadPreviewTile(
            spread = spread,
            selected = selected,
            modifier = Modifier
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            color = if (selected) harmonyBlue else harmonyInk,
            fontSize = if (label.length > 3) 14.sp else 16.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (selected) {
            Spacer(Modifier.weight(1f))
            HarmonyIconGlyph(HarmonyIcon.Check, harmonyBlue, Modifier.size(18.dp))
        }
    }
}

private fun countSpreadDisplayLabel(spread: SpreadOption, spreads: List<SpreadOption>): String {
    val duplicatedCount = spreads.count { it.cardCount == spread.cardCount } > 1
    if (!duplicatedCount) return "${spread.cardCount}장"
    val shortName = when (spread.layoutId) {
        "five_cross" -> "기본"
        else -> spread.positionPresetTitle
            .replace("스프레드", "")
            .replace("배열", "")
            .trim()
            .take(5)
            .ifBlank { spread.layoutTitle.take(5) }
    }
    return "${spread.cardCount}장 · $shortName"
}

@Composable
internal fun SpreadChoiceGroupRow(
    group: SpreadChoiceGroup,
    selectedKey: String,
    onSelect: (SpreadOption) -> Unit
) {
    val representative = group.representative
    val selected = selectedKey in group.selectedKeys
    val single = group.spreads.size == 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) harmonySecondaryPanel else Color.Transparent)
            .border(
                1.dp,
                if (selected) harmonyBlue.copy(alpha = 0.55f) else harmonyDivider.copy(alpha = 0.62f),
                RoundedCornerShape(18.dp)
            )
            .then(if (single) Modifier.clickable { onSelect(representative) } else Modifier)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpreadPreviewTile(
            spread = representative,
            selected = selected,
            modifier = Modifier.clickable { onSelect(representative) }
        )
        Spacer(Modifier.width(13.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(if (single) 0.dp else 6.dp)
        ) {
            group.spreads.forEach { spread ->
                val optionSelected = spread.key == selectedKey
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (optionSelected && !single) harmonyBlue.copy(alpha = 0.10f) else Color.Transparent)
                        .clickable { onSelect(spread) }
                        .padding(horizontal = if (single) 0.dp else 8.dp, vertical = if (single) 0.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        spread.positionPresetTitle,
                        color = if (optionSelected) harmonyBlue else harmonyInk,
                        fontSize = 16.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (selected) {
            Spacer(Modifier.width(10.dp))
            HarmonyIconGlyph(HarmonyIcon.Check, harmonyBlue, Modifier.size(20.dp))
        }
    }
}

@Composable
internal fun SpreadPreviewTile(spread: SpreadOption, selected: Boolean, modifier: Modifier = Modifier) {
    val color = if (selected) harmonyBlue else harmonySub
    val slots = remember(spread.key, spread.cardCount) { previewSlots(spread) }
    val previewDescription = if (spread.key in countSpreadKeys) {
        "${spread.cardCount}장 스프레드 그림"
    } else {
        "${spread.positionPresetTitle} 스프레드 그림"
    }
    Box(
        modifier = modifier
            .width(68.dp)
            .height(50.dp)
            .semantics { contentDescription = previewDescription }
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = if (selected) 0.14f else 0.08f))
            .border(1.dp, color.copy(alpha = if (selected) 0.42f else 0.16f), RoundedCornerShape(16.dp))
            .padding(horizontal = 7.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (slots.isEmpty()) return@Canvas
            val minX = slots.minOf { it.x }
            val minY = slots.minOf { it.y }
            val maxX = slots.maxOf { it.x }
            val maxY = slots.maxOf { it.y }
            val columns = (maxX - minX + 1f).coerceAtLeast(1f)
            val rows = (maxY - minY + 1f).coerceAtLeast(1f)
            val cellW = size.width / columns
            val cellH = size.height / rows
            val cardW = min(cellW * 0.68f, cellH * 0.48f).coerceAtLeast(5.2f)
            val cardH = (cardW / 0.62f).coerceAtMost(cellH * 0.9f)

            slots.forEach { slot ->
                val x = (slot.x - minX) * cellW + cellW / 2f - cardW / 2f
                val y = (slot.y - minY) * cellH + cellH / 2f - cardH / 2f
                rotate(slot.rotation, pivot = Offset(x + cardW / 2f, y + cardH / 2f)) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(cardW, cardH),
                        cornerRadius = CornerRadius(cardW * 0.18f, cardW * 0.18f),
                        alpha = if (selected) 0.95f else 0.72f
                    )
                }
            }
        }
    }
}

internal fun previewSlots(spread: SpreadOption): List<SpreadSlot> {
    if (spread.key in countSpreadKeys) {
        return countPreviewRows(spread.cardCount).flatMapIndexed { rowIndex, count ->
            val offset = (5 - count) / 2f
            List(count) { columnIndex ->
                SpreadSlot(offset + columnIndex, rowIndex.toFloat())
            }
        }
    }
    return spreadSlots(spread, spread.cardCount)
}

private fun countPreviewRows(count: Int): List<Int> {
    return when (count) {
        1 -> listOf(1)
        2 -> listOf(2)
        3 -> listOf(3)
        4 -> listOf(4)
        5 -> listOf(5)
        6 -> listOf(3, 3)
        7 -> listOf(3, 1, 3)
        8 -> listOf(4, 4)
        9 -> listOf(3, 3, 3)
        10 -> listOf(5, 5)
        else -> listOf(count.coerceAtMost(5))
    }
}

@Composable
private fun EmptySpreadCategory(category: SpreadCategory) {
    HarmonyCard {
        Text("${category.label}이 없습니다", color = harmonyInk, fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("리딩을 진행하면 이곳에 최근 사용한 스프레드가 남습니다.", color = harmonySub, fontSize = 12.sp, lineHeight = 15.sp)
    }
}

@Composable
fun QuestionInputScreen(
    spread: SpreadOption,
    question: String,
    onQuestionChange: (String) -> Unit,
    onSpreadChange: (SpreadOption) -> Unit,
    onSavePositionLabels: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    bottomPadding: Dp
) {
    val focusManager = LocalFocusManager.current
    val fontScale = LocalDensity.current.fontScale
    val questionExampleColumns = if (fontScale >= 1.5f) 1 else 2
    val keyboardController = LocalSoftwareKeyboardController.current
    fun hideKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }
    val presetOptions = remember(spread.layoutId, spread.cardCount, spread.drawMode) {
        compatiblePositionPresets(spread)
    }
    var arrangementExpanded by remember(spread.cardCount, spread.drawMode) { mutableStateOf(false) }
    var customMeaningVisible by rememberSaveable(spread.key) {
        mutableStateOf(true)
    }
    val arrangementTitle = if (customMeaningVisible) {
        "직접 입력"
    } else {
        arrangementOptionLabel(spread, presetOptions)
    }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = harmonyBlue.copy(alpha = 0.62f),
        unfocusedBorderColor = harmonyDivider.copy(alpha = 0.74f),
        focusedContainerColor = harmonyPanel,
        unfocusedContainerColor = harmonySecondaryPanel.copy(alpha = 0.42f),
        cursorColor = harmonyBlue,
        focusedTextColor = harmonyInk,
        unfocusedTextColor = harmonyInk
    )
    var actionStackHeight by remember { mutableStateOf(hoscatInitialActionStackHeight(1)) }
    val actionBottomPadding = hoscatActionStackBottomPadding(bottomPadding)
    val contentBottomPadding = hoscatContentBottomPaddingForActionStack(
        actionStackHeight = actionStackHeight,
        actionStackBottomPadding = actionBottomPadding
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { hideKeyboard() })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = contentBottomPadding)
                .clipToBounds()
                .verticalScroll(rememberScrollState())
                .padding(top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompactTopBar(
                title = "질문 정리",
                subtitle = "${spread.cardCount}장",
                onBack = onBack,
                action = null
            )

            HarmonyCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "오늘 무엇을 묻고 싶나요?",
                        color = harmonyInk,
                        fontSize = 19.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${question.length}/240",
                        color = harmonyTertiary,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.End
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = question,
                    onValueChange = { onQuestionChange(it.take(240)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 132.dp),
                    placeholder = { Text("예: 이번 선택에서 제가 가장 먼저 봐야 할 것은 무엇일까요?") },
                    shape = RoundedCornerShape(18.dp),
	                    colors = fieldColors,
	                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
	                    keyboardActions = KeyboardActions(onDone = { hideKeyboard() }),
	                    minLines = 3,
	                    maxLines = 5
	                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "질문 예시",
                    color = harmonySub,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "관계를 정리하고 싶어요",
                        "선택지를 비교하고 싶어요",
                        "예/아니오에 가까운 답을 보고 싶어요",
                        "막힌 지점을 보고 싶어요"
                    ).chunked(questionExampleColumns).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { example ->
                                QuestionExampleChip(
                                    label = example,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        hideKeyboard()
                                        onQuestionChange(example.take(240))
                                    }
                                )
                            }
                            if (questionExampleColumns == 2 && rowItems.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            HarmonyCard {
                Text("카드 위치 의미", color = harmonyInk, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = MyangControlHeight)
                        .clip(RoundedCornerShape(18.dp))
                        .background(harmonySecondaryPanel.copy(alpha = 0.54f))
                        .border(1.dp, harmonyDivider.copy(alpha = 0.68f), RoundedCornerShape(18.dp))
                        .clickable {
                            hideKeyboard()
                            arrangementExpanded = !arrangementExpanded
                        }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "현재 의미 · $arrangementTitle",
                        color = harmonyInk,
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    HarmonyIconGlyph(HarmonyIcon.Chevron, harmonyTertiary, Modifier.size(18.dp))
                }
                AnimatedVisibility(
                    visible = arrangementExpanded,
                    enter = fadeIn(tween(120)) + expandVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)),
                    exit = fadeOut(tween(90)) + shrinkVertically(animationSpec = tween(150, easing = FastOutSlowInEasing))
                ) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetOptions.forEach { option ->
                            val selected = option.key == spread.key && !customMeaningVisible
                            val optionLabel = arrangementOptionLabel(option, presetOptions)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .sizeIn(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) harmonySecondaryPanel else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (selected) harmonyBlue.copy(alpha = 0.50f) else harmonyDivider.copy(alpha = 0.50f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        hideKeyboard()
                                        customMeaningVisible = false
                                        arrangementExpanded = false
                                        onSpreadChange(option)
                                    }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (selected) "$optionLabel · 선택됨" else optionLabel,
                                    color = if (selected) harmonyBlue else harmonyInk,
                                    fontSize = 13.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selected) {
                                    HarmonyIconGlyph(HarmonyIcon.Check, harmonyBlue, Modifier.size(17.dp))
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sizeIn(minHeight = 48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (customMeaningVisible) harmonySecondaryPanel else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (customMeaningVisible) harmonyBlue.copy(alpha = 0.50f) else harmonyDivider.copy(alpha = 0.50f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    hideKeyboard()
                                    customMeaningVisible = true
                                    arrangementExpanded = false
                                    onSpreadChange(spread.copy(positionPresetTitle = "직접 의미"))
                                }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (customMeaningVisible) "직접 입력 · 선택됨" else "직접 입력으로 바꾸기",
                                color = if (customMeaningVisible) harmonyBlue else harmonyInk,
                                fontSize = 13.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            if (customMeaningVisible) {
                                HarmonyIconGlyph(HarmonyIcon.Check, harmonyBlue, Modifier.size(17.dp))
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = customMeaningVisible,
                enter = fadeIn(tween(120)) + expandVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(90)) + shrinkVertically(animationSpec = tween(150, easing = FastOutSlowInEasing))
            ) {
                HarmonyCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "카드 위치 의미",
                            color = harmonyInk,
                            fontSize = 17.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier.height(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    hideKeyboard()
                                    onSavePositionLabels()
                                },
                                modifier = Modifier.height(38.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = harmonySecondaryPanel,
                                    contentColor = harmonyInk
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                            ) {
                                Text("저장", fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("각 카드가 무엇을 말해주면 좋을지 직접 정할 수 있습니다.", color = harmonySub, fontSize = 12.sp, lineHeight = 16.sp)
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        spread.positionLabels.chunked(2).forEachIndexed { rowIndex, rowLabels ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowLabels.forEachIndexed { columnIndex, label ->
                                    val index = rowIndex * 2 + columnIndex
                                    OutlinedTextField(
                                        value = label,
                                        onValueChange = { value ->
                                            val nextLabels = spread.positionLabels.toMutableList()
                                            nextLabels[index] = value.take(24)
                                            onSpreadChange(
                                                spread.copy(
                                                    positionPresetTitle = "직접 의미",
                                                    positionLabels = nextLabels
                                                )
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .sizeIn(minHeight = 48.dp),
                                        label = { Text("${index + 1}번") },
                                        shape = RoundedCornerShape(18.dp),
                                        colors = fieldColors,
                                        singleLine = true
                                    )
                                }
                                if (rowLabels.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        HoscatBottomActionStack(
            bottomPadding = bottomPadding,
            onMeasuredHeightChanged = { actionStackHeight = it }
        ) {
            HoscatBottomCta(
                label = "카드 선택으로 이동",
                onClick = {
                    hideKeyboard()
                    onContinue()
                },
                fontSize = 16.sp
            )
        }
    }
}
