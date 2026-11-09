package app.virtualwheelracing.ui.inicio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.virtualwheelracing.R
import app.virtualwheelracing.domain.GlobalSettings
import app.virtualwheelracing.domain.ResponseCurve
import app.virtualwheelracing.protocol.PairingPayload
import app.virtualwheelracing.ui.components.IconSquareButton
import app.virtualwheelracing.ui.components.Segmented
import app.virtualwheelracing.ui.components.drawChevronRight
import app.virtualwheelracing.ui.components.drawSliders
import app.virtualwheelracing.ui.components.drawWheelIcon
import app.virtualwheelracing.ui.theme.Fonts
import app.virtualwheelracing.ui.theme.Palette

/** Três jeitos simples de ajustar a direção, no lugar de graus e curvas. */
enum class SteeringFeel(val label: String, val arcDegrees: Float, val curve: ResponseCurve) {
    SOFT("Suave", 90f, ResponseCurve.SOFT),
    NORMAL("Normal", 90f, ResponseCurve.LINEAR),
    FAST("Rápida", 60f, ResponseCurve.LINEAR);

    fun applyTo(settings: GlobalSettings) = settings.copy(arcDegrees = arcDegrees, curve = curve)

    companion object {
        /** Null quando os ajustes avançados não batem com nenhuma opção simples. */
        fun of(settings: GlobalSettings) = entries.firstOrNull { it.arcDegrees == settings.arcDegrees && it.curve == settings.curve }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    pairing: PairingPayload,
    settings: GlobalSettings,
    onFeel: (SteeringFeel) -> Unit,
    onSettings: () -> Unit,
    onGuide: () -> Unit,
    onDrive: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Background)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Image(painterResource(R.drawable.logo_tile), contentDescription = null, modifier = Modifier.size(40.dp))
            Text(
                buildAnnotatedString {
                    append("Virtual Wheel ")
                    withStyle(SpanStyle(color = Palette.AccentText)) { append("Racing") }
                },
                fontFamily = Fonts.Display,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Palette.Text,
                modifier = Modifier.weight(1f),
            )
            IconSquareButton("Ajustes", onSettings) { drawSliders(it) }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Palette.OkBackground, RoundedCornerShape(24.dp)).padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(Modifier.size(52.dp).background(Palette.Throttle, CircleShape), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(26.dp)) {
                        val w = size.width
                        val check = Path().apply {
                            moveTo(w * 0.18f, w * 0.52f)
                            lineTo(w * 0.4f, w * 0.72f)
                            lineTo(w * 0.82f, w * 0.3f)
                        }
                        drawPath(check, Palette.Background, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
                Column {
                    Text("Tudo pronto!", fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Palette.Text)
                    Text("Pareado com ${pairing.pcName}", fontSize = 14.sp, color = Color(0xFFCFF5E3))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .background(Palette.Accent, RoundedCornerShape(28.dp))
                    .clickable(role = Role.Button, onClick = onDrive),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Canvas(Modifier.size(44.dp)) { drawWheelIcon(Palette.OnAccent) }
                Spacer(Modifier.height(6.dp))
                Text("JOGAR", fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 30.sp, letterSpacing = 1.2.sp, color = Palette.OnAccent)
            }
            Text(
                "Gire o celular para a horizontal e segure como um volante",
                fontSize = 13.sp,
                color = Palette.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Palette.Surface, RoundedCornerShape(20.dp))
                    .border(1.dp, Palette.Line, RoundedCornerShape(20.dp))
                    .clickable(role = Role.Button, onClick = onGuide)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Configurar meu jogo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
                        Text("Passo a passo de 1 minuto", fontSize = 13.sp, color = Palette.Muted)
                    }
                    Canvas(Modifier.size(18.dp)) { drawChevronRight(Palette.Muted) }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Euro Truck", "Assetto Corsa", "BeamNG", "rFactor", "Outro").forEach { game ->
                        Text(
                            game,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (game == "Outro") Palette.Muted else Palette.Text,
                            modifier = Modifier.background(Palette.Surface2, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Palette.Surface, RoundedCornerShape(20.dp))
                    .border(1.dp, Palette.Line, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Direção", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
                val current = SteeringFeel.of(settings)
                Segmented(
                    options = SteeringFeel.entries.map { it to it.label },
                    selected = current,
                    onSelect = { feel -> feel?.let(onFeel) },
                )
                if (current == null) {
                    Text("Você está usando um ajuste personalizado.", fontSize = 12.sp, color = Palette.Muted)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            Canvas(Modifier.size(18.dp)) {
                val w = size.width
                drawCircle(Palette.Warning, radius = w * 0.32f, center = Offset(w / 2, w * 0.4f), style = Stroke(2.dp.toPx()))
                drawLine(Palette.Warning, Offset(w * 0.38f, w * 0.88f), Offset(w * 0.62f, w * 0.88f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            }
            Text(
                "Dica: Wi-Fi 5 GHz e o PC no cabo deixam a direção mais rápida.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Palette.Muted,
            )
        }
    }
}
