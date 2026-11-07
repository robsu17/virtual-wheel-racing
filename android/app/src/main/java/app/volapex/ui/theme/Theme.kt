package app.volapex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Cores do design aprovado. As fontes Barlow e Inter entram depois do protótipo. */
object Palette {
    val Background = Color(0xFF0E1013)
    val Surface = Color(0xFF171A1F)
    val Surface2 = Color(0xFF20242B)
    val Line = Color(0xFF2C313A)
    val Text = Color(0xFFF2F4F7)
    val Muted = Color(0xFFA3ABB8)
    val Amber = Color(0xFFFFB020)
    val OnAmber = Color(0xFF1A1300)
    val Throttle = Color(0xFF3DDC97)
    val Brake = Color(0xFFFF6B5E)
}

@Composable
fun VolapexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Palette.Amber,
            onPrimary = Palette.OnAmber,
            background = Palette.Background,
            onBackground = Palette.Text,
            surface = Palette.Surface,
            onSurface = Palette.Text,
            surfaceVariant = Palette.Surface2,
            onSurfaceVariant = Palette.Muted,
            outline = Palette.Line,
            error = Palette.Brake,
        ),
        content = content,
    )
}
