package app.prwheel.desktop.receiver

import app.prwheel.domain.FakeWheel
import app.prwheel.domain.WheelState
import app.prwheel.protocol.PacketAuthenticator
import app.prwheel.protocol.PacketCodec
import app.prwheel.protocol.PacketLayout
import app.prwheel.protocol.PacketType
import app.prwheel.protocol.PinPairing
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.fail

/** Integração: celular simulado → UDP local → receptor → volante falso. */
class ReceiverServiceTest {
    private val wheel = FakeWheel()
    private val service = ReceiverService(wheel, requestedPort = 0)
    private val key = PinPairing.keyFromPin("123456")

    @BeforeTest
    fun setUp() {
        service.setPairingKey(key)
        service.start()
    }

    @AfterTest
    fun tearDown() = service.close()

    private fun send(type: Int, sequence: Int, signingKey: ByteArray = key, configure: WheelState.() -> Unit = {}) {
        val buffer = ByteArray(PacketLayout.SIZE)
        PacketCodec.write(buffer, type, 7, sequence, 0, WheelState().apply(configure))
        PacketAuthenticator(signingKey).sign(buffer)
        DatagramSocket().use { socket ->
            socket.send(DatagramPacket(buffer, buffer.size, InetAddress.getLoopbackAddress(), service.boundPort))
        }
    }

    private fun eventually(timeoutMs: Long = 2000, condition: () -> Boolean) {
        val deadline = System.nanoTime() + timeoutMs * 1_000_000
        while (!condition()) {
            if (System.nanoTime() > deadline) fail("Condição não atingida em $timeoutMs ms")
            Thread.sleep(5)
        }
    }

    @Test
    fun `aplica o estado de um pacote autenticado`() {
        send(PacketType.STATE, 1) { steering = 0.5f; throttle = 1f; buttons = 2 }
        eventually { wheel.current().throttle > 0.99f }
        val applied = wheel.current()
        assertEquals(0.5f, applied.steering, 0.001f)
        assertEquals(2, applied.buttons)
        assertEquals(true, service.snapshot().connected)
    }

    @Test
    fun `ignora pacote assinado com outra chave`() {
        send(PacketType.STATE, 1, signingKey = PinPairing.keyFromPin("654321")) { throttle = 1f }
        eventually { service.snapshot().stats.rejected >= 1 }
        assertEquals(0, wheel.applyCount)
    }

    @Test
    fun `solta os pedais quando os pacotes param`() {
        send(PacketType.STATE, 1) { steering = 0.4f; throttle = 1f }
        eventually { wheel.current().throttle > 0.99f }
        eventually { wheel.current().throttle == 0f }
        assertEquals(0.4f, wheel.current().steering, 0.001f)
    }

    @Test
    fun `bye volta ao neutro e desconecta`() {
        send(PacketType.STATE, 1) { steering = -1f }
        eventually { wheel.current().steering < -0.99f }
        send(PacketType.BYE, 2)
        eventually { !service.snapshot().connected && wheel.current().steering == 0f }
        assertFalse(service.snapshot().connected)
    }

    @Test
    fun `pacote com sequencia antiga e descartado`() {
        send(PacketType.STATE, 5) { throttle = 1f }
        eventually { wheel.current().throttle > 0.99f }
        send(PacketType.STATE, 3) { throttle = 0.2f }
        eventually { service.snapshot().stats.rejected >= 1 }
    }
}
