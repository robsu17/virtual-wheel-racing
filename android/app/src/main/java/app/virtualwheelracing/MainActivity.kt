package app.virtualwheelracing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.virtualwheelracing.ui.ajustes.SettingsScreen
import app.virtualwheelracing.ui.calibracao.CalibrationScreen
import app.virtualwheelracing.ui.conectar.ConectarScreen
import app.virtualwheelracing.ui.dirigir.DirigirScreen
import app.virtualwheelracing.ui.inicio.HomeScreen
import app.virtualwheelracing.ui.theme.VwrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as VwrApplication).container
        setContent {
            VwrTheme { AppRoot(container) }
        }
    }
}

private enum class Destination { HOME, PAIRING, SETTINGS, CALIBRATION, DRIVING }

@Composable
private fun AppRoot(container: AppContainer) {
    val pairing by container.pairingStore.pairing.collectAsState()
    val settings by container.settingsRepository.settings.collectAsState()
    val sensorStats by container.lastSensorStats.collectAsState()
    var destination by rememberSaveable { mutableStateOf(Destination.HOME) }

    val current = pairing
    if (current == null) {
        // Primeira vez: sem PC pareado, o app começa pela leitura do QR.
        ConectarScreen(onPaired = { container.pairingStore.save(it) }, onBack = null)
        return
    }

    val home = { destination = Destination.HOME }
    BackHandler(enabled = destination != Destination.HOME, onBack = home)

    when (destination) {
        Destination.HOME -> HomeScreen(
            pairing = current,
            settings = settings,
            sensorStats = sensorStats,
            onPairing = { destination = Destination.PAIRING },
            onSettings = { destination = Destination.SETTINGS },
            onCalibration = { destination = Destination.CALIBRATION },
            onDrive = { destination = Destination.DRIVING },
        )
        Destination.PAIRING -> ConectarScreen(
            onPaired = {
                container.pairingStore.save(it)
                home()
            },
            onBack = home,
        )
        Destination.SETTINGS -> SettingsScreen(settings, container.settingsRepository::update, onBack = home)
        Destination.CALIBRATION -> CalibrationScreen(container, onBack = home)
        Destination.DRIVING -> DirigirScreen(container, current, settings, onBack = home)
    }
}
