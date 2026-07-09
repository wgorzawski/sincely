package app.sincely.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SincelyGreen = Color(0xFF1B5E20)
private val SincelyGreenLight = Color(0xFF4C8C4A)

private val LightColors = lightColorScheme(
    primary = SincelyGreen,
    secondary = SincelyGreenLight,
)

private val DarkColors = darkColorScheme(
    primary = SincelyGreenLight,
    secondary = SincelyGreen,
)

@Composable
fun SincelyTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
