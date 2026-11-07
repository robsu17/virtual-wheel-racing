package app.volapex.protocol

import app.volapex.domain.WheelState
import app.volapex.protocol.PacketLayout.OFFSET_BRAKE
import app.volapex.protocol.PacketLayout.OFFSET_BUTTONS
import app.volapex.protocol.PacketLayout.OFFSET_FLAGS
import app.volapex.protocol.PacketLayout.OFFSET_MAC
import app.volapex.protocol.PacketLayout.OFFSET_MAGIC
import app.volapex.protocol.PacketLayout.OFFSET_SEQUENCE
import app.volapex.protocol.PacketLayout.OFFSET_SESSION
import app.volapex.protocol.PacketLayout.OFFSET_STEERING
import app.volapex.protocol.PacketLayout.OFFSET_THROTTLE
import app.volapex.protocol.PacketLayout.OFFSET_TIME_MICROS
import app.volapex.protocol.PacketLayout.OFFSET_TYPE
import app.volapex.protocol.PacketLayout.OFFSET_VERSION
import app.volapex.protocol.PacketLayout.SIZE
import kotlin.math.roundToInt

/** Escreve e lê o pacote. Só formato: autenticação e transporte ficam em outras classes. */
object PacketCodec {
    private const val STEERING_SCALE = 32767f
    private const val PEDAL_SCALE = 65535f

    fun write(buffer: ByteArray, type: Int, sessionId: Int, sequence: Int, timeMicros: Int, state: WheelState) {
        writeHeader(buffer, type, sessionId, sequence, timeMicros)
        putShort(buffer, OFFSET_STEERING, (state.steering * STEERING_SCALE).roundToInt())
        putShort(buffer, OFFSET_THROTTLE, (state.throttle * PEDAL_SCALE).roundToInt())
        putShort(buffer, OFFSET_BRAKE, (state.brake * PEDAL_SCALE).roundToInt())
        putShort(buffer, OFFSET_BUTTONS, state.buttons)
    }

    /** Pacote de controle: em vez do estado, leva um número de 64 bits (nonce ou zero). */
    fun writeControl(buffer: ByteArray, type: Int, sessionId: Int, sequence: Int, timeMicros: Int, payload: Long) {
        writeHeader(buffer, type, sessionId, sequence, timeMicros)
        putInt(buffer, PacketLayout.OFFSET_PAYLOAD, (payload ushr 32).toInt())
        putInt(buffer, PacketLayout.OFFSET_PAYLOAD + 4, payload.toInt())
    }

    fun payload(buffer: ByteArray): Long =
        (getInt(buffer, PacketLayout.OFFSET_PAYLOAD).toLong() shl 32) or
            (getInt(buffer, PacketLayout.OFFSET_PAYLOAD + 4).toLong() and 0xFFFFFFFFL)

    private fun writeHeader(buffer: ByteArray, type: Int, sessionId: Int, sequence: Int, timeMicros: Int) {
        require(buffer.size >= SIZE) { "Buffer menor que $SIZE bytes" }
        buffer[OFFSET_MAGIC] = PacketLayout.MAGIC
        buffer[OFFSET_VERSION] = PacketLayout.VERSION
        buffer[OFFSET_TYPE] = type.toByte()
        buffer[OFFSET_FLAGS] = 0
        putInt(buffer, OFFSET_SESSION, sessionId)
        putInt(buffer, OFFSET_SEQUENCE, sequence)
        putInt(buffer, OFFSET_TIME_MICROS, timeMicros)
        buffer.fill(0.toByte(), OFFSET_MAC, SIZE)
    }

    fun isWellFormed(buffer: ByteArray, length: Int): Boolean =
        length == SIZE &&
            buffer[OFFSET_MAGIC] == PacketLayout.MAGIC &&
            buffer[OFFSET_VERSION] == PacketLayout.VERSION

    fun type(buffer: ByteArray): Int = buffer[OFFSET_TYPE].toInt() and 0xFF

    fun sessionId(buffer: ByteArray): Int = getInt(buffer, OFFSET_SESSION)

    fun sequence(buffer: ByteArray): Int = getInt(buffer, OFFSET_SEQUENCE)

    fun timeMicros(buffer: ByteArray): Int = getInt(buffer, OFFSET_TIME_MICROS)

    fun readState(buffer: ByteArray, into: WheelState) {
        into.steering = getUnsignedShort(buffer, OFFSET_STEERING).toShort() / STEERING_SCALE
        into.throttle = getUnsignedShort(buffer, OFFSET_THROTTLE) / PEDAL_SCALE
        into.brake = getUnsignedShort(buffer, OFFSET_BRAKE) / PEDAL_SCALE
        into.buttons = getUnsignedShort(buffer, OFFSET_BUTTONS)
    }

    private fun putShort(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value ushr 8).toByte()
        buffer[offset + 1] = value.toByte()
    }

    private fun putInt(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value ushr 24).toByte()
        buffer[offset + 1] = (value ushr 16).toByte()
        buffer[offset + 2] = (value ushr 8).toByte()
        buffer[offset + 3] = value.toByte()
    }

    private fun getUnsignedShort(buffer: ByteArray, offset: Int): Int =
        ((buffer[offset].toInt() and 0xFF) shl 8) or (buffer[offset + 1].toInt() and 0xFF)

    private fun getInt(buffer: ByteArray, offset: Int): Int =
        ((buffer[offset].toInt() and 0xFF) shl 24) or
            ((buffer[offset + 1].toInt() and 0xFF) shl 16) or
            ((buffer[offset + 2].toInt() and 0xFF) shl 8) or
            (buffer[offset + 3].toInt() and 0xFF)
}
