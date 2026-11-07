package app.volapex.desktop.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.volapex.desktop.DesktopContainer
import app.volapex.domain.LinkHealth
import app.volapex.domain.OpenResult
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun PainelScreen(container: DesktopContainer) {
    var snapshot by remember { mutableStateOf(container.receiver.snapshot()) }
    var payload by remember { mutableStateOf(container.pairingPayload()) }

    LaunchedEffect(Unit) {
        while (true) {
            snapshot = container.receiver.snapshot()
            delay(66)
        }
    }

    MaterialTheme(colorScheme = Palette.scheme) {
        Surface(color = Palette.Background, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text("Volapex", fontSize = 30.sp, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Panel("Conectar um celular", Modifier.weight(1f)) {
                        val current = payload
                        if (current == null) {
                            Text("Nenhuma rede local encontrada. Conecte o PC ao Wi-Fi ou ao cabo de rede.", color = Palette.Brake)
                        } else {
                            Label("No app Volapex do Android, toque em Ler código QR e aponte a câmera para cá.")
                            QrCode(current.encode(), Modifier.size(260.dp))
                            Label("${container.pcName} · ${current.hosts.joinToString(", ")} · porta ${container.port}")
                        }
                        Label("O celular lembra deste PC. Gerar outro código desconecta os celulares pareados.")
                        OutlinedButton(onClick = {
                            container.newPairingCode()
                            payload = container.pairingPayload()
                        }) { Text("Gerar novo código") }
                    }
                    Panel("Verificação do PC", Modifier.weight(1f)) {
                        val driver = container.driverStatus
                        CheckLine("Driver vJoy carregado", driver.driverLoaded && driver.driverEnabled)
                        CheckLine("Volante virtual: ${openText(container.openResult)}", container.openResult == OpenResult.Ok)
                        CheckLine("Eixos X, Y e Z habilitados", driver.hasSteeringAxis && driver.hasThrottleAxis && driver.hasBrakeAxis)
                        CheckLine("${driver.buttonCount} botões disponíveis", driver.buttonCount >= 8)
                        val error = container.startError
                        CheckLine(error ?: "Recebendo na porta ${container.port}", error == null)
                        Label("Se o Windows perguntar, permita o acesso na rede privada.")
                    }
                }

                Panel("Conexão", Modifier.fillMaxWidth()) {
                    StatusPill(snapshot.connected, snapshot.health, snapshot.peer)
                    AxisBar("Direção", snapshot.steering, centered = true, color = Palette.Amber)
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Box(Modifier.weight(1f)) { AxisBar("Acelerador", snapshot.throttle, centered = false, color = Palette.Throttle) }
                        Box(Modifier.weight(1f)) { AxisBar("Freio", snapshot.brake, centered = false, color = Palette.Brake) }
                    }
                    Label("Botões pressionados: ${pressedButtons(snapshot.buttons)}")
                }

                Panel("Intervalo entre pacotes", Modifier.fillMaxWidth()) {
                    val s = snapshot.stats
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatTile("Pacotes/s", "${s.packetsPerSecond}")
                        StatTile("Típico (p50)", ms(s.gapP50Ms))
                        StatTile("p95", ms(s.gapP95Ms))
                        StatTile("p99", ms(s.gapP99Ms))
                        StatTile("Maior falha", ms(s.maxGapMs))
                        StatTile("Perdidos", "${s.lost}")
                        StatTile("Rejeitados", "${s.rejected}")
                    }
                    Label("Mede a regularidade da chegada, não o atraso total do celular até o jogo.")
                }
            }
        }
    }
}

@Composable
private fun Panel(title: String, modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .background(Palette.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(20.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun Label(text: String) {
    Text(text, color = Palette.Muted, fontSize = 13.sp)
}

@Composable
private fun CheckLine(text: String, ok: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (ok) "OK" else "!", color = if (ok) Palette.Throttle else Palette.Brake, fontWeight = FontWeight.Bold)
        Text(text, fontSize = 14.sp)
    }
}

@Composable
private fun StatusPill(connected: Boolean, health: LinkHealth, peer: String?) {
    val (text, foreground, background) = when {
        !connected && peer != null -> Triple("Celular $peer pareado, aguardando dados", Palette.Amber, Palette.WarnBackground)
        !connected -> Triple("Aguardando o celular", Palette.Muted, Palette.Surface2)
        health == LinkHealth.OK -> Triple("Conectado a ${peer ?: "celular"}", Palette.Throttle, Palette.OkBackground)
        health == LinkHealth.WARNING -> Triple("Sinal instável", Palette.Amber, Palette.WarnBackground)
        else -> Triple("Sem sinal · failsafe ativo", Palette.Brake, Palette.ErrorBackground)
    }
    Row(
        modifier = Modifier.background(background, RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(8.dp).background(foreground, CircleShape))
        Text(text, color = foreground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AxisBar(label: String, value: Float, centered: Boolean, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("${(value * 100).roundToInt()}%")
        }
        Canvas(Modifier.fillMaxWidth().height(12.dp)) {
            val radius = CornerRadius(6.dp.toPx())
            drawRoundRect(Palette.Surface2, cornerRadius = radius)
            if (centered) {
                val mid = size.width / 2f
                val width = mid * kotlin.math.abs(value)
                val start = if (value >= 0f) mid else mid - width
                drawRect(color, topLeft = Offset(start, 0f), size = Size(width, size.height))
                drawRect(Palette.Muted, topLeft = Offset(mid - 1f, 0f), size = Size(2f, size.height))
            } else {
                drawRoundRect(color, size = Size(size.width * value, size.height), cornerRadius = radius)
            }
        }
    }
}

@Composable
private fun RowScope.StatTile(label: String, value: String) {
    Column(
        modifier = Modifier.weight(1f).background(Palette.Surface2, RoundedCornerShape(12.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = Palette.Muted, fontSize = 12.sp)
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

private fun ms(value: Double): String = if (value <= 0.0) "—" else "%.1f ms".format(value)

private fun pressedButtons(bits: Int): String {
    val pressed = (0 until 16).filter { bits and (1 shl it) != 0 }.map { it + 1 }
    return if (pressed.isEmpty()) "nenhum" else pressed.joinToString(", ")
}

private fun openText(result: OpenResult): String = when (result) {
    OpenResult.Ok -> "pronto (dispositivo 1)"
    OpenResult.DriverMissing -> "vJoy não instalado"
    OpenResult.DriverDisabled -> "vJoy desativado"
    OpenResult.DeviceMissing -> "dispositivo 1 não existe"
    OpenResult.DeviceBusy -> "ocupado por outro programa"
    is OpenResult.Failed -> result.reason
}
