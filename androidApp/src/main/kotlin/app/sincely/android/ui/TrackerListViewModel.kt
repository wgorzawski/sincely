package app.sincely.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sincely.shared.domain.Tracker
import app.sincely.shared.domain.TrackerCategory
import app.sincely.shared.repository.TrackerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * Holds no computed state of its own — it only forwards what the repository
 * emits. All status/formatting logic lives in `shared`'s commonMain.
 */
class TrackerListViewModel(
    private val repository: TrackerRepository,
) : ViewModel() {

    val trackers: StateFlow<List<Tracker>> = repository.observeTrackers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Adds a hardcoded tracker — proves the repository -> UI flow end-to-end. */
    fun addSampleTracker() {
        viewModelScope.launch {
            repository.addTracker(
                name = "Podlej monsterę",
                emoji = "🪴",
                targetDays = 7,
                category = TrackerCategory.PLANTS,
                createdAt = Clock.System.now(),
            )
        }
    }
}
