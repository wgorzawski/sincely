package app.sincely.shared.repository

import app.sincely.shared.db.CheckInEntity
import app.sincely.shared.db.TrackerEntity
import app.sincely.shared.domain.CheckIn
import app.sincely.shared.domain.Tracker

internal fun TrackerEntity.toDomain(): Tracker = Tracker(
    id = id,
    name = name,
    emoji = emoji,
    targetDays = targetDays?.toInt(),
    category = category,
    customCategoryLabel = customCategoryLabel,
    reminderEnabled = reminderEnabled,
    reminderTime = reminderTime,
    createdAt = createdAt,
    archivedAt = archivedAt,
)

internal fun CheckInEntity.toDomain(): CheckIn = CheckIn(
    id = id,
    trackerId = trackerId,
    timestamp = timestamp,
    note = note,
    backdated = backdated,
)
