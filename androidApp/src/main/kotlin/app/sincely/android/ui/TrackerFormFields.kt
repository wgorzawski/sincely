package app.sincely.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sincely.android.ui.theme.LocalSincelyColors
import app.sincely.shared.domain.EmojiOptions
import app.sincely.shared.domain.ReminderTime
import app.sincely.shared.domain.TrackerCategory
import app.sincely.shared.domain.TrackerCategoryPresentation

@Composable
private fun Divider(color: Color) {
    Box(Modifier.fillMaxWidth().height(1.dp).background(color))
}

@Composable
private fun FieldRow(label: String, showDivider: Boolean = true, content: @Composable RowScope.() -> Unit) {
    val colors = LocalSincelyColors.current
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(label, color = colors.muted, fontSize = 15.sp, modifier = Modifier.width(90.dp))
            content()
        }
        if (showDivider) Divider(colors.border)
    }
}

@Composable
fun NameFieldRow(name: String, placeholder: String, onNameChange: (String) -> Unit) {
    val colors = LocalSincelyColors.current
    FieldRow(label = AndroidStrings.NAME_LABEL) {
        Box(Modifier.weight(1f)) {
            if (name.isEmpty()) {
                Text(placeholder, color = colors.faint, fontSize = 16.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
            }
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = TextStyle(color = colors.text, fontSize = 16.sp, textAlign = TextAlign.End),
                cursorBrush = SolidColor(colors.accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun EmojiFieldRow(emoji: String, pickerOpen: Boolean, onToggle: () -> Unit) {
    val colors = LocalSincelyColors.current
    FieldRow(label = AndroidStrings.EMOJI_LABEL, showDivider = !pickerOpen) {
        Box(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surface2)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 20.sp)
        }
    }
}

@Composable
fun EmojiPickerGrid(selected: String, onPick: (String) -> Unit) {
    val colors = LocalSincelyColors.current
    Column(Modifier.fillMaxWidth()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        ) {
            EmojiOptions.all.forEach { option ->
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (option == selected) colors.accentSoft else colors.surface2)
                        .clickable { onPick(option) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(option, fontSize = 19.sp)
                }
            }
        }
        Divider(colors.border)
    }
}

@Composable
fun CategorySection(
    category: TrackerCategory,
    customLabel: String,
    onPickCategory: (TrackerCategory) -> Unit,
    onCustomLabelChange: (String) -> Unit,
) {
    val colors = LocalSincelyColors.current
    Column(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        Text(AndroidStrings.CATEGORY_LABEL, color = colors.muted, fontSize = 15.sp, modifier = Modifier.padding(bottom = 10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TrackerCategoryPresentation.pickerOrder.forEach { option ->
                val isSelected = option == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isSelected) colors.accent else colors.surface2)
                        .clickable { onPickCategory(option) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    val label = if (option == TrackerCategory.CUSTOM) {
                        AndroidStrings.CUSTOM_CATEGORY_CHIP
                    } else {
                        TrackerCategoryPresentation.label(option)
                    }
                    Text(
                        "${TrackerCategoryPresentation.emoji(option)} $label",
                        color = if (isSelected) colors.accentInk else colors.muted,
                        fontSize = 14.sp,
                    )
                }
            }
        }
        if (category == TrackerCategory.CUSTOM) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface2)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                if (customLabel.isEmpty()) {
                    Text(AndroidStrings.CUSTOM_CATEGORY_PLACEHOLDER, color = colors.faint, fontSize = 14.sp)
                }
                BasicTextField(
                    value = customLabel,
                    onValueChange = onCustomLabelChange,
                    singleLine = true,
                    textStyle = TextStyle(color = colors.text, fontSize = 14.sp),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun ToggleSwitch(checked: Boolean, onToggle: () -> Unit) {
    val colors = LocalSincelyColors.current
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(if (checked) colors.accent else colors.border)
            .clickable(onClick = onToggle),
    ) {
        Box(
            modifier = Modifier
                .padding(start = if (checked) 21.dp else 3.dp, top = 3.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
fun ToggleFieldRow(label: String, checked: Boolean, onToggle: () -> Unit, showDivider: Boolean = true) {
    val colors = LocalSincelyColors.current
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = colors.muted, fontSize = 15.sp, modifier = Modifier.weight(1f))
            ToggleSwitch(checked, onToggle)
        }
        if (showDivider) Divider(colors.border)
    }
}

@Composable
fun IntervalStepperRow(days: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    val colors = LocalSincelyColors.current
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(AndroidStrings.INTERVAL_DAYS_LABEL, color = colors.muted, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StepperButton("–", onDecrement)
                Text("$days", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                StepperButton("+", onIncrement)
            }
        }
        Divider(colors.border)
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit) {
    val colors = LocalSincelyColors.current
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface2)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = colors.text, fontSize = 18.sp)
    }
}

@Composable
fun ReminderTimeRow(selected: ReminderTime, onPick: (ReminderTime) -> Unit) {
    val colors = LocalSincelyColors.current
    Column(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        Text(AndroidStrings.REMINDER_TIME_LABEL, color = colors.muted, fontSize = 15.sp, modifier = Modifier.padding(bottom = 10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ReminderTimeOption(
                label = AndroidStrings.REMINDER_MORNING,
                selected = selected == ReminderTime.RANO,
                onClick = { onPick(ReminderTime.RANO) },
                modifier = Modifier.weight(1f),
            )
            ReminderTimeOption(
                label = AndroidStrings.REMINDER_EVENING,
                selected = selected == ReminderTime.WIECZOREM,
                onClick = { onPick(ReminderTime.WIECZOREM) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ReminderTimeOption(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalSincelyColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.accent else colors.surface2)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (selected) colors.accentInk else colors.muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun NotificationPreview(title: String, body: String) {
    val colors = LocalSincelyColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.raised)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(16.dp).clip(RoundedCornerShape(6.dp)).background(colors.accent))
            Text(AndroidStrings.NOTIF_SUBTITLE, color = colors.muted, fontSize = 11.sp)
        }
        Text(title, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
        Text(body, color = colors.muted, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
