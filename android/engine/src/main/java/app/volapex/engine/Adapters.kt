package app.volapex.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.os.Handler
import app.volapex.domain.GlobalSettings
import app.volapex.domain.PedalMapper
import app.volapex.domain.WheelState
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicInteger

/** Orientação pela fusão do próprio Android, sem magnetômetro, a 200 Hz. */
class RotationVectorSource(private val sensorManager: SensorManager) : OrientationSource, SensorEventListener {
    private val quaternion = FloatArray(4)
    private var listener: OrientationListener? = null

    override fun start(listener: OrientationListener, handler: Handler): Boolean {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) ?: return false
        this.listener = listener
        return sensorManager.registerListener(this, sensor, SAMPLING_PERIOD_US, handler)
    }

    override fun stop() {
        sensorManager.unregisterListener(this)
        listener = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        SensorManager.getQuaternionFromVector(quaternion, event.values) // [w, x, y, z]
        listener?.onOrientation(quaternion[0], quaternion[1], quaternion[2], quaternion[3], event.timestamp)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    private companion object {
        const val SAMPLING_PERIOD_US = 5_000 // 200 Hz, o limite sem permissão especial
    }
}

/**
 * UDP com o PC. Um socket só, sem `connect`, para poder tentar todos os endereços do QR
 * e depois falar só com o que respondeu. O envio de STATE reaproveita o mesmo DatagramPacket.
 */
class UdpLink(private val hosts: List<String>, private val pcPort: Int) : PacketLink {
    @Volatile private var socket: DatagramSocket? = null
    private var candidates: List<InetSocketAddress> = emptyList()
    private var sendPacket: DatagramPacket? = null
    private var receivePacket: DatagramPacket? = null
    @Volatile private var peer: SocketAddress? = null

    @Volatile
    override var lastSender: SocketAddress? = null
        private set

    override fun open() {
        candidates = hosts.map { host -> InetSocketAddress(InetAddress.getByName(host), pcPort) }
        val newSocket = DatagramSocket()
        newSocket.soTimeout = RECEIVE_TIMEOUT_MS
        socket = newSocket
    }

    override fun sendToCandidates(buffer: ByteArray, length: Int) {
        val s = requireSocket()
        for (candidate in candidates) s.send(DatagramPacket(buffer, length, candidate))
    }

    override fun selectPeer(address: SocketAddress) {
        peer = address
    }

    override fun send(buffer: ByteArray, length: Int) {
        val s = requireSocket()
        val target = peer ?: throw IOException("O PC ainda não respondeu")
        val p = sendPacket?.takeIf { it.data === buffer } ?: DatagramPacket(buffer, length).also { sendPacket = it }
        p.length = length
        p.socketAddress = target
        s.send(p)
    }

    override fun receive(buffer: ByteArray): Int {
        val s = requireSocket()
        val p = receivePacket?.takeIf { it.data === buffer } ?: DatagramPacket(buffer, buffer.size).also { receivePacket = it }
        p.length = buffer.size
        return try {
            s.receive(p)
            lastSender = p.socketAddress
            p.length
        } catch (e: SocketTimeoutException) {
            -1
        }
    }

    override fun close() {
        socket?.close()
        socket = null
        sendPacket = null
        receivePacket = null
    }

    private fun requireSocket(): DatagramSocket = socket ?: throw IOException("Conexão não aberta")

    private companion object {
        const val RECEIVE_TIMEOUT_MS = 250
    }
}

/** Wi-Fi em modo de baixa latência (só vale com a tela ligada e o app em primeiro plano). */
class WifiLowLatencyLock(context: Context) : NetworkLock {
    private val lock = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
        .createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, "volapex:driving")
        .apply { setReferenceCounted(false) }

    override fun acquire() = lock.acquire()

    override fun release() {
        if (lock.isHeld) lock.release()
    }
}

/** Pedais e botões tocados na tela. A UI escreve; o motor lê a cada pacote. */
class TouchControls(settings: GlobalSettings) : ControlInput {
    private val pedalMapper = PedalMapper(settings)
    private val buttons = AtomicInteger(0)

    @Volatile var throttlePosition: Float = 0f
    @Volatile var brakePosition: Float = 0f

    fun setButton(index: Int, pressed: Boolean) {
        if (index !in 0 until WheelState.BUTTON_COUNT) return
        val bit = 1 shl index
        buttons.updateAndGet { if (pressed) it or bit else it and bit.inv() }
    }

    override fun readInto(state: WheelState) {
        state.throttle = pedalMapper.toPedal(throttlePosition)
        state.brake = pedalMapper.toPedal(brakePosition)
        state.buttons = buttons.get()
    }
}
