package app.sincely.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sincely.android.ui.theme.LocalSincelyColors
import app.sincely.shared.domain.PopularTrackerSuggestion
import app.sincely.shared.domain.PopularTrackerSuggestions
import app.sincely.shared.domain.Tracker
import app.sincely.shared.domain.TrackerCategoryPresentation
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerListScreen(viewModel: TrackerListViewModel) {
    val colors = LocalSincelyColors.current
    val trackers by viewModel.trackers.collectAsState()
    val lastCheckIns by viewModel.lastCheckIns.collectAsState()
    val filterCategoryLabel by viewModel.filterCategoryLabel.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val sheetOpen by viewModel.sheetOpen.collectAsState()
    val draft by viewModel.draft.collectAsState()
    val checkInSheetTrackerId by viewModel.checkInSheetTrackerId.collectAsState()
    val checkInDraft by viewModel.checkInDraft.collectAsState()
    val toast by viewModel.toast.collectAsState()

    val now = Clock.System.now()
    val presentations = trackers.map { presentationFor(it, lastCheckIns[it.id], now) }

    Box(Modifier.fillMaxSize().background(colors.bg)) {
        Column(Modifier.fillMaxSize()) {
            SceneHeader(isDarkTheme = isDarkTheme, onToggleTheme = viewModel::toggleTheme)

            Text(
                AndroidStrings.APP_TITLE,
                color = colors.text,
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
            )

            if (trackers.isNotEmpty()) {
                FilterChipsRow(
                    trackers = trackers,
                    selectedLabel = filterCategoryLabel,
                    onSelect = viewModel::setFilterCategoryLabel,
                )
            }

            when {
                trackers.isEmpty() -> EmptyState(onQuickAdd = viewModel::addFromSuggestion)
                else -> {
                    val filtered = presentations
                        .filter { filterCategoryLabel == null || it.tracker.categoryLabel == filterCategoryLabel }
                        .sortedWith(trackerPresentationOrder)
                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text(AndroidStrings.FILTERED_EMPTY, color = colors.muted, fontSize = 15.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(filtered, key = { it.tracker.id }) { presentation ->
                                TrackerCard(
                                    presentation = presentation,
                                    onCheckIn = { viewModel.quickCheckIn(presentation.tracker.id) },
                                    onOpenCheckInOptions = { viewModel.openCheckInSheet(presentation.tracker.id) },
                                    onOpenDetail = { viewModel.openDetail(presentation.tracker.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = viewModel::openAddSheet,
            containerColor = colors.accent,
            contentColor = colors.accentInk,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = AndroidStrings.ADD_TRACKER_CONTENT_DESCRIPTION)
        }

        if (toast != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 96.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.raised)
                    .padding(start = 18.dp, end = 10.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(toast?.message.orEmpty(), color = colors.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(
                    AndroidStrings.UNDO,
                    color = colors.accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = viewModel::undoToast)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }

    if (sheetOpen) {
        ModalBottomSheet(onDismissRequest = viewModel::closeAddSheet, sheetState = rememberModalBottomSheetState()) {
            AddTrackerSheetContent(draft = draft, viewModel = viewModel)
        }
    }

    val checkInTracker = checkInSheetTrackerId?.let { id -> trackers.find { it.id == id } }
    if (checkInTracker != null) {
        ModalBottomSheet(onDismissRequest = viewModel::closeCheckInSheet, sheetState = rememberModalBottomSheetState()) {
            CheckInOptionsSheetContent(tracker = checkInTracker, checkInDraft = checkInDraft, viewModel = viewModel)
        }
    }
}

@Composable
private fun FilterChipsRow(trackers: List<Tracker>, selectedLabel: String?, onSelect: (String?) -> Unit) {
    val categories = LinkedHashMap<String, String>()
    trackers.forEach { categories.putIfAbsent(it.categoryLabel, it.categoryEmoji) }

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(AndroidStrings.FILTER_ALL, emoji = null, selected = selectedLabel == null) { onSelect(null) }
        }
        items(categories.entries.toList()) { (label, emoji) ->
            FilterChip(label, emoji = emoji, selected = selectedLabel == label) { onSelect(label) }
        }
    }
}

@Composable
private fun FilterChip(label: String, emoji: String?, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalSincelyColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (selected) colors.accent else colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (emoji != null) Text(emoji, fontSize = 14.sp)
        Text(label, color = if (selected) colors.accentInk else colors.muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyState(onQuickAdd: (PopularTrackerSuggestion) -> Unit) {
    val colors = LocalSincelyColors.current
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                AndroidStrings.EMPTY_LIST_TITLE,
                color = colors.text,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            Text(
                AndroidStrings.EMPTY_LIST_SUBTITLE,
                color = colors.muted,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            AndroidStrings.POPULAR_SECTION,
            color = colors.muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PopularTrackerSuggestions.all.forEach { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .clickable { onQuickAdd(suggestion) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface2),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(suggestion.emoji, fontSize = 22.sp)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(suggestion.name, color = colors.text, fontSize = 16.sp)
                        Text(
                            TrackerCategoryPresentation.label(suggestion.category),
                            color = colors.muted,
                            fontSize = 12.sp,
                        )
                    }
                    Text("+", color = colors.accent, fontSize = 20.sp)
                }
            }
        }
    }
}
