package app.sincely.shared.repository

import app.sincely.shared.domain.CheckIn
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
        createdAt: Instant,
    ): Long

    suspend fun checkIn(trackerId: Long, timestamp: Instant, note: String? = null): Long

    suspend fun lastCheckIn(trackerId: Long): CheckIn?

    fun observeCheckIns(trackerId: Long): Flow<List<CheckIn>>
}
