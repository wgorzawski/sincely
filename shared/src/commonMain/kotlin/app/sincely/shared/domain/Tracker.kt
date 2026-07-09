package app.sincely.shared.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * A single thing being tracked, e.g. "podlej monstere" or "wymień filtr".
 * targetDays is null for trackers with no expected cadence — they only ever
 * show how long it's been, never a warning/overdue status.
 */
@Serializable
data class Tracker(
    val id: Long,
    val name: String,
    val emoji: String,
    val targetDays: Int?,
    val category: TrackerCategory,
    /** Only meaningful when [category] is [TrackerCategory.CUSTOM]. */
    val customCategoryLabel: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderTime: ReminderTime = ReminderTime.RANO,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = InstantSerializer::class)
    val archivedAt: Instant? = null,
) {
    val categoryLabel: String get() = TrackerCategoryPresentation.label(category, customCategoryLabel)
    val categoryEmoji: String get() = TrackerCategoryPresentation.emoji(category)
}
