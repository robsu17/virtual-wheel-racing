package app.virtualwheelracing.engine

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import app.virtualwheelracing.domain.AxisAssignment
import app.virtualwheelracing.domain.AxisMapper
import app.virtualwheelracing.domain.GlobalSettings
import app.virtualwheelracing.domain.MonotonicClock
import app.virtualwheelracing.domain.OneEuroFilter
import app.virtualwheelracing.domain.SteeringAngleTracker
import app.virtualwheelracing.domain.SteeringPipeline
import app.virtualwheelracing.domain.WheelState
import app.virtualwheelracing.protocol.ClientHandshake
import app.virtualwheelracing.protocol.PacketAuthenticator
import app.virtualwheelracing.protocol.PacketCodec
import app.virtualwheelracing.protocol.PacketLayout
import app.virtualwheelracing.protocol.PacketType
import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.security.SecureRandom

enum class LinkState { SEARCHING, CONNECTED }

/**
 * Caso de uso "dirigir": abre a sessão com o PC e liga sensor, pedais e rede numa thread própria.
 *
 * Sessão: manda HELLO a cada 250 ms até o PC responder WELCOME; daí em diante cada amostra do sensor
 * vira um STATE assinado com a chave da sessão. Se os HEARTBEATs do PC sumirem por 2 s, volta a procurar.
 * Uma segunda thread só recebe (WELCOME e HEARTBEAT). A tela lê um resumo pelo [snapshot].
 */
