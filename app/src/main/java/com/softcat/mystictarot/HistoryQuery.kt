package com.softcat.mystictarot

internal fun savedReadingsForDisplay(
    readings: List<SavedReading>,
    query: String,
    pinnedOnly: Boolean
): List<SavedReading> {
    val normalizedQuery = query.trim().lowercase()
    return readings.asSequence()
        .filter { !pinnedOnly || it.isPinned }
        .filter { reading ->
            normalizedQuery.isBlank() || sequenceOf(
                reading.question,
                reading.title,
                reading.spreadTitle,
                reading.layoutTitle,
                reading.positionPresetTitle,
                reading.deckName,
                reading.cards.joinToString(" ") { "${it.nameKr} ${it.nameEn} ${it.positionLabel}" }
            ).any { value -> value.lowercase().contains(normalizedQuery) }
        }
        .sortedWith(compareByDescending<SavedReading> { it.isPinned }.thenByDescending { it.savedAt })
        .toList()
}
