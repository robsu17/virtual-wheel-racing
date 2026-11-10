package app.virtualwheelracing.desktop.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.virtualwheelracing.desktop.DesktopContainer
import app.virtualwheelracing.desktop.receiver.ReceiverSnapshot
import app.virtualwheelracing.desktop.setup.SetupResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI

private const val DOWNLOAD_URL = "https://github.com/robsu17/virtual-wheel-racing/releases"

private enum class StepState { DONE, CURRENT, NEXT }

/** Tela de quem só quer jogar: três etapas, um botão para preparar o PC e o código QR grande. */
@Composable
internal fun SimpleScreen(
    container: DesktopContainer,
    checks: Checks,
    snapshot: ReceiverSnapshot,
    onChecksChanged: (Checks) -> Unit,
    onTechnical: () -> Unit,
) {
    val logo = remember { Fonts::class.java.getResourceAsStream("/icon.png")?.use { BitmapPainter(loadImageBitmap(it)) } }
    val pcReady = checks.allOk
    val connected = snapshot.connected
    val t = Strings.current

    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 32.dp), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 1040.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                logo?.let { Image(it, contentDescription = null, modifier = Modifier.size(44.dp)) }
                Text(
                    buildAnnotatedString {
                        append(t.appNamePrefix)
                        withStyle(SpanStyle(color = Palette.AccentText)) { append("Racing") }
                    },
                    fontFamily = Fonts.Display,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = Palette.Text,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onTechnical) {
                    Text(t.technicalDetails, color = Palette.Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Step(1, t.stepPcReady, if (pcReady) t.stepPcReadyDone else t.stepPcReadyTodo, if (pcReady) StepState.DONE else StepState.CURRENT)
                Step(
                    2,
                    t.stepConnect,
                    if (connected) t.stepConnected else if (pcReady) t.stepNow else t.stepLater,
                    when {
                        connected -> StepState.DONE
                        pcReady -> StepState.CURRENT
                        else -> StepState.NEXT
                    },
                )
                Step(3, t.stepPlay, t.stepPlaySubtitle, if (connected && pcReady) StepState.CURRENT else StepState.NEXT)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                BigCard(Modifier.weight(1.3f)) {
                    when {
                        !pcReady -> PreparePc(container, checks, onChecksChanged, onTechnical)
                        connected -> Connected(snapshot.peer)
                        else -> ShowQr(container)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SideCard(t.noAppTitle) {
                        Text(
                            t.noAppBody,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = Palette.Muted,
                        )
                        OutlinedButton(onClick = { openBrowser(DOWNLOAD_URL) }, shape = RoundedCornerShape(12.dp), modifier = Modifier.height(44.dp)) {
                            Text(t.howToDownload, color = Palette.Text, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    SideCard(t.notConnectingTitle) {
                        t.notConnectingTips.forEach { Text("•  $it", fontSize = 15.sp, lineHeight = 22.sp, color = Palette.Muted) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.PreparePc(container: DesktopContainer, checks: Checks, onChecksChanged: (Checks) -> Unit, onTechnical: () -> Unit) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val t = Strings.current

    Text(t.prepareTitle, fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Palette.Text, textAlign = TextAlign.Center)
    Text(
        t.prepareBody,
        fontSize = 16.sp,
        lineHeight = 23.sp,
        color = Palette.Muted,
        textAlign = TextAlign.Center,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        TaskLine(t.taskVJoy, checks.driverOk && checks.axesOk && checks.deviceOk)
        TaskLine(t.taskFirewall, checks.firewallOk)
        if (!checks.portOk) TaskLine(t.taskPort, false)
    }
    Button(
        onClick = {
            message = null
            scope.launch {
                message = withContext(Dispatchers.IO) { preparePc(container) { busy = it } }
                onChecksChanged(withContext(Dispatchers.IO) { container.checks() })
                busy = null
            }
        },
        enabled = busy == null,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(60.dp).widthIn(min = 280.dp),
    ) {
        Text(busy ?: t.prepareButton, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
    if (busy != null) Text(t.confirmWindowsPrompt, color = Palette.Warning, fontSize = 14.sp)
    message?.let {
        Text(it, color = Palette.Brake, fontSize = 14.sp, textAlign = TextAlign.Center)
        TextButton(onClick = onTechnical) { Text(t.seeTechnicalDetails, color = Palette.AccentText) }
    }
}

/** Faz só o que falta, na ordem certa. Devolve uma mensagem se algo não deu certo. */
private fun preparePc(container: DesktopContainer, onStep: (String) -> Unit): String? {
    val t = Strings.current
    fun fail(result: SetupResult, what: String): String? = when (result) {
        SetupResult.Done -> null
        SetupResult.Cancelled -> t.cancelled(what)
        is SetupResult.Failed -> t.failed(what, result.reason)
    }

    var checks = container.checks()
    if (!checks.driverOk) {
        onStep(t.installing)
        fail(container.setup.installVJoy(), t.whatInstallVJoy)?.let { return it }
        container.refreshDriver()
        checks = container.checks()
        if (!checks.driverOk) return t.vjoyNeedsRestart
    }
    if (!checks.axesOk) {
        onStep(t.configuring)
        fail(container.setup.configureVJoyDevice(), t.whatConfigureVJoy)?.let { return it }
        container.refreshDriver()
        checks = container.checks()
    }
    if (!checks.deviceOk) {
        container.refreshDriver()
        checks = container.checks()
        if (!checks.deviceOk) return t.vjoyInUse
    }
    if (!checks.firewallOk) {
        onStep(t.openingFirewall)
        fail(container.setup.addFirewallRule(), t.whatFirewall)?.let { return it }
    }
    if (!checks.portOk) return container.startError
    return null
}

@Composable
private fun ColumnScope.ShowQr(container: DesktopContainer) {
    val payload = remember { container.pairingPayload() }
    val t = Strings.current
    Text(t.pointPhoneHere, fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Palette.Text, textAlign = TextAlign.Center)
    Text(
        buildAnnotatedString {
            append(t.openAppAndTapStart)
            withStyle(SpanStyle(color = Palette.Text, fontWeight = FontWeight.SemiBold)) { append(t.scanButtonName) }
            append(".")
        },
        fontSize = 16.sp,
        lineHeight = 23.sp,
        color = Palette.Muted,
        textAlign = TextAlign.Center,
    )
    if (payload == null) {
        Text(t.noNetworkSimple, color = Palette.Brake, fontSize = 15.sp, textAlign = TextAlign.Center)
    } else {
        QrCode(payload.encode(), Modifier.size(280.dp))
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(10.dp).background(Palette.Warning, CircleShape))
        Text(t.waitingPhone, fontSize = 15.sp, color = Palette.Muted)
    }
    Text(t.pairedBefore, fontSize = 13.sp, color = Palette.Muted)
}

@Composable
private fun ColumnScope.Connected(peer: String?) {
    val t = Strings.current
    Box(Modifier.size(88.dp).background(Palette.Throttle, CircleShape), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(44.dp)) { drawCheck(Palette.Background) }
    }
    Text(t.connectedTitle, fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 40.sp, color = Palette.Text)
    Text(t.openYourGame, fontSize = 18.sp, color = Palette.Muted)
    peer?.let { Text(t.phone(it), fontSize = 13.sp, color = Palette.Muted) }
    Text(
        t.firstTimeHint,
        fontSize = 14.sp,
        color = Palette.Muted,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Step(number: Int, title: String, subtitle: String, state: StepState) {
    val (bg, border) = when (state) {
        StepState.DONE -> Palette.OkBackground to Palette.OkBackground
        StepState.CURRENT -> Color(0xFF241415) to Palette.Accent
        StepState.NEXT -> Palette.Surface to Palette.Surface
    }
    Row(
        modifier = Modifier
            .weight(1f)
            .background(bg, RoundedCornerShape(18.dp))
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val circle = when (state) {
            StepState.DONE -> Palette.Throttle
            StepState.CURRENT -> Palette.Accent
            StepState.NEXT -> Palette.Surface2
        }
        Box(Modifier.size(32.dp).background(circle, CircleShape), contentAlignment = Alignment.Center) {
            if (state == StepState.DONE) {
                Canvas(Modifier.size(18.dp)) { drawCheck(Palette.Background) }
            } else {
                Text(
                    "$number",
                    fontFamily = Fonts.Display,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (state == StepState.CURRENT) Palette.OnAccent else Palette.Muted,
                )
            }
        }
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
            Text(subtitle, fontSize = 13.sp, color = if (state == StepState.DONE) Color(0xFFCFF5E3) else Palette.Muted)
        }
    }
}

@Composable
private fun TaskLine(text: String, ok: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(22.dp).background(if (ok) Palette.Throttle else Palette.Surface2, CircleShape), contentAlignment = Alignment.Center) {
            if (ok) Canvas(Modifier.size(13.dp)) { drawCheck(Palette.Background) }
        }
        Text(text, fontSize = 15.sp, color = if (ok) Palette.Text else Palette.Muted)
    }
}

@Composable
private fun BigCard(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .background(Palette.Surface, RoundedCornerShape(28.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(28.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
        content = content,
    )
}

@Composable
private fun SideCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Palette.Surface, RoundedCornerShape(24.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(24.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Palette.Text)
        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCheck(color: Color) {
    val w = size.width
    val path = Path().apply {
        moveTo(w * 0.2f, w * 0.52f)
        lineTo(w * 0.4f, w * 0.72f)
        lineTo(w * 0.8f, w * 0.3f)
    }
    drawPath(path, color, style = Stroke(width = w / 7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun openBrowser(url: String) {
    runCatching {
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(url))
        else ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start()
    }
}
