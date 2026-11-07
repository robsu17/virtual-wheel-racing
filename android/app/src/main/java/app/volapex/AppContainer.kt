package app.volapex

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import android.os.SystemClock
import app.volapex.domain.GlobalSettings
import app.volapex.domain.MonotonicClock
import app.volapex.domain.ResponseCurve
import app.volapex.domain.SensorStats
import app.volapex.engine.DrivingSession
import app.volapex.engine.RotationVectorSource
import app.volapex.engine.SensorDiagnostics
import app.volapex.engine.TouchControls
import app.volapex.engine.UdpLink
import app.volapex.engine.WifiLowLatencyLock
import app.volapex.protocol.PairingPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom

class VolapexApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

/** Raiz de composição do app: o único lugar que liga portas a adaptadores. */
class AppContainer(private val context: Context) {
    val pairingStore = PairingStore(context)
    val settingsRepository: SettingsRepository = SharedPrefsSettingsRepository(context)

    /** Último resultado do teste de sensores, mostrado na tela Início. */
    val lastSensorStats = MutableStateFlow<SensorStats?>(null)

    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val clock = MonotonicClock { SystemClock.elapsedRealtimeNanos() }
    private val random = SecureRandom()

    fun newControls() = TouchControls(settingsRepository.settings.value)

    fun newSession(pairing: PairingPayload, controls: TouchControls) = DrivingSession(
        orientation = RotationVectorSource(sensorManager),
        controls = controls,
        link = UdpLink(pairing.hosts, pairing.port),
        pairingKey = pairing.pairingKey,
        networkLock = WifiLowLatencyLock(context),
        clock = clock,
        settings = settingsRepository.settings.value,
        random = random,
    )

    fun newDiagnostics() = SensorDiagnostics(RotationVectorSource(sensorManager), clock)
}

/**
 * Lembra o último PC pareado, para reconectar sem ler o QR de novo.
 * Provisório: no MVP a chave vai para o Android Keystore.
 */
class PairingStore(context: Context) {
    private val prefs = context.getSharedPreferences("pareamento", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(prefs.getString(KEY, null)?.let(PairingPayload::parse))
    val pairing: StateFlow<PairingPayload?> = state.asStateFlow()

    fun save(pairing: PairingPayload) {
        prefs.edit().putString(KEY, pairing.encode()).apply()
        state.value = pairing
    }

    private companion object {
        const val KEY = "codigo"
    }
}

/** Ajustes globais do volante (configuração universal). Porta: trocável por DataStore depois. */
interface SettingsRepository {
    val settings: StateFlow<GlobalSettings>
    fun update(transform: (GlobalSettings) -> GlobalSettings)
}

class SharedPrefsSettingsRepository(context: Context) : SettingsRepository {
    private val prefs = context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(load())
    override val settings: StateFlow<GlobalSettings> = state.asStateFlow()

    override fun update(transform: (GlobalSettings) -> GlobalSettings) {
        val updated = try {
            transform(state.value)
        } catch (e: IllegalArgumentException) {
            return // valor fora da faixa: mantém o anterior
        }
        state.value = updated
        prefs.edit()
            .putFloat("giro", updated.arcDegrees)
            .putFloat("zonaMorta", updated.deadzoneDegrees)
            .putString("curva", updated.curve.name)
            .putBoolean("freioEsquerda", updated.brakeOnLeft)
            .putFloat("pedalFundo", updated.pedalFullAt)
            .apply()
    }

    private fun load(): GlobalSettings {
        val defaults = GlobalSettings()
        return try {
            GlobalSettings(
                arcDegrees = prefs.getFloat("giro", defaults.arcDegrees),
                deadzoneDegrees = prefs.getFloat("zonaMorta", defaults.deadzoneDegrees),
                curve = ResponseCurve.entries.firstOrNull { it.name == prefs.getString("curva", null) } ?: defaults.curve,
                brakeOnLeft = prefs.getBoolean("freioEsquerda", defaults.brakeOnLeft),
                pedalFullAt = prefs.getFloat("pedalFundo", defaults.pedalFullAt),
            )
        } catch (e: IllegalArgumentException) {
            defaults
        }
    }
}
