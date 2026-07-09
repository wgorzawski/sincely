package app.sincely.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Decorative day/night banner from the design, simplified: a sky gradient plus a
 * glowing sun/moon orb and a tap target that flips the app's theme. The design's
 * procedural building skyline is left out — pure decoration, not core UX.
 */
@Composable
fun SceneHeader(isDarkTheme: Boolean, onToggleTheme: () -> Unit) {
    val sky = if (isDarkTheme) {
        Brush.verticalGradient(listOf(Color(0xFF251C35), Color(0xFF1C1626)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFD9A377), Color(0xFFC98A9A), Color(0xFF8BA0BC)))
    }
    val orbColor = if (isDarkTheme) Color(0xFFF2EAD8) else Color(0xFFF2C879)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(sky),
    ) {
        Box(
            modifier = Modifier
                .padding(top = 14.dp, end = 22.dp)
                .align(Alignment.TopEnd)
                .size(22.dp)
                .clip(CircleShape)
                .background(orbColor),
        )
        Box(
            modifier = Modifier
                .padding(top = 12.dp, start = 16.dp)
                .align(Alignment.TopStart)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable(onClick = onToggleTheme),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (isDarkTheme) "🌙" else "☀️", fontSize = 15.sp)
        }
    }
}
