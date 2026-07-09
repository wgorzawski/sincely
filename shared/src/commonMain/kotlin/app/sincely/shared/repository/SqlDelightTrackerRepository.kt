package app.sincely.shared.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.sincely.shared.db.SincelyDatabase
import app.sincely.shared.domain.CheckIn
import app.sincely.shared.domain.ReminderTime
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
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
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
        customCategoryLabel: String?,
        reminderEnabled: Boolean,
        reminderTime: ReminderTime,
        createdAt: Instant,
    ): Long = withContext(ioDispatcher) {
        database.transactionWithResult {
            database.trackerQueries.insert(
                name = name,
                emoji = emoji,
                targetDays = targetDays?.toLong(),
                category = category,
                customCategoryLabel = customCategoryLabel,
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime,
                createdAt = createdAt,
                archivedAt = null,
            )
            database.trackerQueries.lastInsertRowId().executeAsOne()
        }
    }

    override suspend fun updateTracker(
        id: Long,
        name: String,
        emoji: String,
        targetDays: Int?,
        category: TrackerCategory,
        customCategoryLabel: String?,
        reminderEnabled: Boolean,
        reminderTime: ReminderTime,
    ): Unit = withContext(ioDispatcher) {
        database.trackerQueries.update(
            name = name,
            emoji = emoji,
            targetDays = targetDays?.toLong(),
            category = category,
            customCategoryLabel = customCategoryLabel,
            reminderEnabled = reminderEnabled,
            reminderTime = reminderTime,
            id = id,
        )
    }

    override suspend fun archiveTracker(id: Long, archivedAt: Instant): Unit =
        withContext(ioDispatcher) {
            database.trackerQueries.archive(archivedAt = archivedAt, id = id)
        }

    override suspend fun deleteTracker(id: Long): Unit =
        withContext(ioDispatcher) {
            database.trackerQueries.deleteById(id)
        }

    override suspend fun checkIn(trackerId: Long, timestamp: Instant, note: String?, backdated: Boolean): Long =
        withContext(ioDispatcher) {
            database.transactionWithResult {
                database.checkInQueries.insert(
                    trackerId = trackerId,
                    timestamp = timestamp,
                    note = note,
                    backdated = backdated,
                )
                database.checkInQueries.lastInsertRowId().executeAsOne()
            }
        }

    override suspend fun undoLastCheckIn(trackerId: Long): Unit =
        withContext(ioDispatcher) {
            database.checkInQueries.deleteLastForTracker(trackerId)
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

    override fun observeLastCheckIns(): Flow<Map<Long, Instant>> =
        database.checkInQueries.selectLastCheckInPerTracker()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows -> rows.associate { it.trackerId to it.timestamp } }
}
