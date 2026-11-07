package app.prwheel.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.os.Handler
import app.prwheel.domain.GlobalSettings
import app.prwheel.domain.PedalMapper
import app.prwheel.domain.WheelState
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
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

/** Envia por UDP para o PC. Reaproveita o mesmo DatagramPacket a cada envio. */
class UdpPacketSink(private val host: String, private val port: Int) : PacketSink {
    private var socket: DatagramSocket? = null
    private var packet: DatagramPacket? = null

    override fun open() {
        val address = InetAddress.getByName(host)
        // Sem `apply`: dentro dele, `port` seria a porta do próprio socket (-1), e não a do PC.
        val newSocket = DatagramSocket()
        newSocket.connect(address, this.port)
        socket = newSocket
    }

    override fun send(buffer: ByteArray, length: Int) {
        val s = socket ?: throw IOException("Conexão não aberta")
        val p = packet?.takeIf { it.data === buffer } ?: DatagramPacket(buffer, length).also { packet = it }
        p.length = length
        s.send(p)
    }

    override fun close() {
        socket?.close()
        socket = null
        packet = null
    }
}

/** Wi-Fi em modo de baixa latência (só vale com a tela ligada e o app em primeiro plano). */
class WifiLowLatencyLock(context: Context) : NetworkLock {
    private val lock = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
        .createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, "pr-wheel:driving")
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
