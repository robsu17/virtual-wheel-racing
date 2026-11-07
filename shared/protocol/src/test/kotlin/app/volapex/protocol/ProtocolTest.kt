package app.volapex.protocol

import app.volapex.domain.WheelState
import java.security.SecureRandom
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun key(seed: Int) = ByteArray(16) { (it * 31 + seed).toByte() }

class PacketCodecTest {
    @Test
    fun `layout de ouro bate byte a byte`() {
        val state = WheelState().apply { steering = 1f; throttle = 1f; brake = 0f; buttons = 0x0005 }
        val buffer = ByteArray(PacketLayout.SIZE)
        PacketCodec.write(buffer, PacketType.STATE, 0x01020304, 0x0A0B0C0D, 0x11223344, state)
        val expected = byteArrayOf(
            0x50, 0x01, 0x01, 0x00,
            0x01, 0x02, 0x03, 0x04,
            0x0A, 0x0B, 0x0C, 0x0D,
            0x11, 0x22, 0x33, 0x44,
            0x7F, 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
            0x00, 0x00, 0x00, 0x05,
            0, 0, 0, 0, 0, 0, 0, 0,
        )
        assertContentEquals(expected, buffer)
    }

    @Test
    fun `ida e volta preserva o estado`() {
        val state = WheelState().apply { steering = -0.37f; throttle = 0.62f; brake = 0.05f; buttons = 0x8001 }
        val buffer = ByteArray(PacketLayout.SIZE)
        PacketCodec.write(buffer, PacketType.STATE, -5, 42, 7, state)
        assertTrue(PacketCodec.isWellFormed(buffer, PacketLayout.SIZE))
        assertEquals(-5, PacketCodec.sessionId(buffer))
        assertEquals(42, PacketCodec.sequence(buffer))
        assertEquals(PacketType.STATE, PacketCodec.type(buffer))

        val read = WheelState()
        PacketCodec.readState(buffer, read)
        assertEquals(-0.37f, read.steering, 1f / 32767f)
        assertEquals(0.62f, read.throttle, 1f / 65535f)
        assertEquals(0.05f, read.brake, 1f / 65535f)
        assertEquals(0x8001, read.buttons)
    }

    @Test
    fun `pacote de controle leva 64 bits de payload`() {
        val buffer = ByteArray(PacketLayout.SIZE)
        PacketCodec.writeControl(buffer, PacketType.HELLO, 9, 1, 0, -0x123456789ABCDEFL)
        assertEquals(-0x123456789ABCDEFL, PacketCodec.payload(buffer))
        assertEquals(PacketType.HELLO, PacketCodec.type(buffer))
    }

    @Test
    fun `tamanho ou versao errados sao rejeitados`() {
        val buffer = ByteArray(PacketLayout.SIZE)
        PacketCodec.write(buffer, PacketType.STATE, 1, 1, 0, WheelState())
        assertFalse(PacketCodec.isWellFormed(buffer, 31))
        buffer[PacketLayout.OFFSET_VERSION] = 9
        assertFalse(PacketCodec.isWellFormed(buffer, PacketLayout.SIZE))
    }
}

class PacketAuthenticatorTest {
    private val key = key(1)

    private fun signedPacket(): ByteArray {
        val buffer = ByteArray(PacketLayout.SIZE)
        PacketCodec.write(buffer, PacketType.STATE, 1, 1, 0, WheelState().apply { throttle = 1f })
        PacketAuthenticator(key).sign(buffer)
        return buffer
    }

    @Test
    fun `pacote assinado e aceito`() {
        assertTrue(PacketAuthenticator(key).verify(signedPacket()))
    }

    @Test
    fun `qualquer byte alterado invalida o pacote`() {
        val buffer = signedPacket()
        buffer[PacketLayout.OFFSET_THROTTLE] = 0
        assertFalse(PacketAuthenticator(key).verify(buffer))
    }

    @Test
    fun `outra chave nao valida`() {
        assertFalse(PacketAuthenticator(key(2)).verify(signedPacket()))
    }
}

class SequenceGuardTest {
    @Test
    fun `aceita crescente e conta perdidos`() {
        val guard = SequenceGuard()
        assertTrue(guard.accept(1, 10))
        assertTrue(guard.accept(1, 13))
        assertEquals(2, guard.lastGap)
    }

