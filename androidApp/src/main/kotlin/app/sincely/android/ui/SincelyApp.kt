package app.sincely.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.sincely.android.ui.theme.LocalSincelyColors
import app.sincely.android.ui.theme.SincelyTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SincelyApp(viewModel: TrackerListViewModel = koinViewModel()) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val screen by viewModel.screen.collectAsState()

    SincelyTheme(darkTheme = isDarkTheme) {
        Box(Modifier.fillMaxSize().background(LocalSincelyColors.current.bg)) {
            when (val current = screen) {
                Screen.List -> TrackerListScreen(viewModel)
                is Screen.Detail -> TrackerDetailScreen(viewModel, current.trackerId)
            }
        }
    }
}
