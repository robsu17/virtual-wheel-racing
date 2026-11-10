package app.virtualwheelracing.ui.ajustes

import app.virtualwheelracing.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.virtualwheelracing.domain.GlobalSettings
import app.virtualwheelracing.domain.ResponseCurve
import app.virtualwheelracing.ui.components.ScreenHeader
import app.virtualwheelracing.ui.components.Segmented
import app.virtualwheelracing.ui.components.VwrCard
import app.virtualwheelracing.ui.components.drawChevronRight
import app.virtualwheelracing.ui.theme.Fonts
import app.virtualwheelracing.ui.theme.Palette
import kotlin.math.roundToInt

/** Ajustes globais do volante: valem para todos os jogos. Cada mudança é salva na hora. */
@Composable
fun SettingsScreen(
    settings: GlobalSettings,
    pcName: String,
    onChange: ((GlobalSettings) -> GlobalSettings) -> Unit,
    onChangePc: () -> Unit,
    onCalibration: () -> Unit,
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
        ScreenHeader(stringResource(R.string.settings), stringResource(R.string.settings_subtitle), onBack)

        VwrCard {
            SectionTitle(stringResource(R.string.steering))
            SliderSetting(
                label = stringResource(R.string.phone_rotation),
                value = stringResource(R.string.phone_rotation_value, settings.arcDegrees.roundToInt()),
                help = stringResource(R.string.phone_rotation_help),
                current = settings.arcDegrees,
                range = 60f..90f,
                steps = 5,
                onValue = { v -> onChange { it.copy(arcDegrees = v.roundToInt().toFloat()) } },
            )
            SliderSetting(
                label = stringResource(R.string.deadzone),
                value = "${"%.1f".format(settings.deadzoneDegrees)}°",
                help = stringResource(R.string.deadzone_help),
                current = settings.deadzoneDegrees,
                range = 0f..3f,
                steps = 29,
                onValue = { v -> onChange { it.copy(deadzoneDegrees = (v * 10).roundToInt() / 10f) } },
            )
            Text(stringResource(R.string.response), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
            Segmented(
                options = listOf(ResponseCurve.LINEAR to stringResource(R.string.curve_linear), ResponseCurve.SOFT to stringResource(R.string.curve_soft), ResponseCurve.FIRM to stringResource(R.string.curve_firm)),
                selected = settings.curve,
                onSelect = { curve -> onChange { it.copy(curve = curve) } },
            )
        }

        VwrCard {
            SectionTitle(stringResource(R.string.pedals))
            Text(stringResource(R.string.brake_side), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
            Segmented(
                options = listOf(true to stringResource(R.string.side_left), false to stringResource(R.string.side_right)),
                selected = settings.brakeOnLeft,
                onSelect = { left -> onChange { it.copy(brakeOnLeft = left) } },
            )
            SliderSetting(
                label = stringResource(R.string.pedal_full_at),
                value = stringResource(R.string.pedal_full_at_value, (settings.pedalFullAt * 100).roundToInt()),
                help = stringResource(R.string.pedal_full_at_help),
                current = settings.pedalFullAt,
                range = 0.6f..1f,
                steps = 7,
                onValue = { v -> onChange { it.copy(pedalFullAt = (v * 20).roundToInt() / 20f) } },
            )
        }

        VwrCard {
            SectionTitle(stringResource(R.string.pc_and_sensors))
            LinkRow(stringResource(R.string.change_pc), stringResource(R.string.paired_with, pcName), onChangePc)
            LinkRow(stringResource(R.string.test_sensors), stringResource(R.string.test_sensors_subtitle), onCalibration)
        }

        VwrCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.vibration), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
                    Text(stringResource(R.string.vibration_subtitle), fontSize = 12.sp, color = Palette.Muted)
                }
                Switch(checked = false, onCheckedChange = null, enabled = false)
            }
        }
    }
}

@Composable
private fun LinkRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
            Text(subtitle, fontSize = 12.sp, color = Palette.Muted)
        }
        Canvas(Modifier.size(18.dp)) { drawChevronRight(Palette.Muted) }
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
                thumbColor = Palette.Accent,
                activeTrackColor = Palette.Accent,
                inactiveTrackColor = Palette.Surface2,
                activeTickColor = Palette.OnAccent,
                inactiveTickColor = Palette.Line,
            ),
        )
        Text(help, fontSize = 12.sp, color = Palette.Muted)
    }
}
