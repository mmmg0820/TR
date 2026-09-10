package com.softcat.mystictarot

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.softcat.mystictarot.ui.components.HarmonyIcon
import com.softcat.mystictarot.ui.components.HarmonyIconGlyph
import com.softcat.mystictarot.ui.components.HarmonyIconButton
import com.softcat.mystictarot.ui.components.TarotImage
import kotlin.math.abs

@Composable
fun HistoryScreenV2(
    savedReadings: List<SavedReading>,
    savedSpreadPresets: List<SavedSpreadPreset>,
    onBack: () -> Unit,
    onOpenReading: (SavedReading) -> Unit,
    onUseSavedSpread: (SavedSpreadPreset) -> Unit,
    onSaveSpreadPreset: (SavedSpreadPreset) -> Unit,
    onDeleteSavedSpread: (String) -> Unit,
    onDeleteReadings: (Set<Long>) -> Unit,
    listState: LazyListState,
    bottomPadding: Dp
) {
    var section by remember { mutableStateOf(HistorySection.SavedTarot) }
    var showSpreadEditor by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Long>() }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var spreadToDelete by remember { mutableStateOf<SavedSpreadPreset?>(null) }
    fun toggleReading(reading: SavedReading) {
        if (selectedIds.contains(reading.id)) selectedIds.remove(reading.id) else selectedIds.add(reading.id)
        if (selectedIds.isEmpty()) selectionMode = false
    }

    if (showSpreadEditor) {
        SavedSpreadPresetEditor(
            onBack = { showSpreadEditor = false },
            onSave = {
                onSaveSpreadPreset(it)
                showSpreadEditor = false
            },
            bottomPadding = bottomPadding
        )
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .pointerInput(section) {
                var dragX = 0f
                detectHorizontalDragGestures(
                    onDragStart = { dragX = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        dragX += dragAmount
                        if (abs(dragX) > 56f) {
                            section = if (dragX < 0f) HistorySection.SavedSpread else HistorySection.SavedTarot
                            selectedIds.clear()
                            selectionMode = false
                            dragX = 0f
                            change.consume()
                        }
                    },
                    onDragEnd = { dragX = 0f },
                    onDragCancel = { dragX = 0f }
                )
            }
            .padding(horizontal = 20.dp)
            .padding(top = 14.dp, bottom = hoscatContentBottomPadding(bottomPadding)),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CompactTopBar(
                title = if (selectionMode) "기록 선택" else "기록",
                subtitle = if (selectionMode) "${selectedIds.size}개 선택됨" else "",
                onBack = {
                    if (selectionMode) {
                        selectedIds.clear()
                        selectionMode = false
                    } else {
                        onBack()
                    }
                },
                action = {
                    if (section == HistorySection.SavedTarot && selectionMode && selectedIds.isNotEmpty()) {
                        HarmonyIconButton(HarmonyIcon.Delete, "선택 기록 삭제") { showDeleteConfirm = true }
                    } else if (section == HistorySection.SavedTarot && savedReadings.isNotEmpty()) {
                        CompactTextAction("편집", "기록 편집") {
                            selectionMode = true
                        }
                    } else if (section == HistorySection.SavedSpread) {
                        CompactTextAction("추가", "스프레드 추가") {
                            showSpreadEditor = true
                        }
                    } else {
                        Spacer(Modifier.size(48.dp))
                    }
                }
            )
        }

        item {
            HistorySectionTabs(
                selected = section,
                onSelect = {
                    section = it
                    selectedIds.clear()
                    selectionMode = false
                }
            )
        }

        if (section == HistorySection.SavedTarot) {
            if (savedReadings.isEmpty()) {
                item {
                    HarmonyCard {
                        Text("저장된 타로가 없어요", color = harmonyInk, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text("마음에 남는 리딩을 저장하면 이곳에서 다시 볼 수 있어요.", color = harmonySub, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            } else {
                val readingsForDisplay = savedReadings.asReversed()
                items(
                    items = readingsForDisplay,
                    key = { it.id }
                ) { reading ->
                    HarmonyCard(padding = PaddingValues(0.dp)) {
                        HistoryReadingRowV2(
                            reading = reading,
                            selected = selectedIds.contains(reading.id),
                            selectionMode = selectionMode,
                            onClick = {
                                if (selectionMode) toggleReading(reading) else onOpenReading(reading)
                            },
                            onLongPress = {
                                selectionMode = true
                                if (!selectedIds.contains(reading.id)) selectedIds.add(reading.id)
                            }
                        )
                    }
                }
            }
        } else {
            if (savedSpreadPresets.isEmpty()) {
                item {
                    HarmonyCard {
                        Text("저장된 스프레드가 없어요", color = harmonyInk, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text("자주 쓰는 카드 위치 의미를 저장해두면 다음 리딩에서 바로 사용할 수 있어요.", color = harmonySub, fontSize = 13.sp, lineHeight = 18.sp)
                        Spacer(Modifier.height(10.dp))
                        HoscatBottomCta(
                            label = "스프레드 추가",
                            onClick = { showSpreadEditor = true },
                            containerColor = harmonySecondaryPanel,
                            contentColor = harmonyInk
                        )
                    }
                }
            } else {
                items(
                    items = savedSpreadPresets.sortedByDescending { it.updatedAt },
                    key = { it.id }
                ) { preset ->
                    SavedSpreadPresetRow(
                        preset = preset,
                        onUse = { onUseSavedSpread(preset) },
                        onDelete = { spreadToDelete = preset }
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteReadingsDialog(
            count = selectedIds.size,
            onCancel = { showDeleteConfirm = false },
            onDelete = {
                val ids = selectedIds.toSet()
                selectedIds.clear()
                selectionMode = false
                showDeleteConfirm = false
                onDeleteReadings(ids)
            }
        )
    }
    spreadToDelete?.let { preset ->
        DeleteSavedSpreadDialog(
            presetName = preset.name,
            onCancel = { spreadToDelete = null },
            onDelete = {
                spreadToDelete = null
                onDeleteSavedSpread(preset.id)
            }
        )
    }
}

private enum class HistorySection(val label: String) {
    SavedTarot("저장된 타로"),
    SavedSpread("저장된 스프레드")
}

@Composable
private fun HistorySectionTabs(selected: HistorySection, onSelect: (HistorySection) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        HistorySection.entries.forEach { section ->
            val isSelected = selected == section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .sizeIn(minHeight = HoscatBottomCtaMinTouchHeight)
                    .clickable { onSelect(section) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = section.label,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 8.dp),
                    color = if (isSelected) harmonyBlue else harmonySub,
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(if (isSelected) 84.dp else 0.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (isSelected) harmonyBlue else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun SavedSpreadPresetRow(
    preset: SavedSpreadPreset,
    onUse: () -> Unit,
    onDelete: () -> Unit
) {
    HarmonyCard(padding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    preset.name,
                    color = harmonyInk,
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${preset.cardCount}장 배열 · ${preset.positionPresetTitle}",
                    color = harmonySub,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    preset.positionLabels.take(4).mapIndexed { index, label -> "${index + 1}. $label" }.joinToString("  "),
                    color = harmonyTertiary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactTextAction("사용", "저장된 스프레드 사용", onUse)
                CompactTextAction("삭제", "저장된 스프레드 삭제", onDelete)
            }
        }
    }
}

@Composable
private fun SavedSpreadPresetEditor(
    onBack: () -> Unit,
    onSave: (SavedSpreadPreset) -> Unit,
    bottomPadding: Dp
) {
    val initialSpread = remember {
        spreadOptions.firstOrNull { it.key == spreadKey("one_card", "daily") } ?: spreadOptions.first()
    }
    var selectedCategory by remember { mutableStateOf(SpreadCategory.Count) }
    var selectedBase by remember { mutableStateOf(initialSpread) }
    var name by remember { mutableStateOf("${selectedBase.cardCount}장 스프레드") }
    var labels by remember(selectedBase.key) { mutableStateOf(selectedBase.positionLabels) }
    val categories = SpreadCategory.entries.toList()
    val visibleSpreads = remember(selectedCategory) {
        spreadsForCategory(selectedCategory, recentSpread = null, useCounts = emptyMap())
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
    fun selectBaseSpread(spread: SpreadOption) {
        selectedBase = spread
        name = "${spread.cardCount}장 스프레드"
        labels = spread.positionLabels
    }
    fun moveCategory(delta: Int) {
        val currentIndex = categories.indexOf(selectedCategory)
        selectedCategory = categories[(currentIndex + delta).coerceIn(0, categories.lastIndex)]
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
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = contentBottomPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CompactTopBar(
                    title = "스프레드 추가",
                    subtitle = "",
                    onBack = onBack,
                    action = null
                )
            }
            item {
                HarmonyCard {
                    Text("스프레드 이름", color = harmonyInk, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(24) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true,
                        colors = fieldColors
                    )
                }
            }
            item {
                HarmonyCard {
                    Text("스프레드 선택", color = harmonyInk, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("리딩에서 고르던 화면처럼 그림을 보고 저장할 배열을 선택합니다.", color = harmonySub, fontSize = 12.sp, lineHeight = 16.sp)
                    Spacer(Modifier.height(10.dp))
                    SpreadCategorySelector(
                        selected = selectedCategory,
                        onSelect = { selectedCategory = it }
                    )
                    Spacer(Modifier.height(10.dp))
                    Column(
                        modifier = Modifier.pointerInput(selectedCategory) {
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
                        },
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when {
                            visibleSpreads.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(66.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(1.dp, harmonyDivider.copy(alpha = 0.62f), RoundedCornerShape(18.dp))
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        "최근 사용한 스프레드가 없습니다",
                                        color = harmonySub,
                                        fontSize = 13.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            selectedCategory == SpreadCategory.Count -> {
                                CountSpreadGrid(
                                    spreads = visibleSpreads,
                                    selectedKey = selectedBase.key,
                                    onSelect = { selectBaseSpread(it) }
                                )
                            }
                            else -> {
                                visibleSpreadGroups.forEach { group ->
                                    SpreadChoiceGroupRow(
                                        group = group,
                                        selectedKey = selectedBase.key,
                                        onSelect = { selectBaseSpread(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                HarmonyCard {
                    Text("카드 위치 의미", color = harmonyInk, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("저장할 스프레드의 카드 위치 의미를 정합니다.", color = harmonySub, fontSize = 12.sp, lineHeight = 16.sp)
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        labels.chunked(2).forEachIndexed { rowIndex, rowLabels ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowLabels.forEachIndexed { columnIndex, label ->
                                    val index = rowIndex * 2 + columnIndex
                                    OutlinedTextField(
                                        value = label,
                                        onValueChange = { value ->
                                            labels = labels.toMutableList().also { it[index] = value.take(24) }
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
                                if (rowLabels.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
        HoscatBottomActionStack(
            bottomPadding = bottomPadding,
            horizontalPadding = 20.dp,
            onMeasuredHeightChanged = { actionStackHeight = it }
        ) {
            HoscatBottomCta(
                label = "스프레드 저장",
                onClick = {
                    val now = System.currentTimeMillis()
                    onSave(
                        SavedSpreadPreset(
                            id = "spread-$now",
                            name = name.trim().ifBlank { "${selectedBase.cardCount}장 스프레드" },
                            baseSpreadKey = selectedBase.key,
                            cardCount = selectedBase.cardCount,
                            drawMode = selectedBase.drawMode,
                            layoutId = selectedBase.layoutId,
                            layoutTitle = selectedBase.layoutTitle,
                            positionPresetTitle = "직접 의미",
                            positionLabels = labels.take(selectedBase.cardCount),
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                },
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun DeleteSavedSpreadDialog(presetName: String, onCancel: () -> Unit, onDelete: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(harmonyPanel)
                .border(1.dp, harmonyDivider.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("저장된 스프레드를 삭제할까요?", color = harmonyInk, fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
            Text("$presetName 스프레드를 삭제합니다. 삭제한 스프레드는 되돌릴 수 없습니다.", color = harmonySub, fontSize = 14.sp, lineHeight = 20.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HoscatBottomCta(
                    label = "취소",
                    modifier = Modifier.weight(1f),
                    onClick = onCancel,
                    containerColor = harmonySecondaryPanel,
                    contentColor = harmonyInk
                )
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
private fun HistoryReadingRowV2(
    reading: SavedReading,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val question = reading.question.trim()
    val hasQuestion = question.isNotBlank()
    val bg by animateColorAsState(
        targetValue = if (selected) harmonyBlue.copy(alpha = 0.10f) else Color.Transparent,
        animationSpec = tween(160, easing = FastOutSlowInEasing),
        label = "history-row-bg"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .pointerInput(reading.id, selectionMode) {
                detectTapGestures(onTap = { onClick() }, onLongPress = { onLongPress() })
            }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectionMode) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (selected) harmonyBlue else harmonySecondaryPanel)
                    .border(1.dp, if (selected) harmonyBlue else harmonyDivider, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected) HarmonyIconGlyph(HarmonyIcon.Check, Color.White, Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
        }
        if (!hasQuestion) {
            SavedCardThumbnail(
                card = reading.cards.firstOrNull(),
                modifier = Modifier
                    .width(44.dp)
                    .height(64.dp)
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                if (hasQuestion) question else reading.title,
                color = harmonyInk,
                fontSize = if (hasQuestion) 17.sp else 15.sp,
                lineHeight = if (hasQuestion) 21.sp else 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = if (hasQuestion) 2 else 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text("${formatReadingDate(reading.savedAt)} · ${reading.layoutTitle}", color = harmonySub, fontSize = 12.sp, lineHeight = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                if (hasQuestion) reading.cards.joinToString(" · ") { it.nameKr } else reading.cards.joinToString(" · ") { it.nameKr },
                color = harmonyTertiary,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        HarmonyIconGlyph(if (selectionMode) HarmonyIcon.Check else HarmonyIcon.Chevron, if (selected) harmonyBlue else harmonyTertiary, Modifier.size(20.dp))
    }
}

@Composable
private fun SavedCardThumbnail(card: SavedReadingCard?, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(harmonySecondaryPanel)
            .border(1.dp, harmonyDivider.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (card != null) {
            SavedTarotImage(card = card, modifier = Modifier.fillMaxSize(), maxTextureSize = 320)
        } else {
            HarmonyIconGlyph(HarmonyIcon.Deck, harmonyTertiary, Modifier.size(22.dp))
        }
    }
}

@Composable
private fun SavedTarotImage(card: SavedReadingCard, modifier: Modifier, maxTextureSize: Int = 768) {
    val tarot = TarotCard(
        id = card.cardId,
        nameEn = card.nameEn,
        nameKr = card.nameKr,
        arcana = "",
        basicMeaning = "",
        imageUri = card.imageUri
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (card.cardId >= 0 || card.imageUri != null) {
            TarotImage(
                card = tarot,
                isReversed = card.directionLabel.contains("역방향"),
                maxTextureSize = maxTextureSize
            )
        } else {
            Text(card.nameKr.take(1), color = harmonySub, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DeleteReadingsDialog(count: Int, onCancel: () -> Unit, onDelete: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(harmonyPanel)
                .border(1.dp, harmonyDivider.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("선택한 기록을 삭제할까요?", color = harmonyInk, fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
            Text("${count}개 기록이 삭제됩니다. 삭제한 기록은 되돌릴 수 없습니다.", color = harmonySub, fontSize = 14.sp, lineHeight = 20.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HoscatBottomCta(
                    label = "취소",
                    modifier = Modifier.weight(1f),
                    onClick = onCancel,
                    containerColor = harmonySecondaryPanel,
                    contentColor = harmonyInk
                )
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
fun HistoryDetailScreenV2(
    reading: SavedReading?,
    adsDisabled: Boolean,
    onBack: () -> Unit,
    onDeleteReading: (Long) -> Unit,
    bottomPadding: Dp
) {
    var expandedCard by remember { mutableStateOf<SavedReadingCard?>(null) }
    var selectedCard by remember(reading?.id) { mutableStateOf(reading?.cards?.firstOrNull()) }
    var showSavedInterpretation by remember(reading?.id) { mutableStateOf(false) }
    var showDeleteConfirm by remember(reading?.id) { mutableStateOf(false) }
    val showAiAction = reading != null
    val actionRows = if (reading != null) 1 + if (showAiAction) 1 else 0 else 0
    val fallbackActionStackHeight = hoscatInitialActionStackHeight(actionRows)
    var actionStackHeight by remember(reading?.id, actionRows) {
        mutableStateOf(fallbackActionStackHeight)
    }
    val actionBottomPadding = hoscatActionStackBottomPadding(bottomPadding)
    val contentBottomPadding = if (reading != null) {
        hoscatContentBottomPaddingForActionStack(
            actionStackHeight = actionStackHeight,
            actionStackBottomPadding = actionBottomPadding
        )
    } else {
        hoscatContentBottomPadding(bottomPadding)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = contentBottomPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompactTopBar(
                title = "기록 다시보기",
                subtitle = reading?.let { "${it.positionPresetTitle} | ${formatReadingDate(it.savedAt)}" } ?: "저장된 타로",
                onBack = onBack,
                action = {
                    if (reading != null) {
                        HarmonyIconButton(HarmonyIcon.Delete, "기록 삭제") { showDeleteConfirm = true }
                    } else {
                        Spacer(Modifier.size(48.dp))
                    }
                }
            )
            if (reading == null) {
                HarmonyCard {
                    Text("기록을 찾을 수 없습니다", color = harmonyInk, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("목록으로 돌아가 다시 선택해 주세요.", color = harmonySub, fontSize = 13.sp, lineHeight = 18.sp)
                }
            } else {
                val currentCard = selectedCard ?: reading.cards.firstOrNull()
                InfoSummaryCard(
                    title = reading.layoutTitle,
                    subtitle = reading.question.trim().takeIf { it.isNotBlank() }?.let { "질문: $it" }.orEmpty()
                )
                SavedSpreadReplayCard(
                    modifier = Modifier.weight(0.40f),
                    reading = reading,
                    selectedCard = currentCard,
                    onSelectCard = {
                        selectedCard = it
                        expandedCard = it
                    }
                )
                SavedCardDetailPanel(
                    modifier = Modifier.weight(0.50f),
                    card = currentCard,
                    reading = reading,
                    onOpenImage = { currentCard?.let { expandedCard = it } }
                )
            }
        }
        if (reading != null) {
            HoscatBottomActionStack(
                bottomPadding = bottomPadding,
                horizontalPadding = 20.dp,
                onMeasuredHeightChanged = { actionStackHeight = it }
            ) {
                if (showAiAction) {
                    HoscatBottomCta(
                        label = "AI 해석",
                        onClick = { showSavedInterpretation = true },
                        containerColor = harmonyBlue,
                        contentColor = Color.White
                    )
                }
                HoscatBottomCta(
                    label = "기록 삭제",
                    onClick = { showDeleteConfirm = true },
                    containerColor = harmonyRose.copy(alpha = 0.92f),
                    contentColor = Color.White
                )
            }
        }
    }
    if (reading != null && showSavedInterpretation) {
        SavedInterpretationSheet(
            reading = reading,
            adsDisabled = adsDisabled,
            onClose = { showSavedInterpretation = false }
        )
    }
    expandedCard?.let { card ->
        SavedCardImageSheet(
            card = card,
            adsDisabled = adsDisabled,
            onClose = { expandedCard = null }
        )
    }
    if (reading != null && showDeleteConfirm) {
        DeleteReadingsDialog(
            count = 1,
            onCancel = { showDeleteConfirm = false },
            onDelete = {
                showDeleteConfirm = false
                onDeleteReading(reading.id)
            }
        )
    }
}

@Composable
private fun SavedSpreadReplayCard(
    modifier: Modifier,
    reading: SavedReading,
    selectedCard: SavedReadingCard?,
    onSelectCard: (SavedReadingCard) -> Unit
) {
    val spread = remember(reading.id, reading.cards.size) { resolveSavedSpreadOption(reading) }
    val slots = remember(spread.key, reading.cards.size) { spreadSlots(spread, reading.cards.size) }
    HarmonyCard(modifier = modifier, padding = PaddingValues(12.dp)) {
        Text("스프레드", color = harmonyInk, fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (reading.cards.isEmpty() || slots.isEmpty()) {
                Text("저장된 카드가 없습니다", color = harmonySub, fontSize = 13.sp)
            } else {
                val maxX = slots.maxOf { it.x }
                val maxY = slots.maxOf { it.y }
                val cardWFromWidth = maxWidth / ((maxX + 1f) * 1.14f)
                val cardHFromHeight = (maxHeight / ((maxY + 1f) * 1.18f)).coerceAtLeast(34.dp)
                val cardWFromHeight = cardHFromHeight * 0.62f
                val cardW = if (cardWFromWidth < cardWFromHeight) cardWFromWidth else cardWFromHeight
                val cardH = cardW / 0.62f
                val boardW = cardW * (maxX + 1f)
                val boardH = cardH * (maxY + 1f)
                Box(
                    modifier = Modifier
                        .width(boardW)
                        .height(boardH)
                        .align(Alignment.Center)
                ) {
                    reading.cards.take(slots.size).forEachIndexed { index, card ->
                        val slot = slots[index]
                        val selected = selectedCard?.order == card.order
                        Box(
                            modifier = Modifier
                                .width(cardW)
                                .height(cardH)
                                .offset(x = cardW * slot.x, y = cardH * slot.y)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    if (selected) 2.dp else 1.dp,
                                    if (selected) harmonyBlue else harmonyDivider.copy(alpha = 0.58f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectCard(card) },
                            contentAlignment = Alignment.Center
                        ) {
                            SavedCardThumbnail(card = card, modifier = Modifier.fillMaxSize())
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(harmonyBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(card.order.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedCardDetailPanel(
    modifier: Modifier,
    card: SavedReadingCard?,
    reading: SavedReading,
    onOpenImage: () -> Unit
) {
    val displayMeaning = card?.let { displaySavedCardMeaning(it) }.orEmpty()
    HarmonyCard(
        modifier = modifier,
        padding = PaddingValues(14.dp)
    ) {
        Text("선택 카드 설명", color = harmonyInk, fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (card == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("카드를 선택해 주세요", color = harmonySub, fontSize = 13.sp)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SavedCardThumbnail(
                    card = card,
                    modifier = Modifier
                        .width(54.dp)
                        .height(78.dp)
                        .clickable { onOpenImage() }
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("${card.order}. ${card.nameKr}", color = harmonyInk, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(card.nameEn, color = harmonySub, fontSize = 12.sp, lineHeight = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${card.positionLabel.ifBlank { "${card.order}번째 위치" }} · ${card.directionLabel}", color = harmonyBlue, fontSize = 12.sp, lineHeight = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                displayMeaning.ifBlank { card.nameEn },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                color = harmonyInk,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
            if (reading.interpretation.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "전체 해석은 저장된 타로 원문에 보관됩니다.",
                    color = harmonyTertiary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun resolveSavedSpreadOption(reading: SavedReading): SpreadOption {
    return spreadOptions.firstOrNull {
        it.cardCount == reading.cards.size &&
            it.layoutTitle == reading.layoutTitle &&
            it.positionPresetTitle == reading.positionPresetTitle
    } ?: spreadOptions.firstOrNull {
        it.cardCount == reading.cards.size && it.positionPresetTitle == reading.positionPresetTitle
    } ?: spreadOptions.firstOrNull {
        it.cardCount == reading.cards.size
    } ?: spreadOptions.first()
}

@Composable
private fun SavedCardImageSheet(
    card: SavedReadingCard,
    adsDisabled: Boolean,
    onClose: () -> Unit
) {
    val displayMeaning = displaySavedCardMeaning(card)
    AppBottomSheet(
        heightFraction = 0.76f,
        adsDisabled = adsDisabled,
        onClose = onClose
    ) {
        SheetHeader(
            title = card.nameKr,
            subtitle = "${card.positionLabel.ifBlank { "${card.order}번째 위치" }} · ${card.directionLabel}",
            closeDescription = "카드 이미지 닫기",
            onClose = onClose
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    SavedTarotImage(
                        card = card,
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .aspectRatio(0.62f),
                        maxTextureSize = 960
                    )
                }
            }
            HarmonyCard(padding = PaddingValues(14.dp)) {
                Text(
                    "${card.order.toString().padStart(2, '0')}  ${card.nameKr}",
                    color = harmonyInk,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${card.positionLabel.ifBlank { "${card.order}번째 위치" }} · ${card.directionLabel}",
                    color = harmonySub,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    displayMeaning.ifBlank { card.nameEn },
                    color = harmonyInk,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
private fun SavedInterpretationSheet(
    reading: SavedReading,
    adsDisabled: Boolean,
    onClose: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val aiPrompt = remember(reading.id) { buildSavedReadingAiPrompt(reading) }
    val savedInterpretationScrollState = rememberScrollState()
    val promptPreviewScrollState = rememberScrollState()
    AppBottomSheet(
        heightFraction = 0.86f,
        adsDisabled = adsDisabled,
        onClose = onClose
    ) {
        SheetHeader(
            title = "AI 해석",
            subtitle = "저장된 타로를 확인한 뒤 다시 AI 앱에서 열 수 있습니다",
            closeDescription = "AI 해석 닫기",
            onClose = onClose
        )
        Text(
            reading.interpretation.ifBlank { displaySavedReadingInterpretation(reading) },
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .verticalScroll(savedInterpretationScrollState)
                .clip(RoundedCornerShape(18.dp))
                .background(harmonySecondaryPanel)
                .padding(14.dp),
            color = harmonyInk,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        Text(
            aiPrompt,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .verticalScroll(promptPreviewScrollState)
                .clip(RoundedCornerShape(18.dp))
                .background(harmonySecondaryPanel.copy(alpha = 0.72f))
                .padding(14.dp),
            color = harmonySub,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        HoscatBottomSheetActionStack {
            HoscatBottomCta(
                label = "해석 요청문 복사",
                onClick = {
                    runCatching {
                        clipboard.setText(AnnotatedString(aiPrompt))
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
            AiAppLaunchRow("Gemini", "Google Gemini 앱 또는 웹으로 열기", "com.google.android.apps.bard", "https://gemini.google.com/app", aiPrompt)
            SystemDivider()
            AiAppLaunchRow("ChatGPT", "ChatGPT 앱 또는 웹으로 열기", "com.openai.chatgpt", "https://chatgpt.com/", aiPrompt)
            SystemDivider()
            AiAppLaunchRow("Claude", "Claude 앱 또는 웹으로 열기", "com.anthropic.claude", "https://claude.ai/new", aiPrompt)
        }
    }
}

private fun buildSavedReadingAiPrompt(reading: SavedReading): String {
    val questionLine = reading.question.trim().takeIf { it.isNotBlank() }?.let {
        "사용자 질문: $it"
    }.orEmpty()
    val cardLines = reading.cards.joinToString("\n") { card ->
        val position = card.positionLabel.ifBlank { "${card.order}번째 위치" }
        val meaning = displaySavedCardMeaning(card).ifBlank { card.nameEn }
        "- ${card.order}. 위치: $position / 카드: ${card.nameKr} (${card.nameEn}) / 방향: ${card.directionLabel} / 저장된 의미: $meaning"
    }
    val deckPromptBlock = reading.deckAiPromptSnapshot.trim().takeIf { it.isNotBlank() }?.let {
        "\n[저장 당시 덱 전용 해석 원칙]\n$it"
    }.orEmpty()
    return """
당신은 차분하고 통찰력 있는 한국어 타로 리더입니다.
아래 저장된 타로 내용을 바탕으로 사용자가 바로 이해할 수 있는 자연스러운 해석을 다시 작성하세요.

[해석 원칙]
- 단정적인 예언처럼 말하지 말고, 카드가 보여주는 경향과 선택지를 중심으로 설명하세요.
- 카드 하나씩 따로 읽은 뒤, 마지막에 스프레드 전체 흐름을 통합하세요.
- 불안감을 키우거나 결제를 유도하는 말투를 쓰지 마세요.
- 의료, 법률, 투자 등 전문 판단이 필요한 영역은 단정하지 마세요.
- 말투는 신비롭지만 과장하지 말고, 따뜻하고 선명한 한국어로 작성하세요.
$deckPromptBlock

[스프레드]
덱: ${reading.deckName}
배열: ${reading.layoutTitle}
위치 의미: ${reading.positionPresetTitle}
카드 수: ${reading.cards.size}
$questionLine

[저장된 카드]
$cardLines

[기존 저장 해석]
${reading.interpretation.ifBlank { displaySavedReadingInterpretation(reading) }}

[출력 형식]
1. 전체 분위기 요약
2. 위치별 카드 해석
3. 카드 사이의 연결
4. 지금 가장 중요한 조언
5. 한 줄 메시지
""".trimIndent()
}
