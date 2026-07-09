package app.sincely.shared.domain

/** A one-tap suggestion shown in the empty state and in the "nowy tracker" sheet. */
data class PopularTrackerSuggestion(
    val name: String,
    val emoji: String,
    val targetDays: Int,
    val category: TrackerCategory,
)

object PopularTrackerSuggestions {
    val all: List<PopularTrackerSuggestion> = listOf(
        PopularTrackerSuggestion("Podlewanie kwiatów", "🪴", 7, TrackerCategory.DOM),
        PopularTrackerSuggestion("Wymiana filtra", "💧", 30, TrackerCategory.DOM),
        PopularTrackerSuggestion("Backup danych", "💾", 14, TrackerCategory.SERWER),
        PopularTrackerSuggestion("Olej w aucie", "🚗", 180, TrackerCategory.AUTO),
        PopularTrackerSuggestion("Zmiana pościeli", "🛏️", 14, TrackerCategory.DOM),
    )
}
