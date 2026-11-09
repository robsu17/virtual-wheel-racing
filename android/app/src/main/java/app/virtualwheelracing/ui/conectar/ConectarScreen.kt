package app.virtualwheelracing.ui.conectar

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.virtualwheelracing.protocol.PairingPayload
import app.virtualwheelracing.ui.components.ScreenHeader
import app.virtualwheelracing.ui.components.VwrCard
import app.virtualwheelracing.ui.theme.Fonts
import app.virtualwheelracing.ui.theme.Palette
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/** Pareamento: lê o QR mostrado pelo Virtual Wheel Racing no Windows. Sem [onBack], é a primeira tela do app. */
@Composable
fun ConectarScreen(onPaired: (PairingPayload) -> Unit, onBack: (() -> Unit)?) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (onBack != null) {
            ScreenHeader("Conectar ao PC", onBack = onBack)
        } else {
            Text("Virtual Wheel Racing", fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Palette.Text)
            Text("Conectar ao PC", fontFamily = Fonts.Display, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Palette.Text)
        }
        Text(
            "Abra o Virtual Wheel Racing no Windows e aponte a câmera para o código QR que aparece na tela.",
            fontSize = 15.sp,
            color = Palette.Muted,
            lineHeight = 21.sp,
        )

        ViewfinderIllustration()

        Button(
            onClick = {
                scanPairingCode(
                    context,
                    onPaired = {
                        error = null
                        onPaired(it)
                    },
                    onError = { error = it },
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text("Ler código QR", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        error?.let { Text(it, color = Palette.Brake, fontSize = 14.sp) }

        VwrCard {
            Text(
                "Celular e PC precisam estar na mesma rede Wi-Fi, de preferência 5 GHz. O celular lembra do PC; " +
                    "só é preciso ler o código de novo se você trocar de PC ou gerar outro código.",
                fontSize = 13.sp,
                color = Palette.Muted,
                lineHeight = 19.sp,
            )
        }
    }
}

/** Moldura de visor, como no design; a câmera de verdade abre no leitor do Google. */
@Composable
private fun ViewfinderIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(androidx.compose.ui.graphics.Color(0xFF07080A), RoundedCornerShape(20.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(20.dp))
            .semantics { contentDescription = "Moldura para o código QR" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(150.dp)) {
            val w = size.width
            val arm = w * 0.22f
            val stroke = 4.dp.toPx()
            val corners = listOf(Offset(0f, 0f) to Offset(1f, 1f), Offset(w, 0f) to Offset(-1f, 1f), Offset(0f, w) to Offset(1f, -1f), Offset(w, w) to Offset(-1f, -1f))
            corners.forEach { (p, d) ->
                drawLine(Palette.Accent, p, Offset(p.x + arm * d.x, p.y), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(Palette.Accent, p, Offset(p.x, p.y + arm * d.y), strokeWidth = stroke, cap = StrokeCap.Round)
            }
            drawLine(Palette.Accent, Offset(w * 0.1f, w * 0.46f), Offset(w * 0.9f, w * 0.46f), strokeWidth = 2.dp.toPx())
        }
        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)) {
            Text("Toque em Ler código QR", fontSize = 13.sp, color = Palette.Muted)
        }
    }
}

/**
 * Lê o QR com o leitor do Google Play Services: não pede permissão de câmera ao app.
 * Na primeira vez, o Android pode baixar o módulo do leitor.
 */
fun scanPairingCode(context: Context, onPaired: (PairingPayload) -> Unit, onError: (String) -> Unit) {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    GmsBarcodeScanning.getClient(context, options)
        .startScan()
        .addOnSuccessListener { barcode ->
            val pairing = barcode.rawValue?.let(PairingPayload::parse)
            if (pairing != null) onPaired(pairing) else onError("Esse código QR não é do Virtual Wheel Racing.")
        }
        .addOnFailureListener { e -> onError("Não foi possível ler o código: ${e.message}") }
}
