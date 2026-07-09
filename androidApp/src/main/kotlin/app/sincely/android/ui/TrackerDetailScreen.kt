package app.sincely.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sincely.android.ui.theme.LocalSincelyColors
import app.sincely.shared.domain.CheckIn
import app.sincely.shared.domain.HistoryDateFormatter
import app.sincely.shared.domain.ReminderTimePresentation
import app.sincely.shared.domain.TrackerStats
import app.sincely.shared.domain.computeTrackerStats
import kotlin.time.Clock

@Composable
fun TrackerDetailScreen(viewModel: TrackerListViewModel, trackerId: Long) {
    val colors = LocalSincelyColors.current
    val trackers by viewModel.trackers.collectAsState()
    val tracker = trackers.find { it.id == trackerId }
    if (tracker == null) {
        // Tracker was archived/deleted (e.g. from this very screen) — bounce back to the list.
        viewModel.backToList()
        return
    }

    val lastCheckIns by viewModel.lastCheckIns.collectAsState()
    val history by viewModel.checkInsFor(trackerId).collectAsState(initial = emptyList())
    val detailEmojiPickerOpen by viewModel.detailEmojiPickerOpen.collectAsState()

    val now = Clock.System.now()
    val presentation = presentationFor(tracker, lastCheckIns[trackerId], now)
    val accent = statusAccentColor(colors, presentation.status)
    val chipBg = statusChipBackground(colors, presentation.status)
    val stats = computeTrackerStats(history.map { it.timestamp }, tracker.targetDays)

    Column(Modifier.fillMaxSize().background(colors.bg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable(onClick = viewModel::backToList),
                contentAlignment = Alignment.Center,
            ) {
                Text("←", color = colors.text, fontSize = 20.sp)
            }
            Text(AndroidStrings.DETAIL_TITLE, color = colors.muted, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(chipBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(tracker.emoji, fontSize = 38.sp)
                    }
                    Text(
                        tracker.name,
                        color = colors.text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
                    )
                    Text(
                        "${tracker.categoryEmoji} ${tracker.categoryLabel}",
                        color = colors.muted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (presentation.days == 0L) "0" else presentation.days.toString(),
                            color = accent,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            AndroidStrings.bigLabel(presentation.days),
                            color = accent,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                    Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tracker.targetDays?.let { target ->
                            Badge(AndroidStrings.GOAL_BADGE_FORMAT.format(target))
                        }
                        if (tracker.reminderEnabled) {
                            Badge("🔔 ${ReminderTimePresentation.clockLabel(tracker.reminderTime)}")
                        }
                    }
                }
            }

            item {
                SectionLabel(AndroidStrings.STATS_SECTION)
                if (stats != null) {
                    StatsCard(stats, tracker.targetDays)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.surface)
                            .padding(16.dp),
                    ) {
                        Text(AndroidStrings.NO_STATS_YET, color = colors.muted, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                SectionLabel(AndroidStrings.EDIT_SECTION)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .padding(horizontal = 16.dp),
                ) {
                    NameFieldRow(tracker.name, placeholder = "") { viewModel.updateDetailName(trackerId, it) }
                    EmojiFieldRow(tracker.emoji, detailEmojiPickerOpen, viewModel::toggleDetailEmojiPicker)
                    if (detailEmojiPickerOpen) {
                        EmojiPickerGrid(tracker.emoji) { viewModel.pickDetailEmoji(trackerId, it) }
                    }
                    CategorySection(
                        category = tracker.category,
                        customLabel = tracker.customCategoryLabel.orEmpty(),
                        onPickCategory = { viewModel.pickDetailCategory(trackerId, it) },
                        onCustomLabelChange = { viewModel.updateDetailCustomCategoryLabel(trackerId, it) },
                    )
                    val targetDays = tracker.targetDays
                    ToggleFieldRow(
                        label = AndroidStrings.INTERVAL_TOGGLE_LABEL_EDIT,
                        checked = targetDays != null,
                        onToggle = { viewModel.toggleDetailInterval(trackerId) },
                        showDivider = targetDays != null,
                    )
                    targetDays?.let { days ->
                        IntervalStepperRow(
                            days = days,
                            onIncrement = { viewModel.incrementDetailInterval(trackerId) },
                            onDecrement = { viewModel.decrementDetailInterval(trackerId) },
                        )
                        ToggleFieldRow(
                            label = AndroidStrings.REMINDER_TOGGLE_LABEL,
                            checked = tracker.reminderEnabled,
                            onToggle = { viewModel.toggleDetailReminder(trackerId) },
                            showDivider = false,
                        )
                        if (tracker.reminderEnabled) {
                            ReminderTimeRow(tracker.reminderTime) { viewModel.setDetailReminderTime(trackerId, it) }
                            Column(Modifier.padding(bottom = 14.dp)) {
                                Text(
                                    AndroidStrings.NOTIF_PREVIEW_LABEL,
                                    color = colors.muted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                                NotificationPreview(
                                    title = "${tracker.emoji} ${tracker.name}",
                                    body = AndroidStrings.notifBody(days),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                SectionLabel(AndroidStrings.HISTORY_SECTION)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .padding(horizontal = 16.dp),
                ) {
                    history.forEachIndexed { index, checkIn ->
                        HistoryRow(checkIn, isLatest = index == 0, accent = accent, showDivider = index != history.lastIndex)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 32.dp)) {
                    ActionButton(
                        label = AndroidStrings.UNDO_LAST_CHECKIN,
                        color = if (history.size > 1) colors.text else colors.faint,
                        background = colors.surface,
                        onClick = { viewModel.undoLastCheckIn(trackerId) },
                    )
                    ActionButton(
                        label = AndroidStrings.ARCHIVE,
                        color = colors.smoky,
                        background = colors.surface,
                        onClick = { viewModel.archiveTracker(trackerId) },
                    )
                    ActionButton(
                        label = AndroidStrings.DELETE,
                        color = colors.danger,
                        background = colors.dangerSoft,
                        onClick = { viewModel.deleteTracker(trackerId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = LocalSincelyColors.current
    Text(text, color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
private fun Badge(text: String) {
    val colors = LocalSincelyColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(colors.surface2)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(text, color = colors.muted, fontSize = 14.sp)
    }
}

@Composable
private fun StatsCard(stats: TrackerStats, targetDays: Int?) {
    val colors = LocalSincelyColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(16.dp),
    ) {
        Text(AndroidStrings.comparisonText(stats.averageGapDays, targetDays), color = colors.text, fontSize = 15.sp)
        Text(
            AndroidStrings.longestGapText(stats.longestGapDays),
            color = colors.muted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp),
        )
        val chartHeight = 90.dp
        Box(Modifier.fillMaxWidth().height(chartHeight)) {
            stats.goalLineFraction?.let { fraction ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(bottom = chartHeight * fraction)
                        .height(1.dp)
                        .background(colors.faint),
                )
            }
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                stats.bars.forEach { bar ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(bar.heightFraction)
                            .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                            .background(if (bar.exceedsTarget) colors.danger else colors.accent),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(checkIn: CheckIn, isLatest: Boolean, accent: Color, showDivider: Boolean) {
    val colors = LocalSincelyColors.current
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isLatest) accent else colors.faint),
            )
            if (checkIn.backdated) Text("🕓", fontSize = 12.sp)
            Text(HistoryDateFormatter.format(checkIn.timestamp), color = colors.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
            if (isLatest) {
                Text(AndroidStrings.LATEST_BADGE, color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        checkIn.note?.let { note ->
            Box(
                modifier = Modifier
                    .padding(start = 20.dp, bottom = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface2)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(note, color = colors.muted, fontSize = 13.sp)
            }
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

@Composable
private fun ActionButton(label: String, color: Color, background: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = color, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}
