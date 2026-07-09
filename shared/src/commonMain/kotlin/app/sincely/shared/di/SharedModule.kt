package app.sincely.shared.di

import app.sincely.shared.db.DatabaseDriverFactory
import app.sincely.shared.db.createDatabase
import app.sincely.shared.repository.SqlDelightTrackerRepository
import app.sincely.shared.repository.TrackerRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/** Database + repository wiring shared by every platform. */
val sharedModule = module {
    single { get<DatabaseDriverFactory>().createDriver() }
    single { createDatabase(get()) }
    single<TrackerRepository> { SqlDelightTrackerRepository(get()) }
}

/** Provides the platform-specific [DatabaseDriverFactory] binding. */
expect fun platformModule(): Module
