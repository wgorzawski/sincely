package app.sincely.shared.domain

import kotlin.time.Instant

/**
 * Number of whole 24h periods between [from] and [to]. [to] is expected to
 * be at or after [from]; a negative reading (clock skew, edited check-in in
 * the future) clamps to 0 rather than producing a negative day count.
 */
fun daysSince(from: Instant, to: Instant): Long =
    (to - from).inWholeDays.coerceAtLeast(0)

/**
 * Derives a [TrackerStatus] purely from timestamps — no side effects, no
 * stored state. A tracker with no [targetDays] never warns: it just reports
 * how long it's been.
 *
 * - null [lastCheckIn] with a target means "never done" -> overdue.
 * - within target -> OK.
 * - past target but within the grace window -> WARNING.
 * - past the grace window -> OVERDUE.
 */
fun computeStatus(lastCheckIn: Instant?, targetDays: Int?, now: Instant): TrackerStatus {
    if (targetDays == null) return TrackerStatus.OK
    if (lastCheckIn == null) return TrackerStatus.OVERDUE

    val elapsedDays = daysSince(lastCheckIn, now)
    val graceDays = warningGraceDays(targetDays)

    return when {
        elapsedDays <= targetDays -> TrackerStatus.OK
        elapsedDays <= targetDays + graceDays -> TrackerStatus.WARNING
        else -> TrackerStatus.OVERDUE
    }
}

/** Half the target (rounded up, minimum 1 day) — the WARNING window past due. */
private fun warningGraceDays(targetDays: Int): Int =
    ((targetDays + 1) / 2).coerceAtLeast(1)
