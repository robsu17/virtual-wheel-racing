package app.volapex

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import android.os.SystemClock
import app.volapex.domain.GlobalSettings
import app.volapex.domain.MonotonicClock
import app.volapex.engine.DrivingSession
import app.volapex.engine.RotationVectorSource
import app.volapex.engine.TouchControls
import app.volapex.engine.UdpLink
import app.volapex.engine.WifiLowLatencyLock
import app.volapex.protocol.PairingPayload
import java.security.SecureRandom

class VolapexApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

/** Raiz de composição do app: o único lugar que liga portas a adaptadores. */
class AppContainer(private val context: Context) {
    val pairingStore = PairingStore(context)
    val settings = GlobalSettings()
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val random = SecureRandom()

    fun newControls() = TouchControls(settings)

    fun newSession(pairing: PairingPayload, controls: TouchControls) = DrivingSession(
        orientation = RotationVectorSource(sensorManager),
        controls = controls,
        link = UdpLink(pairing.hosts, pairing.port),
        pairingKey = pairing.pairingKey,
        networkLock = WifiLowLatencyLock(context),
        clock = MonotonicClock { SystemClock.elapsedRealtimeNanos() },
        settings = settings,
        random = random,
    )
}

/**
 * Lembra o último PC pareado, para reconectar sem ler o QR de novo.
 * Provisório: no MVP a chave vai para o Android Keystore.
 */
class PairingStore(context: Context) {
    private val prefs = context.getSharedPreferences("pareamento", Context.MODE_PRIVATE)

    fun load(): PairingPayload? = prefs.getString(KEY, null)?.let(PairingPayload::parse)

    fun save(pairing: PairingPayload) {
        prefs.edit().putString(KEY, pairing.encode()).apply()
    }

    private companion object {
        const val KEY = "codigo"
    }
}
