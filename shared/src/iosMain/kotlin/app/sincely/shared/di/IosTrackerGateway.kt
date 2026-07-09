package app.sincely.shared.di

import app.sincely.shared.domain.HistoryDateFormatter
import app.sincely.shared.domain.PopularTrackerSuggestion
import app.sincely.shared.domain.ReminderTime
import app.sincely.shared.domain.Tracker
import app.sincely.shared.domain.TrackerCategory
import app.sincely.shared.domain.TrackerStatus
import app.sincely.shared.domain.computeStatus
import app.sincely.shared.domain.computeTrackerStats
import app.sincely.shared.domain.daysSince
import app.sincely.shared.repository.TrackerRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock
import kotlin.time.Instant

/** One card's worth of already-computed display data — see [IosTrackerGateway] for why. */
data class TrackerCardData(
    val tracker: Tracker,
    val days: Long,
    val status: TrackerStatus,
    val subtitle: String,
    /** -1f means "no target, no progress bar". */
    val progressFraction: Float,
)

data class HistoryEntryData(
    val dateText: String,
    val isLatest: Boolean,
    val note: String?,
    val backdated: Boolean,
)

data class BarData(val heightFraction: Float, val exceedsTarget: Boolean)

data class TrackerDetailData(
    val tracker: Tracker,
    val days: Long,
    val status: TrackerStatus,
    val bigValue: String,
    val bigLabel: String,
    val history: List<HistoryEntryData>,
    val hasStats: Boolean,
    val comparisonText: String,
    val longestGapText: String,
    val bars: List<BarData>,
    /** -1f means "no target, no goal line". */
    val goalLineFraction: Float,
    val canUndo: Boolean,
)

/**
 * Swift-friendly facade over [TrackerRepository]. Kotlin `Flow` doesn't bridge
 * to Swift without extra tooling (SKIE, KMP-NativeCoroutines) that this
 * skeleton intentionally doesn't pull in, so every method is a plain one-shot
 * suspend function — Swift calls them as `async` and re-fetches after each
 * mutation instead of observing a stream.
 *
 * All status/date math (statuses, gap stats, relative/absolute date text)
 * happens here rather than in Swift, for the same reason `Instant`/nullable
 * `Int` don't cross the Swift boundary cleanly: SwiftUI only ever sees plain
 * `String`/`Bool`/`Float`/`Long` fields, computed from `shared`'s commonMain.
 */
class IosTrackerGateway : KoinComponent {
    private val repository: TrackerRepository by inject()

    suspend fun getTrackerCards(): List<TrackerCardData> {
        val now = Clock.System.now()
        val trackers = repository.observeTrackers().first()
        val enriched = trackers.map { tracker ->
            val lastCheckIn = repository.lastCheckIn(tracker.id)?.timestamp
            val days = lastCheckIn?.let { daysSince(it, now) } ?: 0L
            val status = computeStatus(lastCheckIn, tracker.targetDays, now)
            TrackerCardData(
                tracker = tracker,
                days = days,
                status = status,
                subtitle = cardSubtitle(tracker, days, status),
                progressFraction = progressFraction(tracker, days),
            )
        }
        return enriched.sortedWith(
            compareByDescending<TrackerCardData> { statusRank(it.status) }.thenByDescending { it.days },
        )
    }

    suspend fun getDetail(trackerId: Long): TrackerDetailData? {
        val now = Clock.System.now()
        val tracker = repository.observeTrackers().first().find { it.id == trackerId } ?: return null
        val history = repository.observeCheckIns(trackerId).first()
        val lastCheckIn = history.firstOrNull()?.timestamp
        val days = lastCheckIn?.let { daysSince(it, now) } ?: 0L
        val status = computeStatus(lastCheckIn, tracker.targetDays, now)
        val stats = computeTrackerStats(history.map { it.timestamp }, tracker.targetDays)

        return TrackerDetailData(
            tracker = tracker,
            days = days,
            status = status,
            bigValue = if (days == 0L) "0" else days.toString(),
            bigLabel = when (days) {
                0L -> "dziś"
                1L -> "wczoraj"
                else -> "dni temu"
            },
            history = history.mapIndexed { index, checkIn ->
                HistoryEntryData(
                    dateText = HistoryDateFormatter.format(checkIn.timestamp),
                    isLatest = index == 0,
                    note = checkIn.note,
                    backdated = checkIn.backdated,
                )
            },
            hasStats = stats != null,
            comparisonText = stats?.let { comparisonText(it.averageGapDays, tracker.targetDays) } ?: "",
            longestGapText = stats?.let { "najdłuższa przerwa: ${it.longestGapDays} dni" } ?: "",
            bars = stats?.bars?.map { BarData(it.heightFraction, it.exceedsTarget) } ?: emptyList(),
            goalLineFraction = stats?.goalLineFraction ?: -1f,
            canUndo = history.size > 1,
        )
    }

