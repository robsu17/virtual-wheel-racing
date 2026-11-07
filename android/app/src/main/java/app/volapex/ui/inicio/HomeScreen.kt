package app.volapex.ui.inicio

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.volapex.domain.GlobalSettings
import app.volapex.domain.SensorStats
import app.volapex.protocol.PairingPayload
import app.volapex.ui.components.IconSquareButton
import app.volapex.ui.components.VolapexCard
import app.volapex.ui.components.drawChevronRight
import app.volapex.ui.components.drawMonitor
import app.volapex.ui.components.drawSliders
import app.volapex.ui.components.drawWheel
import app.volapex.ui.theme.Fonts
import app.volapex.ui.theme.Palette
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    pairing: PairingPayload,
    settings: GlobalSettings,
    sensorStats: SensorStats?,
    onPairing: () -> Unit,
    onSettings: () -> Unit,
    onCalibration: () -> Unit,
    onDrive: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Background)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Canvas(Modifier.size(32.dp)) { drawWheel(Palette.Amber) }
            Text(
                "Volapex",
                fontFamily = Fonts.Display,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Palette.Text,
                modifier = Modifier.weight(1f),
            )
            IconSquareButton("Ajustes do volante", onSettings) { drawSliders(it) }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            VolapexCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(44.dp).background(Palette.Surface2, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(Modifier.size(22.dp)) { drawMonitor(Palette.Text) }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("PC pareado", fontSize = 12.sp, color = Palette.Muted)
                        Text(pairing.pcName, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
                    }
                }
                Text(pairing.hosts.joinToString(" · "), fontSize = 13.sp, color = Palette.Muted)
                OutlinedButton(onClick = onPairing, modifier = Modifier.fillMaxWidth().height(44.dp)) {
                    Text("Trocar de PC")
                }
            }

            LinkCard(
                title = "Sensores",
                subtitle = sensorStats?.let {
                    "${it.rateHz} Hz · modo ${if (it.fullMode) "completo" else "reduzido"}"
                } ?: "Ainda não testados",
                action = "Calibrar",
                onClick = onCalibration,
            )

            LinkCard(
                title = "Ajustes do volante",
                subtitle = "Giro de ${settings.arcDegrees.roundToInt()}° · zona morta ${"%.1f".format(settings.deadzoneDegrees)}° · " +
                    "freio à ${if (settings.brakeOnLeft) "esquerda" else "direita"}",
                action = null,
                onClick = onSettings,
            )

            VolapexCard {
                Text("Configurar em um jogo", fontFamily = Fonts.Display, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Palette.Text)
                Text(
                    "Nas opções de controle do jogo, escolha o vJoy Device. Na tela de direção, use os botões " +
                        "Atribuir para mexer um eixo por vez enquanto o jogo pede cada controle.",
                    fontSize = 13.sp,
                    color = Palette.Muted,
                    lineHeight = 19.sp,
                )
                Text("Os botões da tela aparecem no jogo como Botão 1 a 16.", fontSize = 12.sp, color = Palette.Muted)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onDrive, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Começar a dirigir", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("Gire o celular para a horizontal", fontSize = 13.sp, color = Palette.Muted)
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun LinkCard(title: String, subtitle: String, action: String?, onClick: () -> Unit) {
    VolapexCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
                Text(subtitle, fontSize = 13.sp, color = Palette.Muted)
            }
            if (action != null) {
                Text(action, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Palette.Amber)
            } else {
                Canvas(Modifier.size(18.dp)) { drawChevronRight(Palette.Muted) }
            }
        }
    }
}
