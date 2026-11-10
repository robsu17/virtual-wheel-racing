package app.virtualwheelracing.ui.dirigir

import app.virtualwheelracing.engine.SessionError
import app.virtualwheelracing.R
import androidx.compose.ui.res.stringResource
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
import androidx.compose.ui.draw.clip
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
import app.virtualwheelracing.AppContainer
import app.virtualwheelracing.domain.AxisAssignment
import app.virtualwheelracing.domain.GlobalSettings
import app.virtualwheelracing.engine.DrivingSession
import app.virtualwheelracing.engine.EngineSnapshot
import app.virtualwheelracing.engine.LinkState
import app.virtualwheelracing.engine.TouchControls
import app.virtualwheelracing.protocol.PairingPayload
import app.virtualwheelracing.ui.components.IconSquareButton
import app.virtualwheelracing.ui.components.LandscapeWhileVisible
import app.virtualwheelracing.ui.components.StatusChip
import app.virtualwheelracing.ui.components.drawBack
import app.virtualwheelracing.ui.theme.Fonts
import app.virtualwheelracing.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun DirigirScreen(container: AppContainer, pairing: PairingPayload, settings: GlobalSettings, setupMode: Boolean = false, onBack: () -> Unit) {
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
            val brake: @Composable () -> Unit = { Pedal(stringResource(R.string.pedal_brake), Palette.Brake, PedalIcon.BRAKE) { controls?.brakePosition = it } }
            val throttle: @Composable () -> Unit = { Pedal(stringResource(R.string.pedal_throttle), Palette.Throttle, PedalIcon.THROTTLE) { controls?.throttlePosition = it } }
            if (settings.brakeOnLeft) brake() else throttle()
            CenterPanel(
                snapshot = snapshot,
                controls = controls,
                setupMode = setupMode,
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

private enum class PedalIcon { BRAKE, THROTTLE }

/** Pedal por posição do polegar: embaixo = solto, em cima = no fundo. Solta ao tirar o dedo. */
@Composable
private fun Pedal(label: String, color: Color, icon: PedalIcon, onPosition: (Float) -> Unit) {
    val level = remember { mutableFloatStateOf(0f) }
    val shape = RoundedCornerShape(26.dp)
    val description = stringResource(R.string.pedal_description, label)
    Column(
        modifier = Modifier
            .width(150.dp)
            .fillMaxHeight()
            .clip(shape)
            .background(Palette.Surface)
            .border(1.dp, Palette.Line, shape)
            .drawBehind {
                val h = size.height * level.floatValue
                drawRect(color.copy(alpha = 0.35f), topLeft = Offset(0f, size.height - h), size = Size(size.width, h))
            }
            .semantics { contentDescription = description }
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
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Canvas(Modifier.size(40.dp)) {
            val w = size.width
            val s = Stroke(w / 12f, cap = StrokeCap.Round)
            if (icon == PedalIcon.BRAKE) {
                drawRoundRect(color, topLeft = Offset(w * 0.25f, w * 0.17f), size = Size(w * 0.5f, w * 0.66f), cornerRadius = CornerRadius(w * 0.12f), style = s)
                drawLine(color, Offset(w * 0.38f, w * 0.38f), Offset(w * 0.62f, w * 0.38f), s.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.38f, w * 0.54f), Offset(w * 0.62f, w * 0.54f), s.width, StrokeCap.Round)
            } else {
                drawRoundRect(color, topLeft = Offset(w * 0.33f, w * 0.12f), size = Size(w * 0.34f, w * 0.76f), cornerRadius = CornerRadius(w * 0.12f), style = s)
                for (y in listOf(0.3f, 0.46f, 0.62f)) {
                    drawLine(color, Offset(w * 0.5f, w * y), Offset(w * 0.5f, w * (y + 0.08f)), s.width, StrokeCap.Round)
                }
            }
        }
        Text(label, color = Palette.Text, fontFamily = Fonts.Display, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun RowScope.CenterPanel(
    snapshot: EngineSnapshot?,
    controls: TouchControls?,
    setupMode: Boolean,
    assignment: AxisAssignment?,
    onAssignment: (AxisAssignment) -> Unit,
    onRecenter: () -> Unit,
    onBack: () -> Unit,
) {
    var page by remember { mutableIntStateOf(0) }
    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IconSquareButton(stringResource(R.string.back_home), onBack) { drawBack(it) }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (snapshot?.linkState == LinkState.CONNECTED) {
                    StatusChip(stringResource(R.string.connected), Palette.Throttle, Palette.OkBackground)
                } else {
                    StatusChip(stringResource(R.string.searching_pc), Palette.Warning, Palette.WarnBackground)
                }
            }
            Button(onClick = onRecenter, modifier = Modifier.height(44.dp), shape = RoundedCornerShape(12.dp)) {
                Text(stringResource(R.string.recenter), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }

        if (setupMode) {
            SetupPanel(assignment, onAssignment, Modifier.weight(1f).fillMaxWidth())
        } else {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                SteeringGauge(snapshot?.steering ?: 0f, snapshot?.phoneDegrees ?: 0f, Modifier.fillMaxSize())
                if (snapshot?.tilted == true) {
                    Text(
                        stringResource(R.string.straighten_phone),
                        color = Palette.Warning,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .background(Palette.WarnBackground, RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }

        val moreButtons = stringResource(R.string.more_buttons, page + 1, PAGES)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            for (column in 0 until BUTTONS_PER_PAGE) {
                GameButton(page * BUTTONS_PER_PAGE + column, controls)
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Palette.Surface2, RoundedCornerShape(16.dp))
                    .clickable(role = Role.Button) { page = (page + 1) % PAGES }
                    .semantics { contentDescription = moreButtons },
                contentAlignment = Alignment.Center,
            ) {
                Text("${page + 1}/$PAGES", color = Palette.Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** Arco de 180°: o ponto mostra a direção enviada ao jogo; o número, o giro do celular. */
@Composable
private fun SteeringGauge(steering: Float, phoneDegrees: Float, modifier: Modifier) {
    val percent = (steering * 100).roundToInt()
    val description = stringResource(R.string.gauge_description, percent)
    Box(modifier, contentAlignment = Alignment.BottomCenter) {
        Canvas(
            Modifier
                .fillMaxSize()
                .semantics { contentDescription = description },
        ) {
            val stroke = 14.dp.toPx()
            val radius = minOf(size.width / 2f, size.height) - stroke
            val center = Offset(size.width / 2f, size.height - stroke / 2f)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)
            drawArc(Palette.Line, 180f, 180f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(Palette.Accent, -90f, steering * 90f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            val angle = Math.toRadians((-90.0 + steering * 90.0))
            val knob = Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())
            drawCircle(Palette.Background, radius = 13.dp.toPx(), center = knob)
            drawCircle(Palette.Text, radius = 10.dp.toPx(), center = knob)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 6.dp)) {
            Text(formatDegrees(phoneDegrees), fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 44.sp, color = Palette.Text)
            Text(
                stringResource(R.string.gauge_caption, percent),
                fontSize = 12.sp,
                color = Palette.Muted,
            )
        }
    }
}

private fun formatDegrees(degrees: Float): String {
    val rounded = degrees.roundToInt()
    return (if (rounded < 0) "−" else "") + abs(rounded) + "°"
}

private const val BUTTONS_PER_PAGE = 4
private const val PAGES = 16 / BUTTONS_PER_PAGE

/** Modo de configuração: manda um comando de cada vez, para o jogo reconhecer o certo. */
@Composable
private fun SetupPanel(assignment: AxisAssignment?, onAssignment: (AxisAssignment) -> Unit, modifier: Modifier) {
    Column(
        modifier
            .background(Palette.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.setting_up_in_game), fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Palette.Text)
        Text(
            stringResource(R.string.setting_up_body),
            fontSize = 13.sp,
            color = Palette.Muted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssignmentChip(stringResource(R.string.axis_steering), AxisAssignment.STEERING, assignment, onAssignment)
            AssignmentChip(stringResource(R.string.axis_throttle), AxisAssignment.THROTTLE, assignment, onAssignment)
            AssignmentChip(stringResource(R.string.axis_brake), AxisAssignment.BRAKE, assignment, onAssignment)
        }
        Text(
            if (assignment == null) stringResource(R.string.assign_none) else stringResource(R.string.assign_one),
            fontSize = 12.sp,
            color = if (assignment == null) Palette.Muted else Palette.AccentText,
        )
    }
}

@Composable
private fun RowScope.GameButton(index: Int, controls: TouchControls?) {
    var pressed by remember { mutableStateOf(false) }
    val letter = 'A' + index
    val description = stringResource(R.string.button_description, letter.toString())
    Box(
        modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .background(if (pressed) Palette.AccentDark else Palette.Surface, RoundedCornerShape(16.dp))
            .border(1.dp, if (pressed) Palette.Accent else Palette.Line, RoundedCornerShape(16.dp))
            .semantics {
                role = Role.Button
                contentDescription = description
            }
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
        Text("$letter", color = if (pressed) Palette.AccentText else Palette.Text, fontFamily = Fonts.Display, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AssignmentChip(label: String, axis: AxisAssignment, selected: AxisAssignment?, onSelect: (AxisAssignment) -> Unit) {
    val active = selected == axis
    Box(
        modifier = Modifier
            .height(48.dp)
            .background(if (active) Palette.Accent else Palette.Surface2, RoundedCornerShape(12.dp))
            .border(1.dp, if (active) Palette.Accent else Palette.Line, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button) { onSelect(axis) }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (active) Palette.OnAccent else Palette.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Cobre a tela enquanto não há sessão: no início ("procurando") ou depois de cair ("conexão perdida"). */
@Composable
private fun SearchingOverlay(pcName: String, lost: Boolean, error: SessionError?, onBack: () -> Unit) {
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
                if (lost) stringResource(R.string.reconnecting) else stringResource(R.string.searching_named, pcName),
                fontFamily = Fonts.Display,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Palette.Text,
                textAlign = TextAlign.Center,
            )
            Text(
                if (lost) {
                    stringResource(R.string.lost_body)
                } else {
                    stringResource(R.string.searching_body)
                },
                fontSize = 14.sp,
                color = Palette.Muted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )
            error?.let { Text(errorText(it), fontSize = 12.sp, color = Palette.Brake, textAlign = TextAlign.Center) }
            OutlinedButton(onClick = onBack, modifier = Modifier.height(44.dp)) { Text(stringResource(R.string.back_home)) }
        }
    }
}

@Composable
private fun errorText(error: SessionError): String {
    val detail = error.detail.orEmpty()
    return when (error.kind) {
        SessionError.Kind.OPEN_FAILED -> stringResource(R.string.error_open_failed, detail)
        SessionError.Kind.NO_SENSOR -> stringResource(R.string.error_no_sensor)
        SessionError.Kind.SEARCH_FAILED -> stringResource(R.string.error_search_failed, detail)
        SessionError.Kind.SEND_FAILED -> stringResource(R.string.error_send_failed, detail)
    }
}