class DrivingSession(
    private val orientation: OrientationSource,
    private val controls: ControlInput,
    private val link: PacketLink,
    private val pairingKey: ByteArray,
    private val networkLock: NetworkLock,
    private val clock: MonotonicClock,
    settings: GlobalSettings,
    private val random: SecureRandom,
) : OrientationListener {
    private val tracker = SteeringAngleTracker()
    private val pipeline = SteeringPipeline(listOf(OneEuroFilter()))
    private val mapper = AxisMapper(settings)
    private val state = WheelState()
    private val txBuffer = ByteArray(PacketLayout.SIZE)
    private val rxBuffer = ByteArray(64)

    private var thread: HandlerThread? = null
    @Volatile private var handler: Handler? = null
    @Volatile private var running = false
    private var receiverThread: Thread? = null

    // Só a thread do motor usa.
    private var sessionId = 0
    private var signer: PacketAuthenticator? = null
    private var sequence = 0
    private var startNanos = 0L
    private var lastSendNanos = 0L
    private var lastHelloNanos = 0L
    private var windowStartNanos = 0L
    private var windowCount = 0
    private var linkOpen = false

    // Compartilhados entre o motor, a thread de recepção e a tela.
    @Volatile private var pendingHandshake: ClientHandshake? = null
    @Volatile private var heartbeatVerifier: PacketAuthenticator? = null // usado só pela recepção
    @Volatile private var heartbeatSessionId = 0
    @Volatile private var lastHeartbeatNanos = 0L
    @Volatile private var linkState = LinkState.SEARCHING
    @Volatile private var peerName: String? = null
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
        val engineThread = HandlerThread("vwr-engine", Process.THREAD_PRIORITY_URGENT_DISPLAY).apply { start() }
        val engineHandler = Handler(engineThread.looper)
        thread = engineThread
        handler = engineHandler
        running = true
        engineHandler.post {
            startNanos = clock.nanos()
            networkLock.acquire()
            try {
                link.open()
                linkOpen = true
                receiverThread = Thread({ receiveLoop() }, "vwr-rx").apply {
                    isDaemon = true
                    start()
                }
            } catch (e: Exception) {
                // Nenhuma falha de rede pode derrubar o app: vira mensagem na tela.
                lastError = "Não foi possível abrir a conexão: ${e.message}"
            }
            sensorAvailable = orientation.start(this, engineHandler)
            if (!sensorAvailable) lastError = "Este celular não tem o sensor de rotação necessário."
            engineHandler.post(tick)
        }
    }

    /** Envia BYE, solta tudo e encerra as threads. Pode ser chamado mais de uma vez. */
    fun stop() {
        val engineHandler = handler ?: return
        val engineThread = thread
        running = false
        handler = null
        thread = null
        engineHandler.post {
            engineHandler.removeCallbacks(tick)
            orientation.stop()
            state.reset()
            signer?.let { s -> repeat(BYE_REPEATS) { transmit(PacketType.BYE, clock.nanos(), s) } }
            try {
                link.close()
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
        linkState = linkState,
        peer = peerName,
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
        if (linkState == LinkState.CONNECTED) sendState(clock.nanos())
    }

    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            val now = clock.nanos()
            if (linkOpen) {
                when (linkState) {
                    LinkState.SEARCHING ->
                        if (now - lastHelloNanos >= HELLO_INTERVAL_NANOS) sendHello(now)
                    LinkState.CONNECTED -> when {
                        now - lastHeartbeatNanos >= PC_SILENT_NANOS -> {
                            dropSession()
                            sendHello(now)
                        }
                        now - lastSendNanos >= KEEP_ALIVE_NANOS -> sendState(now)
                    }
                }
            }
            handler?.postDelayed(this, TICK_MS)
        }
    }

    private fun sendHello(nowNanos: Long) {
        val attempt = ClientHandshake(pairingKey, random)
        pendingHandshake = attempt
        attempt.writeHello(txBuffer, 0, micros(nowNanos))
        try {
            link.sendToCandidates(txBuffer, txBuffer.size)
        } catch (e: Exception) {
            lastError = "Falha ao procurar o PC: ${e.message}"
        }
        lastHelloNanos = nowNanos
    }

    /** Roda na thread do motor, depois que a recepção validou o WELCOME. */
    private fun activateSession(id: Int, sessionKey: ByteArray, peer: SocketAddress) {
        if (!running) return
        link.selectPeer(peer)
        sessionId = id
        sequence = 0
        signer = PacketAuthenticator(sessionKey)
        heartbeatSessionId = id
        heartbeatVerifier = PacketAuthenticator(sessionKey)
        lastHeartbeatNanos = clock.nanos()
        peerName = (peer as? InetSocketAddress)?.address?.hostAddress
        linkState = LinkState.CONNECTED
        if (sensorAvailable) lastError = null
    }

    private fun dropSession() {
        linkState = LinkState.SEARCHING
        signer = null
        heartbeatVerifier = null
    }

    private fun receiveLoop() {
        while (running) {
            val length = try {
                link.receive(rxBuffer)
            } catch (e: IOException) {
                if (!running) break
                Thread.sleep(RECEIVE_ERROR_PAUSE_MS)
                continue
            }
            if (length < 0 || !PacketCodec.isWellFormed(rxBuffer, length)) continue
            when (PacketCodec.type(rxBuffer)) {
                PacketType.WELCOME -> {
                    val attempt = pendingHandshake ?: continue
                    val sessionKey = attempt.acceptWelcome(rxBuffer, length) ?: continue
                    val peer = link.lastSender ?: continue
                    pendingHandshake = null
                    handler?.post { activateSession(attempt.sessionId, sessionKey, peer) }
                }
                PacketType.HEARTBEAT -> {
                    val verifier = heartbeatVerifier ?: continue
                    if (PacketCodec.sessionId(rxBuffer) == heartbeatSessionId && verifier.verify(rxBuffer)) {
                        lastHeartbeatNanos = clock.nanos()
                    }
                }
            }
        }
    }

    private fun sendState(nowNanos: Long) {
        val s = signer ?: return
        controls.readInto(state)
        state.steering = steeringAxis
        assignment?.applyTo(state, (nowNanos - startNanos) / 1_000_000_000f)
        sentThrottle = state.throttle
        sentBrake = state.brake
        transmit(PacketType.STATE, nowNanos, s)
    }

    private fun transmit(type: Int, nowNanos: Long, s: PacketAuthenticator) {
        sequence++
        PacketCodec.write(txBuffer, type, sessionId, sequence, micros(nowNanos), state)
        s.sign(txBuffer)
        try {
            link.send(txBuffer, txBuffer.size)
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

    private fun micros(nanos: Long): Int = (nanos / 1000L).toInt()

    private companion object {
        const val TICK_MS = 10L
        const val KEEP_ALIVE_NANOS = 20_000_000L
        const val HELLO_INTERVAL_NANOS = 250_000_000L
        const val PC_SILENT_NANOS = 2_000_000_000L
        const val ONE_SECOND_NANOS = 1_000_000_000L
        const val RECEIVE_ERROR_PAUSE_MS = 50L
        const val BYE_REPEATS = 3
    }
}

data class EngineSnapshot(
    val linkState: LinkState,
    val peer: String?,
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
