package app.virtualwheelracing.desktop.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.virtualwheelracing.desktop.DesktopContainer
import app.virtualwheelracing.desktop.setup.SetupResult
import app.virtualwheelracing.domain.LinkHealth
import app.virtualwheelracing.domain.OpenResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class Section(val label: String) { PAINEL("Painel"), CONECTAR("Conectar celular"), VERIFICACAO("Verificação do PC") }

/** Estado da verificação do PC, recalculado depois de cada correção. */
private data class Checks(
    val driverOk: Boolean,
    val deviceOk: Boolean,
    val axesOk: Boolean,
    val firewallOk: Boolean,
    val portOk: Boolean,
) {
    val allOk get() = driverOk && deviceOk && axesOk && firewallOk && portOk
}

private fun DesktopContainer.checks(): Checks {
    val driver = driverStatus
    return Checks(
        driverOk = driver.driverLoaded && driver.driverEnabled,
        deviceOk = openResult == OpenResult.Ok,
        axesOk = driver.hasSteeringAxis && driver.hasThrottleAxis && driver.hasBrakeAxis && driver.buttonCount >= 16,
        firewallOk = setup.firewallRuleExists(),
        portOk = startError == null,
    )
}

@Composable
fun MainScreen(container: DesktopContainer) {
    var checks by remember { mutableStateOf(container.checks()) }
    var section by remember { mutableStateOf(if (checks.allOk) Section.CONECTAR else Section.VERIFICACAO) }
    var snapshot by remember { mutableStateOf(container.receiver.snapshot()) }

    LaunchedEffect(Unit) {
        while (true) {
            snapshot = container.receiver.snapshot()
            delay(66)
        }
    }

    MaterialTheme(colorScheme = Palette.scheme) {
        Surface(color = Palette.Background, modifier = Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxSize()) {
                Sidebar(section, checks.allOk, snapshot.connected, onSelect = { section = it })
                Box(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(horizontal = 48.dp, vertical = 40.dp)) {
                    Column(Modifier.widthIn(max = 1000.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        when (section) {
                            Section.PAINEL -> PainelSection(container, snapshot)
                            Section.CONECTAR -> ConectarSection(container)
                            Section.VERIFICACAO -> VerificacaoSection(container, checks, onChecksChanged = { checks = it })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Sidebar(selected: Section, checksOk: Boolean, connected: Boolean, onSelect: (Section) -> Unit) {
    Column(
        modifier = Modifier.width(232.dp).fillMaxHeight().background(Palette.Sidebar).padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(start = 8.dp, bottom = 18.dp)) {
            Canvas(Modifier.size(30.dp)) { drawLogo() }
            Text("Virtual Wheel Racing", fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Palette.Text)
        }
        Section.entries.forEach { item ->
            val active = item == selected
            val badge = when (item) {
                Section.VERIFICACAO -> if (checksOk) null else "!"
                Section.PAINEL -> if (connected) "●" else null
                else -> null
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(if (active) Palette.Surface2 else Palette.Sidebar, RoundedCornerShape(10.dp))
                    .clickable(role = Role.Tab) { onSelect(item) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    item.label,
                    color = if (active) Palette.Text else Palette.Muted,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
                badge?.let {
                    Text(it, color = if (item == Section.VERIFICACAO) Palette.Amber else Palette.Throttle, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PainelSection(container: DesktopContainer, snapshot: app.virtualwheelracing.desktop.receiver.ReceiverSnapshot) {
    PageTitle("Painel")
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Panel("Celular", Modifier.weight(1f)) {
            val (text, fg, bg) = when {
                !snapshot.connected && snapshot.peer != null -> Triple("Pareado (${snapshot.peer}), aguardando dados", Palette.Amber, Palette.WarnBackground)
                !snapshot.connected -> Triple("Aguardando o celular", Palette.Muted, Palette.Surface2)
                snapshot.health == LinkHealth.OK -> Triple("Conectado a ${snapshot.peer ?: "celular"}", Palette.Throttle, Palette.OkBackground)
                snapshot.health == LinkHealth.WARNING -> Triple("Sinal instável", Palette.Amber, Palette.WarnBackground)
                else -> Triple("Sem sinal · failsafe ativo", Palette.Brake, Palette.ErrorBackground)
            }
            Pill(text, fg, bg)
            Label("Abra o Virtual Wheel Racing no celular e toque em Começar a dirigir.")
        }
        Panel("Volante virtual", Modifier.weight(1f)) {
            Text(
                if (container.openResult == OpenResult.Ok) "vJoy Device · ativo" else "vJoy indisponível",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (container.openResult == OpenResult.Ok) Palette.Text else Palette.Brake,
            )
            Label("Eixo X direção · Y acelerador · Z freio · 16 botões")
            Label("Configure os controles nas opções de cada jogo.")
        }
    }
    Panel("Entradas ao vivo", Modifier.fillMaxWidth()) {
        AxisBar("Direção", snapshot.steering, centered = true, color = Palette.Amber)
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(Modifier.weight(1f)) { AxisBar("Acelerador", snapshot.throttle, centered = false, color = Palette.Throttle) }
            Box(Modifier.weight(1f)) { AxisBar("Freio", snapshot.brake, centered = false, color = Palette.Brake) }
        }
        val pressed = (0 until 16).filter { snapshot.buttons and (1 shl it) != 0 }.map { it + 1 }
        Label("Botões pressionados: ${if (pressed.isEmpty()) "nenhum" else pressed.joinToString(", ")}")
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

@Composable
private fun ConectarSection(container: DesktopContainer) {
    var payload by remember { mutableStateOf(container.pairingPayload()) }
    PageTitle("Conectar um celular", "No app Virtual Wheel Racing do Android, toque em Ler código QR e aponte a câmera para este código.")
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Panel("Código QR", Modifier.weight(1f)) {
            val current = payload
            if (current == null) {
                Text("Nenhuma rede local encontrada. Conecte o PC ao Wi-Fi ou ao cabo de rede.", color = Palette.Brake)
            } else {
                QrCode(current.encode(), Modifier.size(280.dp))
            }
        }
        Panel("Este PC", Modifier.weight(1f)) {
            Label("Nome")
            Text(container.pcName, fontFamily = Fonts.Display, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
            Label("Endereços na rede local")
            payload?.hosts?.forEach { Text(it, fontSize = 18.sp, color = Palette.Text) }
            Label("Porta ${container.port} · celular e PC na mesma rede Wi-Fi")
            Spacer(Modifier.height(8.dp))
            Label("O celular lembra deste PC. Gerar outro código desconecta os celulares pareados.")
            OutlinedButton(onClick = {
                container.newPairingCode()
                payload = container.pairingPayload()
            }) { Text("Gerar novo código") }
        }
    }
}

@Composable
private fun VerificacaoSection(container: DesktopContainer, checks: Checks, onChecksChanged: (Checks) -> Unit) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    fun runFix(label: String, action: () -> SetupResult) {
        busy = label
        message = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                val r = action()
                container.refreshDriver()
                r
            }
            message = when (result) {
                SetupResult.Done -> "$label: concluído."
                SetupResult.Cancelled -> "$label: cancelado no aviso do Windows."
                is SetupResult.Failed -> "$label: ${result.reason}"
            }
            onChecksChanged(withContext(Dispatchers.IO) { container.checks() })
            busy = null
        }
    }

    PageTitle("Verificação do PC", "Tudo precisa estar OK para o volante virtual funcionar nos jogos.")
    Panel("Itens", Modifier.fillMaxWidth()) {
        CheckRow(
            "Driver vJoy instalado",
            checks.driverOk,
            fix = if (container.setup.bundledVJoyInstaller != null) "Instalar vJoy" else null,
            busy = busy,
            onFix = { runFix("Instalar vJoy") { container.setup.installVJoy() } },
        )
        CheckRow(
            "Dispositivo 1 com eixos X, Y, Z e 16 botões",
            checks.driverOk && checks.axesOk,
            fix = if (checks.driverOk) "Configurar automaticamente" else null,
            busy = busy,
            onFix = { runFix("Configurar o vJoy") { container.setup.configureVJoyDevice() } },
        )
        CheckRow(
            "Volante virtual pronto para o Virtual Wheel Racing",
            checks.deviceOk,
            fix = if (checks.driverOk && checks.axesOk) "Tentar de novo" else null,
            busy = busy,
            onFix = { runFix("Reabrir o volante virtual") { SetupResult.Done } },
        )
        CheckRow(
            "Firewall liberado para a rede privada",
            checks.firewallOk,
            fix = "Liberar no firewall",
            busy = busy,
            onFix = { runFix("Liberar no firewall") { container.setup.addFirewallRule() } },
        )
        CheckRow(container.startError ?: "Recebendo na porta ${container.port}", checks.portOk, fix = null, busy = busy, onFix = {})
        Label("As correções pedem permissão de administrador pelo aviso do Windows.")
        busy?.let { Text("$it… confirme no aviso do Windows.", color = Palette.Amber, fontSize = 14.sp) }
        message?.let { Text(it, color = Palette.Muted, fontSize = 14.sp) }
    }
}

@Composable
private fun CheckRow(text: String, ok: Boolean, fix: String?, busy: String?, onFix: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(if (ok) "OK" else "!", color = if (ok) Palette.Throttle else Palette.Amber, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
        Text(text, fontSize = 15.sp, color = Palette.Text, modifier = Modifier.weight(1f))
        if (!ok && fix != null) {
            Button(onClick = onFix, enabled = busy == null) { Text(fix) }
        }
    }
}

private fun ms(value: Double): String = if (value <= 0.0) "—" else "%.1f ms".format(value)
