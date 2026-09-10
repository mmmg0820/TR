package com.softcat.mystictarot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softcat.mystictarot.ui.components.HarmonyIcon
import com.softcat.mystictarot.ui.components.HarmonyIconButton
import com.softcat.mystictarot.ui.components.HarmonyIconGlyph

@Composable
fun HomeWorkbenchScreen(
    deckName: String,
    deckCount: Int,
    customDeck: Boolean,
    showDeckMenu: Boolean,
    useReversed: Boolean,
    savedCount: Int,
    latestReading: SavedReading?,
    onMenuClick: () -> Unit,
    onSelectDeckClick: () -> Unit,
    onDirectionChange: (Boolean) -> Unit,
    onThemeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onOpenLatestReading: (SavedReading) -> Unit,
    onStartReading: () -> Unit,
    listState: LazyListState,
    bottomPadding: Dp
) {
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
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = contentBottomPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.myang_home_logo),
                            contentDescription = "먕타로 로고",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("먕타로", color = harmonyInk, fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    HarmonyIconButton(
                        icon = if (showDeckMenu) HarmonyIcon.Close else HarmonyIcon.Menu,
                        contentDescription = if (showDeckMenu) "메뉴 닫기" else "메뉴 열기",
                        onClick = onMenuClick
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = showDeckMenu,
                    enter = fadeIn(tween(180, easing = FastOutSlowInEasing)) + slideInVertically(tween(180, easing = FastOutSlowInEasing)) { -it / 4 },
                    exit = fadeOut(tween(150, easing = FastOutSlowInEasing)) + slideOutVertically(tween(150, easing = FastOutSlowInEasing)) { -it / 4 }
                ) {
                    SystemPanel {
                        SystemMenuRow("테마 설정", "호스캣/라이트/홍염/흑림", HarmonyIcon.Theme, onThemeClick)
                        SystemDivider()
                        SystemMenuRow("앱 설정", "리딩 방향 · 카드 뒷면 · 정보", HarmonyIcon.Settings, onSettingsClick)
                    }
                }
            }

            item {
                CurrentSessionCard(
                    deckName = deckName,
                    deckCount = deckCount,
                    customDeck = customDeck,
                    useReversed = useReversed,
                    savedCount = savedCount,
                    onSelectDeckClick = onSelectDeckClick,
                    onDirectionToggle = { onDirectionChange(!useReversed) }
                )
            }

            item {
                RecentReadingCard(
                    reading = latestReading,
                    onOpenReading = onOpenLatestReading,
                    onHistoryClick = onHistoryClick
                )
            }
        }

        HoscatBottomActionStack(
            bottomPadding = bottomPadding,
            onMeasuredHeightChanged = { actionStackHeight = it }
        ) {
            HoscatBottomCta(
                label = "새 리딩 시작",
                onClick = onStartReading,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CurrentSessionCard(
    deckName: String,
    deckCount: Int,
    customDeck: Boolean,
    useReversed: Boolean,
    savedCount: Int,
    onSelectDeckClick: () -> Unit,
    onDirectionToggle: () -> Unit
) {
    HarmonyCard(padding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SystemIconTile(HarmonyIcon.Deck, harmonyBlue)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("현재 세션", color = harmonyInk, fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold)
                Text(
                    "$deckName · ${deckCount}장 · ${if (customDeck) "개인 덱" else "기본 덱"}",
                    color = harmonySub,
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
                Text(
                    "${if (useReversed) "정/역방향" else "정방향만"} · 저장 ${savedCount}건",
                    color = harmonyTertiary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactPillButton(
                label = "덱 변경",
                icon = HarmonyIcon.Deck,
                modifier = Modifier.weight(1f),
                onClick = onSelectDeckClick
            )
            CompactPillButton(
                label = if (useReversed) "정/역방향" else "정방향",
                icon = HarmonyIcon.Direction,
                modifier = Modifier.weight(1f),
                onClick = onDirectionToggle
            )
        }
    }
}

@Composable
private fun RecentReadingCard(
    reading: SavedReading?,
    onOpenReading: (SavedReading) -> Unit,
    onHistoryClick: () -> Unit
) {
    HarmonyCard(padding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SystemIconTile(HarmonyIcon.History, harmonyBlue)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("최근 리딩", color = harmonyInk, fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold)
                if (reading == null) {
                    Text("아직 저장된 기록이 없습니다", color = harmonySub, fontSize = 12.sp, lineHeight = 15.sp)
                    Text("리딩 결과에서 저장하면 이곳에 남습니다", color = harmonyTertiary, fontSize = 11.sp, lineHeight = 14.sp)
                } else {
                    Text(reading.title, color = harmonySub, fontSize = 12.sp, lineHeight = 15.sp)
                    Text(formatReadingDate(reading.savedAt), color = harmonyTertiary, fontSize = 11.sp, lineHeight = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        CompactPillButton(
            label = if (reading == null) "기록 보기" else "최근 리딩 열기",
            icon = null,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 15.sp,
            onClick = {
                if (reading == null) onHistoryClick() else onOpenReading(reading)
            }
        )
    }
}

@Composable
private fun CompactPillButton(
    label: String,
    icon: HarmonyIcon?,
    modifier: Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp,
    onClick: () -> Unit
) {
    HoscatActionControlSurface(
        label = label,
        onClick = onClick,
        modifier = modifier,
        containerColor = harmonySecondaryPanel,
        visualHeight = if (fontSize == 15.sp) HoscatStandardActionVisualHeight else HoscatCompactActionVisualHeight,
        maxVisualHeight = if (fontSize == 15.sp) HoscatStandardActionMaxVisualHeight else HoscatCompactActionMaxVisualHeight,
        touchVerticalInset = if (fontSize == 15.sp) HoscatStandardActionTouchVerticalInset else HoscatCompactActionTouchVerticalInset,
        radius = if (fontSize == 15.sp) HoscatStandardActionRadius else HoscatCompactActionRadius
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                HarmonyIconGlyph(icon, harmonyBlue, Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                label,
                color = harmonyInk,
                fontSize = fontSize,
                lineHeight = if (fontSize == 15.sp) 18.sp else 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center
            )
        }
    }
}
