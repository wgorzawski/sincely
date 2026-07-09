package app.sincely.shared.di

import app.sincely.shared.domain.Tracker
import app.sincely.shared.domain.TrackerCategory
import app.sincely.shared.repository.TrackerRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock

/**
 * Swift-friendly facade over [TrackerRepository]. Kotlin `Flow` doesn't bridge
 * to Swift without extra tooling (SKIE, KMP-NativeCoroutines) that this
 * skeleton intentionally doesn't pull in yet, so this exposes plain one-shot
 * suspend functions instead — Swift calls them as `async` automatically.
 *
 * [addSampleTracker] takes no parameters on purpose: nullable numeric/enum
 * arguments crossing the Swift boundary need explicit boxed types, which this
 * skeleton avoids. Same hardcoded tracker as Android's addSampleTracker.
 */
class IosTrackerGateway : KoinComponent {
    private val repository: TrackerRepository by inject()

    suspend fun getTrackers(): List<Tracker> = repository.observeTrackers().first()

    suspend fun addSampleTracker(): Long = repository.addTracker(
        name = "Podlej monsterę",
        emoji = "🪴",
        targetDays = 7,
        category = TrackerCategory.PLANTS,
        createdAt = Clock.System.now(),
    )
}
