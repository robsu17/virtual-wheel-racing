package app.prwheel.ui.dirigir

import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.prwheel.AppContainer
import app.prwheel.Connection
import app.prwheel.domain.AxisAssignment
import app.prwheel.engine.DrivingSession
import app.prwheel.engine.EngineSnapshot
import app.prwheel.engine.TouchControls
import app.prwheel.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DirigirScreen(container: AppContainer, connection: Connection, onBack: () -> Unit) {
    val controls = remember { container.newControls() }
    var session by remember { mutableStateOf<DrivingSession?>(null) }
    var snapshot by remember { mutableStateOf<EngineSnapshot?>(null) }
    var assignment by remember { mutableStateOf<AxisAssignment?>(null) }

    LandscapeWhileVisible()
    BackHandler(onBack = onBack)

    // A sessão vive enquanto a tela está visível: sai do app, o PC recebe BYE e solta tudo.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> if (session == null) {
                    session = container.newSession(connection, controls).also { it.start() }
                }
                Lifecycle.Event.ON_STOP -> {
                    session?.stop()
                    session = null
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
            snapshot = session?.snapshot()
            delay(66)
        }
    }

    val brakeOnLeft = container.settings.brakeOnLeft
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Background)
            .safeDrawingPadding()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (brakeOnLeft) BrakePedal(controls) else ThrottlePedal(controls)
        CenterPanel(
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
        if (brakeOnLeft) ThrottlePedal(controls) else BrakePedal(controls)
    }
}

@Composable
private fun LandscapeWhileVisible() {
    val activity = LocalActivity.current
    DisposableEffect(activity) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
private fun BrakePedal(controls: TouchControls) =
    Pedal("FREIO", Palette.Brake) { controls.brakePosition = it }

@Composable
private fun ThrottlePedal(controls: TouchControls) =
    Pedal("ACELERADOR", Palette.Throttle) { controls.throttlePosition = it }

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
        Text(label, color = Palette.Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
        Text("${(level.floatValue * 100).roundToInt()}%", color = Palette.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RowScope.CenterPanel(
    snapshot: EngineSnapshot?,
    controls: TouchControls,
    assignment: AxisAssignment?,
    onAssignment: (AxisAssignment) -> Unit,
    onRecenter: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text("Voltar") }
            Text(statusText(snapshot), color = Palette.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Button(onClick = onRecenter) { Text("Recentrar", fontWeight = FontWeight.SemiBold) }
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val degrees = snapshot?.phoneDegrees ?: 0f
            Text(formatDegrees(degrees), color = Palette.Text, fontSize = 44.sp, fontWeight = FontWeight.Bold)
            Text(
                "giro do celular · direção ${((snapshot?.steering ?: 0f) * 100).roundToInt()}%",
                color = Palette.Muted,
                fontSize = 12.sp,
            )
            SteeringBar(snapshot?.steering ?: 0f)
            snapshot?.error?.let { Text(it, color = Palette.Brake, fontSize = 12.sp) }
            if (snapshot?.tilted == true) {
                Text("Celular inclinado demais: endireite para voltar a esterçar", color = Palette.Amber, fontSize = 12.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0 until 4) GameButton(i, controls)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 4 until 8) GameButton(i, controls)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Atribuir no jogo:", color = Palette.Muted, fontSize = 12.sp)
            AssignmentChip("Direção", AxisAssignment.STEERING, assignment, onAssignment)
            AssignmentChip("Acelerador", AxisAssignment.THROTTLE, assignment, onAssignment)
            AssignmentChip("Freio", AxisAssignment.BRAKE, assignment, onAssignment)
        }
    }
}

@Composable
private fun SteeringBar(steering: Float) {
    Box(
        Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(0.8f)
            .height(10.dp)
            .drawBehind {
                drawRoundRect(Palette.Surface2, cornerRadius = CornerRadius(5.dp.toPx()))
                val mid = size.width / 2f
                val w = mid * abs(steering)
                val start = if (steering >= 0f) mid else mid - w
                drawRect(Palette.Amber, topLeft = Offset(start, 0f), size = Size(w, size.height))
                drawRect(Palette.Muted, topLeft = Offset(mid - 1f, 0f), size = Size(2f, size.height))
            },
    )
}

@Composable
private fun RowScope.GameButton(index: Int, controls: TouchControls) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .background(if (pressed) Palette.Surface2 else Palette.Surface, RoundedCornerShape(12.dp))
            .border(1.dp, if (pressed) Palette.Amber else Palette.Line, RoundedCornerShape(12.dp))
            .semantics { role = Role.Button }
            .pointerInput(index) {
                detectTapGestures(onPress = {
                    pressed = true
                    controls.setButton(index, true)
                    tryAwaitRelease()
                    controls.setButton(index, false)
                    pressed = false
                })
            },
        contentAlignment = Alignment.Center,
    ) {
        Text("Botão ${index + 1}", color = Palette.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AssignmentChip(
    label: String,
    axis: AxisAssignment,
    selected: AxisAssignment?,
    onSelect: (AxisAssignment) -> Unit,
) {
    val active = selected == axis
    if (active) {
        Button(onClick = { onSelect(axis) }) { Text(label, fontSize = 12.sp) }
    } else {
        OutlinedButton(onClick = { onSelect(axis) }) { Text(label, fontSize = 12.sp) }
    }
}

private fun statusText(snapshot: EngineSnapshot?): String =
    if (snapshot == null) "Iniciando…" else "${snapshot.sensorRateHz} Hz · ${snapshot.packetsSent} pacotes"

private fun formatDegrees(degrees: Float): String {
    val rounded = degrees.roundToInt()
    return (if (rounded < 0) "−" else "") + abs(rounded) + "°"
}
