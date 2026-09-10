package com.softcat.mystictarot

import android.app.Activity
import android.animation.ValueAnimator
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.softcat.mystictarot.ui.components.HarmonyIcon
import com.softcat.mystictarot.ui.components.HarmonyIconButton
import com.softcat.mystictarot.ui.components.HarmonyIconGlyph
import com.softcat.mystictarot.ui.components.HoscatMark
import com.softcat.mystictarot.ui.components.TarotImage
import com.softcat.mystictarot.ui.components.sampledImageBitmapFromUri
import com.softcat.mystictarot.ui.theme.LocalHarmonyColors
import com.softcat.mystictarot.ui.theme.MyangTarotTheme
import com.softcat.mystictarot.ui.theme.ThemeChoice
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InfoItem(
    val title: String,
    val subtitle: String,
    val body: String,
    val icon: HarmonyIcon
)

private val SpreadOptionStateSaver = listSaver<SpreadOption, String>(
    save = { spread ->
        listOf(
            spread.key,
            spread.title,
            spread.subtitle,
            spread.cardCount.toString(),
            spread.drawMode.name,
            spread.layoutId,
            spread.layoutTitle,
            spread.layoutSubtitle,
            spread.positionPresetTitle,
            spread.positionLabels.size.toString()
        ) + spread.positionLabels
    },
    restore = { saved ->
        val labelCount = saved.getOrNull(9)?.toIntOrNull() ?: 0
        if (saved.size < 10 || labelCount < 0 || saved.size < 10 + labelCount) {
            spreadOptions.first()
        } else {
            SpreadOption(
                key = saved[0],
                title = saved[1],
                subtitle = saved[2],
                cardCount = saved[3].toIntOrNull() ?: 1,
                drawMode = SpreadDrawMode.entries.firstOrNull { it.name == saved[4] } ?: SpreadDrawMode.Normal,
                layoutId = saved[5],
                layoutTitle = saved[6],
                layoutSubtitle = saved[7],
                positionPresetTitle = saved[8],
                positionLabels = saved.drop(10).take(labelCount)
            )
        }
    }
)

val harmonyBgTop: Color
    @Composable get() = LocalHarmonyColors.current.appBackground
val harmonyBgBottom: Color
    @Composable get() = LocalHarmonyColors.current.appBackground
val harmonyInk: Color
    @Composable get() = LocalHarmonyColors.current.textPrimary
val harmonySub: Color
    @Composable get() = LocalHarmonyColors.current.textSecondary
val harmonyTertiary: Color
    @Composable get() = LocalHarmonyColors.current.textTertiary
val harmonyDivider: Color
    @Composable get() = LocalHarmonyColors.current.divider
val harmonyBlue: Color
    @Composable get() = LocalHarmonyColors.current.primaryBlue
val harmonyCyan: Color
    @Composable get() = LocalHarmonyColors.current.successGreen
val harmonyViolet: Color
    @Composable get() = LocalHarmonyColors.current.textSecondary
val harmonyRose: Color
    @Composable get() = LocalHarmonyColors.current.warningRed
val harmonyPanel: Color
    @Composable get() = LocalHarmonyColors.current.surfacePrimary
val harmonySecondaryPanel: Color
    @Composable get() = LocalHarmonyColors.current.surfaceSecondary
val harmonyCardBackTop: Color
    @Composable get() = LocalHarmonyColors.current.cardBackTop
val harmonyCardBackBottom: Color
    @Composable get() = LocalHarmonyColors.current.cardBackBottom
val harmonyCardBackLine: Color
    @Composable get() = LocalHarmonyColors.current.cardBackLine
val harmonyCardBackLogo: Color
    @Composable get() = LocalHarmonyColors.current.cardBackLogo
val myangBlackCat = Color(0xFF15161A)
private val myangForestShadow = Color(0xFF1F2A24)
private val myangDeepMoss = Color(0xFF31483A)
private val myangSoftMauve = Color(0xFFD7C7D9)
private val myangWarmCream = Color(0xFFF7F1EC)

@Suppress("DEPRECATION")
private fun Window.applyMyangSystemBars(color: Int, lightBars: Boolean) {
    statusBarColor = color
    navigationBarColor = color
    val lightSystemBars = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    decorView.systemUiVisibility = if (lightBars) {
        decorView.systemUiVisibility or lightSystemBars
    } else {
        decorView.systemUiVisibility and lightSystemBars.inv()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyangTarotApp()
        }
    }
}

