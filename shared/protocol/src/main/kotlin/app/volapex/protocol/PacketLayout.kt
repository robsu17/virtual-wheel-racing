package app.volapex.protocol

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

    /** Pacotes de controle (HELLO, WELCOME, HEARTBEAT) usam os bytes 16 a 23 como um número de 64 bits. */
    const val OFFSET_PAYLOAD = 16

    const val MAC_LENGTH = 8
    const val AUTHENTICATED_LENGTH = OFFSET_MAC
}

object PacketType {
    /** Celular → PC: estado do volante, assinado com a chave da sessão. */
    const val STATE = 1

    /** Celular → PC: fim da sessão, assinado com a chave da sessão. */
    const val BYE = 2

    /** Celular → PC: pede uma sessão; leva o nonce do celular e é assinado com a chave de pareamento. */
    const val HELLO = 3

    /** PC → celular: aceita a sessão; leva o nonce do PC e é assinado com a chave de pareamento. */
    const val WELCOME = 4

    /** PC → celular: "ainda estou aqui", duas vezes por segundo, assinado com a chave da sessão. */
    const val HEARTBEAT = 5
}

object ProtocolDefaults {
    const val PORT = 47820
}
