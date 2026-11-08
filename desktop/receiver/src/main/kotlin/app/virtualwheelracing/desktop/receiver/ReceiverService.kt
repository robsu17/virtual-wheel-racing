package app.virtualwheelracing.desktop.receiver

import app.virtualwheelracing.domain.FailsafePolicy
import app.virtualwheelracing.domain.LinkHealth
import app.virtualwheelracing.domain.MonotonicClock
import app.virtualwheelracing.domain.VirtualWheel
import app.virtualwheelracing.domain.WheelState
import app.virtualwheelracing.protocol.PacketAuthenticator
import app.virtualwheelracing.protocol.PacketCodec
import app.virtualwheelracing.protocol.PacketLayout
import app.virtualwheelracing.protocol.PacketType
import app.virtualwheelracing.protocol.ProtocolDefaults
import app.virtualwheelracing.protocol.SequenceGuard
import app.virtualwheelracing.protocol.ServerHandshake
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.security.SecureRandom
import kotlin.concurrent.thread

/**
 * Recebe os pacotes do celular e alimenta o volante virtual.
 *
 * Fluxo: o celular manda HELLO (assinado com a chave de pareamento do QR), o PC responde WELCOME
 * e os dois derivam a chave da sessão. Depois disso, só STATE e BYE assinados com essa chave valem.
 * O PC manda HEARTBEAT duas vezes por segundo, para o celular saber que ainda está sendo ouvido.
 *
 * Threads: a de recepção acorda a cada pacote; o watchdog, a cada 10 ms, aplica o failsafe e envia o
 * heartbeat. O caminho de um STATE não cria objetos.
 */
