package app.volapex.protocol

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Chave de cada sessão, derivada da chave de pareamento e de um nonce de cada lado.
 * Como o PC sorteia um nonce novo a cada HELLO, pacotes gravados de uma sessão antiga
 * não valem em nenhuma sessão nova.
 */
object SessionKeys {
    private val LABEL = "volapex/session/v1".toByteArray(Charsets.UTF_8)

    fun derive(pairingKey: ByteArray, sessionId: Int, clientNonce: Long, serverNonce: Long): ByteArray {
        val mac = Mac.getInstance(HMAC_SHA256)
        mac.init(SecretKeySpec(pairingKey, HMAC_SHA256))
        mac.update(LABEL)
        mac.update(ByteBuffer.allocate(20).putInt(sessionId).putLong(clientNonce).putLong(serverNonce).array())
        return mac.doFinal()
    }
}

/** Lado do celular: uma tentativa de abrir sessão. Cada tentativa usa sessionId e nonce novos. */
class ClientHandshake(private val pairingKey: ByteArray, random: SecureRandom) {
    val sessionId: Int = random.nextInt()
    private val clientNonce: Long = random.nextLong()
    private val signer = PacketAuthenticator(pairingKey)
    private val verifier = PacketAuthenticator(pairingKey)

    /** Escreve o HELLO assinado. Usado pela thread que envia. */
    fun writeHello(buffer: ByteArray, sequence: Int, timeMicros: Int) {
        PacketCodec.writeControl(buffer, PacketType.HELLO, sessionId, sequence, timeMicros, clientNonce)
        signer.sign(buffer)
    }

    /** Devolve a chave da sessão se o pacote for o WELCOME desta tentativa; senão, null. Usado pela thread que recebe. */
    fun acceptWelcome(buffer: ByteArray, length: Int): ByteArray? {
        if (!PacketCodec.isWellFormed(buffer, length) || PacketCodec.type(buffer) != PacketType.WELCOME) return null
        if (PacketCodec.sessionId(buffer) != sessionId || !verifier.verify(buffer)) return null
        return SessionKeys.derive(pairingKey, sessionId, clientNonce, PacketCodec.payload(buffer))
    }
}

class AcceptedSession(val sessionId: Int, val sessionKey: ByteArray)

/** Lado do PC: responde a HELLOs válidos e recusa HELLOs repetidos. Não é thread-safe. */
class ServerHandshake(private val pairingKey: ByteArray, private val random: SecureRandom = SecureRandom()) {
    private val authenticator = PacketAuthenticator(pairingKey)
    private val seenNonces = object : LinkedHashMap<Long, Unit>(MAX_REMEMBERED_NONCES, 0.75f, false) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Unit>?): Boolean =
            size > MAX_REMEMBERED_NONCES
    }

    /** Se [buffer] for um HELLO novo e válido, escreve o WELCOME em [reply] e devolve a sessão aceita. */
    fun handleHello(buffer: ByteArray, length: Int, reply: ByteArray, timeMicros: Int): AcceptedSession? {
        if (!PacketCodec.isWellFormed(buffer, length) || PacketCodec.type(buffer) != PacketType.HELLO) return null
        if (!authenticator.verify(buffer)) return null
        val clientNonce = PacketCodec.payload(buffer)
        if (seenNonces.containsKey(clientNonce)) return null
        seenNonces[clientNonce] = Unit

        val sessionId = PacketCodec.sessionId(buffer)
        val serverNonce = random.nextLong()
        PacketCodec.writeControl(reply, PacketType.WELCOME, sessionId, 0, timeMicros, serverNonce)
        authenticator.sign(reply)
        return AcceptedSession(sessionId, SessionKeys.derive(pairingKey, sessionId, clientNonce, serverNonce))
    }

    private companion object {
        const val MAX_REMEMBERED_NONCES = 256
    }
}
