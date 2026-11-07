package app.volapex.ui.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.volapex.domain.GlobalSettings
import app.volapex.domain.ResponseCurve
import app.volapex.ui.components.ScreenHeader
import app.volapex.ui.components.Segmented
import app.volapex.ui.components.VolapexCard
import app.volapex.ui.theme.Fonts
import app.volapex.ui.theme.Palette
import kotlin.math.roundToInt

/** Ajustes globais do volante: valem para todos os jogos. Cada mudança é salva na hora. */
@Composable
fun SettingsScreen(
    settings: GlobalSettings,
    onChange: ((GlobalSettings) -> GlobalSettings) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader("Ajustes do volante", "Valem para todos os jogos", onBack)

        VolapexCard {
            SectionTitle("Direção")
            SliderSetting(
                label = "Giro do celular",
                value = "${settings.arcDegrees.roundToInt()}° para cada lado",
                help = "Menos giro deixa a direção mais rápida. A rotação do volante é ajustada no jogo.",
                current = settings.arcDegrees,
                range = 60f..90f,
                steps = 5,
                onValue = { v -> onChange { it.copy(arcDegrees = v.roundToInt().toFloat()) } },
            )
            SliderSetting(
                label = "Zona morta",
                value = "${"%.1f".format(settings.deadzoneDegrees)}°",
                help = "Ignora tremidas pequenas perto do centro. Deixe a zona morta do jogo em 0%.",
                current = settings.deadzoneDegrees,
                range = 0f..3f,
                steps = 29,
                onValue = { v -> onChange { it.copy(deadzoneDegrees = (v * 10).roundToInt() / 10f) } },
            )
            Text("Resposta", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
            Segmented(
                options = listOf(ResponseCurve.LINEAR to "Linear", ResponseCurve.SOFT to "Suave", ResponseCurve.FIRM to "Firme"),
                selected = settings.curve,
                onSelect = { curve -> onChange { it.copy(curve = curve) } },
            )
        }

        VolapexCard {
            SectionTitle("Pedais")
            Text("Freio fica do lado", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
            Segmented(
                options = listOf(true to "Esquerdo", false to "Direito"),
                selected = settings.brakeOnLeft,
                onSelect = { left -> onChange { it.copy(brakeOnLeft = left) } },
            )
            SliderSetting(
                label = "Pedal no fundo em",
                value = "${(settings.pedalFullAt * 100).roundToInt()}% do curso",
                help = "Com menos curso, o pedal chega ao fundo sem esticar o polegar.",
                current = settings.pedalFullAt,
                range = 0.6f..1f,
                steps = 7,
                onValue = { v -> onChange { it.copy(pedalFullAt = (v * 20).roundToInt() / 20f) } },
            )
        }

        VolapexCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Vibração por eventos", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
                    Text("Em breve: depende da telemetria do jogo", fontSize = 12.sp, color = Palette.Muted)
                }
                Switch(checked = false, onCheckedChange = null, enabled = false)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontFamily = Fonts.Display, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Palette.Text)
}

@Composable
private fun SliderSetting(
    label: String,
    value: String,
    help: String,
    current: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValue: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text, modifier = Modifier.weight(1f))
            Text(value, fontSize = 14.sp, color = Palette.Text)
        }
        Slider(
            value = current.coerceIn(range.start, range.endInclusive),
            onValueChange = onValue,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Palette.Amber,
                activeTrackColor = Palette.Amber,
                inactiveTrackColor = Palette.Surface2,
                activeTickColor = Palette.OnAmber,
                inactiveTickColor = Palette.Line,
            ),
        )
        Text(help, fontSize = 12.sp, color = Palette.Muted)
    }
}
