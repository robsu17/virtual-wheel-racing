package app.virtualwheelracing.desktop.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
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

private enum class Section {
    PAINEL, CONECTAR, VERIFICACAO;

    val label: String
        get() = when (this) {
            PAINEL -> Strings.current.sectionDashboard
            CONECTAR -> Strings.current.sectionConnect
            VERIFICACAO -> Strings.current.sectionCheck
        }
}

private val t get() = Strings.current

/** Estado da verificação do PC, recalculado depois de cada correção. */
internal data class Checks(
    val driverOk: Boolean,
    val deviceOk: Boolean,
    val axesOk: Boolean,
    val firewallOk: Boolean,
    val portOk: Boolean,
) {
    val allOk get() = driverOk && deviceOk && axesOk && firewallOk && portOk
}

internal fun DesktopContainer.checks(): Checks {
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
    var section by remember { mutableStateOf(if (checks.allOk) Section.PAINEL else Section.VERIFICACAO) }
    var technical by remember { mutableStateOf(false) }
    var snapshot by remember { mutableStateOf(container.receiver.snapshot()) }

    LaunchedEffect(Unit) {
        while (true) {
            snapshot = container.receiver.snapshot()
            delay(66)
        }
    }

    MaterialTheme(colorScheme = Palette.scheme) {
        Surface(color = Palette.Background, modifier = Modifier.fillMaxSize()) {
            if (!technical) {
                SimpleScreen(container, checks, snapshot, onChecksChanged = { checks = it }, onTechnical = { technical = true })
                return@Surface
            }
            Row(Modifier.fillMaxSize()) {
                Sidebar(section, checks.allOk, snapshot.connected, onSelect = { section = it }, onSimple = { technical = false })
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
private fun Sidebar(selected: Section, checksOk: Boolean, connected: Boolean, onSelect: (Section) -> Unit, onSimple: () -> Unit) {
    val logo = remember { Fonts::class.java.getResourceAsStream("/icon.png")?.use { BitmapPainter(loadImageBitmap(it)) } }
    Column(
        modifier = Modifier.width(232.dp).fillMaxHeight().background(Palette.Sidebar).padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(start = 8.dp, bottom = 18.dp)) {
            logo?.let { Image(it, contentDescription = null, modifier = Modifier.size(36.dp)) }
            Text("Virtual Wheel Racing", fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 21.sp, color = Palette.Text)
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
                    Text(it, color = if (item == Section.VERIFICACAO) Palette.Warning else Palette.Throttle, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onSimple, modifier = Modifier.fillMaxWidth()) { Text(t.backToSimple) }
    }
}

@Composable
private fun PainelSection(container: DesktopContainer, snapshot: app.virtualwheelracing.desktop.receiver.ReceiverSnapshot) {
    PageTitle(t.sectionDashboard)
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Panel(t.phonePanel, Modifier.weight(1f)) {
            val (text, fg, bg) = when {
                !snapshot.connected && snapshot.peer != null -> Triple(t.pairedWaiting(snapshot.peer.toString()), Palette.Warning, Palette.WarnBackground)
                !snapshot.connected -> Triple(t.waitingForPhone, Palette.Muted, Palette.Surface2)
                snapshot.health == LinkHealth.OK -> Triple(t.connectedTo(snapshot.peer), Palette.Throttle, Palette.OkBackground)
                snapshot.health == LinkHealth.WARNING -> Triple(t.unstableSignal, Palette.Warning, Palette.WarnBackground)
                else -> Triple(t.noSignal, Palette.Brake, Palette.ErrorBackground)
            }
            Pill(text, fg, bg)
            Label(t.dashboardPhoneHint)
        }
        Panel(t.virtualWheel, Modifier.weight(1f)) {
            Text(
                if (container.openResult == OpenResult.Ok) t.vjoyActive else t.vjoyUnavailable,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (container.openResult == OpenResult.Ok) Palette.Text else Palette.Brake,
            )
            Label(t.axesSummary)
            Label(t.configureInGames)
        }
    }
    Panel(t.liveInputs, Modifier.fillMaxWidth()) {
        AxisBar(t.axisSteering, snapshot.steering, centered = true, color = Palette.Accent)
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(Modifier.weight(1f)) { AxisBar(t.axisThrottle, snapshot.throttle, centered = false, color = Palette.Throttle) }
            Box(Modifier.weight(1f)) { AxisBar(t.axisBrake, snapshot.brake, centered = false, color = Palette.Brake) }
        }
        val pressed = (0 until 16).filter { snapshot.buttons and (1 shl it) != 0 }.map { it + 1 }
        Label(t.buttonsPressed(if (pressed.isEmpty()) null else pressed.joinToString(", ")))
    }
    Panel(t.packetGaps, Modifier.fillMaxWidth()) {
        val s = snapshot.stats
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(t.packetsPerSecond, "${s.packetsPerSecond}")
            StatTile(t.typical, ms(s.gapP50Ms))
            StatTile("p95", ms(s.gapP95Ms))
            StatTile("p99", ms(s.gapP99Ms))
            StatTile(t.biggestGap, ms(s.maxGapMs))
            StatTile(t.lost, "${s.lost}")
            StatTile(t.rejected, "${s.rejected}")
        }
        Label(t.gapsNote)
    }
}

@Composable
private fun ConectarSection(container: DesktopContainer) {
    var payload by remember { mutableStateOf(container.pairingPayload()) }
    PageTitle(t.connectTitle, t.connectSubtitle)
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Panel(t.qrCode, Modifier.weight(1f)) {
            val current = payload
            if (current == null) {
                Text(t.noNetwork, color = Palette.Brake)
            } else {
                QrCode(current.encode(), Modifier.size(280.dp))
            }
        }
        Panel(t.thisPc, Modifier.weight(1f)) {
            Label(t.name)
            Text(container.pcName, fontFamily = Fonts.Display, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
            Label(t.localAddresses)
            payload?.hosts?.forEach { Text(it, fontSize = 18.sp, color = Palette.Text) }
            Label(t.portNote(container.port))
            Spacer(Modifier.height(8.dp))
            Label(t.rememberNote)
            OutlinedButton(onClick = {
                container.newPairingCode()
                payload = container.pairingPayload()
            }) { Text(t.newCode) }
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
                SetupResult.Done -> t.fixDone(label)
                SetupResult.Cancelled -> t.fixCancelled(label)
                is SetupResult.Failed -> "$label: ${result.reason}"
            }
            onChecksChanged(withContext(Dispatchers.IO) { container.checks() })
            busy = null
        }
    }

    PageTitle(t.checkTitle, t.checkSubtitle)
    Panel(t.items, Modifier.fillMaxWidth()) {
        CheckRow(
            t.checkDriver,
            checks.driverOk,
            fix = if (container.setup.bundledVJoyInstaller != null) t.fixInstallVJoy else null,
            busy = busy,
            onFix = { runFix(t.fixInstallVJoy) { container.setup.installVJoy() } },
        )
        CheckRow(
            t.checkAxes,
            checks.driverOk && checks.axesOk,
            fix = if (checks.driverOk) t.fixConfigure else null,
            busy = busy,
            onFix = { runFix(t.labelConfigure) { container.setup.configureVJoyDevice() } },
        )
        CheckRow(
            t.checkDevice,
            checks.deviceOk,
            fix = if (checks.driverOk && checks.axesOk) t.fixRetry else null,
            busy = busy,
            onFix = { runFix(t.labelReopen) { SetupResult.Done } },
        )
        CheckRow(
            t.checkFirewall,
            checks.firewallOk,
            fix = t.fixFirewall,
            busy = busy,
            onFix = { runFix(t.fixFirewall) { container.setup.addFirewallRule() } },
        )
        CheckRow(container.startError ?: t.receivingOnPort(container.port), checks.portOk, fix = null, busy = busy, onFix = {})
        Label(t.adminNote)
        busy?.let { Text(t.busyConfirm(it), color = Palette.Warning, fontSize = 14.sp) }
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
        Text(if (ok) "OK" else "!", color = if (ok) Palette.Throttle else Palette.Warning, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
        Text(text, fontSize = 15.sp, color = Palette.Text, modifier = Modifier.weight(1f))
        if (!ok && fix != null) {
            Button(onClick = onFix, enabled = busy == null) { Text(fix) }
        }
    }
}

private fun ms(value: Double): String = if (value <= 0.0) "—" else "%.1f ms".format(value)
