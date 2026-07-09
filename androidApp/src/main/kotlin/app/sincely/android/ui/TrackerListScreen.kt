package app.sincely.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.sincely.shared.domain.Tracker
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrackerListScreen(viewModel: TrackerListViewModel = koinViewModel()) {
    val trackers by viewModel.trackers.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::addSampleTracker) {
                Icon(Icons.Filled.Add, contentDescription = AndroidStrings.ADD_TRACKER_CONTENT_DESCRIPTION)
            }
        },
    ) { padding ->
        if (trackers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(AndroidStrings.EMPTY_LIST)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                items(trackers, key = { it.id }) { tracker ->
                    TrackerRow(tracker)
                }
            }
        }
    }
}

@Composable
private fun TrackerRow(tracker: Tracker) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "${tracker.emoji} ${tracker.name}",
            style = MaterialTheme.typography.titleMedium,
        )
        val targetLabel = tracker.targetDays
            ?.let { AndroidStrings.TARGET_DAYS_FORMAT.format(it) }
            ?: AndroidStrings.NO_TARGET
        Text(
            text = "${tracker.category} · $targetLabel",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