    suspend fun addTracker(
        name: String,
        emoji: String,
        targetEnabled: Boolean,
        targetDays: Int,
        category: TrackerCategory,
        customCategoryLabel: String?,
        reminderEnabled: Boolean,
        reminderTime: ReminderTime,
    ): Long {
        val now = Clock.System.now()
        val id = repository.addTracker(
            name = name,
            emoji = emoji,
            targetDays = if (targetEnabled) targetDays else null,
            category = category,
            customCategoryLabel = customCategoryLabel,
            reminderEnabled = targetEnabled && reminderEnabled,
            reminderTime = reminderTime,
            createdAt = now,
        )
        repository.checkIn(trackerId = id, timestamp = now)
        return id
    }

    suspend fun addFromSuggestion(suggestion: PopularTrackerSuggestion): Long {
        val now = Clock.System.now()
        val id = repository.addTracker(
            name = suggestion.name,
            emoji = suggestion.emoji,
            targetDays = suggestion.targetDays,
            category = suggestion.category,
            createdAt = now,
        )
        repository.checkIn(trackerId = id, timestamp = now)
        return id
    }

    suspend fun updateTracker(
        id: Long,
        name: String,
        emoji: String,
        targetEnabled: Boolean,
        targetDays: Int,
        category: TrackerCategory,
        customCategoryLabel: String?,
        reminderEnabled: Boolean,
        reminderTime: ReminderTime,
    ) {
        repository.updateTracker(
            id = id,
            name = name,
            emoji = emoji,
            targetDays = if (targetEnabled) targetDays else null,
            category = category,
            customCategoryLabel = customCategoryLabel,
            reminderEnabled = targetEnabled && reminderEnabled,
            reminderTime = reminderTime,
        )
    }

    suspend fun archiveTracker(id: Long) {
        repository.archiveTracker(id, Clock.System.now())
    }

    suspend fun deleteTracker(id: Long) {
        repository.deleteTracker(id)
    }

    suspend fun quickCheckIn(trackerId: Long) {
        repository.checkIn(trackerId = trackerId, timestamp = Clock.System.now())
    }

    /** [daysAgo] is 0/1/2 for "teraz"/"wczoraj"/"przedwczoraj", or -1 alongside a [customDateEpochMillis]. */
    suspend fun checkInWithOptions(
        trackerId: Long,
        daysAgo: Int,
        customDateEpochMillis: Double,
        note: String?,
    ) {
        val now = Clock.System.now()
        val (timestamp, backdated) = when (daysAgo) {
            0 -> now to false
            -1 -> Instant.fromEpochMilliseconds(customDateEpochMillis.toLong()) to true
            else -> Instant.fromEpochMilliseconds(now.toEpochMilliseconds() - daysAgo * 86_400_000L) to true
        }
        repository.checkIn(trackerId = trackerId, timestamp = timestamp, note = note, backdated = backdated)
    }

    suspend fun undoLastCheckIn(trackerId: Long) {
        repository.undoLastCheckIn(trackerId)
    }
}

private fun statusRank(status: TrackerStatus): Int = when (status) {
    TrackerStatus.OVERDUE -> 2
    TrackerStatus.WARNING -> 1
    TrackerStatus.OK -> 0
}

private fun cardSubtitle(tracker: Tracker, days: Long, status: TrackerStatus): String {
    val dayText = when (days) {
        0L -> "dziś"
        1L -> "wczoraj"
        else -> "$days dni temu"
    }
    val targetDays = tracker.targetDays
    return when {
        targetDays != null && status != TrackerStatus.OK -> "$dayText · cel: co $targetDays dni"
        targetDays == null && days > 30 -> "ponad miesiąc"
        else -> dayText
    }
}

private fun progressFraction(tracker: Tracker, days: Long): Float {
    val targetDays = tracker.targetDays ?: return -1f
    if (targetDays <= 0) return 1f
    return (days.toFloat() / targetDays).coerceIn(0f, 1f)
}

private fun comparisonText(averageDays: Long, targetDays: Int?): String =
    if (targetDays != null) "średnio co $averageDays dni · cel co $targetDays"
    else "średnio co $averageDays dni"
