package app.prwheel.desktop.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/** Cores do design aprovado. */
object Palette {
    val Background = Color(0xFF0E1013)
    val Sidebar = Color(0xFF121418)
    val Surface = Color(0xFF171A1F)
    val Surface2 = Color(0xFF20242B)
    val Line = Color(0xFF2C313A)
    val Text = Color(0xFFF2F4F7)
    val Muted = Color(0xFFA3ABB8)
    val Amber = Color(0xFFFFB020)
    val OnAmber = Color(0xFF1A1300)
    val Throttle = Color(0xFF3DDC97)
    val Brake = Color(0xFFFF6B5E)
    val OkBackground = Color(0xFF12352A)
    val WarnBackground = Color(0xFF3A2C0A)
    val ErrorBackground = Color(0xFF3A1715)

    val scheme = darkColorScheme(
        primary = Amber,
        onPrimary = OnAmber,
        background = Background,
        onBackground = Text,
        surface = Surface,
        onSurface = Text,
        surfaceVariant = Surface2,
        onSurfaceVariant = Muted,
        outline = Line,
        error = Brake,
    )
}
