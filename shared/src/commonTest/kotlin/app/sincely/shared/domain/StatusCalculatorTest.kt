package app.sincely.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class StatusCalculatorTest {

    private val now = Instant.parse("2026-07-06T12:00:00Z")

    @Test
    fun daysSince_returnsWholeDaysElapsed() {
        assertEquals(5, daysSince(now - 5.days, now))
    }

    @Test
    fun daysSince_clampsNegativeToZero() {
        assertEquals(0, daysSince(now + 1.days, now))
    }

    @Test
    fun daysSince_sameInstantIsZero() {
        assertEquals(0, daysSince(now, now))
    }

    @Test
    fun computeStatus_noTarget_isAlwaysOk() {
        assertEquals(TrackerStatus.OK, computeStatus(now - 400.days, null, now))
        assertEquals(TrackerStatus.OK, computeStatus(null, null, now))
    }

    @Test
    fun computeStatus_neverCheckedInWithTarget_isOverdue() {
        assertEquals(TrackerStatus.OVERDUE, computeStatus(null, 7, now))
    }

    @Test
    fun computeStatus_withinTarget_isOk() {
        assertEquals(TrackerStatus.OK, computeStatus(now - 3.days, 7, now))
        assertEquals(TrackerStatus.OK, computeStatus(now - 7.days, 7, now))
    }

    @Test
    fun computeStatus_pastTargetWithinGrace_isWarning() {
        // target 7 days -> grace is ceil(7/2) = 4, so warning window is (7, 11] days
        assertEquals(TrackerStatus.WARNING, computeStatus(now - 8.days, 7, now))
        assertEquals(TrackerStatus.WARNING, computeStatus(now - 11.days, 7, now))
    }

    @Test
    fun computeStatus_pastGrace_isOverdue() {
        assertEquals(TrackerStatus.OVERDUE, computeStatus(now - 12.days, 7, now))
    }

    @Test
    fun computeStatus_smallTarget_graceIsAtLeastOneDay() {
        // target 1 day -> grace = max(ceil(1/2), 1) = 1, warning window is (1, 2] days
        assertEquals(TrackerStatus.OK, computeStatus(now - 1.days, 1, now))
        assertEquals(TrackerStatus.WARNING, computeStatus(now - 2.days, 1, now))
        assertEquals(TrackerStatus.OVERDUE, computeStatus(now - 3.days, 1, now))
    }
}
