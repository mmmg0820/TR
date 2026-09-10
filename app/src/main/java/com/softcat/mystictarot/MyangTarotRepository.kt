package com.softcat.mystictarot

import android.content.Context
import com.softcat.mystictarot.ui.theme.ThemeChoice
import org.json.JSONArray
import org.json.JSONObject

class MyangTarotRepository(context: Context) {
    private val appContext = context.applicationContext

    init {
        migrateStoredDataIfNeeded()
    }

    private fun persistJson(prefName: String, key: String, value: String) {
        appContext.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }

    private fun migrateStoredDataIfNeeded() {
        migrateAppSettingsIfNeeded()
        migrateCustomDecksIfNeeded()
        migrateSavedReadingsIfNeeded()
    }

    private fun migrateAppSettingsIfNeeded() {
        val raw = appContext.getSharedPreferences(APP_SETTINGS_PREF, Context.MODE_PRIVATE)
            .getString(APP_SETTINGS_KEY, null)
            ?: return
        val shouldMigrate = runCatching {
            JSONObject(raw).optInt("schemaVersion", 1) < APP_SETTINGS_SCHEMA_VERSION
        }.getOrDefault(false)
        if (!shouldMigrate) return

        val settings = loadAppSettings()
        persistAppSettings(
            themeChoice = settings.themeChoice,
            selectedDeckId = settings.selectedDeckId ?: "standard",
            useReversed = settings.useReversed,
            recentSpreadKey = settings.recentSpreadKey,
            spreadUseCounts = settings.spreadUseCounts,
            adsDisabled = settings.adsDisabled,
            activatedPurchaseCodeLabels = settings.activatedPurchaseCodeLabels,
            cardBackStyle = settings.cardBackStyle,
            customCardBackUri = settings.customCardBackUri,
            hapticsEnabled = settings.hapticsEnabled,
            customPositionLabelsBySpreadKey = settings.customPositionLabelsBySpreadKey
        )
    }

