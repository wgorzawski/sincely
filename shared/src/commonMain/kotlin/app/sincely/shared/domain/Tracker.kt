package app.sincely.shared.domain

import kotlinx.datetime.serializers.InstantIso8601Serializer
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
    @Serializable(with = InstantIso8601Serializer::class)
    val createdAt: Instant,
    @Serializable(with = InstantIso8601Serializer::class)
    val archivedAt: Instant? = null,
)
