package app.sincely.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.sincely.android.ui.TrackerListScreen
import app.sincely.android.ui.theme.SincelyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SincelyTheme {
                TrackerListScreen()
            }
        }
    }
}
