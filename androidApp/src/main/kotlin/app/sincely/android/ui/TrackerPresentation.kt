package app.sincely.android.ui

import androidx.compose.ui.graphics.Color
import app.sincely.android.ui.theme.SincelyColors
import app.sincely.shared.domain.RelativeTimeStrings
import app.sincely.shared.domain.Tracker
import app.sincely.shared.domain.TrackerStatus
import app.sincely.shared.domain.computeStatus
import app.sincely.shared.domain.daysSince
import kotlin.time.Instant

/** One tracker enriched with everything a card/detail header needs to render — computed once per recomposition. */
data class TrackerPresentation(
    val tracker: Tracker,
    val days: Long,
    val status: TrackerStatus,
)

fun presentationFor(tracker: Tracker, lastCheckIn: Instant?, now: Instant): TrackerPresentation {
    val status = computeStatus(lastCheckIn, tracker.targetDays, now)
    val days = lastCheckIn?.let { daysSince(it, now) } ?: 0L
    return TrackerPresentation(tracker, days, status)
}

/** Highest-priority (most overdue) trackers first, then longest-waiting first — mirrors the design's card ordering. */
private fun statusRank(status: TrackerStatus): Int = when (status) {
    TrackerStatus.OVERDUE -> 2
    TrackerStatus.WARNING -> 1
    TrackerStatus.OK -> 0
}

val trackerPresentationOrder: Comparator<TrackerPresentation> =
    compareByDescending<TrackerPresentation> { statusRank(it.status) }.thenByDescending { it.days }

fun statusAccentColor(colors: SincelyColors, status: TrackerStatus): Color = when (status) {
    TrackerStatus.OVERDUE -> colors.danger
    TrackerStatus.WARNING -> colors.warn
    TrackerStatus.OK -> colors.faint
}

fun statusChipBackground(colors: SincelyColors, status: TrackerStatus): Color = when (status) {
    TrackerStatus.OVERDUE -> colors.dangerSoft
    TrackerStatus.WARNING -> colors.warnSoft
    TrackerStatus.OK -> colors.surface2
}

fun cardSubtitle(tracker: Tracker, days: Long, status: TrackerStatus): String {
    val dayText = when (days) {
        0L -> RelativeTimeStrings.TODAY
        1L -> RelativeTimeStrings.YESTERDAY
        else -> RelativeTimeStrings.daysAgo(days)
    }
    val targetDays = tracker.targetDays
    return when {
        targetDays != null && status != TrackerStatus.OK ->
            "$dayText · ${AndroidStrings.GOAL_BADGE_FORMAT.format(targetDays)}"
        targetDays == null && days > 30 -> "ponad miesiąc"
        else -> dayText
    }
}

/** 0f..1f, or null when the tracker has no target cadence (no progress bar to show). */
fun progressFraction(tracker: Tracker, days: Long): Float? {
    val targetDays = tracker.targetDays ?: return null
    if (targetDays <= 0) return 1f
    return (days.toFloat() / targetDays).coerceIn(0f, 1f)
}
