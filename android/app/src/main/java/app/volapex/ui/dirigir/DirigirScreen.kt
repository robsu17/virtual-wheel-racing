package app.volapex.ui.dirigir

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.volapex.AppContainer
import app.volapex.domain.AxisAssignment
import app.volapex.domain.GlobalSettings
import app.volapex.engine.DrivingSession
import app.volapex.engine.EngineSnapshot
import app.volapex.engine.LinkState
import app.volapex.engine.TouchControls
import app.volapex.protocol.PairingPayload
import app.volapex.ui.components.IconSquareButton
import app.volapex.ui.components.LandscapeWhileVisible
import app.volapex.ui.components.StatusChip
import app.volapex.ui.components.drawBack
import app.volapex.ui.theme.Fonts
import app.volapex.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun DirigirScreen(container: AppContainer, pairing: PairingPayload, settings: GlobalSettings, onBack: () -> Unit) {
    var controls by remember { mutableStateOf<TouchControls?>(null) }
    var session by remember { mutableStateOf<DrivingSession?>(null) }
    var snapshot by remember { mutableStateOf<EngineSnapshot?>(null) }
    var assignment by remember { mutableStateOf<AxisAssignment?>(null) }
    var everConnected by remember { mutableStateOf(false) }

    LandscapeWhileVisible()
    BackHandler(onBack = onBack)

    // A sessão vive enquanto a tela está visível: ao sair do app, o PC recebe BYE e solta tudo.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> if (session == null) {
                    val newControls = container.newControls()
                    controls = newControls
                    session = container.newSession(pairing, newControls).also { it.start() }
                }
                Lifecycle.Event.ON_STOP -> {
                    session?.stop()
                    session = null
                    assignment = null
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            session?.stop()
            session = null
        }
    }

    LaunchedEffect(session) {
        while (true) {
            val s = session?.snapshot()
            snapshot = s
            if (s?.linkState == LinkState.CONNECTED) everConnected = true
            delay(66)
        }
    }

    Box(Modifier.fillMaxSize().background(Palette.Background)) {
        Row(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val brake: @Composable () -> Unit = { Pedal("FREIO", Palette.Brake) { controls?.brakePosition = it } }
            val throttle: @Composable () -> Unit = { Pedal("ACELERADOR", Palette.Throttle) { controls?.throttlePosition = it } }
            if (settings.brakeOnLeft) brake() else throttle()
            CenterPanel(
                pcName = pairing.pcName,
                snapshot = snapshot,
                controls = controls,
                assignment = assignment,
                onAssignment = { axis ->
                    assignment = if (assignment == axis) null else axis
                    session?.setAssignment(assignment)
                },
                onRecenter = { session?.recenter() },
                onBack = onBack,
            )
            if (settings.brakeOnLeft) throttle() else brake()
        }

        if (snapshot?.linkState == LinkState.SEARCHING) {
            SearchingOverlay(pcName = pairing.pcName, lost = everConnected, error = snapshot?.error, onBack = onBack)
        }
    }
}

/** Pedal por posição do polegar: embaixo = solto, em cima = no fundo. Solta ao tirar o dedo. */
@Composable
private fun Pedal(label: String, color: Color, onPosition: (Float) -> Unit) {
    val level = remember { mutableFloatStateOf(0f) }
    Column(
        modifier = Modifier
            .width(128.dp)
            .fillMaxHeight()
            .background(Palette.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(20.dp))
            .semantics { contentDescription = "Pedal $label" }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pointerId = down.id
                    fun update(y: Float) {
                        val value = (1f - y / size.height).coerceIn(0f, 1f)
                        level.floatValue = value
                        onPosition(value)
                    }
                    update(down.position.y)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                        if (!change.pressed) break
                        update(change.position.y)
                        change.consume()
                    }
                    level.floatValue = 0f
                    onPosition(0f)
                }
            }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, color = Palette.Muted, fontFamily = Fonts.Display, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        Box(
            Modifier
                .weight(1f)
                .width(72.dp)
                .drawBehind {
                    val radius = CornerRadius(16.dp.toPx())
                    drawRoundRect(Palette.Surface2, cornerRadius = radius)
                    val h = size.height * level.floatValue
                    drawRoundRect(color, topLeft = Offset(0f, size.height - h), size = Size(size.width, h), cornerRadius = radius)
                },
        )
        Text("${(level.floatValue * 100).roundToInt()}%", color = Palette.Text, fontFamily = Fonts.Display, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RowScope.CenterPanel(
    pcName: String,
    snapshot: EngineSnapshot?,
    controls: TouchControls?,
    assignment: AxisAssignment?,
    onAssignment: (AxisAssignment) -> Unit,
    onRecenter: () -> Unit,
    onBack: () -> Unit,
) {
    var page by remember { mutableIntStateOf(0) }
    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconSquareButton("Voltar ao início", onBack) { drawBack(it) }
            Column(Modifier.weight(1f)) {
                Text("Conectado a", fontSize = 11.sp, color = Palette.Muted)
                Text(pcName, fontFamily = Fonts.Display, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Palette.Text, maxLines = 1)
            }
            if (snapshot?.linkState == LinkState.CONNECTED) {
                StatusChip("Conectado · ${snapshot.sensorRateHz} Hz", Palette.Throttle, Palette.OkBackground)
            } else {
                StatusChip("Procurando", Palette.Amber, Palette.WarnBackground)
            }
            Button(onClick = onRecenter, modifier = Modifier.height(44.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Recentrar", fontWeight = FontWeight.SemiBold)
            }
        }

        SteeringGauge(snapshot?.steering ?: 0f, snapshot?.phoneDegrees ?: 0f, Modifier.weight(1f).fillMaxWidth())
        if (snapshot?.tilted == true) {
            Text("Celular inclinado demais: endireite para voltar a esterçar", color = Palette.Amber, fontSize = 12.sp)
        }

        for (row in 0 until 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (column in 0 until 4) {
                    GameButton(page * 8 + row * 4 + column, controls)
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (page == 0) "Botões 1 a 8 · ver 9 a 16" else "Botões 9 a 16 · ver 1 a 8",
                color = Palette.Muted,
                fontSize = 12.sp,
                modifier = Modifier
                    .weight(1f)
                    .clickable(role = Role.Button) { page = 1 - page }
                    .padding(vertical = 10.dp),
            )
            Text("Atribuir:", color = Palette.Muted, fontSize = 12.sp)
            AssignmentChip("Direção", AxisAssignment.STEERING, assignment, onAssignment)
            AssignmentChip("Acelerador", AxisAssignment.THROTTLE, assignment, onAssignment)
            AssignmentChip("Freio", AxisAssignment.BRAKE, assignment, onAssignment)
        }
    }
}

