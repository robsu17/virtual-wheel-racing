package app.prwheel

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import android.os.SystemClock
import app.prwheel.domain.GlobalSettings
import app.prwheel.domain.MonotonicClock
import app.prwheel.engine.DrivingSession
import app.prwheel.engine.RotationVectorSource
import app.prwheel.engine.TouchControls
import app.prwheel.engine.UdpPacketSink
import app.prwheel.engine.WifiLowLatencyLock
import app.prwheel.protocol.PacketAuthenticator
import app.prwheel.protocol.PinPairing
import app.prwheel.protocol.ProtocolDefaults
import java.security.SecureRandom

class PrWheelApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

data class Connection(val host: String = "", val pin: String = "", val port: Int = ProtocolDefaults.PORT) {
    val isValid: Boolean get() = host.isNotBlank() && PinPairing.isValidPin(pin)
}

/** Raiz de composição do app: o único lugar que liga portas a adaptadores. */
class AppContainer(private val context: Context) {
    val preferences = ConnectionPreferences(context)
    val settings = GlobalSettings()
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val random = SecureRandom()

    fun newControls() = TouchControls(settings)

    fun newSession(connection: Connection, controls: TouchControls) = DrivingSession(
        orientation = RotationVectorSource(sensorManager),
        controls = controls,
        sink = UdpPacketSink(connection.host.trim(), connection.port),
        authenticator = PacketAuthenticator(PinPairing.keyFromPin(connection.pin)),
        networkLock = WifiLowLatencyLock(context),
        clock = MonotonicClock { SystemClock.elapsedRealtimeNanos() },
        settings = settings,
        sessionId = random.nextInt(),
    )
}

/** Guarda o último PC usado. Provisório: o MVP usa DataStore criptografado e pareamento por QR. */
class ConnectionPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("conexao", Context.MODE_PRIVATE)

    fun load() = Connection(host = prefs.getString("host", "") ?: "", pin = prefs.getString("pin", "") ?: "")

    fun save(connection: Connection) {
        prefs.edit().putString("host", connection.host).putString("pin", connection.pin).apply()
    }
}
