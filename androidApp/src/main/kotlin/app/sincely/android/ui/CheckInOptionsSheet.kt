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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sincely.android.ui.theme.LocalSincelyColors
import app.sincely.shared.domain.Tracker
import java.time.Instant as JavaInstant
import java.time.ZoneOffset

@Composable
fun CheckInOptionsSheetContent(tracker: Tracker, checkInDraft: CheckInDraft, viewModel: TrackerListViewModel) {
    val colors = LocalSincelyColors.current
    var showDatePicker by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(tracker.emoji, fontSize = 20.sp)
            }
            Column {
                Text(AndroidStrings.CHECKIN_OPTIONS_TITLE, color = colors.text, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Text(tracker.name, color = colors.muted, fontSize = 13.sp)
            }
        }

        Text(AndroidStrings.CHECKIN_DATE_LABEL, color = colors.muted, fontSize = 15.sp, modifier = Modifier.padding(top = 18.dp, bottom = 10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateChoiceChip(AndroidStrings.CHECKIN_NOW, checkInDraft.dateChoice == CheckInDateChoice.NOW) {
                viewModel.setCheckInDateChoice(CheckInDateChoice.NOW)
            }
            DateChoiceChip(AndroidStrings.CHECKIN_YESTERDAY, checkInDraft.dateChoice == CheckInDateChoice.YESTERDAY) {
                viewModel.setCheckInDateChoice(CheckInDateChoice.YESTERDAY)
            }
            DateChoiceChip(AndroidStrings.CHECKIN_DAY_BEFORE, checkInDraft.dateChoice == CheckInDateChoice.DAY_BEFORE) {
                viewModel.setCheckInDateChoice(CheckInDateChoice.DAY_BEFORE)
            }
        }

        val customDateLabel = checkInDraft.customDateMillis?.let { millis ->
            JavaInstant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (checkInDraft.dateChoice == CheckInDateChoice.CUSTOM) colors.accent else colors.surface)
                .clickable { showDatePicker = true }
                .padding(14.dp),
        ) {
            Text(
                customDateLabel ?: "wybierz własną datę",
                color = if (checkInDraft.dateChoice == CheckInDateChoice.CUSTOM) colors.accentInk else colors.text,
                fontSize = 15.sp,
            )
        }

        Text(AndroidStrings.CHECKIN_NOTE_LABEL, color = colors.muted, fontSize = 15.sp, modifier = Modifier.padding(bottom = 10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 22.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surface)
                .padding(14.dp),
        ) {
            if (checkInDraft.note.isEmpty()) {
                Text(AndroidStrings.CHECKIN_NOTE_PLACEHOLDER, color = colors.faint, fontSize = 15.sp)
            }
            BasicTextField(
                value = checkInDraft.note,
                onValueChange = viewModel::updateCheckInNote,
                singleLine = true,
                textStyle = TextStyle(color = colors.text, fontSize = 15.sp),
                cursorBrush = SolidColor(colors.accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(colors.accent)
                .clickable(onClick = viewModel::submitCheckInWithOptions)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(AndroidStrings.CHECKIN_SUBMIT, color = colors.accentInk, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let(viewModel::updateCheckInCustomDate)
                    showDatePicker = false
                }) { Text(AndroidStrings.CHECKIN_SUBMIT) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(AndroidStrings.CANCEL) }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun DateChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalSincelyColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.accent else colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(label, color = if (selected) colors.accentInk else colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
