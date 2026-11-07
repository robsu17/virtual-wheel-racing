package app.volapex.ui.calibracao

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.volapex.AppContainer
import app.volapex.domain.SensorStats
import app.volapex.engine.DiagnosticsSnapshot
import app.volapex.engine.SensorDiagnostics
import app.volapex.ui.components.LandscapeWhileVisible
import app.volapex.ui.components.ScreenHeader
import app.volapex.ui.components.StatTile
import app.volapex.ui.components.StatusChip
import app.volapex.ui.theme.Fonts
import app.volapex.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

/** Teste de sensores (10 s) e giro ao vivo. O resultado fica salvo para a tela Início. */
@Composable
fun CalibrationScreen(container: AppContainer, onBack: () -> Unit) {
    LandscapeWhileVisible()
    var diagnostics by remember { mutableStateOf<SensorDiagnostics?>(null) }
    var snapshot by remember { mutableStateOf<DiagnosticsSnapshot?>(null) }
    var run by remember { mutableStateOf(0) }

    DisposableEffect(run) {
        val d = container.newDiagnostics().also { it.start() }
        diagnostics = d
        onDispose { d.stop() }
    }
    LaunchedEffect(diagnostics) {
        while (true) {
            val s = diagnostics?.snapshot()
            snapshot = s
            if (s?.finished == true && s.stats != null) container.lastSensorStats.value = s.stats
            delay(66)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Background)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ScreenHeader("Calibração", onBack = onBack)
            Text(
                "Segure o celular como um volante, reto e na altura em que vai jogar. Depois toque em Recentrar.",
                fontSize = 14.sp,
                color = Palette.Muted,
                lineHeight = 20.sp,
            )
            Row(
                Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PhoneIllustration(snapshot?.liveDegrees ?: 0f)
                Spacer(Modifier.width(20.dp))
                Column {
                    Text(
                        formatDegrees(snapshot?.liveDegrees ?: 0f),
                        fontFamily = Fonts.Display,
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp,
                        color = Palette.Text,
                    )
                    Text(
                        if (snapshot?.tilted == true) "inclinado demais" else "ângulo atual",
                        fontSize = 12.sp,
                        color = if (snapshot?.tilted == true) Palette.Amber else Palette.Muted,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { diagnostics?.recenter() }, modifier = Modifier.height(48.dp)) {
                    Text("Recentrar", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp)) { Text("Concluir") }
            }
        }

        DiagnosticsCard(snapshot, onRetry = { run++ })
    }
}

@Composable
private fun DiagnosticsCard(snapshot: DiagnosticsSnapshot?, onRetry: () -> Unit) {
    val stats: SensorStats? = snapshot?.stats
    Column(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .background(Palette.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Teste dos sensores",
                fontFamily = Fonts.Display,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Palette.Text,
                modifier = Modifier.weight(1f),
            )
            when {
                snapshot?.sensorAvailable == false -> StatusChip("Sem sensor", Palette.Brake, Palette.ErrorBackground)
                stats == null -> StatusChip("Testando ${((snapshot?.progress ?: 0f) * 100).roundToInt()}%", Palette.Muted, Palette.Surface2)
                stats.fullMode -> StatusChip("Tudo certo", Palette.Throttle, Palette.OkBackground)
                else -> StatusChip("Modo reduzido", Palette.Amber, Palette.WarnBackground)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Leituras por segundo", stats?.let { "${it.rateHz} Hz" } ?: "…", Modifier.weight(1f))
            StatTile("Maior espera (p95)", stats?.let { "%.1f ms".format(it.p95IntervalMs) } ?: "…", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Leituras repetidas", stats?.let { "${it.repeatedPercent.roundToInt()}%" } ?: "…", Modifier.weight(1f))
            StatTile("Modo", stats?.let { if (it.fullMode) "Completo" else "Reduzido" } ?: "…", Modifier.weight(1f))
        }
        Text(
            "Em celulares com sensor lento, o app avisa e usa o modo reduzido.",
            fontSize = 12.sp,
            color = Palette.Muted,
        )
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(44.dp)) { Text("Testar de novo") }
    }
}

/** Celular na horizontal, girando junto com o ângulo medido. */
@Composable
private fun PhoneIllustration(degrees: Float) {
    Canvas(
        Modifier
            .size(width = 220.dp, height = 130.dp)
            .semantics { contentDescription = "Celular na horizontal, girando como um volante" },
    ) {
        rotate(degrees) {
            val w = size.width * 0.72f
            val h = size.height * 0.62f
            val topLeft = Offset((size.width - w) / 2f, (size.height - h) / 2f)
            drawRoundRect(Palette.Surface, topLeft, Size(w, h), CornerRadius(14.dp.toPx()))
            drawRoundRect(Palette.Text, topLeft, Size(w, h), CornerRadius(14.dp.toPx()), style = Stroke(2.5.dp.toPx()))
            drawLine(
                Palette.Amber,
                Offset(size.width / 2f, topLeft.y + 10.dp.toPx()),
                Offset(size.width / 2f, topLeft.y + h - 10.dp.toPx()),
                strokeWidth = 2.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f)),
            )
            drawCircle(Palette.Amber, radius = 6.dp.toPx(), center = center)
        }
    }
}

private fun formatDegrees(degrees: Float): String {
    val tenths = (degrees * 10).roundToInt()
    val sign = if (tenths < 0) "−" else ""
    return "$sign${abs(tenths) / 10},${abs(tenths) % 10}°"
}
