package app.volapex.desktop.receiver

import app.volapex.domain.FailsafePolicy
import app.volapex.domain.LinkHealth
import app.volapex.domain.MonotonicClock
import app.volapex.domain.VirtualWheel
import app.volapex.domain.WheelState
import app.volapex.protocol.PacketAuthenticator
import app.volapex.protocol.PacketCodec
import app.volapex.protocol.PacketType
import app.volapex.protocol.ProtocolDefaults
import app.volapex.protocol.SequenceGuard
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import kotlin.concurrent.thread

/**
 * Recebe os pacotes do celular e alimenta o volante virtual.
 *
 * Duas threads: a de recepção, que acorda a cada pacote, e o watchdog, que a cada 10 ms
 * aplica o failsafe quando os pacotes param. O caminho do pacote não cria objetos.
 */
class ReceiverService(
    private val wheel: VirtualWheel,
    private val clock: MonotonicClock = MonotonicClock { System.nanoTime() },
    private val requestedPort: Int = ProtocolDefaults.PORT,
) : AutoCloseable {
    private val lock = Any()
    private val latest = WheelState()
    private val guard = SequenceGuard()
    private val stats = LinkStats()

    @Volatile private var authenticator: PacketAuthenticator? = null
    @Volatile private var running = false
    private var socket: DatagramSocket? = null
    private var receiveThread: Thread? = null
    private var watchdogThread: Thread? = null

    // Protegidos por lock.
    private var connected = false
    private var lastPacketNanos = 0L
    private var health = LinkHealth.DISCONNECTED

    /** Porta realmente aberta (útil quando se pede a porta 0 nos testes). */
    var boundPort: Int = -1
        private set

    /** Troca a chave de pareamento; a sessão atual cai e o volante volta ao neutro. */
    fun setPairingKey(key: ByteArray) {
        synchronized(lock) {
            authenticator = PacketAuthenticator(key)
            disconnectLocked()
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
        receiveThread = thread(name = "volapex-receive", isDaemon = true, priority = Thread.MAX_PRIORITY) {
            receiveLoop(s)
        }
        watchdogThread = thread(name = "volapex-watchdog", isDaemon = true) { watchdogLoop() }
    }

    fun snapshot(): ReceiverSnapshot = synchronized(lock) {
        ReceiverSnapshot(
            connected = connected,
            health = health,
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
        synchronized(lock) { disconnectLocked() }
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
            handle(buffer, packet.length, clock.nanos())
        }
    }

    internal fun handle(buffer: ByteArray, length: Int, nowNanos: Long) {
        if (!PacketCodec.isWellFormed(buffer, length)) {
            stats.recordRejected()
            return
        }
        val auth = authenticator
        if (auth == null || !auth.verify(buffer)) {
            stats.recordRejected()
            return
        }
        synchronized(lock) {
            if (!guard.accept(PacketCodec.sessionId(buffer), PacketCodec.sequence(buffer))) {
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
                PacketType.BYE -> disconnectLocked()
                else -> stats.recordRejected()
            }
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
            if (!connected) return
            val silence = nowNanos - lastPacketNanos
            health = FailsafePolicy.health(silence)
            if (health == LinkHealth.DISCONNECTED) {
                disconnectLocked()
                return
            }
            if (FailsafePolicy.apply(latest, silence, dtSeconds)) wheel.apply(latest)
        }
    }

    private fun disconnectLocked() {
        connected = false
        health = LinkHealth.DISCONNECTED
        latest.reset()
        wheel.neutral()
        guard.reset()
        stats.resetSession()
    }

    companion object {
        const val WATCHDOG_PERIOD_MS = 10L
    }
}

data class ReceiverSnapshot(
    val connected: Boolean,
    val health: LinkHealth,
    val steering: Float,
    val throttle: Float,
    val brake: Float,
    val buttons: Int,
    val stats: LinkStatsSnapshot,
)