class ReceiverService(
    private val wheel: VirtualWheel,
    private val clock: MonotonicClock = MonotonicClock { System.nanoTime() },
    private val requestedPort: Int = ProtocolDefaults.PORT,
    private val random: SecureRandom = SecureRandom(),
) : AutoCloseable {

    /** Uma sessão aceita. Cada autenticador é usado por uma única thread. */
    private class Session(
        val id: Int,
        val peer: SocketAddress,
        val receiveAuth: PacketAuthenticator,
        val heartbeatAuth: PacketAuthenticator,
    )

    private val lock = Any()
    private val latest = WheelState()
    private val guard = SequenceGuard()
    private val stats = LinkStats()

    @Volatile private var handshake: ServerHandshake? = null
    @Volatile private var session: Session? = null
    @Volatile private var running = false
    @Volatile private var socket: DatagramSocket? = null
    private var receiveThread: Thread? = null
    private var watchdogThread: Thread? = null

    private val replyBuffer = ByteArray(PacketLayout.SIZE) // thread de recepção
    private val heartbeatBuffer = ByteArray(PacketLayout.SIZE) // watchdog
    private val heartbeatPacket = DatagramPacket(heartbeatBuffer, PacketLayout.SIZE)

    // Protegidos por lock.
    private var connected = false
    private var lastPacketNanos = 0L
    private var lastHeartbeatNanos = 0L
    private var heartbeatSequence = 0
    private var health = LinkHealth.DISCONNECTED

    /** Porta realmente aberta (útil quando se pede a porta 0 nos testes). */
    var boundPort: Int = -1
        private set

    /** Troca a chave de pareamento (novo QR). A sessão atual cai e o volante volta ao neutro. */
    fun setPairingKey(key: ByteArray) {
        synchronized(lock) {
            handshake = ServerHandshake(key, random)
            endSessionLocked()
        }
    }

    @Throws(IOException::class)
    fun start() {
        check(!running) { "Receptor já iniciado" }
        val s = DatagramSocket(null)
        s.bind(InetSocketAddress(requestedPort))
        socket = s
        boundPort = s.localPort
        running = true
        receiveThread = thread(name = "vwr-receive", isDaemon = true, priority = Thread.MAX_PRIORITY) {
            receiveLoop(s)
        }
        watchdogThread = thread(name = "vwr-watchdog", isDaemon = true) { watchdogLoop() }
    }

    fun snapshot(): ReceiverSnapshot = synchronized(lock) {
        ReceiverSnapshot(
            connected = connected,
            health = health,
            peer = (session?.peer as? InetSocketAddress)?.address?.hostAddress,
            steering = latest.steering,
            throttle = latest.throttle,
            brake = latest.brake,
            buttons = latest.buttons,
            stats = stats.snapshot(clock.nanos()),
        )
    }

    override fun close() {
        running = false
        socket?.close()
        watchdogThread?.interrupt()
        receiveThread?.join(500)
        watchdogThread?.join(500)
        synchronized(lock) { endSessionLocked() }
    }

    private fun receiveLoop(s: DatagramSocket) {
        val buffer = ByteArray(64)
        val packet = DatagramPacket(buffer, buffer.size)
        while (running) {
            try {
                packet.length = buffer.size
                s.receive(packet)
            } catch (e: IOException) {
                if (!running) break
                continue
            }
            handle(buffer, packet.length, clock.nanos(), packet)
        }
    }

    internal fun handle(buffer: ByteArray, length: Int, nowNanos: Long, packet: DatagramPacket?) {
        if (!PacketCodec.isWellFormed(buffer, length)) {
            stats.recordRejected()
            return
        }
        if (PacketCodec.type(buffer) == PacketType.HELLO) {
            handleHello(buffer, length, nowNanos, packet?.socketAddress)
            return
        }
        val current = session
        if (current == null || PacketCodec.sessionId(buffer) != current.id || !current.receiveAuth.verify(buffer)) {
            stats.recordRejected()
            return
        }
        synchronized(lock) {
            if (session !== current) return
            if (!guard.accept(current.id, PacketCodec.sequence(buffer))) {
                stats.recordRejected()
                return
            }
            when (PacketCodec.type(buffer)) {
                PacketType.STATE -> {
                    PacketCodec.readState(buffer, latest)
                    lastPacketNanos = nowNanos
                    connected = true
                    health = LinkHealth.OK
                    wheel.apply(latest)
                    stats.recordAccepted(nowNanos, guard.lastGap)
                }
                PacketType.BYE -> endSessionLocked()
                else -> stats.recordRejected()
            }
        }
    }

    private fun handleHello(buffer: ByteArray, length: Int, nowNanos: Long, sender: SocketAddress?) {
        val responder = handshake
        val s = socket
        if (responder == null || sender == null || s == null) {
            stats.recordRejected()
            return
        }
        val accepted = responder.handleHello(buffer, length, replyBuffer, micros(nowNanos))
        if (accepted == null) {
            stats.recordRejected()
            return
        }
        synchronized(lock) {
            endSessionLocked()
            session = Session(
                id = accepted.sessionId,
                peer = sender,
                receiveAuth = PacketAuthenticator(accepted.sessionKey),
                heartbeatAuth = PacketAuthenticator(accepted.sessionKey),
            )
            lastPacketNanos = nowNanos
            lastHeartbeatNanos = nowNanos
        }
        try {
            s.send(DatagramPacket(replyBuffer, PacketLayout.SIZE, sender))
        } catch (e: IOException) {
            // O celular repete o HELLO até receber resposta.
        }
    }

    private fun watchdogLoop() {
        var previous = clock.nanos()
        while (running) {
            try {
                Thread.sleep(WATCHDOG_PERIOD_MS)
            } catch (e: InterruptedException) {
                break
            }
            val now = clock.nanos()
            tick(now, (now - previous) / 1_000_000_000f)
            previous = now
        }
    }

    internal fun tick(nowNanos: Long, dtSeconds: Float) {
        synchronized(lock) {
            val current = session ?: return
            val silence = nowNanos - lastPacketNanos
            if (silence >= FailsafePolicy.DISCONNECT_NANOS) {
                endSessionLocked()
                return
            }
            if (connected) {
                health = FailsafePolicy.health(silence)
                if (FailsafePolicy.apply(latest, silence, dtSeconds)) wheel.apply(latest)
            }
            if (nowNanos - lastHeartbeatNanos >= HEARTBEAT_PERIOD_NANOS) {
                lastHeartbeatNanos = nowNanos
                sendHeartbeat(current, nowNanos)
            }
        }
    }

    private fun sendHeartbeat(current: Session, nowNanos: Long) {
        val s = socket ?: return
        heartbeatSequence++
        PacketCodec.writeControl(heartbeatBuffer, PacketType.HEARTBEAT, current.id, heartbeatSequence, micros(nowNanos), 0L)
        current.heartbeatAuth.sign(heartbeatBuffer)
        heartbeatPacket.socketAddress = current.peer
        try {
            s.send(heartbeatPacket)
        } catch (e: IOException) {
            // Sem rede no momento; o failsafe cuida do resto.
        }
    }

    private fun endSessionLocked() {
        session = null
        connected = false
        health = LinkHealth.DISCONNECTED
        latest.reset()
        wheel.neutral()
        guard.reset()
        stats.resetSession()
    }

    private fun micros(nanos: Long): Int = (nanos / 1000L).toInt()

    companion object {
        const val WATCHDOG_PERIOD_MS = 10L
        const val HEARTBEAT_PERIOD_NANOS = 500_000_000L
    }
}

data class ReceiverSnapshot(
    val connected: Boolean,
    val health: LinkHealth,
    val peer: String?,
    val steering: Float,
    val throttle: Float,
    val brake: Float,
    val buttons: Int,
    val stats: LinkStatsSnapshot,
)
