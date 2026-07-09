package app.sincely.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Color tokens lifted 1:1 from the "Kiedy ostatnio?" design (`--bg`, `--surface`, …).
 * Exposed via [LocalSincelyColors] instead of `MaterialTheme.colorScheme` because the
 * design's palette (accent/warn/danger/smoky, soft variants) doesn't map cleanly onto
 * Material 3's role names.
 */
data class SincelyColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val raised: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val faint: Color,
    val accent: Color,
    val accentInk: Color,
    val accentSoft: Color,
    val warn: Color,
    val warnSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val smoky: Color,
)

private val DarkSincelyColors = SincelyColors(
    bg = Color(0xFF1C1626),
    surface = Color(0xFF261E34),
    surface2 = Color(0xFF302641),
    raised = Color(0xFF3B2E4E),
    border = Color(0xFF473A5C),
    text = Color(0xFFF7ECDF),
    muted = Color(0xFFB7A7BD),
    faint = Color(0xFF84718D),
    accent = Color(0xFFE8A659),
    accentInk = Color(0xFF2C1A08),
    accentSoft = Color(0x2EE8A659),
    warn = Color(0xFFE8A659),
    warnSoft = Color(0x29E8A659),
    danger = Color(0xFFB5573F),
    dangerSoft = Color(0x33B5573F),
    smoky = Color(0xFF8BA0BC),
)

private val LightSincelyColors = SincelyColors(
    bg = Color(0xFFF4E8D4),
    surface = Color(0xFFFFFAF0),
    surface2 = Color(0xFFEEDDBE),
    raised = Color(0xFFFFF3DD),
    border = Color(0xFFE0CCA0),
    text = Color(0xFF3A2A1C),
    muted = Color(0xFF8A7357),
    faint = Color(0xFFBBA989),
    accent = Color(0xFFE8A659),
    accentInk = Color(0xFF2C1A08),
    accentSoft = Color(0x47E8A659),
    warn = Color(0xFFE0964A),
    warnSoft = Color(0x38E0964A),
    danger = Color(0xFFA8503A),
    dangerSoft = Color(0x29A8503A),
    smoky = Color(0xFF5F7891),
)

val LocalSincelyColors = staticCompositionLocalOf { DarkSincelyColors }

@Composable
fun SincelyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkSincelyColors else LightSincelyColors
    val materialScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.accentInk,
            background = colors.bg,
            surface = colors.surface,
            onSurface = colors.text,
            surfaceVariant = colors.surface2,
            error = colors.danger,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = colors.accentInk,
            background = colors.bg,
            surface = colors.surface,
            onSurface = colors.text,
            surfaceVariant = colors.surface2,
            error = colors.danger,
        )
    }
    CompositionLocalProvider(LocalSincelyColors provides colors) {
        MaterialTheme(colorScheme = materialScheme, content = content)
    }
}
