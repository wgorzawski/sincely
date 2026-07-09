package app.sincely.shared.db

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.sincely.shared.domain.ReminderTime
import app.sincely.shared.domain.TrackerCategory
import kotlin.time.Instant

internal val instantAdapter = object : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant = Instant.fromEpochMilliseconds(databaseValue)
    override fun encode(value: Instant): Long = value.toEpochMilliseconds()
}

internal val trackerCategoryAdapter = object : ColumnAdapter<TrackerCategory, String> {
    override fun decode(databaseValue: String): TrackerCategory = TrackerCategory.valueOf(databaseValue)
    override fun encode(value: TrackerCategory): String = value.name
}

internal val reminderTimeAdapter = object : ColumnAdapter<ReminderTime, String> {
    override fun decode(databaseValue: String): ReminderTime = ReminderTime.valueOf(databaseValue)
    override fun encode(value: ReminderTime): String = value.name
}

internal val booleanAdapter = object : ColumnAdapter<Boolean, Long> {
    override fun decode(databaseValue: Long): Boolean = databaseValue != 0L
    override fun encode(value: Boolean): Long = if (value) 1L else 0L
}

/** Wires the generated SQLDelight database with adapters for domain types. */
fun createDatabase(driver: SqlDriver): SincelyDatabase =
    SincelyDatabase(
        driver = driver,
        trackerEntityAdapter = TrackerEntity.Adapter(
            categoryAdapter = trackerCategoryAdapter,
            reminderEnabledAdapter = booleanAdapter,
            reminderTimeAdapter = reminderTimeAdapter,
            createdAtAdapter = instantAdapter,
            archivedAtAdapter = instantAdapter,
        ),
        checkInEntityAdapter = CheckInEntity.Adapter(
            timestampAdapter = instantAdapter,
            backdatedAdapter = booleanAdapter,
        ),
    )
