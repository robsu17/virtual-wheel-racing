package app.virtualwheelracing.ui.boasvindas

import androidx.compose.ui.res.stringResource
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.virtualwheelracing.R
import app.virtualwheelracing.protocol.PairingPayload
import app.virtualwheelracing.ui.conectar.scanPairingCode
import app.virtualwheelracing.ui.theme.Fonts
import app.virtualwheelracing.ui.theme.Palette

const val DOWNLOAD_URL = "https://github.com/robsu17/virtual-wheel-racing/releases"

/** Primeira tela de quem nunca usou o app: explica em três passos e já abre o leitor do código. */
@Composable
fun WelcomeScreen(onPaired: (PairingPayload) -> Unit) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    val titleStart = stringResource(R.string.welcome_title_start)
    val titleHighlight = stringResource(R.string.welcome_title_highlight)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Image(painterResource(R.drawable.logo_tile), contentDescription = stringResource(R.string.logo_description), modifier = Modifier.size(104.dp))
            Text(
                buildAnnotatedString {
                    append(titleStart)
                    withStyle(SpanStyle(color = Palette.AccentText)) { append(titleHighlight) }
                },
                fontFamily = Fonts.Display,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                lineHeight = 36.sp,
                color = Palette.Text,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.welcome_subtitle),
                fontSize = 16.sp,
                lineHeight = 23.sp,
                color = Palette.Muted,
                textAlign = TextAlign.Center,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Step(1, stringResource(R.string.welcome_step1_title), stringResource(R.string.welcome_step1_subtitle))
            Step(2, stringResource(R.string.welcome_step2_title), stringResource(R.string.welcome_step2_subtitle))
            Step(3, stringResource(R.string.welcome_step3_title), stringResource(R.string.welcome_step3_subtitle))
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { scanPairingCode(context, onPaired = { error = null; onPaired(it) }, onError = { error = it }) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(stringResource(R.string.scan_pc_code), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        error?.let { Text(it, color = Palette.Brake, fontSize = 14.sp) }
        TextButton(
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DOWNLOAD_URL))) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(stringResource(R.string.not_installed_on_pc), color = Palette.AccentText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Step(number: Int, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Palette.Surface, RoundedCornerShape(18.dp)).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(36.dp).background(Palette.Accent, CircleShape), contentAlignment = Alignment.Center) {
            Text("$number", color = Palette.OnAccent, fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Column {
            Text(title, color = Palette.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Palette.Muted, fontSize = 13.sp)
        }
    }
}
