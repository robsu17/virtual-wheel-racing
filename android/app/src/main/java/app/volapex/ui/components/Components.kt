package app.volapex.ui.components

import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.volapex.ui.theme.Fonts
import app.volapex.ui.theme.Palette

/** Cartão padrão do design: fundo escuro, borda fina e cantos de 18 dp. Clicável se [onClick] existir. */
@Composable
fun VolapexCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    var m = modifier
        .fillMaxWidth()
        .background(if (highlighted) Palette.AmberDark else Palette.Surface, shape)
        .border(1.dp, if (highlighted) Palette.Amber else Palette.Line, shape)
    if (onClick != null) m = m.clickable(role = Role.Button, onClick = onClick)
    Column(m.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

/** Botão quadrado de 44 dp só com ícone. */
@Composable
fun IconSquareButton(description: String, onClick: () -> Unit, icon: DrawScope.(Color) -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Palette.Surface, RoundedCornerShape(12.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(20.dp)) { icon(Palette.Text) }
    }
}

/** Título de tela com botão de voltar. */
@Composable
fun ScreenHeader(title: String, subtitle: String? = null, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        IconSquareButton("Voltar", onBack) { drawBack(it) }
        Column {
            Text(title, fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Palette.Text)
            subtitle?.let { Text(it, fontSize = 12.sp, color = Palette.Muted) }
        }
    }
}

/** Seletor de opções lado a lado (Linear | Suave | Firme). */
@Composable
fun <T> Segmented(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Palette.Surface2, RoundedCornerShape(12.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(if (active) Palette.Amber else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable(role = Role.Tab) { onSelect(value) }
                    .semantics { this.selected = active },
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = if (active) Palette.OnAmber else Palette.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun StatusChip(text: String, foreground: Color, background: Color) {
    Row(
        modifier = Modifier.background(background, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(7.dp).background(foreground, RoundedCornerShape(50)))
        Text(text, color = foreground, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Valor grande com rótulo pequeno, como nos cartões de estatística do design. */
@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.background(Palette.Surface2, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(label, fontSize = 12.sp, color = Palette.Muted)
        Text(value, fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Palette.Text)
    }
}

/** Gira a tela para a horizontal e mantém a tela ligada enquanto a composição estiver visível. */
@Composable
fun LandscapeWhileVisible() {
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

// Ícones de traço, desenhados à mão como no design (sem biblioteca de ícones).

private fun DrawScope.stroke(width: Dp = 2.dp) =
    Stroke(width = width.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

fun DrawScope.drawBack(color: Color) {
    val w = size.width
    val path = Path().apply {
        moveTo(w * 0.62f, w * 0.25f)
        lineTo(w * 0.38f, w * 0.5f)
        lineTo(w * 0.62f, w * 0.75f)
    }
    drawPath(path, color, style = stroke())
}

fun DrawScope.drawChevronRight(color: Color) {
    val w = size.width
    val path = Path().apply {
        moveTo(w * 0.38f, w * 0.25f)
        lineTo(w * 0.62f, w * 0.5f)
        lineTo(w * 0.38f, w * 0.75f)
    }
    drawPath(path, color, style = stroke())
}

fun DrawScope.drawSliders(color: Color) {
    val w = size.width
    val s = stroke()
    for ((row, knob) in listOf(0.25f to 0.65f, 0.5f to 0.4f, 0.75f to 0.75f)) {
        drawLine(color, Offset(w * 0.15f, w * row), Offset(w * 0.85f, w * row), strokeWidth = s.width, cap = StrokeCap.Round)
        drawCircle(Palette.Surface, radius = w * 0.1f, center = Offset(w * knob, w * row))
        drawCircle(color, radius = w * 0.1f, center = Offset(w * knob, w * row), style = s)
    }
}

/** Logo do Volapex (opção A): volante cujos raios formam um V, com o centro em branco. */
fun DrawScope.drawLogo() {
    val u = size.width / 100f
    val s = Stroke(width = 9f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawCircle(Palette.Amber, radius = 38f * u, center = Offset(50f * u, 50f * u), style = s)
    val v = Path().apply {
        moveTo(19f * u, 33f * u)
        lineTo(50f * u, 68f * u)
        lineTo(81f * u, 33f * u)
    }
    drawPath(v, Palette.Amber, style = s)
    drawCircle(Palette.Text, radius = 7f * u, center = Offset(50f * u, 68f * u))
}

fun DrawScope.drawMonitor(color: Color) {
    val w = size.width
    val s = stroke()
    drawRoundRect(
        color,
        topLeft = Offset(w * 0.12f, w * 0.18f),
        size = androidx.compose.ui.geometry.Size(w * 0.76f, w * 0.5f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f),
        style = s,
    )
    drawLine(color, Offset(w * 0.5f, w * 0.68f), Offset(w * 0.5f, w * 0.82f), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.33f, w * 0.84f), Offset(w * 0.67f, w * 0.84f), strokeWidth = s.width, cap = StrokeCap.Round)
}
