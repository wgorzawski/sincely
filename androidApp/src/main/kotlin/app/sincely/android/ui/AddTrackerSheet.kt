package app.sincely.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sincely.android.ui.theme.LocalSincelyColors
import app.sincely.shared.domain.PopularTrackerSuggestions
import app.sincely.shared.domain.TrackerCategory
import app.sincely.shared.domain.TrackerCategoryPresentation

@Composable
fun AddTrackerSheetContent(draft: TrackerDraft, viewModel: TrackerListViewModel) {
    val colors = LocalSincelyColors.current

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(AndroidStrings.NEW_TRACKER_TITLE, color = colors.text, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .padding(horizontal = 16.dp),
        ) {
            NameFieldRow(draft.name, AndroidStrings.NAME_PLACEHOLDER, viewModel::updateDraftName)
            EmojiFieldRow(draft.emoji, draft.emojiPickerOpen, viewModel::toggleDraftEmojiPicker)
            if (draft.emojiPickerOpen) {
                EmojiPickerGrid(draft.emoji, viewModel::pickDraftEmoji)
            }
            CategorySection(
                category = draft.category,
                customLabel = draft.customCategoryLabel,
                onPickCategory = viewModel::pickDraftCategory,
                onCustomLabelChange = viewModel::updateDraftCustomCategoryLabel,
            )
            ToggleFieldRow(
                label = AndroidStrings.INTERVAL_TOGGLE_LABEL_ADD,
                checked = draft.intervalEnabled,
                onToggle = viewModel::toggleDraftInterval,
                showDivider = draft.intervalEnabled,
            )
            if (draft.intervalEnabled) {
                IntervalStepperRow(draft.intervalDays, viewModel::incrementDraftInterval, viewModel::decrementDraftInterval)
                ToggleFieldRow(
                    label = AndroidStrings.REMINDER_TOGGLE_LABEL,
                    checked = draft.reminderEnabled,
                    onToggle = viewModel::toggleDraftReminder,
                    showDivider = false,
                )
                if (draft.reminderEnabled) {
                    ReminderTimeRow(draft.reminderTime, viewModel::setDraftReminderTime)
                    Column(Modifier.padding(bottom = 14.dp)) {
                        Text(AndroidStrings.NOTIF_PREVIEW_LABEL, color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                        val title = "${draft.emoji} ${draft.name.trim().ifEmpty { "Nowy tracker" }}"
                        NotificationPreview(title, AndroidStrings.notifBody(draft.intervalDays))
                    }
                }
            }
        }

        Text(
            AndroidStrings.POPULAR_SECTION,
            color = colors.muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 18.dp, bottom = 10.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PopularTrackerSuggestions.all.forEach { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .clickable { viewModel.applySuggestionToDraft(suggestion) }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface2),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(suggestion.emoji, fontSize = 18.sp)
                    }
                    Text(suggestion.name, color = colors.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Text(categoryLabelFor(suggestion.category), color = colors.muted, fontSize = 12.sp)
                }
            }
        }

        val canSubmit = draft.name.isNotBlank()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (canSubmit) colors.accent else colors.border)
                .clickable(enabled = canSubmit, onClick = viewModel::submitAdd)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(AndroidStrings.SUBMIT_ADD, color = colors.accentInk, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

private fun categoryLabelFor(category: TrackerCategory) =
    TrackerCategoryPresentation.label(category)
