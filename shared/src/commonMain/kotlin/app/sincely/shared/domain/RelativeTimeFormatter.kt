package app.sincely.shared.domain

import kotlin.time.Instant

/**
 * Polish-only for now. Every user-facing string lives in [RelativeTimeStrings]
 * so a future locale switch only touches that object, not call sites.
 */
object RelativeTimeFormatter {
    fun format(instant: Instant, now: Instant): String =
        when (val days = daysSince(instant, now)) {
            0L -> RelativeTimeStrings.TODAY
            1L -> RelativeTimeStrings.YESTERDAY
            else -> RelativeTimeStrings.daysAgo(days)
        }
}

object RelativeTimeStrings {
    const val TODAY = "dziś"
    const val YESTERDAY = "wczoraj"

    fun daysAgo(days: Long): String = "$days dni temu"
}
