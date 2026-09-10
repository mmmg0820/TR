package com.softcat.mystictarot

internal fun buildAutoSavedSpreadPreset(
    spread: SpreadOption,
    baseSpreads: List<SpreadOption>,
    existingPresets: List<SavedSpreadPreset>,
    now: Long
): SavedSpreadPreset? {
    if (spread.key.startsWith("saved:")) return null
    val base = baseSpreads.firstOrNull { it.key == spread.key } ?: return null
    val normalizedLabels = spread.positionLabels
        .take(spread.cardCount)
        .map(String::trim)
    if (normalizedLabels.size != spread.cardCount || normalizedLabels.any(String::isBlank)) return null
    if (normalizedLabels == base.positionLabels.take(base.cardCount).map(String::trim)) return null
    if (existingPresets.any { preset ->
            preset.layoutId == spread.layoutId &&
                preset.cardCount == spread.cardCount &&
                preset.positionLabels.map(String::trim) == normalizedLabels
        }
    ) return null

    return SavedSpreadPreset(
        id = "auto-${spread.layoutId}-$now",
        name = "${spread.layoutTitle} · 직접 의미",
        baseSpreadKey = base.key,
        cardCount = spread.cardCount,
        drawMode = spread.drawMode,
        layoutId = spread.layoutId,
        layoutTitle = spread.layoutTitle,
        positionPresetTitle = "저장한 직접 의미",
        positionLabels = normalizedLabels,
        createdAt = now,
        updatedAt = now
    )
}
