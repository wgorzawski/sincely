package app.sincely.shared.domain

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Absolute "6 lip 2026" formatting for the detail screen's history list.
 * [RelativeTimeFormatter] covers the "dziś"/"wczoraj"/"N dni temu" copy used elsewhere.
 */
object HistoryDateFormatter {
    private val months = listOf(
        "sty", "lut", "mar", "kwi", "maj", "cze",
        "lip", "sie", "wrz", "paź", "lis", "gru",
    )

    fun format(instant: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
        val local = instant.toLocalDateTime(timeZone)
        return "${local.dayOfMonth} ${months[local.monthNumber - 1]} ${local.year}"
    }
}
