package app.sincely.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sincely.shared.domain.CheckIn
import app.sincely.shared.domain.PopularTrackerSuggestion
import app.sincely.shared.domain.ReminderTime
import app.sincely.shared.domain.Tracker
import app.sincely.shared.domain.TrackerCategory
import app.sincely.shared.repository.TrackerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

sealed interface Screen {
    data object List : Screen
    data class Detail(val trackerId: Long) : Screen
}

enum class CheckInDateChoice { NOW, YESTERDAY, DAY_BEFORE, CUSTOM }

data class TrackerDraft(
    val name: String = "",
    val emoji: String = "🪴",
    val emojiPickerOpen: Boolean = false,
    val category: TrackerCategory = TrackerCategory.DOM,
    val customCategoryLabel: String = "",
    val intervalEnabled: Boolean = false,
    val intervalDays: Int = 7,
    val reminderEnabled: Boolean = false,
    val reminderTime: ReminderTime = ReminderTime.RANO,
)

data class CheckInDraft(
    val dateChoice: CheckInDateChoice = CheckInDateChoice.NOW,
    /** UTC epoch millis of the picked calendar day (as returned by Compose's DatePickerState). */
    val customDateMillis: Long? = null,
    val note: String = "",
)

data class ToastState(val message: String, val trackerId: Long)

/**
 * Owns every bit of screen state the "Kiedy ostatnio?" flows need: the tracker
 * list (straight from the repository), which screen is showing, the two
 * bottom-sheet drafts, the undo toast, and the theme toggle. Status/date math
 * itself still lives entirely in `shared`'s commonMain.
 */
