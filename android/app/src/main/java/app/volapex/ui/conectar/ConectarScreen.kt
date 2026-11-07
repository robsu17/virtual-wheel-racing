package app.volapex.ui.conectar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.volapex.Connection
import app.volapex.protocol.PinPairing
import app.volapex.ui.theme.Palette

@Composable
fun ConectarScreen(initial: Connection, onConnect: (Connection) -> Unit) {
    var host by remember { mutableStateOf(initial.host) }
    var pin by remember { mutableStateOf(initial.pin) }
    val candidate = Connection(host = host.trim(), pin = pin)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Background)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Volapex", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Palette.Text)
        Text("Conectar ao PC", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
        Text(
            "Abra o Volapex no Windows e digite o endereço e o PIN que aparecem lá.",
            color = Palette.Muted,
            fontSize = 15.sp,
        )
        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Endereço do PC") },
            placeholder = { Text("192.168.0.10") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = pin,
            onValueChange = { value -> pin = value.filter(Char::isDigit).take(PinPairing.PIN_LENGTH) },
            label = { Text("PIN") },
            placeholder = { Text("6 dígitos") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { onConnect(candidate) },
            enabled = candidate.isValid,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("Conectar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Celular e PC precisam estar na mesma rede Wi-Fi, de preferência 5 GHz.",
            color = Palette.Muted,
            fontSize = 13.sp,
        )
    }
}