/** Arco de 180°: o ponto mostra a direção enviada ao jogo; o número, o giro do celular. */
@Composable
private fun SteeringGauge(steering: Float, phoneDegrees: Float, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.BottomCenter) {
        Canvas(
            Modifier
                .fillMaxSize()
                .semantics { contentDescription = "Direção em ${(steering * 100).roundToInt()} por cento" },
        ) {
            val stroke = 14.dp.toPx()
            val radius = minOf(size.width / 2f, size.height) - stroke
            val center = Offset(size.width / 2f, size.height - stroke / 2f)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)
            drawArc(Palette.Line, 180f, 180f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(Palette.Amber, -90f, steering * 90f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            val angle = Math.toRadians((-90.0 + steering * 90.0))
            val knob = Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())
            drawCircle(Palette.Background, radius = 13.dp.toPx(), center = knob)
            drawCircle(Palette.Text, radius = 10.dp.toPx(), center = knob)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 6.dp)) {
            Text(formatDegrees(phoneDegrees), fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 44.sp, color = Palette.Text)
            Text(
                "giro do celular · direção ${(steering * 100).roundToInt()}%",
                fontSize = 12.sp,
                color = Palette.Muted,
            )
        }
    }
}

@Composable
private fun RowScope.GameButton(index: Int, controls: TouchControls?) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .background(if (pressed) Palette.AmberDark else Palette.Surface, RoundedCornerShape(12.dp))
            .border(1.dp, if (pressed) Palette.Amber else Palette.Line, RoundedCornerShape(12.dp))
            .semantics { role = Role.Button }
            .pointerInput(index) {
                detectTapGestures(onPress = {
                    pressed = true
                    controls?.setButton(index, true)
                    tryAwaitRelease()
                    controls?.setButton(index, false)
                    pressed = false
                })
            },
        contentAlignment = Alignment.Center,
    ) {
        Text("Botão ${index + 1}", color = if (pressed) Palette.Amber else Palette.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AssignmentChip(label: String, axis: AxisAssignment, selected: AxisAssignment?, onSelect: (AxisAssignment) -> Unit) {
    val active = selected == axis
    Box(
        modifier = Modifier
            .height(40.dp)
            .background(if (active) Palette.Amber else Palette.Surface, RoundedCornerShape(10.dp))
            .border(1.dp, if (active) Palette.Amber else Palette.Line, RoundedCornerShape(10.dp))
            .clickable(role = Role.Button) { onSelect(axis) }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (active) Palette.OnAmber else Palette.Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Cobre a tela enquanto não há sessão: no início ("procurando") ou depois de cair ("conexão perdida"). */
@Composable
private fun SearchingOverlay(pcName: String, lost: Boolean, error: String?, onBack: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Palette.Background.copy(alpha = 0.92f))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(440.dp)
                .background(Palette.Surface, RoundedCornerShape(20.dp))
                .border(1.dp, if (lost) Color(0xFF5A2621) else Palette.Line, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                if (lost) "Conexão perdida" else "Procurando $pcName…",
                fontFamily = Fonts.Display,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Palette.Text,
                textAlign = TextAlign.Center,
            )
            Text(
                if (lost) {
                    "Os pedais foram soltos e a direção está voltando ao centro. Tentando reconectar…"
                } else {
                    "Confira se o Volapex está aberto no Windows e se os dois estão na mesma rede Wi-Fi."
                },
                fontSize = 14.sp,
                color = Palette.Muted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )
            error?.let { Text(it, fontSize = 12.sp, color = Palette.Brake, textAlign = TextAlign.Center) }
            OutlinedButton(onClick = onBack, modifier = Modifier.height(44.dp)) { Text("Voltar ao início") }
        }
    }
}

private fun formatDegrees(degrees: Float): String {
    val rounded = degrees.roundToInt()
    return (if (rounded < 0) "−" else "") + abs(rounded) + "°"
}
