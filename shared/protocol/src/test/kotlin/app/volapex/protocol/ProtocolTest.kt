package app.volapex.protocol

import app.volapex.domain.WheelState
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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
    fun `tamanho ou versao errados sao rejeitados`() {
        val buffer = ByteArray(PacketLayout.SIZE)
        PacketCodec.write(buffer, PacketType.STATE, 1, 1, 0, WheelState())
        assertFalse(PacketCodec.isWellFormed(buffer, 31))
        buffer[PacketLayout.OFFSET_VERSION] = 9
        assertFalse(PacketCodec.isWellFormed(buffer, PacketLayout.SIZE))
    }
}

class PacketAuthenticatorTest {
    private val key = PinPairing.keyFromPin("123456")

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
        assertFalse(PacketAuthenticator(PinPairing.keyFromPin("654321")).verify(signedPacket()))
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

class PinPairingTest {
    @Test
    fun `pin tem seis digitos`() {
        repeat(20) { assertTrue(PinPairing.isValidPin(PinPairing.generatePin())) }
    }

    @Test
    fun `mesmo pin gera a mesma chave e pins diferentes geram chaves diferentes`() {
        assertContentEquals(PinPairing.keyFromPin("000123"), PinPairing.keyFromPin("000123"))
        assertNotEquals(PinPairing.keyFromPin("000123").toList(), PinPairing.keyFromPin("000124").toList())
    }

    @Test
    fun `pin invalido e recusado`() {
        assertFailsWith<IllegalArgumentException> { PinPairing.keyFromPin("12a456") }
    }
}
