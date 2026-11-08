package app.virtualwheelracing.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import app.virtualwheelracing.R

/** Cores do design aprovado. */
object Palette {
    val Background = Color(0xFF0E1013)
    val Surface = Color(0xFF171A1F)
    val Surface2 = Color(0xFF20242B)
    val Line = Color(0xFF2C313A)
    val Text = Color(0xFFF2F4F7)
    val Muted = Color(0xFFA3ABB8)
    val Accent = Color(0xFFE31E24) // vermelho da logo: botÃµes e destaques
    val AccentText = Color(0xFFFF5A5F) // vermelho legÃ­vel como texto sobre o fundo escuro
    val OnAccent = Color(0xFFFFFFFF)
    val Warning = Color(0xFFFFB020) // Ã¢mbar: sÃ³ para avisos
    val AccentDark = Color(0xFF3A1416)
    val Throttle = Color(0xFF3DDC97)
    val Brake = Color(0xFFFF6B5E)
    val OkBackground = Color(0xFF12352A)
    val WarnBackground = Color(0xFF3A2C0A)
    val ErrorBackground = Color(0xFF3A1715)
}

/** Títulos e números: Barlow Semi Condensed. Texto: Inter. Ambas sob licença OFL (pasta licenses/). */
object Fonts {
    val Display = FontFamily(
        Font(R.font.barlow_semicondensed_semibold, FontWeight.SemiBold),
        Font(R.font.barlow_semicondensed_bold, FontWeight.Bold),
    )

    @OptIn(ExperimentalTextApi::class)
    val Body = FontFamily(
        Font(R.font.inter_variable, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
        Font(R.font.inter_variable, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
        Font(R.font.inter_variable, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
        Font(R.font.inter_variable, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    )
}

private val base = Typography()

private val typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Fonts.Display, fontWeight = FontWeight.Bold),
    displayMedium = base.displayMedium.copy(fontFamily = Fonts.Display, fontWeight = FontWeight.Bold),
    displaySmall = base.displaySmall.copy(fontFamily = Fonts.Display, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontFamily = Fonts.Display, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = Fonts.Display, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = Fonts.Display, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = Fonts.Display, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontFamily = Fonts.Body, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = Fonts.Body, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontFamily = Fonts.Body),
    bodyMedium = base.bodyMedium.copy(fontFamily = Fonts.Body),
    bodySmall = base.bodySmall.copy(fontFamily = Fonts.Body),
    labelLarge = base.labelLarge.copy(fontFamily = Fonts.Body, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = Fonts.Body),
    labelSmall = base.labelSmall.copy(fontFamily = Fonts.Body),
)

@Composable
fun VwrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Palette.Accent,
            onPrimary = Palette.OnAccent,
            background = Palette.Background,
            onBackground = Palette.Text,
            surface = Palette.Surface,
            onSurface = Palette.Text,
            surfaceVariant = Palette.Surface2,
            onSurfaceVariant = Palette.Muted,
            outline = Palette.Line,
            error = Palette.Brake,
        ),
        typography = typography,
        content = content,
    )
}
