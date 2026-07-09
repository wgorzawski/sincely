package app.sincely.shared.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.sincely.shared.db.SincelyDatabase
import app.sincely.shared.domain.CheckIn
import app.sincely.shared.domain.Tracker
import app.sincely.shared.domain.TrackerCategory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Instant

class SqlDelightTrackerRepository(
    private val database: SincelyDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : TrackerRepository {

    override fun observeTrackers(): Flow<List<Tracker>> =
        database.trackerQueries.selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun addTracker(
        name: String,
        emoji: String,
        targetDays: Int?,
        category: TrackerCategory,
        createdAt: Instant,
    ): Long = withContext(ioDispatcher) {
        database.transactionWithResult {
            database.trackerQueries.insert(
                name = name,
                emoji = emoji,
                targetDays = targetDays?.toLong(),
                category = category,
                createdAt = createdAt,
                archivedAt = null,
            )
            database.trackerQueries.lastInsertRowId().executeAsOne()
        }
    }

    override suspend fun checkIn(trackerId: Long, timestamp: Instant, note: String?): Long =
        withContext(ioDispatcher) {
            database.transactionWithResult {
                database.checkInQueries.insert(
                    trackerId = trackerId,
                    timestamp = timestamp,
                    note = note,
                )
                database.checkInQueries.lastInsertRowId().executeAsOne()
            }
        }

    override suspend fun lastCheckIn(trackerId: Long): CheckIn? =
        withContext(ioDispatcher) {
            database.checkInQueries.lastCheckIn(trackerId).executeAsOneOrNull()?.toDomain()
        }

    override fun observeCheckIns(trackerId: Long): Flow<List<CheckIn>> =
        database.checkInQueries.selectCheckInsForTracker(trackerId)
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows -> rows.map { it.toDomain() } }
}
