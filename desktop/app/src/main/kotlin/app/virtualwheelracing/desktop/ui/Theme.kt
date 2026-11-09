package app.virtualwheelracing.desktop.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

/** Cores do design aprovado. */
object Palette {
    val Background = Color(0xFF0E1013)
    val Sidebar = Color(0xFF121418)
    val Surface = Color(0xFF171A1F)
    val Surface2 = Color(0xFF20242B)
    val Line = Color(0xFF2C313A)
    val Text = Color(0xFFF2F4F7)
    val Muted = Color(0xFFA3ABB8)
    val Accent = Color(0xFFE31E24) // vermelho da logo: botões e destaques
    val AccentText = Color(0xFFFF5A5F) // vermelho legível como texto sobre o fundo escuro
    val OnAccent = Color(0xFFFFFFFF)
    val Warning = Color(0xFFFFB020) // âmbar: só para avisos
    val Throttle = Color(0xFF3DDC97)
    val Brake = Color(0xFFFF6B5E)
    val OkBackground = Color(0xFF12352A)
    val WarnBackground = Color(0xFF3A2C0A)
    val ErrorBackground = Color(0xFF3A1715)

    val scheme = darkColorScheme(
        primary = Accent,
        onPrimary = OnAccent,
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

/** Títulos e números em Barlow Semi Condensed (OFL), como no design; texto na fonte do Windows. */
object Fonts {
    val Display: FontFamily = runCatching {
        FontFamily(
            load("barlow_semicondensed_semibold", FontWeight.SemiBold),
            load("barlow_semicondensed_bold", FontWeight.Bold),
        )
    }.getOrDefault(FontFamily.Default)

    private fun load(name: String, weight: FontWeight) = Font(
        identity = name,
        data = requireNotNull(Fonts::class.java.getResourceAsStream("/fonts/$name.ttf")) { "Fonte $name ausente" }
            .use { it.readBytes() },
        weight = weight,
    )
}
