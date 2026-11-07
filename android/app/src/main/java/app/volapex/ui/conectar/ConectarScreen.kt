package app.volapex.ui.conectar

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.volapex.protocol.PairingPayload
import app.volapex.ui.theme.Palette
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
fun ConectarScreen(saved: PairingPayload?, onConnect: (PairingPayload) -> Unit) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    val scan = {
        scanPairingCode(
            context,
            onPaired = {
                error = null
                onConnect(it)
            },
            onError = { error = it },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Volapex", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Palette.Text)
        Text("Conectar ao PC", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)

        if (saved != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Palette.Surface, RoundedCornerShape(18.dp))
                    .border(1.dp, Palette.Line, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("PC pareado", color = Palette.Muted, fontSize = 12.sp)
                Text(saved.pcName, color = Palette.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(saved.hosts.joinToString(", "), color = Palette.Muted, fontSize = 13.sp)
            }
            Button(onClick = { onConnect(saved) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Conectar a ${saved.pcName}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(onClick = scan, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Ler código QR de outro PC")
            }
        } else {
            Text(
                "Abra o Volapex no Windows e leia o código QR que aparece na tela.",
                color = Palette.Muted,
                fontSize = 15.sp,
            )
            Button(onClick = scan, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Ler código QR", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        error?.let { Text(it, color = Palette.Brake, fontSize = 14.sp) }
        Spacer(Modifier.height(8.dp))
        Text(
            "Celular e PC precisam estar na mesma rede Wi-Fi, de preferência 5 GHz.",
            color = Palette.Muted,
            fontSize = 13.sp,
        )
    }
}

/**
 * Lê o QR com o leitor do Google Play Services: não pede permissão de câmera ao app.
 * Na primeira vez, o Android pode baixar o módulo do leitor.
 */
private fun scanPairingCode(context: Context, onPaired: (PairingPayload) -> Unit, onError: (String) -> Unit) {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    GmsBarcodeScanning.getClient(context, options)
        .startScan()
        .addOnSuccessListener { barcode ->
            val pairing = barcode.rawValue?.let(PairingPayload::parse)
            if (pairing != null) onPaired(pairing) else onError("Esse código QR não é do Volapex.")
        }
        .addOnFailureListener { e -> onError("Não foi possível ler o código: ${e.message}") }
}
