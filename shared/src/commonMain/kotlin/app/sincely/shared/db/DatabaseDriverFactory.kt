package app.sincely.shared.db

import app.cash.sqldelight.db.SqlDriver

/** Provides a platform-specific [SqlDriver]; implemented per-target. */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
