package app.sincely.shared.domain

/**
 * CUSTOM carries no label of its own — the actual name lives in
 * [Tracker.customCategoryLabel], resolved through [TrackerCategoryPresentation].
 */
enum class TrackerCategory {
    DOM,
    AUTO,
    ZDROWIE,
    SERWER,
    INNE,
    CUSTOM,
}

/** Polish labels/emoji for each category — mirrors the design's `CATEGORIES` table. */
object TrackerCategoryPresentation {
    fun emoji(category: TrackerCategory): String = when (category) {
        TrackerCategory.DOM -> "🏠"
        TrackerCategory.AUTO -> "🚗"
        TrackerCategory.ZDROWIE -> "💊"
        TrackerCategory.SERWER -> "🖥️"
        TrackerCategory.INNE -> "🔖"
        TrackerCategory.CUSTOM -> "🔖"
    }

    fun label(category: TrackerCategory, customLabel: String? = null): String = when (category) {
        TrackerCategory.DOM -> "Dom"
        TrackerCategory.AUTO -> "Auto"
        TrackerCategory.ZDROWIE -> "Zdrowie"
        TrackerCategory.SERWER -> "Serwer"
        TrackerCategory.INNE -> "Inne"
        TrackerCategory.CUSTOM -> customLabel?.trim()?.takeIf { it.isNotEmpty() } ?: "Własna"
    }

    /** Every non-custom category, in the order shown as picker chips, plus a trailing "Własna" entry. */
    val pickerOrder: List<TrackerCategory> = listOf(
        TrackerCategory.DOM,
        TrackerCategory.AUTO,
        TrackerCategory.ZDROWIE,
        TrackerCategory.SERWER,
        TrackerCategory.INNE,
        TrackerCategory.CUSTOM,
    )
}
