package app.sincely.android.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sincely.android.ui.theme.LocalSincelyColors
import app.sincely.shared.domain.TrackerStatus

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackerCard(
    presentation: TrackerPresentation,
    onCheckIn: () -> Unit,
    onOpenCheckInOptions: () -> Unit,
    onOpenDetail: () -> Unit,
) {
    val colors = LocalSincelyColors.current
    val tracker = presentation.tracker
    val accent = statusAccentColor(colors, presentation.status)
    val chipBg = statusChipBackground(colors, presentation.status)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCheckIn,
                    onLongClick = onOpenCheckInOptions,
                )
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(modifier = Modifier.size(48.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(chipBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(tracker.emoji, fontSize = 26.sp)
                    }
                    if (tracker.reminderEnabled) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(colors.surface2),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("🔔", fontSize = 9.sp)
                        }
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(tracker.name, color = colors.text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(
                        cardSubtitle(tracker, presentation.days, presentation.status),
                        color = if (presentation.status == TrackerStatus.OK) colors.muted else accent,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            progressFraction(tracker, presentation.days)?.let { fraction ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(colors.surface2),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(5.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(accent),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenDetail,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(">", color = colors.faint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
