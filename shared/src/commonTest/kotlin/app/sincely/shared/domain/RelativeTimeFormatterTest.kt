package app.sincely.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class RelativeTimeFormatterTest {

    private val now = Instant.parse("2026-07-06T12:00:00Z")

    @Test
    fun format_sameDay_isToday() {
        assertEquals("dziś", RelativeTimeFormatter.format(now - 3.hours, now))
    }

    @Test
    fun format_oneDayAgo_isYesterday() {
        assertEquals("wczoraj", RelativeTimeFormatter.format(now - 1.days, now))
    }

    @Test
    fun format_multipleDaysAgo_usesDaysAgoTemplate() {
        assertEquals("5 dni temu", RelativeTimeFormatter.format(now - 5.days, now))
    }
}