    @Test
    fun `rejeita repetido e antigo`() {
        val guard = SequenceGuard()
        guard.accept(1, 10)
        assertFalse(guard.accept(1, 10))
        assertFalse(guard.accept(1, 9))
    }

    @Test
    fun `sessao nova recomeca e volta do contador funciona`() {
        val guard = SequenceGuard()
        guard.accept(1, 100)
        assertTrue(guard.accept(2, 1))
        guard.accept(3, Int.MAX_VALUE)
        assertTrue(guard.accept(3, Int.MIN_VALUE))
    }
}

class HandshakeTest {
    private val pairingKey = key(7)
    private val random = SecureRandom()

    private fun hello(client: ClientHandshake): ByteArray =
        ByteArray(PacketLayout.SIZE).also { client.writeHello(it, 1, 0) }

    @Test
    fun `celular e PC chegam a mesma chave de sessao`() {
        val client = ClientHandshake(pairingKey, random)
        val server = ServerHandshake(pairingKey, random)
        val reply = ByteArray(PacketLayout.SIZE)
        val accepted = assertNotNull(server.handleHello(hello(client), PacketLayout.SIZE, reply, 0))
        val clientKey = assertNotNull(client.acceptWelcome(reply, PacketLayout.SIZE))
        assertEquals(client.sessionId, accepted.sessionId)
        assertContentEquals(accepted.sessionKey, clientKey)
    }

    @Test
    fun `HELLO repetido e recusado`() {
        val client = ClientHandshake(pairingKey, random)
        val server = ServerHandshake(pairingKey, random)
        val packet = hello(client)
        assertNotNull(server.handleHello(packet, PacketLayout.SIZE, ByteArray(PacketLayout.SIZE), 0))
        assertNull(server.handleHello(packet, PacketLayout.SIZE, ByteArray(PacketLayout.SIZE), 0))
    }

    @Test
    fun `HELLO com outra chave e recusado`() {
        val client = ClientHandshake(key(8), random)
        val server = ServerHandshake(pairingKey, random)
        assertNull(server.handleHello(hello(client), PacketLayout.SIZE, ByteArray(PacketLayout.SIZE), 0))
    }

    @Test
    fun `WELCOME de outra tentativa e ignorado`() {
        val first = ClientHandshake(pairingKey, random)
        val second = ClientHandshake(pairingKey, random)
        val reply = ByteArray(PacketLayout.SIZE)
        ServerHandshake(pairingKey, random).handleHello(hello(first), PacketLayout.SIZE, reply, 0)
        assertNull(second.acceptWelcome(reply, PacketLayout.SIZE))
    }

    @Test
    fun `cada sessao tem chave diferente`() {
        val server = ServerHandshake(pairingKey, random)
        val a = server.handleHello(hello(ClientHandshake(pairingKey, random)), PacketLayout.SIZE, ByteArray(PacketLayout.SIZE), 0)
        val b = server.handleHello(hello(ClientHandshake(pairingKey, random)), PacketLayout.SIZE, ByteArray(PacketLayout.SIZE), 0)
        assertFalse(assertNotNull(a).sessionKey.contentEquals(assertNotNull(b).sessionKey))
    }
}

class PairingPayloadTest {
    @Test
    fun `codigo QR vai e volta`() {
        val original = PairingPayload("PC da Sala", listOf("192.168.0.9", "10.0.0.5"), 47820, key(3))
        val parsed = assertNotNull(PairingPayload.parse(original.encode()))
        assertEquals("PC da Sala", parsed.pcName)
        assertEquals(listOf("192.168.0.9", "10.0.0.5"), parsed.hosts)
        assertEquals(47820, parsed.port)
        assertContentEquals(original.pairingKey, parsed.pairingKey)
    }

    @Test
    fun `codigos que nao sao do Volapex sao recusados`() {
        assertNull(PairingPayload.parse("https://exemplo.com"))
        assertNull(PairingPayload.parse("volapex:1?n=PC&h=1.2.3.4&p=abc&k=AAAA"))
        assertNull(PairingPayload.parse("volapex:1?n=PC&h=1.2.3.4&p=47820&k=curta"))
        assertNull(PairingPayload.parse("volapex:1?n=PC&p=47820"))
    }

    @Test
    fun `chave gerada tem 128 bits`() {
        assertEquals(16, PairingPayload.generateKey().size)
    }
}
