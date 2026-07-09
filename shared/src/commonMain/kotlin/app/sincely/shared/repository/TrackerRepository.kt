package app.sincely.shared.repository

import app.sincely.shared.domain.CheckIn
import app.sincely.shared.domain.ReminderTime
import app.sincely.shared.domain.Tracker
import app.sincely.shared.domain.TrackerCategory
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface TrackerRepository {
    fun observeTrackers(): Flow<List<Tracker>>

    suspend fun addTracker(
        name: String,
        emoji: String,
        targetDays: Int?,
        category: TrackerCategory,
        customCategoryLabel: String? = null,
        reminderEnabled: Boolean = false,
        reminderTime: ReminderTime = ReminderTime.RANO,
        createdAt: Instant,
    ): Long

    suspend fun updateTracker(
        id: Long,
        name: String,
        emoji: String,
        targetDays: Int?,
        category: TrackerCategory,
        customCategoryLabel: String?,
        reminderEnabled: Boolean,
        reminderTime: ReminderTime,
    )

    suspend fun archiveTracker(id: Long, archivedAt: Instant)

    suspend fun deleteTracker(id: Long)

    suspend fun checkIn(trackerId: Long, timestamp: Instant, note: String? = null, backdated: Boolean = false): Long

    /** Removes the most recent check-in for a tracker — used both by the undo toast and the detail screen. */
    suspend fun undoLastCheckIn(trackerId: Long)

    suspend fun lastCheckIn(trackerId: Long): CheckIn?

    fun observeCheckIns(trackerId: Long): Flow<List<CheckIn>>

    /** Latest check-in timestamp per tracker id — powers list-screen sorting/status without an N+1 flow fan-out. */
    fun observeLastCheckIns(): Flow<Map<Long, Instant>>
}
