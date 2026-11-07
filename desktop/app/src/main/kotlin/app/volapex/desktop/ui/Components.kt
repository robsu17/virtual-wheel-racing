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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun Panel(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .background(Palette.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(20.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, fontFamily = Fonts.Display, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
        content()
    }
}

@Composable
fun PageTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontFamily = Fonts.Display, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Palette.Text)
        subtitle?.let { Text(it, fontSize = 15.sp, color = Palette.Muted) }
    }
}

@Composable
fun Label(text: String) {
    Text(text, color = Palette.Muted, fontSize = 13.sp)
}

@Composable
fun Pill(text: String, foreground: Color, background: Color) {
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
fun AxisBar(label: String, value: Float, centered: Boolean, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text(label, fontWeight = FontWeight.SemiBold, color = Palette.Text, modifier = Modifier.weight(1f))
            Text("${(value * 100).roundToInt()}%", color = Palette.Text)
        }
        Canvas(Modifier.fillMaxWidth().height(12.dp)) {
            val radius = CornerRadius(6.dp.toPx())
            drawRoundRect(Palette.Surface2, cornerRadius = radius)
            if (centered) {
                val mid = size.width / 2f
                val width = mid * abs(value)
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
fun RowScope.StatTile(label: String, value: String) {
    Column(
        modifier = Modifier.weight(1f).background(Palette.Surface2, RoundedCornerShape(12.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = Palette.Muted, fontSize = 12.sp)
        Text(value, fontFamily = Fonts.Display, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Palette.Text)
    }
}

/** Logo do Volapex (opção A): volante cujos raios formam um V, com o centro em branco. */
fun DrawScope.drawLogo() {
    val u = size.width / 100f
    val s = Stroke(width = 9f * u, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
    drawCircle(Palette.Amber, radius = 38f * u, center = Offset(50f * u, 50f * u), style = s)
    val v = androidx.compose.ui.graphics.Path().apply {
        moveTo(19f * u, 33f * u)
        lineTo(50f * u, 68f * u)
        lineTo(81f * u, 33f * u)
    }
    drawPath(v, Palette.Amber, style = s)
    drawCircle(Palette.Text, radius = 7f * u, center = Offset(50f * u, 68f * u))
}
