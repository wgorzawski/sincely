package app.sincely.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class TrackerStatsTest {

    private val now = Instant.parse("2026-07-06T12:00:00Z")

    @Test
    fun computeTrackerStats_fewerThanTwoCheckIns_isNull() {
        assertNull(computeTrackerStats(emptyList(), targetDays = 7))
        assertNull(computeTrackerStats(listOf(now), targetDays = 7))
    }

    @Test
    fun computeTrackerStats_computesAverageAndLongestGap() {
        // sorted ascending: now-20d, now-10d, now-6d, now -> gaps 10, 4, 6
        val history = listOf(now, now - 6.days, now - 10.days, now - 20.days)
        val stats = computeTrackerStats(history, targetDays = 7)

        requireNotNull(stats)
        assertEquals(7, stats.averageGapDays) // round(20/3) = 7
        assertEquals(10, stats.longestGapDays)
        assertEquals(3, stats.bars.size)
    }

    @Test
    fun computeTrackerStats_unsortedInputIsHandledTheSameAsSorted() {
        val a = now
        val b = now - 5.days
        val c = now - 12.days

        val sorted = computeTrackerStats(listOf(c, b, a), targetDays = null)
        val shuffled = computeTrackerStats(listOf(a, c, b), targetDays = null)

        assertEquals(sorted, shuffled)
    }

    @Test
    fun computeTrackerStats_barExceedsTarget_whenGapPastTarget() {
        val history = listOf(now, now - 10.days)
        val stats = computeTrackerStats(history, targetDays = 7)

        requireNotNull(stats)
        assertEquals(true, stats.bars.single().exceedsTarget)
    }

    @Test
    fun computeTrackerStats_noTarget_hasNoGoalLine() {
        val history = listOf(now, now - 5.days)
        val stats = computeTrackerStats(history, targetDays = null)

        requireNotNull(stats)
        assertNull(stats.goalLineFraction)
    }

    @Test
    fun computeTrackerStats_onlyLastTenGapsAreCharted() {
        val history = (0..12).map { now - (it * 3).days }
        val stats = computeTrackerStats(history, targetDays = 3)

        requireNotNull(stats)
        assertEquals(10, stats.bars.size)
    }
}