class TrackerListViewModel(
    private val repository: TrackerRepository,
) : ViewModel() {

    val trackers: StateFlow<List<Tracker>> = repository.observeTrackers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lastCheckIns: StateFlow<Map<Long, Instant>> = repository.observeLastCheckIns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _screen = MutableStateFlow<Screen>(Screen.List)
    val screen: StateFlow<Screen> = _screen

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val _filterCategoryLabel = MutableStateFlow<String?>(null)
    val filterCategoryLabel: StateFlow<String?> = _filterCategoryLabel

    private val _sheetOpen = MutableStateFlow(false)
    val sheetOpen: StateFlow<Boolean> = _sheetOpen

    private val _draft = MutableStateFlow(TrackerDraft())
    val draft: StateFlow<TrackerDraft> = _draft

    private val _checkInSheetTrackerId = MutableStateFlow<Long?>(null)
    val checkInSheetTrackerId: StateFlow<Long?> = _checkInSheetTrackerId

    private val _checkInDraft = MutableStateFlow(CheckInDraft())
    val checkInDraft: StateFlow<CheckInDraft> = _checkInDraft

    private val _toast = MutableStateFlow<ToastState?>(null)
    val toast: StateFlow<ToastState?> = _toast

    private val _detailEmojiPickerOpen = MutableStateFlow(false)
    val detailEmojiPickerOpen: StateFlow<Boolean> = _detailEmojiPickerOpen

    private var toastJob: Job? = null

    fun checkInsFor(trackerId: Long): Flow<List<CheckIn>> = repository.observeCheckIns(trackerId)

    fun toggleTheme() {
        _isDarkTheme.update { !it }
    }

    fun setFilterCategoryLabel(label: String?) {
        _filterCategoryLabel.value = label
    }

    fun openDetail(trackerId: Long) {
        _detailEmojiPickerOpen.value = false
        _screen.value = Screen.Detail(trackerId)
    }

    fun backToList() {
        _screen.value = Screen.List
    }

    // ---- quick check-in (tap) + long-press options sheet ----

    fun quickCheckIn(trackerId: Long) {
        viewModelScope.launch {
            repository.checkIn(trackerId = trackerId, timestamp = Clock.System.now())
        }
        showToast(trackerId)
    }

    private fun showToast(trackerId: Long) {
        toastJob?.cancel()
        _toast.value = ToastState(AndroidStrings.CHECKED_IN_TOAST, trackerId)
        toastJob = viewModelScope.launch {
            delay(3_200)
            if (_toast.value?.trackerId == trackerId) _toast.value = null
        }
    }

    fun undoToast() {
        val toast = _toast.value ?: return
        toastJob?.cancel()
        _toast.value = null
        viewModelScope.launch {
            repository.undoLastCheckIn(toast.trackerId)
        }
    }

    fun openCheckInSheet(trackerId: Long) {
        _checkInDraft.value = CheckInDraft()
        _checkInSheetTrackerId.value = trackerId
    }

    fun closeCheckInSheet() {
        _checkInSheetTrackerId.value = null
    }

    fun setCheckInDateChoice(choice: CheckInDateChoice) {
        _checkInDraft.update { it.copy(dateChoice = choice) }
    }

    fun updateCheckInCustomDate(epochMillisUtc: Long) {
        _checkInDraft.update { it.copy(dateChoice = CheckInDateChoice.CUSTOM, customDateMillis = epochMillisUtc) }
    }

    fun updateCheckInNote(note: String) {
        _checkInDraft.update { it.copy(note = note) }
    }

    fun submitCheckInWithOptions() {
        val trackerId = _checkInSheetTrackerId.value ?: return
        val checkInDraft = _checkInDraft.value
        val now = Clock.System.now()
        val (timestamp, backdated) = when (checkInDraft.dateChoice) {
            CheckInDateChoice.NOW -> now to false
            CheckInDateChoice.YESTERDAY -> (now - 1.days) to true
            CheckInDateChoice.DAY_BEFORE -> (now - 2.days) to true
            CheckInDateChoice.CUSTOM -> {
                val millis = checkInDraft.customDateMillis
                if (millis != null) (Instant.fromEpochMilliseconds(millis) + 9.hours) to true else now to false
            }
        }
        val note = checkInDraft.note.trim().takeIf { it.isNotEmpty() }
        viewModelScope.launch {
            repository.checkIn(trackerId = trackerId, timestamp = timestamp, note = note, backdated = backdated)
        }
        showToast(trackerId)
        _checkInSheetTrackerId.value = null
    }

    // ---- add-tracker sheet ----

    fun openAddSheet() {
        _draft.value = TrackerDraft()
        _sheetOpen.value = true
    }

    fun closeAddSheet() {
        _sheetOpen.value = false
    }

    fun updateDraftName(name: String) {
        _draft.update { it.copy(name = name) }
    }

    fun toggleDraftEmojiPicker() {
        _draft.update { it.copy(emojiPickerOpen = !it.emojiPickerOpen) }
    }

    fun pickDraftEmoji(emoji: String) {
        _draft.update { it.copy(emoji = emoji, emojiPickerOpen = false) }
    }

    fun pickDraftCategory(category: TrackerCategory) {
        _draft.update { it.copy(category = category) }
    }

    fun updateDraftCustomCategoryLabel(label: String) {
        _draft.update { it.copy(customCategoryLabel = label) }
    }

    fun toggleDraftInterval() {
        _draft.update { it.copy(intervalEnabled = !it.intervalEnabled) }
    }

    fun incrementDraftInterval() {
        _draft.update { it.copy(intervalDays = (it.intervalDays + 1).coerceAtMost(365)) }
    }

    fun decrementDraftInterval() {
        _draft.update { it.copy(intervalDays = (it.intervalDays - 1).coerceAtLeast(1)) }
    }

    fun toggleDraftReminder() {
        _draft.update { it.copy(reminderEnabled = !it.reminderEnabled) }
    }

    fun setDraftReminderTime(time: ReminderTime) {
        _draft.update { it.copy(reminderTime = time) }
    }

    fun applySuggestionToDraft(suggestion: PopularTrackerSuggestion) {
        _draft.update {
            it.copy(
                name = suggestion.name,
                emoji = suggestion.emoji,
                intervalEnabled = true,
                intervalDays = suggestion.targetDays,
                category = suggestion.category,
                emojiPickerOpen = false,
            )
        }
    }

    fun submitAdd() {
        val current = _draft.value
        val trimmedName = current.name.trim()
        if (trimmedName.isEmpty()) return
        addAndCheckIn(
            name = trimmedName,
            emoji = current.emoji,
            targetDays = if (current.intervalEnabled) current.intervalDays else null,
            category = current.category,
            customCategoryLabel = current.customCategoryLabel.takeIf { current.category == TrackerCategory.CUSTOM },
            reminderEnabled = current.intervalEnabled && current.reminderEnabled,
            reminderTime = current.reminderTime,
        )
        _sheetOpen.value = false
    }

    /** One-tap add from the empty state's or the sheet's "popularne" list. */
    fun addFromSuggestion(suggestion: PopularTrackerSuggestion) {
        addAndCheckIn(
            name = suggestion.name,
            emoji = suggestion.emoji,
            targetDays = suggestion.targetDays,
            category = suggestion.category,
            customCategoryLabel = null,
            reminderEnabled = false,
            reminderTime = ReminderTime.RANO,
        )
    }

    private fun addAndCheckIn(
        name: String,
        emoji: String,
        targetDays: Int?,
        category: TrackerCategory,
        customCategoryLabel: String?,
        reminderEnabled: Boolean,
        reminderTime: ReminderTime,
    ) {
        viewModelScope.launch {
            val now = Clock.System.now()
            val id = repository.addTracker(
                name = name,
                emoji = emoji,
                targetDays = targetDays,
                category = category,
                customCategoryLabel = customCategoryLabel,
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime,
                createdAt = now,
            )
            repository.checkIn(trackerId = id, timestamp = now)
        }
    }

    // ---- detail screen: edit + actions ----

    private fun mutateTracker(trackerId: Long, transform: (Tracker) -> Tracker) {
        val current = trackers.value.find { it.id == trackerId } ?: return
        val updated = transform(current)
        viewModelScope.launch {
            repository.updateTracker(
                id = trackerId,
                name = updated.name,
                emoji = updated.emoji,
                targetDays = updated.targetDays,
                category = updated.category,
                customCategoryLabel = updated.customCategoryLabel,
                reminderEnabled = updated.reminderEnabled,
                reminderTime = updated.reminderTime,
            )
        }
    }

    fun updateDetailName(trackerId: Long, name: String) =
        mutateTracker(trackerId) { it.copy(name = name) }

    fun toggleDetailEmojiPicker() {
        _detailEmojiPickerOpen.update { !it }
    }

    fun pickDetailEmoji(trackerId: Long, emoji: String) {
        mutateTracker(trackerId) { it.copy(emoji = emoji) }
        _detailEmojiPickerOpen.value = false
    }

    fun pickDetailCategory(trackerId: Long, category: TrackerCategory) =
        mutateTracker(trackerId) {
            it.copy(
                category = category,
                customCategoryLabel = if (category == TrackerCategory.CUSTOM) it.customCategoryLabel else null,
            )
        }

    fun updateDetailCustomCategoryLabel(trackerId: Long, label: String) =
        mutateTracker(trackerId) { it.copy(customCategoryLabel = label) }

    fun toggleDetailInterval(trackerId: Long) =
        mutateTracker(trackerId) {
            if (it.targetDays != null) it.copy(targetDays = null, reminderEnabled = false)
            else it.copy(targetDays = 7)
        }

    fun incrementDetailInterval(trackerId: Long) =
        mutateTracker(trackerId) { it.copy(targetDays = ((it.targetDays ?: 1) + 1).coerceAtMost(365)) }

    fun decrementDetailInterval(trackerId: Long) =
        mutateTracker(trackerId) { it.copy(targetDays = ((it.targetDays ?: 2) - 1).coerceAtLeast(1)) }

    fun toggleDetailReminder(trackerId: Long) =
        mutateTracker(trackerId) { it.copy(reminderEnabled = !it.reminderEnabled) }

    fun setDetailReminderTime(trackerId: Long, time: ReminderTime) =
        mutateTracker(trackerId) { it.copy(reminderTime = time) }

    fun undoLastCheckIn(trackerId: Long) {
        viewModelScope.launch { repository.undoLastCheckIn(trackerId) }
    }

    fun archiveTracker(trackerId: Long) {
        viewModelScope.launch { repository.archiveTracker(trackerId, Clock.System.now()) }
        backToList()
    }

    fun deleteTracker(trackerId: Long) {
        viewModelScope.launch { repository.deleteTracker(trackerId) }
        backToList()
    }
}