@Composable
fun MyangTarotApp() {
    var showIntro by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1100L)
        showIntro = false
    }
    if (showIntro) {
        MyangIntroScreen()
        return
    }

    val context = LocalContext.current
    val repository = remember(context) { MyangTarotRepository(context) }
    val appSettings = remember(repository) { repository.loadAppSettings() }
    var themeChoice by remember { mutableStateOf(appSettings.themeChoice) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showPurchaseCodeSheet by remember { mutableStateOf(false) }
    var showCardBackSheet by remember { mutableStateOf(false) }
    var adsDisabled by remember { mutableStateOf(appSettings.adsDisabled) }
    var cardBackStyle by remember { mutableStateOf(appSettings.cardBackStyle) }
    var customCardBackUri by remember { mutableStateOf(appSettings.customCardBackUri) }
    var hapticsEnabled by remember { mutableStateOf(appSettings.hapticsEnabled) }
    val activatedPurchaseCodeLabels = remember {
        mutableStateListOf<String>().apply { addAll(appSettings.activatedPurchaseCodeLabels) }
    }
    val standardDeck = remember(repository) {
        TarotDeck("standard", "유니버셜 타로", repository.loadStandardTarotCards(standardTarot78), enabled = true)
    }
    val customDecks = remember {
        mutableStateListOf<TarotDeck>().apply {
            addAll(repository.loadCustomDecks())
        }
    }
    val availableDecks = listOf(standardDeck) + customDecks
    val storedReadingDraft = remember(repository) { repository.loadReadingDraft() }
    val validStoredReadingDraft = remember(storedReadingDraft, availableDecks) {
        storedReadingDraft?.let { draft ->
            availableDecks.firstOrNull { it.id == draft.deckId && it.enabled }
                ?.let { draftDeck -> draft.takeIf { it.isRestorable(draftDeck) } }
        }
    }
    val initialDeckId = validStoredReadingDraft?.deckId
        ?: appSettings.selectedDeckId
        ?: standardDeck.id
    var selectedDeckId by remember { mutableStateOf(initialDeckId) }
    val activeDeck = availableDecks.firstOrNull { it.id == selectedDeckId && it.enabled } ?: standardDeck
    val restoredReadingDraft = validStoredReadingDraft?.takeIf { it.deckId == activeDeck.id }
    val addDeckLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
            val cards = uris.mapIndexed { index, uri ->
                TarotCard(
                    id = index,
                    nameEn = "Custom Card ${index + 1}",
                    nameKr = "개인 카드 ${index + 1}",
                    arcana = "Custom Deck",
                    basicMeaning = "",
                    imageUri = uri.toString()
                )
            }
            val deck = TarotDeck(
                id = "custom-${System.currentTimeMillis()}",
                name = "개인 덱 ${customDecks.size + 1} (${cards.size}장)",
                cards = cards
            )
            customDecks.add(deck)
            selectedDeckId = deck.id
            repository.persistCustomDecks(customDecks)
            repository.persistSelectedDeckId(selectedDeckId)
        }
    }

    var screen by rememberSaveable(
        stateSaver = Saver(
            save = { it.name },
            restore = { name -> AppScreen.entries.firstOrNull { it.name == name } ?: AppScreen.Home }
        )
    ) { mutableStateOf(restoredReadingDraft?.screen ?: AppScreen.Home) }
    var infoBackScreen by remember { mutableStateOf(AppScreen.Home) }
    var showDeckMenu by remember { mutableStateOf(false) }
    var showDeckPicker by remember { mutableStateOf(false) }
    var editingDeck by remember { mutableStateOf<TarotDeck?>(null) }
    var useReversed by remember { mutableStateOf(appSettings.useReversed) }
    var selectedSpread by rememberSaveable(stateSaver = SpreadOptionStateSaver) {
        mutableStateOf(
            restoredReadingDraft?.spread
                ?: selectableSpreadOptions.firstOrNull { it.key == appSettings.recentSpreadKey }
                ?: selectableSpreadOptions.first()
        )
    }
    var recentSpread by remember {
        mutableStateOf(selectableSpreadOptions.firstOrNull { it.key == appSettings.recentSpreadKey })
    }
    val spreadUseCounts = remember { mutableStateMapOf<String, Int>().apply { putAll(appSettings.spreadUseCounts) } }
    val customPositionLabelsBySpreadKey = remember {
        mutableStateMapOf<String, List<String>>().apply { putAll(appSettings.customPositionLabelsBySpreadKey) }
    }
    var currentQuestion by rememberSaveable { mutableStateOf(restoredReadingDraft?.question.orEmpty()) }
    var shuffledDeck by rememberSaveable(
        activeDeck.id,
        stateSaver = listSaver(
            save = { cards -> cards.map { it.id } },
            restore = { ids -> ids.mapNotNull { id -> activeDeck.cards.firstOrNull { it.id == id } } }
        )
    ) {
        val restoredOrder = restoredReadingDraft?.shuffledCardIds
            ?.mapNotNull { id -> activeDeck.cards.firstOrNull { it.id == id } }
            ?.takeIf { it.size == activeDeck.cards.size }
        mutableStateOf(restoredOrder ?: shuffleDeckForReading(activeDeck.cards))
    }
    val selectedCards = rememberSaveable(
        activeDeck.id,
        saver = listSaver(
            save = { cards -> cards.map { it.id } },
            restore = { ids -> mutableStateListOf<TarotCard>().apply {
                addAll(ids.mapNotNull { id -> activeDeck.cards.firstOrNull { it.id == id } })
            } }
        )
    ) {
        mutableStateListOf<TarotCard>().apply {
            addAll(restoredReadingDraft?.selectedCardIds.orEmpty().mapNotNull { id ->
                activeDeck.cards.firstOrNull { it.id == id }
            })
        }
    }
    var reusableSelectionIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val finalCandidateCards = rememberSaveable(
        activeDeck.id,
        saver = listSaver(
            save = { cards -> cards.map { it.id } },
            restore = { ids -> mutableStateListOf<TarotCard>().apply {
                addAll(ids.mapNotNull { id -> activeDeck.cards.firstOrNull { it.id == id } })
            } }
        )
    ) {
        mutableStateListOf<TarotCard>().apply {
            addAll(restoredReadingDraft?.finalCandidateCardIds.orEmpty().mapNotNull { id ->
                activeDeck.cards.firstOrNull { it.id == id }
            })
        }
    }
    var finalOneSecondStep by rememberSaveable {
        mutableStateOf(restoredReadingDraft?.finalOneSecondStep ?: false)
    }
    val drawnCards = rememberSaveable(
        activeDeck.id,
        saver = listSaver(
            save = { cards -> cards.map { "${it.card.id}|${it.direction.name}|${it.order}" } },
            restore = { encoded -> mutableStateListOf<DrawnCard>().apply {
                addAll(encoded.mapNotNull { value ->
                    val parts = value.split('|')
                    val card = parts.getOrNull(0)?.toIntOrNull()
                        ?.let { id -> activeDeck.cards.firstOrNull { it.id == id } }
                        ?: return@mapNotNull null
                    val direction = parts.getOrNull(1)
                        ?.let { name -> CardDirection.entries.firstOrNull { it.name == name } }
                        ?: CardDirection.Upright
                    DrawnCard(card, direction, parts.getOrNull(2)?.toIntOrNull() ?: 1)
                })
            } }
        )
    ) {
        mutableStateListOf<DrawnCard>().apply {
            addAll(restoredReadingDraft?.drawnCards.orEmpty().mapNotNull { draftCard ->
                val card = activeDeck.cards.firstOrNull { it.id == draftCard.cardId } ?: return@mapNotNull null
                DrawnCard(card, draftCard.direction, draftCard.order)
            })
        }
    }
    val savedReadings = remember {
        mutableStateListOf<SavedReading>().apply {
            addAll(repository.loadSavedReadings())
        }
    }
    val savedSpreadPresets = remember {
        mutableStateListOf<SavedSpreadPreset>().apply {
            addAll(repository.loadSavedSpreadPresets())
        }
    }
    var selectedSavedReadingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val selectedSavedReading = savedReadings.firstOrNull { it.id == selectedSavedReadingId }
    var expandedCard by remember { mutableStateOf<DrawnCard?>(null) }
    var showSpreadDetails by remember { mutableStateOf(false) }
    var showLlmPrompt by remember { mutableStateOf(false) }
    var readingSaved by rememberSaveable { mutableStateOf(false) }
    var readingSaveInProgress by rememberSaveable { mutableStateOf(false) }
    var showReselectConfirmation by rememberSaveable { mutableStateOf(false) }
    var lastBackPressAt by remember { mutableLongStateOf(0L) }
    var tabScrollJob by remember { mutableStateOf<Job?>(null) }
    val tabCoroutineScope = rememberCoroutineScope()
    val appCoroutineScope = rememberCoroutineScope()
    val homeListState = rememberLazyListState()
    val spreadListState = rememberLazyListState()
    val historyListState = rememberLazyListState()

    val draftShuffledCardIds = shuffledDeck.map { it.id }
    val draftSelectedCardIds = selectedCards.map { it.id }
    val draftFinalCandidateCardIds = finalCandidateCards.map { it.id }
    val draftDrawnCards = drawnCards.map { ReadingDraftCard(it.card.id, it.direction, it.order) }
    LaunchedEffect(
        screen,
        activeDeck.id,
        selectedSpread,
        currentQuestion,
        draftShuffledCardIds,
        draftSelectedCardIds,
        draftFinalCandidateCardIds,
        finalOneSecondStep,
        draftDrawnCards
    ) {
        delay(300L)
        when (screen) {
            AppScreen.Question, AppScreen.DeckPick, AppScreen.SpreadResult -> withContext(Dispatchers.IO) {
                repository.persistReadingDraft(
                    ReadingDraft(
                        updatedAt = System.currentTimeMillis(),
                        screen = screen,
                        deckId = activeDeck.id,
                        spread = selectedSpread,
                        question = currentQuestion.take(240),
                        shuffledCardIds = draftShuffledCardIds,
                        selectedCardIds = draftSelectedCardIds,
                        finalCandidateCardIds = draftFinalCandidateCardIds,
                        finalOneSecondStep = finalOneSecondStep,
                        drawnCards = draftDrawnCards
                    )
                )
            }
            AppScreen.Home, AppScreen.SpreadSelect -> withContext(Dispatchers.IO) {
                repository.clearReadingDraft()
            }
            else -> Unit
        }
    }

    fun scrollTabToTop(listState: LazyListState) {
        if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) return
        tabScrollJob?.cancel()
        tabScrollJob = tabCoroutineScope.launch {
            listState.animateScrollToItem(0)
        }
    }

    fun persistCurrentSettings() {
        repository.persistAppSettings(
            themeChoice = themeChoice,
            selectedDeckId = selectedDeckId,
            useReversed = useReversed,
            recentSpreadKey = recentSpread?.key,
            spreadUseCounts = spreadUseCounts,
            adsDisabled = adsDisabled,
            activatedPurchaseCodeLabels = activatedPurchaseCodeLabels.toSet(),
            cardBackStyle = cardBackStyle,
            customCardBackUri = customCardBackUri,
            hapticsEnabled = hapticsEnabled,
            customPositionLabelsBySpreadKey = customPositionLabelsBySpreadKey
        )
    }

    fun spreadWithSavedPositionLabels(spread: SpreadOption): SpreadOption {
        val savedLabels = customPositionLabelsBySpreadKey[spread.key]
            ?.takeIf { it.size == spread.cardCount }
            ?: return spread
        return spread.copy(
            positionPresetTitle = "저장한 직접 의미",
            positionLabels = savedLabels
        )
    }

    fun spreadFromSavedPreset(preset: SavedSpreadPreset): SpreadOption {
        val base = spreadOptions.firstOrNull { it.key == preset.baseSpreadKey }
        return SpreadOption(
            key = "saved:${preset.id}",
            title = preset.name,
            subtitle = "${preset.cardCount}장 · 저장된 스프레드",
            cardCount = preset.cardCount,
            drawMode = preset.drawMode,
            layoutId = base?.layoutId ?: preset.layoutId,
            layoutTitle = base?.layoutTitle ?: preset.layoutTitle,
            layoutSubtitle = base?.layoutSubtitle ?: "",
            positionPresetTitle = "저장된 스프레드",
            positionLabels = preset.positionLabels.take(preset.cardCount)
        )
    }

    fun persistCustomDeckState() {
        repository.persistCustomDecks(customDecks)
        persistCurrentSettings()
    }

    fun updateCustomDeck(updated: TarotDeck) {
        val index = customDecks.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            customDecks[index] = updated
            if (selectedDeckId == updated.id && updated.enabled) {
                shuffledDeck = shuffleDeckForReading(updated.cards)
                selectedCards.clear()
                reusableSelectionIndex = null
                finalCandidateCards.clear()
                finalOneSecondStep = false
                drawnCards.clear()
                expandedCard = null
                showSpreadDetails = false
                showLlmPrompt = false
                readingSaved = false
            }
            persistCustomDeckState()
        }
    }

    fun setDeckEnabled(deck: TarotDeck, enabled: Boolean) {
        if (deck.id == standardDeck.id) return
        val updated = deck.copy(enabled = enabled)
        val index = customDecks.indexOfFirst { it.id == deck.id }
        if (index >= 0) {
            customDecks[index] = updated
            if (!enabled && selectedDeckId == deck.id) {
                selectedDeckId = standardDeck.id
                shuffledDeck = shuffleDeckForReading(standardDeck.cards)
                selectedCards.clear()
                reusableSelectionIndex = null
                finalCandidateCards.clear()
                finalOneSecondStep = false
                drawnCards.clear()
                expandedCard = null
                showSpreadDetails = false
                showLlmPrompt = false
                readingSaved = false
            }
            persistCustomDeckState()
        }
    }

    val cardBackImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            cardBackStyle = CardBackStyle.CustomImage
            customCardBackUri = uri.toString()
            persistCurrentSettings()
        }
    }

    fun resetSelection(reshuffle: Boolean = true) {
        if (reshuffle) shuffledDeck = shuffleDeckForReading(activeDeck.cards)
        selectedCards.clear()
        reusableSelectionIndex = null
        finalCandidateCards.clear()
        finalOneSecondStep = false
        drawnCards.clear()
        expandedCard = null
        showSpreadDetails = false
        showLlmPrompt = false
        readingSaved = false
        readingSaveInProgress = false
    }

    BackHandler(enabled = true) {
        when {
            expandedCard != null -> expandedCard = null
            showLlmPrompt -> showLlmPrompt = false
            showSpreadDetails -> showSpreadDetails = false
            showReselectConfirmation -> showReselectConfirmation = false
            showPurchaseCodeSheet -> showPurchaseCodeSheet = false
            showCardBackSheet -> showCardBackSheet = false
            showThemePicker -> showThemePicker = false
            showDeckPicker -> showDeckPicker = false
            showDeckMenu -> showDeckMenu = false
            screen == AppScreen.SpreadResult -> {
                showReselectConfirmation = true
            }
            screen == AppScreen.DeckPick -> {
                if (selectedSpread.drawMode == SpreadDrawMode.FinalOneFromTen && finalOneSecondStep) {
                    selectedCards.clear()
                    reusableSelectionIndex = null
                    finalOneSecondStep = false
                } else {
                    resetSelection(reshuffle = false)
                    screen = AppScreen.Question
                }
            }
            screen == AppScreen.Question -> screen = AppScreen.SpreadSelect
            screen == AppScreen.SpreadSelect -> screen = AppScreen.Home
            screen == AppScreen.HistoryDetail -> {
                selectedSavedReadingId = null
                screen = AppScreen.History
            }
            screen == AppScreen.History -> screen = AppScreen.Home
            screen == AppScreen.DeckManagement -> screen = AppScreen.Home
            screen == AppScreen.PromptSettings -> screen = AppScreen.Home
            screen == AppScreen.Settings -> screen = AppScreen.Home
            screen == AppScreen.Info -> screen = infoBackScreen
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressAt < 1800L) {
                    (context as? Activity)?.finish()
                } else {
                    lastBackPressAt = now
                    Toast.makeText(context, "한 번 더 누르면 앱이 종료됩니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    MyangTarotTheme(themeChoice) {
        val systemBarActivity = LocalContext.current as? Activity
        val systemBarColor = harmonyBgBottom.toArgb()
        SideEffect {
            systemBarActivity?.window?.let { appWindow ->
                appWindow.applyMyangSystemBars(systemBarColor, lightBars = !themeChoice.isDark)
            }
        }
        val selectedBottomTab = when (screen) {
            AppScreen.Home -> BottomTab.Home
            AppScreen.History -> BottomTab.History
            AppScreen.SpreadSelect, AppScreen.Question, AppScreen.DeckPick, AppScreen.SpreadResult, AppScreen.PromptSettings -> BottomTab.Reading
            AppScreen.HistoryDetail -> BottomTab.History
            AppScreen.DeckManagement -> BottomTab.Deck
            AppScreen.Settings, AppScreen.Info -> BottomTab.Home
        }
        HarmonyShell(
            selectedTab = selectedBottomTab,
            hapticsEnabled = hapticsEnabled,
            onHome = {
                showDeckMenu = false
                if (selectedBottomTab == BottomTab.Home && screen == AppScreen.Home) {
                    scrollTabToTop(homeListState)
                } else {
                    screen = AppScreen.Home
                }
            },
            onReading = {
                showDeckMenu = false
                if (selectedBottomTab == BottomTab.Reading && screen == AppScreen.SpreadSelect) {
                    scrollTabToTop(spreadListState)
                } else {
                    screen = AppScreen.SpreadSelect
                }
            },
            onDeck = {
                showDeckMenu = false
                showDeckPicker = false
                screen = AppScreen.DeckManagement
            },
            onHistory = {
                showDeckMenu = false
                if (selectedBottomTab == BottomTab.History && screen == AppScreen.History) {
                    scrollTabToTop(historyListState)
                } else {
                    screen = AppScreen.History
                }
            }
        ) { bottomPadding ->
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    if (initialState == AppScreen.DeckPick && targetState == AppScreen.SpreadResult) {
                        fadeIn(tween(120, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(tween(90, easing = FastOutSlowInEasing))
                    } else {
                        (fadeIn(tween(200, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(tween(200, easing = FastOutSlowInEasing)) { it / 10 }) togetherWith
                            (fadeOut(tween(150, easing = FastOutSlowInEasing)) +
                                slideOutHorizontally(tween(150, easing = FastOutSlowInEasing)) { -it / 12 })
                    }
                },
                label = "main-screen-transition"
            ) { targetScreen ->
                when (targetScreen) {
            AppScreen.Home -> HomeWorkbenchScreen(
                deckName = activeDeck.name,
                deckCount = activeDeck.cards.size,
                customDeck = activeDeck.id != standardDeck.id,
                showDeckMenu = showDeckMenu,
                useReversed = useReversed,
                savedCount = savedReadings.size,
                latestReading = savedReadings.maxByOrNull { it.savedAt },
                onMenuClick = { showDeckMenu = !showDeckMenu },
                onSelectDeckClick = {
                    showDeckMenu = false
                    showDeckPicker = true
                },
                onDirectionChange = {
                    useReversed = it
                    resetSelection(reshuffle = false)
                    persistCurrentSettings()
                },
                onThemeClick = {
                    showDeckMenu = false
                    showThemePicker = true
                },
                onSettingsClick = {
                    showDeckMenu = false
                    screen = AppScreen.Settings
                },
                onHistoryClick = {
                    showDeckMenu = false
                    screen = AppScreen.History
                },
                onOpenLatestReading = {
                    selectedSavedReadingId = it.id
                    screen = AppScreen.HistoryDetail
                },
                onStartReading = {
                    showDeckMenu = false
                    screen = AppScreen.SpreadSelect
                },
                listState = homeListState,
                bottomPadding = bottomPadding
            )

            AppScreen.SpreadSelect -> SpreadSelectScreen(
                selectedSpread = selectedSpread,
                recentSpread = recentSpread,
                useCounts = spreadUseCounts,
                onBack = { screen = AppScreen.Home },
                onSpreadClick = {
                    selectedSpread = spreadWithSavedPositionLabels(it)
                    recentSpread = it
                    spreadUseCounts[it.key] = (spreadUseCounts[it.key] ?: 0) + 1
                    currentQuestion = ""
                    resetSelection(reshuffle = true)
                    persistCurrentSettings()
                    screen = AppScreen.Question
                },
                listState = spreadListState,
                bottomPadding = bottomPadding
            )

            AppScreen.Question -> QuestionInputScreen(
                spread = selectedSpread,
                question = currentQuestion,
                onQuestionChange = { currentQuestion = it.take(240) },
                onSpreadChange = { selectedSpread = it },
                onSavePositionLabels = {
                    customPositionLabelsBySpreadKey[selectedSpread.key] = selectedSpread.positionLabels
                    persistCurrentSettings()
                    Toast.makeText(context, "카드 위치 의미를 저장했어요", Toast.LENGTH_SHORT).show()
                },
                onBack = { screen = AppScreen.SpreadSelect },
                onContinue = { screen = AppScreen.DeckPick },
                bottomPadding = bottomPadding
            )

            AppScreen.DeckPick -> DeckPickScreen(
                deck = if (selectedSpread.drawMode == SpreadDrawMode.FinalOneFromTen && finalOneSecondStep) {
                    finalCandidateCards
                } else {
                    shuffledDeck
                },
                selectedCards = selectedCards,
                spread = selectedSpread,
                cardBackStyle = cardBackStyle,
                customCardBackUri = customCardBackUri,
                targetCountOverride = if (selectedSpread.drawMode == SpreadDrawMode.FinalOneFromTen) {
                    if (finalOneSecondStep) 1 else min(10, activeDeck.cards.size)
                } else {
                    null
                },
                stageLabel = if (selectedSpread.drawMode == SpreadDrawMode.FinalOneFromTen) {
                    if (finalOneSecondStep) "2차 최종 1장" else "1차 후보 10장"
                } else {
                    null
                },
                onBack = {
                    if (selectedSpread.drawMode == SpreadDrawMode.FinalOneFromTen && finalOneSecondStep) {
                        selectedCards.clear()
                        reusableSelectionIndex = null
                        finalOneSecondStep = false
                    } else {
                        resetSelection(reshuffle = false)
                        screen = AppScreen.Question
                    }
                },
                onCardClick = { card ->
                    val currentTargetCount = if (selectedSpread.drawMode == SpreadDrawMode.FinalOneFromTen) {
                        if (finalOneSecondStep) 1 else min(10, activeDeck.cards.size)
                    } else {
                        min(selectedSpread.cardCount, activeDeck.cards.size)
                    }
                    if (selectedCards.contains(card)) {
                        reusableSelectionIndex = selectedCards.indexOf(card).takeIf { it >= 0 }
                        selectedCards.remove(card)
                    } else if (selectedCards.size < currentTargetCount) {
                        val insertIndex = reusableSelectionIndex
                        if (insertIndex != null && insertIndex <= selectedCards.size) {
                            selectedCards.add(insertIndex, card)
                        } else {
                            selectedCards.add(card)
                        }
                        reusableSelectionIndex = null
                    }
                },
                shuffleDeck = {
                    if (selectedSpread.drawMode == SpreadDrawMode.FinalOneFromTen && finalOneSecondStep) {
                        selectedCards.clear()
                        reusableSelectionIndex = null
                    } else {
                        resetSelection(reshuffle = true)
                    }
                },
                bottomPadding = bottomPadding,
                onConfirm = {
                    if (selectedSpread.drawMode == SpreadDrawMode.FinalOneFromTen && !finalOneSecondStep) {
                        finalCandidateCards.clear()
                        finalCandidateCards.addAll(selectedCards)
                        selectedCards.clear()
                        reusableSelectionIndex = null
                        finalOneSecondStep = true
                    } else {
                        val lockedResult = lockDrawnCards(
                            selectedCards = selectedCards,
                            existingDrawnCards = drawnCards,
                            useReversed = useReversed
                        )
                        drawnCards.clear()
                        drawnCards.addAll(lockedResult)
                        expandedCard = null
                        showSpreadDetails = false
                        showLlmPrompt = false
                        readingSaved = false
                        screen = AppScreen.SpreadResult
                    }
                }
            )

            AppScreen.SpreadResult -> SpreadResultScreen(
                spread = selectedSpread,
                question = currentQuestion,
                drawnCards = drawnCards,
                expandedCard = expandedCard,
                showSpreadDetails = showSpreadDetails,
                showLlmPrompt = showLlmPrompt,
                readingSaved = readingSaved,
                readingSaveInProgress = readingSaveInProgress,
                showLlmAction = activeDeck.id == standardDeck.id || activeDeck.aiPrompt.isNotBlank(),
                showCardDetailsAction = drawnCards.any { hasDisplayableCardMeaning(it.card) },
                deckAiPrompt = activeDeck.aiPrompt,
                onBack = {
                    expandedCard = null
                    showSpreadDetails = false
                    showLlmPrompt = false
                    showReselectConfirmation = true
                },
                adsDisabled = adsDisabled,
                bottomPadding = bottomPadding,
                onRestart = {
                    resetSelection(reshuffle = true)
                    screen = AppScreen.Home
                },
                onCardClick = { expandedCard = it },
                onShowDetails = { showSpreadDetails = true },
                onCloseDetails = { showSpreadDetails = false },
                onShowLlmPrompt = { showLlmPrompt = true },
                onCloseLlmPrompt = { showLlmPrompt = false },
                onSaveReading = {
                    if (!readingSaved && !readingSaveInProgress) {
                        readingSaveInProgress = true
                        val savedReading = buildSavedReading(selectedSpread, currentQuestion, drawnCards, activeDeck)
                        val readingsSnapshot = savedReadings.toList() + savedReading
                        val autoSavedPreset = buildAutoSavedSpreadPreset(
                            spread = selectedSpread,
                            baseSpreads = spreadOptions,
                            existingPresets = savedSpreadPresets,
                            now = System.currentTimeMillis()
                        )
                        appCoroutineScope.launch {
                            val persisted = withContext(Dispatchers.IO) {
                                runCatching {
                                    repository.persistSavedReadings(readingsSnapshot)
                                }.getOrDefault(false).also { success ->
                                    if (success) repository.clearReadingDraft()
                                }
                            }
                            if (persisted) {
                                savedReadings.add(savedReading)
                                autoSavedPreset?.let { preset ->
                                    savedSpreadPresets.add(preset)
                                    repository.persistSavedSpreadPresets(savedSpreadPresets)
                                }
                                readingSaved = true
                                if (hapticsEnabled) {
                                    systemBarActivity?.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                }
                            } else {
                                Toast.makeText(context, "저장하지 못했어요. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                            }
                            readingSaveInProgress = false
                        }
                    }
                },
                onCloseExpanded = { expandedCard = null }
            )

            AppScreen.Info -> InfoScreen(
                onBack = { screen = infoBackScreen },
                adsDisabled = adsDisabled,
                activatedCodeCount = activatedPurchaseCodeLabels.size,
                onOpenPurchaseCode = { showPurchaseCodeSheet = true },
                onShareDiagnostics = {
                    val diagnostics = collectPrivacySafeDiagnostics(
                        context = context,
                        screen = AppScreen.Info,
                        settings = AppSettings(
                            themeChoice = themeChoice,
                            selectedDeckId = selectedDeckId,
                            useReversed = useReversed,
                            recentSpreadKey = recentSpread?.key,
                            spreadUseCounts = spreadUseCounts,
                            adsDisabled = adsDisabled,
                            activatedPurchaseCodeLabels = activatedPurchaseCodeLabels.toSet(),
                            cardBackStyle = cardBackStyle,
                            customCardBackUri = customCardBackUri,
                            hapticsEnabled = hapticsEnabled,
                            customPositionLabelsBySpreadKey = customPositionLabelsBySpreadKey
                        ),
                        customDeckCount = customDecks.size,
                        enabledDeckCount = availableDecks.count { it.enabled },
                        savedReadingCount = savedReadings.size,
                        savedSpreadCount = savedSpreadPresets.size,
                        hasReadingDraft = repository.loadReadingDraft()?.isRestorable(activeDeck) == true
                    )
                    if (!sharePrivacySafeDiagnostics(context, diagnostics)) {
                        Toast.makeText(context, "진단 정보를 공유할 수 없어요", Toast.LENGTH_SHORT).show()
                    }
                },
                bottomPadding = bottomPadding
            )

            AppScreen.Settings -> SettingsScreen(
                themeChoice = themeChoice,
                useReversed = useReversed,
                cardBackStyle = cardBackStyle,
                customCardBackUri = customCardBackUri,
                hapticsEnabled = hapticsEnabled,
                onBack = { screen = AppScreen.Home },
                onOpenTheme = { showThemePicker = true },
                onOpenCardBack = { showCardBackSheet = true },
                onDirectionChange = {
                    useReversed = it
                    resetSelection(reshuffle = false)
                    persistCurrentSettings()
                },
                onHapticsChange = {
                    hapticsEnabled = it
                    persistCurrentSettings()
                },
                onOpenInfo = {
                    infoBackScreen = AppScreen.Settings
                    screen = AppScreen.Info
                },
                onOpenPrompt = { screen = AppScreen.PromptSettings },
                bottomPadding = bottomPadding
            )

            AppScreen.History -> HistoryScreenV2(
                savedReadings = savedReadings,
                savedSpreadPresets = savedSpreadPresets,
                onBack = { screen = AppScreen.Home },
                onOpenReading = {
                    selectedSavedReadingId = it.id
                    screen = AppScreen.HistoryDetail
                },
                onUseSavedSpread = { preset ->
                    selectedSpread = spreadFromSavedPreset(preset)
                    recentSpread = spreadOptions.firstOrNull { it.key == preset.baseSpreadKey } ?: recentSpread
                    currentQuestion = ""
                    resetSelection(reshuffle = true)
                    screen = AppScreen.Question
                },
                onSaveSpreadPreset = { preset ->
                    savedSpreadPresets.removeAll { it.id == preset.id }
                    savedSpreadPresets.add(preset)
                    repository.persistSavedSpreadPresets(savedSpreadPresets)
                },
                onDeleteSavedSpread = { presetId ->
                    savedSpreadPresets.removeAll { it.id == presetId }
                    repository.persistSavedSpreadPresets(savedSpreadPresets)
                },
                onDeleteReadings = { ids ->
                    if (ids.isNotEmpty()) {
                        val updatedReadings = savedReadings.filterNot { it.id in ids }
                        appCoroutineScope.launch {
                            val persisted = withContext(Dispatchers.IO) {
                                repository.persistSavedReadings(updatedReadings)
                            }
                            if (persisted) {
                                savedReadings.removeAll { it.id in ids }
                                if (selectedSavedReadingId in ids) {
                                    selectedSavedReadingId = null
                                }
                            } else {
                                Toast.makeText(context, "기록을 삭제하지 못했어요", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                onToggleReadingPinned = { readingId ->
                    val readingIndex = savedReadings.indexOfFirst { it.id == readingId }
                    if (readingIndex >= 0) {
                        val updatedReading = savedReadings[readingIndex].copy(
                            isPinned = !savedReadings[readingIndex].isPinned
                        )
                        val updatedReadings = savedReadings.toMutableList().also {
                            it[readingIndex] = updatedReading
                        }
                        appCoroutineScope.launch {
                            val persisted = withContext(Dispatchers.IO) {
                                repository.persistSavedReadings(updatedReadings)
                            }
                            if (persisted) {
                                val latestIndex = savedReadings.indexOfFirst { it.id == readingId }
                                if (latestIndex >= 0) savedReadings[latestIndex] = updatedReading
                            } else {
                                Toast.makeText(context, "고정 상태를 저장하지 못했어요", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                listState = historyListState,
                bottomPadding = bottomPadding
            )

            AppScreen.HistoryDetail -> HistoryDetailScreenV2(
                reading = selectedSavedReading,
                adsDisabled = adsDisabled,
                onBack = {
                    selectedSavedReadingId = null
                    screen = AppScreen.History
                },
                onDeleteReading = { id ->
                    val updatedReadings = savedReadings.filterNot { it.id == id }
                    appCoroutineScope.launch {
                        val persisted = withContext(Dispatchers.IO) {
                            repository.persistSavedReadings(updatedReadings)
                        }
                        if (persisted) {
                            savedReadings.removeAll { it.id == id }
                            selectedSavedReadingId = null
                            screen = AppScreen.History
                        } else {
                            Toast.makeText(context, "기록을 삭제하지 못했어요", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                bottomPadding = bottomPadding
            )

            AppScreen.DeckManagement -> DeckManagementScreen(
                decks = availableDecks,
                selectedDeckId = activeDeck.id,
                onBack = { screen = AppScreen.Home },
                onAddDeck = {
                    addDeckLauncher.launch(arrayOf("image/*"))
                },
                onSelect = { deck ->
                    if (deck.enabled) {
                        selectedDeckId = deck.id
                        shuffledDeck = shuffleDeckForReading(deck.cards)
                        resetSelection(reshuffle = false)
                        persistCurrentSettings()
                    }
                },
                onToggleDeck = { deck, enabled ->
                    setDeckEnabled(deck, enabled)
                },
                onOpenDeckSettings = { deck ->
                    editingDeck = deck
                },
                onDeleteDeck = { deck ->
                    customDecks.removeAll { it.id == deck.id }
                    if (selectedDeckId == deck.id) {
                        selectedDeckId = standardDeck.id
                        shuffledDeck = shuffleDeckForReading(standardDeck.cards)
                        resetSelection(reshuffle = false)
                    }
                    persistCustomDeckState()
                },
                bottomPadding = bottomPadding
            )

            AppScreen.PromptSettings -> PromptSettingsScreen(
                onBack = { screen = AppScreen.Home },
                bottomPadding = bottomPadding
            )
                }
            }
            if (showDeckPicker) {
                DeckSelectSheet(
                    decks = availableDecks,
                    selectedDeckId = activeDeck.id,
                    onAddDeck = {
                        showDeckPicker = false
                        addDeckLauncher.launch(arrayOf("image/*"))
                    },
                    onSelect = { deck ->
                        if (deck.enabled) {
                            selectedDeckId = deck.id
                            shuffledDeck = shuffleDeckForReading(deck.cards)
                            resetSelection(reshuffle = false)
                            showDeckPicker = false
                            persistCurrentSettings()
                        }
                    },
                    onToggleDeck = { deck, enabled ->
                        setDeckEnabled(deck, enabled)
                    },
                    onOpenDeckSettings = { deck ->
                        editingDeck = deck
                    },
                    onDeleteDeck = { deck ->
                        customDecks.removeAll { it.id == deck.id }
                        if (selectedDeckId == deck.id) {
                            selectedDeckId = standardDeck.id
                            shuffledDeck = shuffleDeckForReading(standardDeck.cards)
                            resetSelection(reshuffle = false)
                        }
                        persistCustomDeckState()
                    },
                    onClose = { showDeckPicker = false }
                )
            }
            editingDeck?.let { deck ->
                DeckSettingsSheet(
                    deck = customDecks.firstOrNull { it.id == deck.id } ?: deck,
                    onSaveDeck = { updated ->
                        updateCustomDeck(updated)
                        editingDeck = updated
                    },
                    onClose = { editingDeck = null }
                )
            }
            if (showThemePicker) {
                ThemeSelectSheetV2(
                    selected = themeChoice,
                    adsDisabled = adsDisabled,
                    onSelect = {
                        themeChoice = it
                        showThemePicker = false
                        persistCurrentSettings()
                    },
                    onClose = { showThemePicker = false }
                )
            }
            if (showPurchaseCodeSheet) {
                PurchaseCodeSheet(
                    adsDisabled = adsDisabled,
                    activatedCodeCount = activatedPurchaseCodeLabels.size,
                    onApplyCode = { code ->
                        val result = redeemPurchaseCode(code)
                        if (result != null) {
                            if (result.disablesAds) adsDisabled = true
                            if (!activatedPurchaseCodeLabels.contains(result.label)) {
                                activatedPurchaseCodeLabels.add(result.label)
                            }
                            persistCurrentSettings()
                            true
                        } else {
                            false
                        }
                    },
                    onClose = { showPurchaseCodeSheet = false }
                )
            }
            if (showCardBackSheet) {
                CardBackSelectSheet(
                    selected = cardBackStyle,
                    customCardBackUri = customCardBackUri,
                    adsDisabled = adsDisabled,
                    onPickCustomImage = {
                        cardBackImageLauncher.launch(arrayOf("image/*"))
                    },
                    onClearCustomImage = {
                        if (cardBackStyle == CardBackStyle.CustomImage) {
                            cardBackStyle = CardBackStyle.ThemeDefault
                        }
                        customCardBackUri = null
                        persistCurrentSettings()
                    },
                    onSelect = {
                        cardBackStyle = it
                        if (it != CardBackStyle.CustomImage) {
                            customCardBackUri = null
                        }
                        persistCurrentSettings()
                    },
                    onClose = { showCardBackSheet = false }
                )
            }
            if (showReselectConfirmation) {
                ReselectCardsDialog(
                    onCancel = { showReselectConfirmation = false },
                    onConfirm = {
                        showReselectConfirmation = false
                        drawnCards.clear()
                        readingSaved = false
                        expandedCard = null
                        showSpreadDetails = false
                        showLlmPrompt = false
                        screen = AppScreen.DeckPick
                    }
                )
            }
        }
    }
}

@Composable
private fun ReselectCardsDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
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
            Text(
                "카드를 다시 선택할까요?",
                color = harmonyInk,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "현재 결과를 유지하거나, 기존 결과를 지우고 카드를 다시 선택할 수 있어요.",
                color = harmonySub,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HoscatBottomCta(
                    label = "결과 유지",
                    modifier = Modifier.weight(1f),
                    onClick = onCancel,
                    containerColor = harmonySecondaryPanel,
                    contentColor = harmonyInk
                ) {
                    Text("결과 유지", color = harmonyInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                HoscatBottomCta(
                    label = "다시 선택",
                    modifier = Modifier.weight(1f),
                    onClick = onConfirm
                ) {
                    Text("다시 선택", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun MyangIntroScreen() {
    val introActivity = LocalContext.current as? Activity
    SideEffect {
        introActivity?.window?.let { window ->
            val introColor = Color(0xFFD8CFF0).toArgb()
            window.applyMyangSystemBars(introColor, lightBars = true)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8CFF0))
    ) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    contentDescription = "먕타로 시작 화면"
                    setImageResource(R.drawable.myang_tarot_start_screen)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun HarmonyShell(
    selectedTab: BottomTab?,
    hapticsEnabled: Boolean,
    onHome: () -> Unit,
    onReading: () -> Unit,
    onDeck: () -> Unit,
    onHistory: () -> Unit,
    content: @Composable BoxScope.(Dp) -> Unit
) {
    val navigationBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentBottomPadding = if (selectedTab != null) navigationBarBottomInset else 0.dp
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        harmonyBgTop,
                        harmonyBgBottom
                    )
                )
            )
    ) {
        content(contentBottomPadding)
        AnimatedVisibility(
            visible = selectedTab != null,
            enter = fadeIn(tween(200, easing = FastOutSlowInEasing)) +
                slideInVertically(tween(200, easing = FastOutSlowInEasing)) { it / 2 },
            exit = fadeOut(tween(160, easing = FastOutSlowInEasing)) +
                slideOutVertically(tween(160, easing = FastOutSlowInEasing)) { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingBottomNav(
                selectedTab = selectedTab ?: BottomTab.Home,
                hapticsEnabled = hapticsEnabled,
                onHome = onHome,
                onReading = onReading,
                onDeck = onDeck,
                onHistory = onHistory
            )
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun CompactTopBar(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)?,
    action: (@Composable () -> Unit)?
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            HarmonyIconButton(
                icon = HarmonyIcon.Back,
                contentDescription = "뒤로가기",
                onClick = onBack
            )
        } else {
            Spacer(Modifier.size(48.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(title, color = harmonyInk, fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2)
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    color = harmonySub,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        action?.invoke() ?: Spacer(Modifier.size(48.dp))
    }
}

@Composable
fun CompactTextAction(
    label: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .semantics { this.contentDescription = contentDescription }
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = harmonyBlue, fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SystemPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(harmonyPanel)
            .border(1.dp, harmonyDivider, RoundedCornerShape(8.dp))
            .padding(vertical = 5.dp),
        content = content
    )
}

@Composable
fun InfoSummaryCard(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(harmonyPanel)
            .border(1.dp, harmonyDivider, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SystemIconTile(HarmonyIcon.Reading, harmonyBlue)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = harmonyInk, fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    color = harmonySub,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SystemDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 66.dp, end = 16.dp)
            .height(1.dp)
            .background(harmonyDivider.copy(alpha = 0.72f))
    )
}

@Composable
fun SystemIconTile(icon: HarmonyIcon, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        HarmonyIconGlyph(icon = icon, color = tint, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun SystemNumberTile(label: String, tint: Color = harmonyBlue) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = tint, fontSize = 15.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SystemMenuRow(title: String, subtitle: String, icon: HarmonyIcon, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 56.dp)
            .clickable { onClick() }
            .semantics(mergeDescendants = true) {
                contentDescription = "$title, $subtitle"
            }
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SystemIconTile(icon, harmonyBlue)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = harmonyInk, fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = harmonySub, fontSize = 11.sp, lineHeight = 14.sp)
        }
        HarmonyIconGlyph(HarmonyIcon.Chevron, harmonyTertiary, Modifier.size(18.dp))
    }
}

@Composable
private fun InfoListRow(item: InfoItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 56.dp)
            .clickable { onClick() }
            .semantics(mergeDescendants = true) {
                contentDescription = "${item.title}, ${item.subtitle}"
            }
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SystemIconTile(item.icon, harmonyBlue)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, color = harmonyInk, fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(item.subtitle, color = harmonySub, fontSize = 11.sp, lineHeight = 14.sp)
        }
        HarmonyIconGlyph(HarmonyIcon.Chevron, harmonyTertiary, Modifier.size(18.dp))
    }
}

@Composable
private fun SpreadShapeThumbnail(spread: SpreadOption, selected: Boolean) {
    val color = if (selected) harmonyBlue else harmonySub
    val slots = spreadSlots(spread, spread.cardCount)
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = if (selected) 0.14f else 0.08f))
            .border(1.dp, color.copy(alpha = if (selected) 0.42f else 0.16f), RoundedCornerShape(16.dp))
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val maxX = slots.maxOfOrNull { it.x } ?: 0f
            val maxY = slots.maxOfOrNull { it.y } ?: 0f
            val columns = maxX + 1f
            val rows = maxY + 1f
            val cellW = size.width / columns
            val cellH = size.height / rows
            val cardW = min(cellW * 0.58f, cellH * 0.44f).coerceAtLeast(4f)
            val cardH = (cardW / 0.62f).coerceAtMost(cellH * 0.9f)
            slots.take(spread.cardCount).forEach { slot ->
                val x = slot.x * cellW + cellW / 2f - cardW / 2f
                val y = slot.y * cellH + cellH / 2f - cardH / 2f
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

@Composable
private fun FloatingBottomNav(
    selectedTab: BottomTab,
    hapticsEnabled: Boolean,
    onHome: () -> Unit,
    onReading: () -> Unit,
    onDeck: () -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(harmonyPanel)
            .border(1.dp, harmonyDivider.copy(alpha = 0.75f), RoundedCornerShape(27.dp))
            .semantics { contentDescription = "하단 탭 바" }
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem("홈", HarmonyIcon.Home, selectedTab == BottomTab.Home, hapticsEnabled, onHome)
        BottomNavItem("리딩", HarmonyIcon.Reading, selectedTab == BottomTab.Reading, hapticsEnabled, onReading)
        BottomNavItem("덱", HarmonyIcon.Deck, selectedTab == BottomTab.Deck, hapticsEnabled, onDeck)
        BottomNavItem("기록", HarmonyIcon.History, selectedTab == BottomTab.History, hapticsEnabled, onHistory)
    }
}

@Composable
private fun BottomNavItem(label: String, icon: HarmonyIcon, selected: Boolean, hapticsEnabled: Boolean, onClick: () -> Unit) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val animationsEnabled = remember { ValueAnimator.areAnimatorsEnabled() }
    Column(
        modifier = Modifier
            .sizeIn(minWidth = 54.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .semantics {
                contentDescription = "$label 탭"
                stateDescription = if (selected) "선택됨" else "선택 안 됨"
            }
            .clickable {
                if (hapticsEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                if (animationsEnabled) {
                    scope.launch {
                        scale.animateTo(
                            1.1f,
                            animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)
                        )
                        scale.animateTo(
                            1f,
                            animationSpec = spring(dampingRatio = 0.90f, stiffness = 360f)
                        )
                    }
                }
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HarmonyIconGlyph(icon, if (selected) harmonyBlue else harmonyTertiary, Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, color = if (selected) harmonyBlue else harmonyTertiary, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingsScreen(
    themeChoice: ThemeChoice,
    useReversed: Boolean,
    cardBackStyle: CardBackStyle,
    customCardBackUri: String?,
    hapticsEnabled: Boolean,
    onBack: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenCardBack: () -> Unit,
    onDirectionChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onOpenInfo: () -> Unit,
    onOpenPrompt: () -> Unit,
    bottomPadding: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp)
            .padding(bottom = hoscatContentBottomPadding(bottomPadding)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CompactTopBar(
            title = "앱 설정",
            subtitle = "",
            onBack = onBack,
            action = null
        )

        SystemPanel {
            SystemMenuRow(
                "테마",
                "${themeChoice.label} · ${themeChoice.subtitle}",
                HarmonyIcon.Theme,
                onOpenTheme
            )
            SystemDivider()
            SystemMenuRow(
                "카드 뒷면",
                if (cardBackStyle == CardBackStyle.CustomImage && customCardBackUri == null) {
                    "개인 이미지를 선택해 주세요"
                } else {
                    "${cardBackStyle.label} · ${cardBackStyle.subtitle}"
                },
                HarmonyIcon.Deck,
                onOpenCardBack
            )
            SystemDivider()
            SystemMenuRow(
                "기본 리딩 방향",
                if (useReversed) "정방향/역방향 사용" else "정방향만 사용",
                HarmonyIcon.Direction
            ) {
                onDirectionChange(!useReversed)
            }
            SystemDivider()
            SystemMenuRow(
                "탭 햅틱 반응",
                if (hapticsEnabled) "하단 탭을 누를 때 약하게 반응" else "하단 탭 햅틱 꺼짐",
                HarmonyIcon.Check
            ) {
                onHapticsChange(!hapticsEnabled)
            }
        }

        SystemPanel {
            SystemMenuRow(
                "정보",
                "제작 정보 · 개인정보 · 이용 고지",
                HarmonyIcon.Info,
                onOpenInfo
            )
            SystemDivider()
            SystemMenuRow(
                "해석 작성 원칙",
                "상담 보조용 해석 톤과 주의사항",
                HarmonyIcon.Ai,
                onOpenPrompt
            )
        }

        HarmonyCard(padding = PaddingValues(16.dp)) {
            Text("저장 데이터", color = harmonyInk, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "리딩 질문, 카드 배열, 카드 이미지 참조, 위치 의미와 해석은 현재 기기 안에 저장됩니다. 현재 빌드는 앱 자동 백업에 이 데이터를 포함하지 않도록 설정되어 있습니다.",
                color = harmonySub,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun InfoScreen(
    onBack: () -> Unit,
    adsDisabled: Boolean,
    activatedCodeCount: Int,
    onOpenPurchaseCode: () -> Unit,
    onShareDiagnostics: () -> Unit,
    bottomPadding: Dp
) {
    val context = LocalContext.current
    val versionLabel = remember(context) {
        runCatching {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            "버전 ${packageInfo.versionName.orEmpty()} (${packageInfo.longVersionCode})"
        }.getOrDefault("버전 정보를 확인할 수 없습니다")
    }
    val infoItems = remember {
        listOf(
            InfoItem(
                "개인정보",
                "기기 내 저장 안내",
                "리딩 질문, 저장한 카드 배열, 카드 위치 의미, 개인 덱 정보와 해석 기록은 현재 기기 안의 앱 저장소에 보관됩니다.\n\n현재 빌드는 광고, 분석, 백엔드 기능을 추가하기 전까지 별도 서버로 리딩 내용을 전송하지 않는 구조입니다. 출시 전 개인정보 처리방침에도 같은 기준을 명시해야 합니다.",
                HarmonyIcon.Info
            ),
            InfoItem(
                "이용 고지",
                "타로와 AI 해석 안내",
                "먕타로의 리딩 결과는 엔터테인먼트와 자기 성찰을 위한 참고 정보입니다.\n\nAI 해석은 선택한 카드, 질문, 스프레드 흐름을 바탕으로 만든 참고 해석입니다. 중요한 결정은 현실의 정보와 자신의 판단을 함께 살펴 주세요.\n\n의료, 법률, 투자, 심리 상담 등 전문적 판단이 필요한 영역에서는 전문가의 도움을 받아 주세요.",
                HarmonyIcon.Info
            ),
            InfoItem(
                "라이선스",
                "카드 이미지 · 오픈소스",
                "앱에 포함된 기본 덱은 유니버셜 타로 78장 구성입니다. 개인 덱을 추가하는 경우 해당 이미지의 사용 권한은 사용자가 확인해야 합니다.\n\n앱에 포함된 오픈소스 구성 요소는 각 프로젝트의 라이선스와 고지 사항을 따릅니다.",
                HarmonyIcon.Deck
            )
        )
    }
    var selectedInfo by remember { mutableStateOf<InfoItem?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp)
            .padding(bottom = hoscatContentBottomPadding(bottomPadding)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CompactTopBar(
            title = "정보",
            subtitle = "",
            onBack = onBack,
            action = null
        )

        HarmonyCard(padding = PaddingValues(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.myang_home_logo),
                    contentDescription = "먕타로 로고",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("먕타로", color = harmonyInk, fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold)
                    Text(versionLabel, color = harmonySub, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }

        SystemPanel {
            infoItems.forEachIndexed { index, item ->
                InfoListRow(item = item, onClick = { selectedInfo = item })
                if (index != infoItems.lastIndex) {
                    SystemDivider()
                }
            }
        }

        SystemPanel {
            SystemMenuRow(
                title = "커피 코드",
                subtitle = if (adsDisabled) "커피 코드 적용됨 · ${activatedCodeCount}개" else "커피를 선물해준 당신에게 드리는 서비스",
                icon = if (adsDisabled) HarmonyIcon.Check else HarmonyIcon.Info,
                onClick = onOpenPurchaseCode
            )
            SystemDivider()
            SystemMenuRow(
                title = "진단 정보 공유",
                subtitle = "질문 · 카드 내용 · 개인 이미지는 포함하지 않음",
                icon = HarmonyIcon.Info,
                onClick = onShareDiagnostics
            )
        }
    }
    selectedInfo?.let { item ->
        InfoDetailSheetV2(
            item = item,
            adsDisabled = adsDisabled,
            onClose = { selectedInfo = null }
        )
    }
}

@Composable
private fun PromptSettingsScreen(
    onBack: () -> Unit,
    bottomPadding: Dp
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val defaultPromptGuide = """
AI 해석 요청 원칙

1. 과장된 예언체 대신 카드가 보여주는 흐름, 선택지, 주의점을 설명한다.
2. 각 카드의 위치 의미와 정/역방향을 분리해서 읽고, 마지막에 전체 흐름을 통합한다.
3. 상담자가 바로 말로 옮길 수 있도록 짧은 문장과 현실적인 조언을 사용한다.
4. 의료, 법률, 투자처럼 전문 판단이 필요한 영역은 단정하지 않는다.
5. 불안을 키우는 표현보다 관찰, 가능성, 다음 행동을 중심으로 정리한다.
""".trimIndent()
    var promptGuide by remember { mutableStateOf(defaultPromptGuide) }
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
            title = "AI 해석",
            subtitle = "상담 보조용 해석 톤",
            onBack = onBack,
            action = null
        )
        HarmonyCard {
            Text("기본 원칙", color = harmonyInk, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = promptGuide,
                onValueChange = { promptGuide = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = harmonyInk,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            )
        }
        HoscatBottomCta(
            label = "해석 요청 원칙 복사",
            onClick = {
                clipboard.setText(AnnotatedString(promptGuide))
                Toast.makeText(context, "해석 요청 원칙을 복사했습니다", Toast.LENGTH_SHORT).show()
            }
        )
        HoscatBottomCta(
            label = "기본값으로 되돌리기",
            onClick = { promptGuide = defaultPromptGuide },
            containerColor = harmonySecondaryPanel,
            contentColor = harmonyInk
        )
    }
}

@Composable
fun AppBottomSheet(
    heightFraction: Float,
    adsDisabled: Boolean,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
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
            if (!adsDisabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    BottomSheetAdSlot(adsDisabled = false)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MyangSheetBottomClearance)
                    .fillMaxHeight(heightFraction)
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MyangSheetGrabber(onClose)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    content()
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun BottomSheetAdSlot(adsDisabled: Boolean) {
    if (adsDisabled) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(harmonySecondaryPanel.copy(alpha = 0.64f))
            .border(1.dp, harmonyDivider.copy(alpha = 0.62f), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("광고 영역", color = harmonyTertiary, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SheetHeader(
    title: String,
    subtitle: String,
    closeDescription: String,
    onClose: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = harmonyInk, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    subtitle,
                    color = harmonySub,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        HarmonyIconButton(HarmonyIcon.Close, closeDescription, onClose)
    }
}

@Composable
private fun PurchaseCodeSheet(
    adsDisabled: Boolean,
    activatedCodeCount: Int,
    onApplyCode: (String) -> Boolean,
    onClose: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var code by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var accepted by remember { mutableStateOf<Boolean?>(null) }
    AppBottomSheet(
        heightFraction = 0.48f,
        adsDisabled = adsDisabled,
        onClose = onClose
    ) {
        SheetHeader(
            title = "커피 코드",
            subtitle = if (adsDisabled) {
                "커피 코드 적용됨 · ${activatedCodeCount}개"
            } else {
                "커피를 선물해준 당신에게 드리는 서비스입니다. 등록된 코드는 광고 숨김 등 앱 혜택을 적용합니다."
            },
            closeDescription = "커피 코드 닫기",
            onClose = onClose
        )
        OutlinedTextField(
            value = code,
            onValueChange = {
                code = it.take(40)
                accepted = null
                resultMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = accepted == false,
            label = { Text("커피 코드") }
        )
        resultMessage?.let { message ->
            Text(
                message,
                color = if (accepted == true) harmonyBlue else harmonyRose,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        HoscatBottomCta(
            label = "커피 코드 적용",
            enabled = code.isNotBlank(),
            onClick = {
                val success = onApplyCode(code)
                accepted = success
                resultMessage = if (success) "커피 코드가 적용되었습니다" else "등록되지 않은 코드입니다"
                if (success) {
                    code = ""
                    focusManager.clearFocus()
                }
            },
            disabledContainerColor = harmonySecondaryPanel
        ) {
            Text(
                "적용",
                color = if (code.isNotBlank()) Color.White else harmonyTertiary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            "코드는 공백과 대소문자를 구분하지 않습니다.",
            color = harmonyTertiary,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun ThemeSelectSheetV2(
    selected: ThemeChoice,
    adsDisabled: Boolean,
    onSelect: (ThemeChoice) -> Unit,
    onClose: () -> Unit
) {
    AppBottomSheet(
        heightFraction = 0.72f,
        adsDisabled = adsDisabled,
        onClose = onClose
    ) {
        SheetHeader(
            title = "테마 설정",
            subtitle = "",
            closeDescription = "테마 설정 닫기",
            onClose = onClose
        )
        SystemPanel {
            ThemeChoice.values().forEachIndexed { index, choice ->
                ThemeChoiceRow(
                    choice = choice,
                    selected = choice == selected,
                    onClick = { onSelect(choice) }
                )
                if (index != ThemeChoice.values().lastIndex) {
                    SystemDivider()
                }
            }
        }
    }
}

@Composable
private fun ThemeChoiceRow(
    choice: ThemeChoice,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 66.dp)
            .clickable { onClick() }
            .semantics(mergeDescendants = true) {
                contentDescription = if (selected) {
                    "${choice.label}, 현재 선택, ${choice.subtitle}"
                } else {
                    "${choice.label}, ${choice.subtitle}"
                }
            }
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SystemIconTile(if (selected) HarmonyIcon.Check else HarmonyIcon.Theme, harmonyBlue)
        Spacer(Modifier.width(13.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                choice.label,
                color = harmonyInk,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                choice.subtitle,
                color = harmonySub,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(12.dp))
        if (selected) {
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(harmonyBlue.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "선택됨",
                    color = harmonyBlue,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            HarmonyIconGlyph(HarmonyIcon.Chevron, harmonyTertiary, Modifier.size(18.dp))
        }
    }
}

@Composable
private fun CardBackSelectSheet(
    selected: CardBackStyle,
    customCardBackUri: String?,
    adsDisabled: Boolean,
    onPickCustomImage: () -> Unit,
    onClearCustomImage: () -> Unit,
    onSelect: (CardBackStyle) -> Unit,
    onClose: () -> Unit
) {
    AppBottomSheet(
        heightFraction = 0.82f,
        adsDisabled = adsDisabled,
        onClose = onClose
    ) {
        SheetHeader(
            title = "카드 뒷면",
            subtitle = "기본 프리셋 또는 개인 이미지를 선택",
            closeDescription = "카드 뒷면 설정 닫기",
            onClose = onClose
        )
        SystemPanel {
            CardBackStyle.values()
                .filterNot { it == CardBackStyle.CustomImage }
                .forEachIndexed { index, style ->
                    SystemMenuRow(
                        title = style.label,
                        subtitle = if (style == selected) "현재 선택 · ${style.subtitle}" else style.subtitle,
                        icon = if (style == selected) HarmonyIcon.Check else HarmonyIcon.Deck,
                        onClick = { onSelect(style) }
                    )
                    if (index != CardBackStyle.values().filterNot { it == CardBackStyle.CustomImage }.lastIndex) {
                        SystemDivider()
                    }
                }
        }
        SystemPanel {
            SystemMenuRow(
                title = "개인 이미지 사용",
                subtitle = when {
                    selected == CardBackStyle.CustomImage && customCardBackUri != null -> "현재 선택 · 이미지 저장됨"
                    customCardBackUri != null -> "저장된 이미지로 변경"
                    else -> "이미지 파일을 골라 카드 뒷면으로 사용"
                },
                icon = if (selected == CardBackStyle.CustomImage) HarmonyIcon.Check else HarmonyIcon.Add,
                onClick = {
                    if (customCardBackUri == null) onPickCustomImage() else onSelect(CardBackStyle.CustomImage)
                }
            )
            SystemDivider()
            SystemMenuRow(
                title = "개인 이미지 다시 선택",
                subtitle = "다른 이미지 파일로 교체",
                icon = HarmonyIcon.Theme,
                onClick = onPickCustomImage
            )
            if (customCardBackUri != null) {
                SystemDivider()
                SystemMenuRow(
                    title = "개인 이미지 해제",
                    subtitle = "테마 기본 카드 뒷면으로 되돌림",
                    icon = HarmonyIcon.Delete,
                    onClick = onClearCustomImage
                )
            }
        }
        Text(
            "개인 이미지는 기기 안의 파일 권한을 저장해 사용합니다. 원본 파일을 삭제하면 앱에서 표시되지 않을 수 있습니다.",
            color = harmonyTertiary,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun InfoDetailSheetV2(
    item: InfoItem,
    adsDisabled: Boolean,
    onClose: () -> Unit
) {
    AppBottomSheet(
        heightFraction = 0.54f,
        adsDisabled = adsDisabled,
        onClose = onClose
    ) {
        SheetHeader(
            title = item.title,
            subtitle = item.subtitle,
            closeDescription = "정보 상세 닫기",
            onClose = onClose
        )
        Text(
            item.body,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .verticalScroll(rememberScrollState()),
            color = harmonyInk,
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}


@Composable
fun HarmonyCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(harmonyPanel)
            .border(1.dp, harmonyDivider, RoundedCornerShape(8.dp))
            .padding(padding),
        content = content
    )
}

@Composable
fun DeckAction(title: String, subtitle: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(harmonySecondaryPanel)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(harmonyBlue.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = harmonyBlue, fontSize = 22.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                title,
                color = harmonyInk,
                fontSize = 16.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                color = harmonySub,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