    private fun migrateCustomDecksIfNeeded() {
        val raw = appContext.getSharedPreferences(CUSTOM_DECKS_PREF, Context.MODE_PRIVATE)
            .getString(CUSTOM_DECKS_KEY, null)
            ?: return
        val shouldMigrate = runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).any { index ->
                (array.optJSONObject(index)?.optInt("schemaVersion", 1) ?: 1) < CUSTOM_DECK_SCHEMA_VERSION
            }
        }.getOrDefault(false)
        if (!shouldMigrate) return

        val decks = loadCustomDecks()
        if (decks.isNotEmpty()) {
            persistCustomDecks(decks)
        }
    }

    private fun migrateSavedReadingsIfNeeded() {
        val raw = appContext.getSharedPreferences(SAVED_READINGS_PREF, Context.MODE_PRIVATE)
            .getString(SAVED_READINGS_KEY, null)
            ?: return
        val shouldMigrate = runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).any { index ->
                (array.optJSONObject(index)?.optInt("schemaVersion", 1) ?: 1) < SAVED_READING_SCHEMA_VERSION
            }
        }.getOrDefault(false)
        if (!shouldMigrate) return

        val readings = loadSavedReadings()
        if (readings.isNotEmpty()) {
            persistSavedReadings(readings)
        }
    }

    fun loadAppSettings(): AppSettings {
        val raw = appContext.getSharedPreferences(APP_SETTINGS_PREF, Context.MODE_PRIVATE)
            .getString(APP_SETTINGS_KEY, null)
            ?: return AppSettings()
        return runCatching {
            val json = JSONObject(raw)
            val countsObject = json.optJSONObject("spreadUseCounts") ?: JSONObject()
            val counts = buildMap {
                val keys = countsObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    put(key, countsObject.optInt(key, 0))
                }
            }
            val customPositionObject = json.optJSONObject("customPositionLabelsBySpreadKey") ?: JSONObject()
            val customPositionLabels = buildMap {
                val keys = customPositionObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val array = customPositionObject.optJSONArray(key) ?: continue
                    val labels = buildList {
                        for (index in 0 until array.length()) {
                            add(array.optString(index))
                        }
                    }
                    if (labels.isNotEmpty()) {
                        put(key, labels)
                    }
                }
            }
            AppSettings(
                schemaVersion = json.optInt("schemaVersion", 1),
                themeChoice = ThemeChoice.fromStored(json.optString("themeChoice", ThemeChoice.HoscatSignature.name)),
                selectedDeckId = json.optString("selectedDeckId").takeIf { it.isNotBlank() && it != "null" },
                useReversed = json.optBoolean("useReversed", true),
                recentSpreadKey = json.optString("recentSpreadKey").takeIf { it.isNotBlank() && it != "null" },
                spreadUseCounts = counts,
                adsDisabled = json.optBoolean("adsDisabled", false),
                activatedPurchaseCodeLabels = json.optJSONArray("activatedPurchaseCodeLabels")?.let { array ->
                    buildSet {
                        for (index in 0 until array.length()) {
                            array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                        }
                    }
                } ?: emptySet(),
                cardBackStyle = cardBackStyleFromStored(json.optString("cardBackStyle", CardBackStyle.ThemeDefault.name)),
                customCardBackUri = json.optString("customCardBackUri").takeIf { it.isNotBlank() && it != "null" },
                hapticsEnabled = json.optBoolean("hapticsEnabled", true),
                customPositionLabelsBySpreadKey = customPositionLabels
            )
        }.getOrDefault(AppSettings())
    }

    fun persistAppSettings(
        themeChoice: ThemeChoice,
        selectedDeckId: String,
        useReversed: Boolean,
        recentSpreadKey: String?,
        spreadUseCounts: Map<String, Int>,
        adsDisabled: Boolean,
        activatedPurchaseCodeLabels: Set<String>,
        cardBackStyle: CardBackStyle,
        customCardBackUri: String?,
        hapticsEnabled: Boolean,
        customPositionLabelsBySpreadKey: Map<String, List<String>> = emptyMap()
    ) {
        val countsObject = JSONObject()
        spreadUseCounts.forEach { (key, value) -> countsObject.put(key, value) }
        val codeLabels = JSONArray()
        activatedPurchaseCodeLabels.forEach { codeLabels.put(it) }
        val customPositionsObject = JSONObject()
        customPositionLabelsBySpreadKey.forEach { (key, labels) ->
            customPositionsObject.put(key, JSONArray(labels))
        }
        val json = JSONObject()
            .put("schemaVersion", APP_SETTINGS_SCHEMA_VERSION)
            .put("themeChoice", themeChoice.name)
            .put("selectedDeckId", selectedDeckId)
            .put("useReversed", useReversed)
            .put("recentSpreadKey", recentSpreadKey ?: JSONObject.NULL)
            .put("spreadUseCounts", countsObject)
            .put("adsDisabled", adsDisabled)
            .put("activatedPurchaseCodeLabels", codeLabels)
            .put("cardBackStyle", cardBackStyle.name)
            .put("customCardBackUri", customCardBackUri ?: JSONObject.NULL)
            .put("hapticsEnabled", hapticsEnabled)
            .put("customPositionLabelsBySpreadKey", customPositionsObject)
        persistJson(APP_SETTINGS_PREF, APP_SETTINGS_KEY, json.toString())
    }

    fun persistSelectedDeckId(selectedDeckId: String) {
        val current = loadAppSettings()
        persistAppSettings(
            themeChoice = current.themeChoice,
            selectedDeckId = selectedDeckId,
            useReversed = current.useReversed,
            recentSpreadKey = current.recentSpreadKey,
            spreadUseCounts = current.spreadUseCounts,
            adsDisabled = current.adsDisabled,
            activatedPurchaseCodeLabels = current.activatedPurchaseCodeLabels,
            cardBackStyle = current.cardBackStyle,
            customCardBackUri = current.customCardBackUri,
            hapticsEnabled = current.hapticsEnabled,
            customPositionLabelsBySpreadKey = current.customPositionLabelsBySpreadKey
        )
    }

    fun persistCustomDecks(decks: List<TarotDeck>) {
        val array = JSONArray()
        decks.forEach { deck ->
            val cards = JSONArray()
            deck.cards.forEach { card ->
                cards.put(
                    JSONObject()
                        .put("id", card.id)
                        .put("nameEn", card.nameEn)
                        .put("nameKr", card.nameKr)
                        .put("arcana", card.arcana)
                        .put("basicMeaning", card.basicMeaning)
                        .put("uprightKeywords", JSONArray(card.uprightKeywords))
                        .put("reversedKeywords", JSONArray(card.reversedKeywords))
                        .put("imageUri", card.imageUri ?: JSONObject.NULL)
                )
            }
            array.put(
                JSONObject()
                    .put("schemaVersion", CUSTOM_DECK_SCHEMA_VERSION)
                    .put("id", deck.id)
                    .put("name", deck.name)
                    .put("enabled", deck.enabled)
                    .put("aiPrompt", deck.aiPrompt)
                    .put("cards", cards)
            )
        }
        persistJson(CUSTOM_DECKS_PREF, CUSTOM_DECKS_KEY, array.toString())
    }

    fun loadCustomDecks(): List<TarotDeck> {
        val raw = appContext.getSharedPreferences(CUSTOM_DECKS_PREF, Context.MODE_PRIVATE)
            .getString(CUSTOM_DECKS_KEY, null)
            ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val deckJson = array.optJSONObject(i) ?: continue
                    val cardsJson = deckJson.optJSONArray("cards") ?: JSONArray()
                    val cards = buildList {
                        for (j in 0 until cardsJson.length()) {
                            val cardJson = cardsJson.optJSONObject(j) ?: continue
                            add(
                                TarotCard(
                                    id = cardJson.optInt("id", j),
                                    nameEn = cardJson.optString("nameEn", "Custom Card ${j + 1}"),
                                    nameKr = cardJson.optString("nameKr", "개인 카드 ${j + 1}"),
                                    arcana = cardJson.optString("arcana", "Custom Deck"),
                                    basicMeaning = cardJson.optString("basicMeaning", ""),
                                    uprightKeywords = cardJson.optJSONArray("uprightKeywords").joinList(),
                                    reversedKeywords = cardJson.optJSONArray("reversedKeywords").joinList(),
                                    imageUri = cardJson.optString("imageUri").takeIf { it.isNotBlank() && it != "null" }
                                )
                            )
                        }
                    }
                    if (cards.isNotEmpty()) {
                        add(
                            TarotDeck(
                                id = deckJson.optString("id", "custom-$i"),
                                name = deckJson.optString("name", "개인 덱 ${i + 1} (${cards.size}장)"),
                                cards = cards,
                                enabled = deckJson.optBoolean("enabled", true),
                                aiPrompt = deckJson.optString("aiPrompt", ""),
                                schemaVersion = deckJson.optInt("schemaVersion", 1)
                            )
                        )
                    }
                }
            }
        }.getOrElse { emptyList() }
    }

    fun persistSavedReadings(readings: List<SavedReading>) {
        val array = JSONArray()
        readings.forEach { reading ->
            val cards = JSONArray()
            reading.cards.forEach { card ->
                cards.put(
                    JSONObject()
                        .put("order", card.order)
                        .put("nameKr", card.nameKr)
                        .put("nameEn", card.nameEn)
                        .put("directionLabel", card.directionLabel)
                        .put("positionLabel", card.positionLabel)
                        .put("meaningSnapshot", card.meaningSnapshot)
                        .put("cardId", card.cardId)
                        .put("imageUri", card.imageUri ?: JSONObject.NULL)
                )
            }
            array.put(
                JSONObject()
                    .put("schemaVersion", SAVED_READING_SCHEMA_VERSION)
                    .put("id", reading.id)
                    .put("savedAt", reading.savedAt)
                    .put("title", reading.title)
                    .put("spreadTitle", reading.spreadTitle)
                    .put("layoutTitle", reading.layoutTitle)
                    .put("positionPresetTitle", reading.positionPresetTitle)
                    .put("question", reading.question)
                    .put("interpretation", reading.interpretation)
                    .put("deckId", reading.deckId)
                    .put("deckName", reading.deckName)
                    .put("deckAiPromptSnapshot", reading.deckAiPromptSnapshot)
                    .put("cards", cards)
            )
        }
        persistJson(SAVED_READINGS_PREF, SAVED_READINGS_KEY, array.toString())
    }

    fun loadSavedReadings(): List<SavedReading> {
        val raw = appContext.getSharedPreferences(SAVED_READINGS_PREF, Context.MODE_PRIVATE)
            .getString(SAVED_READINGS_KEY, null)
            ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val cardsArray = item.optJSONArray("cards") ?: JSONArray()
                    val cards = buildList {
                        for (j in 0 until cardsArray.length()) {
                            val card = cardsArray.optJSONObject(j) ?: continue
                            add(
                                SavedReadingCard(
                                    order = card.optInt("order", j + 1),
                                    nameKr = card.optString("nameKr", "카드 ${j + 1}"),
                                    nameEn = card.optString("nameEn", ""),
                                    directionLabel = card.optString("directionLabel", ""),
                                    positionLabel = card.optString("positionLabel", ""),
                                    meaningSnapshot = card.optString("meaningSnapshot", ""),
                                    cardId = card.optInt("cardId", -1),
                                    imageUri = card.optString("imageUri").takeIf { it.isNotBlank() && it != "null" }
                                )
                            )
                        }
                    }
                    add(
                        SavedReading(
                            id = item.optLong("id", item.optLong("savedAt", System.currentTimeMillis())),
                            savedAt = item.optLong("savedAt", System.currentTimeMillis()),
                            title = item.optString("title", "저장된 타로"),
                            spreadTitle = item.optString("spreadTitle", "리딩"),
                            layoutTitle = item.optString("layoutTitle", item.optString("spreadTitle", "리딩")),
                            positionPresetTitle = item.optString("positionPresetTitle", item.optString("spreadTitle", "리딩")),
                            question = item.optString("question", ""),
                            interpretation = item.optString("interpretation", ""),
                            deckId = item.optString("deckId", "standard").ifBlank { "standard" },
                            deckName = item.optString("deckName", "유니버셜 타로").ifBlank { "유니버셜 타로" },
                            deckAiPromptSnapshot = item.optString("deckAiPromptSnapshot", ""),
                            cards = cards,
                            schemaVersion = item.optInt("schemaVersion", 1)
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    fun persistSavedSpreadPresets(presets: List<SavedSpreadPreset>) {
        val array = JSONArray()
        presets.forEach { preset ->
            array.put(
                JSONObject()
                    .put("id", preset.id)
                    .put("name", preset.name)
                    .put("baseSpreadKey", preset.baseSpreadKey)
                    .put("cardCount", preset.cardCount)
                    .put("drawMode", preset.drawMode.name)
                    .put("layoutId", preset.layoutId)
                    .put("layoutTitle", preset.layoutTitle)
                    .put("positionPresetTitle", preset.positionPresetTitle)
                    .put("positionLabels", JSONArray(preset.positionLabels))
                    .put("createdAt", preset.createdAt)
                    .put("updatedAt", preset.updatedAt)
            )
        }
        persistJson(SAVED_SPREAD_PRESETS_PREF, SAVED_SPREAD_PRESETS_KEY, array.toString())
    }

    fun loadSavedSpreadPresets(): List<SavedSpreadPreset> {
        val raw = appContext.getSharedPreferences(SAVED_SPREAD_PRESETS_PREF, Context.MODE_PRIVATE)
            .getString(SAVED_SPREAD_PRESETS_KEY, null)
            ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val cardCount = item.optInt("cardCount", 0)
                    val labels = item.optJSONArray("positionLabels").joinList()
                    if (cardCount <= 0 || labels.isEmpty()) continue
                    add(
                        SavedSpreadPreset(
                            id = item.optString("id", "spread-${item.optLong("createdAt", System.currentTimeMillis())}"),
                            name = item.optString("name", "저장된 스프레드"),
                            baseSpreadKey = item.optString("baseSpreadKey", ""),
                            cardCount = cardCount,
                            drawMode = runCatching {
                                SpreadDrawMode.valueOf(item.optString("drawMode", SpreadDrawMode.Normal.name))
                            }.getOrDefault(SpreadDrawMode.Normal),
                            layoutId = item.optString("layoutId", "custom"),
                            layoutTitle = item.optString("layoutTitle", "${cardCount}장 배열"),
                            positionPresetTitle = item.optString("positionPresetTitle", "저장된 스프레드"),
                            positionLabels = labels.take(cardCount),
                            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = item.optLong("updatedAt", item.optLong("createdAt", System.currentTimeMillis()))
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    fun loadStandardTarotCards(fallbackCards: List<TarotCard>): List<TarotCard> {
        loadFullTarotCards(fallbackCards)?.let { return it }
        return loadMajorArcanaPatch(fallbackCards)
    }

    private fun loadFullTarotCards(fallbackCards: List<TarotCard>): List<TarotCard>? {
        return runCatching {
            val raw = appContext.assets.open("tarot_cards_78.json")
                .bufferedReader()
                .use { it.readText() }
            val array = JSONArray(raw)
            val cardsById = parseCardMap(array, fallbackCards)
            if (cardsById.size < fallbackCards.size) {
                null
            } else {
                fallbackCards.indices.mapNotNull { cardsById[it] }
                    .takeIf { it.size == fallbackCards.size }
            }
        }.getOrNull()
    }

    private fun loadMajorArcanaPatch(fallbackCards: List<TarotCard>): List<TarotCard> {
        return runCatching {
            val raw = appContext.assets.open("tarot_major_arcana.json")
                .bufferedReader()
                .use { it.readText() }
            val array = JSONArray(raw)
            val majorCards = parseCardMap(array, fallbackCards)
            fallbackCards.map { card -> majorCards[card.id] ?: card }
        }.getOrElse {
            fallbackCards
        }
    }

    private fun parseCardMap(array: JSONArray, fallbackCards: List<TarotCard>): Map<Int, TarotCard> {
        return buildMap {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val id = item.optInt("id", -1)
                if (id >= 0) {
                    val fallback = fallbackCards.getOrNull(id)
                    val upright = item.optJSONArray("upright_keywords").joinList()
                    val reversed = item.optJSONArray("reversed_keywords").joinList()
                    val meaning = buildMeaning(
                        basicMeaning = item.optString("basic_meaning", ""),
                        upright = upright,
                        reversed = reversed
                    )
                    put(
                        id,
                        TarotCard(
                            id = id,
                            nameEn = item.optString("name_en", fallback?.nameEn ?: ""),
                            nameKr = item.optString("name_kr", fallback?.nameKr ?: ""),
                            arcana = item.optString("arcana", fallback?.arcana ?: "Major Arcana"),
                            basicMeaning = meaning.ifBlank { fallback?.basicMeaning ?: "" },
                            uprightKeywords = upright,
                            reversedKeywords = reversed,
                            imageUri = item.optString("image_uri").takeIf { it.isNotBlank() && it != "null" } ?: fallback?.imageUri
                        )
                    )
                }
            }
        }
    }

    private fun buildMeaning(
        basicMeaning: String,
        upright: List<String>,
        reversed: List<String>
    ): String {
        return buildString {
            append(basicMeaning)
            if (upright.isNotEmpty()) append("\n정방향 키워드: ${upright.joinToString(", ")}")
            if (reversed.isNotEmpty()) append("\n역방향 키워드: ${reversed.joinToString(", ")}")
        }
    }

    private fun cardBackStyleFromStored(value: String): CardBackStyle {
        return runCatching { CardBackStyle.valueOf(value) }.getOrDefault(CardBackStyle.ThemeDefault)
    }

    private fun JSONArray?.joinList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }

    private companion object {
        const val APP_SETTINGS_PREF = "myang_app_settings"
        const val APP_SETTINGS_KEY = "settings"
        const val CUSTOM_DECKS_PREF = "myang_custom_decks"
        const val CUSTOM_DECKS_KEY = "decks"
        const val SAVED_READINGS_PREF = "myang_saved_readings"
        const val SAVED_READINGS_KEY = "items"
        const val SAVED_SPREAD_PRESETS_PREF = "myang_saved_spread_presets"
        const val SAVED_SPREAD_PRESETS_KEY = "items"
    }
}
