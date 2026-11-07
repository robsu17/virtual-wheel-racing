package app.prwheel.engine

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import app.prwheel.domain.AxisAssignment
import app.prwheel.domain.AxisMapper
import app.prwheel.domain.GlobalSettings
import app.prwheel.domain.MonotonicClock
import app.prwheel.domain.OneEuroFilter
import app.prwheel.domain.SteeringAngleTracker
import app.prwheel.domain.SteeringPipeline
import app.prwheel.domain.WheelState
import app.prwheel.protocol.PacketAuthenticator
import app.prwheel.protocol.PacketCodec
import app.prwheel.protocol.PacketLayout
import app.prwheel.protocol.PacketType
import java.io.IOException

/**
 * Caso de uso "dirigir": liga sensor, pedais e rede numa thread própria.
 *
 * Cada amostra do sensor vira um pacote, sem passar pela UI e sem criar objetos.
 * A tela só lê um resumo pelo [snapshot], algumas vezes por segundo.
 */
class DrivingSession(
    private val orientation: OrientationSource,
    private val controls: ControlInput,
    private val sink: PacketSink,
    private val authenticator: PacketAuthenticator,
    private val networkLock: NetworkLock,
    private val clock: MonotonicClock,
    settings: GlobalSettings,
    private val sessionId: Int,
) : OrientationListener {
    private val tracker = SteeringAngleTracker()
    private val pipeline = SteeringPipeline(listOf(OneEuroFilter()))
    private val mapper = AxisMapper(settings)
    private val state = WheelState()
    private val buffer = ByteArray(PacketLayout.SIZE)

    private var thread: HandlerThread? = null
    private var handler: Handler? = null
    @Volatile private var running = false

    // Só a thread do motor escreve; a UI lê.
    private var sequence = 0
    private var startNanos = 0L
    private var lastSendNanos = 0L
    private var windowStartNanos = 0L
    private var windowCount = 0
    @Volatile private var steeringAxis = 0f
    @Volatile private var phoneDegrees = 0f
    @Volatile private var sentThrottle = 0f
    @Volatile private var sentBrake = 0f
    @Volatile private var sensorRateHz = 0
    @Volatile private var packetsSent = 0L
    @Volatile private var tilted = false
    @Volatile private var sensorAvailable = true
    @Volatile private var lastError: String? = null
    @Volatile private var assignment: AxisAssignment? = null

    fun start() {
        check(thread == null) { "Sessão já iniciada" }
        val engineThread = HandlerThread("pr-wheel-engine", Process.THREAD_PRIORITY_URGENT_DISPLAY).apply { start() }
        val engineHandler = Handler(engineThread.looper)
        thread = engineThread
        handler = engineHandler
        running = true
        engineHandler.post {
            startNanos = clock.nanos()
            networkLock.acquire()
            try {
                sink.open()
            } catch (e: Exception) {
                // Nenhuma falha de rede pode derrubar o app: vira mensagem na tela.
                lastError = "Não foi possível abrir a conexão: ${e.message}"
            }
            sensorAvailable = orientation.start(this, engineHandler)
            if (!sensorAvailable) lastError = "Este celular não tem o sensor de rotação necessário."
            engineHandler.postDelayed(keepAlive, KEEP_ALIVE_CHECK_MS)
        }
    }

    /** Envia BYE, solta tudo e encerra a thread. Pode ser chamado mais de uma vez. */
    fun stop() {
        val engineHandler = handler ?: return
        val engineThread = thread
        running = false
        handler = null
        thread = null
        engineHandler.post {
            engineHandler.removeCallbacks(keepAlive)
            orientation.stop()
            state.reset()
            repeat(BYE_REPEATS) { transmit(PacketType.BYE, clock.nanos()) }
            try {
                sink.close()
            } catch (e: Exception) {
                // Já está saindo; nada a fazer.
            }
            networkLock.release()
            engineThread?.quitSafely()
        }
    }

    fun recenter() {
        handler?.post {
            tracker.requestRecenter()
            pipeline.reset()
        }
    }

    /** Liga ou desliga o modo "mexer só um eixo", para atribuir controles no jogo. */
    fun setAssignment(axis: AxisAssignment?) {
        assignment = axis
    }

    fun snapshot(): EngineSnapshot = EngineSnapshot(
        phoneDegrees = phoneDegrees,
        steering = steeringAxis,
        throttle = sentThrottle,
        brake = sentBrake,
        sensorRateHz = sensorRateHz,
        packetsSent = packetsSent,
        tilted = tilted,
        sensorAvailable = sensorAvailable,
        error = lastError,
    )

    override fun onOrientation(w: Float, x: Float, y: Float, z: Float, timestampNanos: Long) {
        val degrees = tracker.update(w, x, y, z)
        tilted = tracker.isTilted
        val filtered = pipeline.process(degrees, timestampNanos)
        phoneDegrees = filtered
        steeringAxis = mapper.toAxis(filtered)
        countSensorSample(timestampNanos)
        sendState(clock.nanos())
    }

    private val keepAlive = object : Runnable {
        override fun run() {
            if (!running) return
            val now = clock.nanos()
            if (now - lastSendNanos >= KEEP_ALIVE_NANOS) sendState(now)
            handler?.postDelayed(this, KEEP_ALIVE_CHECK_MS)
        }
    }

    private fun sendState(nowNanos: Long) {
        controls.readInto(state)
        state.steering = steeringAxis
        assignment?.applyTo(state, (nowNanos - startNanos) / 1_000_000_000f)
        sentThrottle = state.throttle
        sentBrake = state.brake
        transmit(PacketType.STATE, nowNanos)
    }

    private fun transmit(type: Int, nowNanos: Long) {
        sequence++
        PacketCodec.write(buffer, type, sessionId, sequence, (nowNanos / 1000L).toInt(), state)
        authenticator.sign(buffer)
        try {
            sink.send(buffer, buffer.size)
            packetsSent++
            if (lastError != null && sensorAvailable) lastError = null
        } catch (e: IOException) {
            lastError = "Falha ao enviar: ${e.message}"
        } catch (e: RuntimeException) {
            lastError = "Falha ao enviar: ${e.message}"
        }
        lastSendNanos = nowNanos
    }

    private fun countSensorSample(timestampNanos: Long) {
        if (windowStartNanos == 0L) windowStartNanos = timestampNanos
        windowCount++
        if (timestampNanos - windowStartNanos >= ONE_SECOND_NANOS) {
            sensorRateHz = windowCount
            windowCount = 0
            windowStartNanos = timestampNanos
        }
    }

    private companion object {
        const val KEEP_ALIVE_CHECK_MS = 10L
        const val KEEP_ALIVE_NANOS = 20_000_000L
        const val ONE_SECOND_NANOS = 1_000_000_000L
        const val BYE_REPEATS = 3
    }
}

data class EngineSnapshot(
    val phoneDegrees: Float,
    val steering: Float,
    val throttle: Float,
    val brake: Float,
    val sensorRateHz: Int,
    val packetsSent: Long,
    val tilted: Boolean,
    val sensorAvailable: Boolean,
    val error: String?,
)
