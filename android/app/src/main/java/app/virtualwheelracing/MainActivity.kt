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
import app.virtualwheelracing.ui.boasvindas.WelcomeScreen
import app.virtualwheelracing.ui.calibracao.CalibrationScreen
import app.virtualwheelracing.ui.conectar.ConectarScreen
import app.virtualwheelracing.ui.dirigir.DirigirScreen
import app.virtualwheelracing.ui.guia.GameGuideScreen
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

private enum class Destination(val parent: Destination?) {
    HOME(null),
    SETTINGS(HOME),
    PAIRING(SETTINGS),
    CALIBRATION(SETTINGS),
    GUIDE(HOME),
    DRIVING(HOME),
    SETUP_DRIVING(GUIDE),
}

@Composable
private fun AppRoot(container: AppContainer) {
    val pairing by container.pairingStore.pairing.collectAsState()
    val settings by container.settingsRepository.settings.collectAsState()
    var destination by rememberSaveable { mutableStateOf(Destination.HOME) }

    val current = pairing
    if (current == null) {
        // Primeira vez: sem PC pareado, o app explica os passos e abre a leitura do QR.
        WelcomeScreen(onPaired = { container.pairingStore.save(it) })
        return
    }

    val back = { destination = destination.parent ?: Destination.HOME }
    BackHandler(enabled = destination != Destination.HOME, onBack = back)

    when (destination) {
        Destination.HOME -> HomeScreen(
            pairing = current,
            settings = settings,
            onFeel = { feel -> container.settingsRepository.update { feel.applyTo(it) } },
            onSettings = { destination = Destination.SETTINGS },
            onGuide = { destination = Destination.GUIDE },
            onDrive = { destination = Destination.DRIVING },
        )
        Destination.SETTINGS -> SettingsScreen(
            settings = settings,
            pcName = current.pcName,
            onChange = container.settingsRepository::update,
            onChangePc = { destination = Destination.PAIRING },
            onCalibration = { destination = Destination.CALIBRATION },
            onBack = back,
        )
        Destination.PAIRING -> ConectarScreen(
            onPaired = {
                container.pairingStore.save(it)
                destination = Destination.HOME
            },
            onBack = back,
        )
        Destination.CALIBRATION -> CalibrationScreen(container, onBack = back)
        Destination.GUIDE -> GameGuideScreen(onSetupMode = { destination = Destination.SETUP_DRIVING }, onBack = back)
        Destination.DRIVING -> DirigirScreen(container, current, settings, onBack = back)
        Destination.SETUP_DRIVING -> DirigirScreen(container, current, settings, setupMode = true, onBack = back)
    }
}
