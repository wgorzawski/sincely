package app.sincely.shared.domain

import kotlin.math.roundToLong
import kotlin.time.Instant

/** One bar in the "ostatnie przerwy" chart on the detail screen. */
data class TrackerStatBar(
    val gapDays: Long,
    /** 0..1, already clamped to a sane minimum so a same-day gap still renders a sliver. */
    val heightFraction: Float,
    val exceedsTarget: Boolean,
)

data class TrackerStats(
    val averageGapDays: Long,
    val longestGapDays: Long,
    /** Oldest-to-newest, capped to the last 10 gaps. */
    val bars: List<TrackerStatBar>,
    /** 0..1 position of the target-cadence dashed line, null when the tracker has no target. */
    val goalLineFraction: Float?,
)

/**
 * Derives gap statistics from a tracker's check-in timestamps. Needs at least
 * two check-ins to produce a gap; returns null otherwise ("za mało danych").
 * [history] need not be pre-sorted.
 */
fun computeTrackerStats(history: List<Instant>, targetDays: Int?): TrackerStats? {
    if (history.size < 2) return null

    val sortedAsc = history.sorted()
    val gaps = sortedAsc.zipWithNext { earlier, later -> daysSince(earlier, later) }
    val average = (gaps.sum().toDouble() / gaps.size).roundToLong()
    val longest = gaps.max()

    val recentGaps = gaps.takeLast(10)
    val maxScale = maxOf(recentGaps.max(), (targetDays ?: 0).toLong(), 1L)
    val bars = recentGaps.map { gap ->
        TrackerStatBar(
            gapDays = gap,
            heightFraction = (gap.toFloat() / maxScale).coerceAtLeast(0.06f),
            exceedsTarget = targetDays != null && gap > targetDays,
        )
    }
    val goalLineFraction = targetDays?.let { (it.toFloat() / maxScale).coerceAtMost(1f) }

    return TrackerStats(
        averageGapDays = average,
        longestGapDays = longest,
        bars = bars,
        goalLineFraction = goalLineFraction,
    )
}
