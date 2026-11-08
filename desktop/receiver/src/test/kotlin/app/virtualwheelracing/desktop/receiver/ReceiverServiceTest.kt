package app.virtualwheelracing.desktop.receiver

import app.virtualwheelracing.domain.FakeWheel
import app.virtualwheelracing.domain.WheelState
import app.virtualwheelracing.protocol.ClientHandshake
import app.virtualwheelracing.protocol.PacketAuthenticator
import app.virtualwheelracing.protocol.PacketCodec
import app.virtualwheelracing.protocol.PacketLayout
import app.virtualwheelracing.protocol.PacketType
import app.virtualwheelracing.protocol.PairingPayload
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.security.SecureRandom
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

/** Integração: celular simulado → UDP local → receptor → volante falso. */
class ReceiverServiceTest {
    private val wheel = FakeWheel()
    private val service = ReceiverService(wheel, requestedPort = 0)
    private val pairingKey = PairingPayload.generateKey()
    private val phones = mutableListOf<FakePhone>()

    @BeforeTest
    fun setUp() {
        service.setPairingKey(pairingKey)
        service.start()
    }

    @AfterTest
    fun tearDown() {
        phones.forEach { it.close() }
        service.close()
    }

    private fun phone(key: ByteArray = pairingKey) = FakePhone(key).also { phones += it }

    /** Celular simulado, com a mesma lógica de handshake do app. */
    private inner class FakePhone(private val key: ByteArray) : AutoCloseable {
        private val socket = DatagramSocket()
        private val target = InetSocketAddress(InetAddress.getLoopbackAddress(), service.boundPort)
        private var handshake = ClientHandshake(key, SecureRandom())
        private var sessionKey: ByteArray? = null
        private var sequence = 0
        var lastHello: ByteArray = ByteArray(0)
            private set

        init {
            socket.soTimeout = 1000
        }

        fun sendHello(raw: ByteArray? = null) {
            val buffer = raw ?: ByteArray(PacketLayout.SIZE).also {
                handshake = ClientHandshake(key, SecureRandom())
                handshake.writeHello(it, 1, 0)
            }
            lastHello = buffer.copyOf()
            socket.send(DatagramPacket(buffer, buffer.size, target))
        }

        /** Espera um pacote do tipo pedido; null se não chegar. */
        fun await(type: Int): ByteArray? {
            val packet = DatagramPacket(ByteArray(64), 64)
            while (true) {
                try {
                    socket.receive(packet)
                } catch (e: SocketTimeoutException) {
                    return null
                }
                if (PacketCodec.isWellFormed(packet.data, packet.length) && PacketCodec.type(packet.data) == type) {
                    return packet.data.copyOf(packet.length)
                }
            }
        }

        fun connect(): Boolean {
            sendHello()
            val welcome = await(PacketType.WELCOME) ?: return false
            sessionKey = handshake.acceptWelcome(welcome, welcome.size)
            return sessionKey != null
        }

        fun send(type: Int, sequenceNumber: Int = ++sequence, signWith: ByteArray? = sessionKey, configure: WheelState.() -> Unit = {}) {
            val buffer = ByteArray(PacketLayout.SIZE)
            PacketCodec.write(buffer, type, handshake.sessionId, sequenceNumber, 0, WheelState().apply(configure))
            PacketAuthenticator(signWith ?: error("sem chave")).sign(buffer)
            socket.send(DatagramPacket(buffer, buffer.size, target))
        }

        fun heartbeatIsValid(): Boolean {
            val heartbeat = await(PacketType.HEARTBEAT) ?: return false
            return PacketCodec.sessionId(heartbeat) == handshake.sessionId &&
                PacketAuthenticator(sessionKey ?: return false).verify(heartbeat)
        }

        override fun close() = socket.close()
    }

    private fun eventually(timeoutMs: Long = 2000, condition: () -> Boolean) {
        val deadline = System.nanoTime() + timeoutMs * 1_000_000
        while (!condition()) {
            if (System.nanoTime() > deadline) fail("Condição não atingida em $timeoutMs ms")
            Thread.sleep(5)
        }
    }

    @Test
    fun `handshake e estado autenticado chegam ao volante`() {
        val phone = phone()
        assertTrue(phone.connect())
        phone.send(PacketType.STATE) { steering = 0.5f; throttle = 1f; buttons = 2 }
        eventually { wheel.current().throttle > 0.99f }
        assertEquals(0.5f, wheel.current().steering, 0.001f)
        assertEquals(2, wheel.current().buttons)
        assertEquals("127.0.0.1", service.snapshot().peer)
    }

    @Test
    fun `HELLO com outra chave nao recebe resposta`() {
        val stranger = phone(PairingPayload.generateKey())
        assertFalse(stranger.connect())
        assertTrue(service.snapshot().stats.rejected >= 1)
    }

    @Test
    fun `HELLO gravado e repetido nao abre outra sessao`() {
        val phone = phone()
        assertTrue(phone.connect())
        phone.sendHello(raw = phone.lastHello)
        assertNull(phone.await(PacketType.WELCOME))
    }

    @Test
    fun `estado assinado com a chave do QR em vez da chave da sessao e ignorado`() {
        val phone = phone()
        assertTrue(phone.connect())
        phone.send(PacketType.STATE, signWith = pairingKey) { throttle = 1f }
        eventually { service.snapshot().stats.rejected >= 1 }
        assertEquals(0f, wheel.current().throttle)
    }

    @Test
    fun `PC envia heartbeat valido`() {
        val phone = phone()
        assertTrue(phone.connect())
        assertTrue(phone.heartbeatIsValid())
    }

    @Test
    fun `solta os pedais quando os pacotes param`() {
        val phone = phone()
        assertTrue(phone.connect())
        phone.send(PacketType.STATE) { steering = 0.4f; throttle = 1f }
        eventually { wheel.current().throttle > 0.99f }
        eventually { wheel.current().throttle == 0f }
        assertEquals(0.4f, wheel.current().steering, 0.001f)
    }

    @Test
    fun `bye volta ao neutro e encerra a sessao`() {
        val phone = phone()
        assertTrue(phone.connect())
        phone.send(PacketType.STATE) { steering = -1f }
        eventually { wheel.current().steering < -0.99f }
        phone.send(PacketType.BYE)
        eventually { service.snapshot().peer == null && wheel.current().steering == 0f }
    }

    @Test
    fun `pacote com sequencia antiga e descartado`() {
        val phone = phone()
        assertTrue(phone.connect())
        phone.send(PacketType.STATE, sequenceNumber = 5) { throttle = 1f }
        eventually { wheel.current().throttle > 0.99f }
        phone.send(PacketType.STATE, sequenceNumber = 3) { throttle = 0.2f }
        eventually { service.snapshot().stats.rejected >= 1 }
    }

    @Test
    fun `novo codigo QR derruba a sessao atual`() {
        val phone = phone()
        assertTrue(phone.connect())
        service.setPairingKey(PairingPayload.generateKey())
        phone.send(PacketType.STATE) { throttle = 1f }
        eventually { service.snapshot().stats.rejected >= 1 }
        assertEquals(0f, wheel.current().throttle)
        assertFalse(phone.connect())
    }
}
