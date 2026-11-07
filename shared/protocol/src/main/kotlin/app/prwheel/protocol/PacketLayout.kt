package app.prwheel.protocol

/** Formato do pacote de 32 bytes, em big-endian. Os primeiros 24 bytes são autenticados. */
object PacketLayout {
    const val SIZE = 32
    const val MAGIC: Byte = 0x50 // 'P'
    const val VERSION: Byte = 1

    const val OFFSET_MAGIC = 0
    const val OFFSET_VERSION = 1
    const val OFFSET_TYPE = 2
    const val OFFSET_FLAGS = 3
    const val OFFSET_SESSION = 4
    const val OFFSET_SEQUENCE = 8
    const val OFFSET_TIME_MICROS = 12
    const val OFFSET_STEERING = 16
    const val OFFSET_THROTTLE = 18
    const val OFFSET_BRAKE = 20
    const val OFFSET_BUTTONS = 22
    const val OFFSET_MAC = 24

    const val MAC_LENGTH = 8
    const val AUTHENTICATED_LENGTH = OFFSET_MAC
}

object PacketType {
    const val STATE = 1
    const val BYE = 2
}

object ProtocolDefaults {
    const val PORT = 47820
}
