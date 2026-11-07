package app.volapex.protocol

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal const val HMAC_SHA256 = "HmacSHA256"

/**
 * Assina e confere os pacotes com HMAC-SHA256 truncado em 8 bytes.
 * Não é thread-safe: cada thread do laço usa a sua instância. Não cria objetos por pacote.
 */
class PacketAuthenticator(key: ByteArray) {
    init {
        require(key.size >= 16) { "Chave curta demais" }
    }

    private val mac: Mac = Mac.getInstance(HMAC_SHA256).apply { init(SecretKeySpec(key, HMAC_SHA256)) }
    private val digest = ByteArray(mac.macLength)

    fun sign(buffer: ByteArray) {
        compute(buffer)
        System.arraycopy(digest, 0, buffer, PacketLayout.OFFSET_MAC, PacketLayout.MAC_LENGTH)
    }

    /** Comparação em tempo constante. */
    fun verify(buffer: ByteArray): Boolean {
        compute(buffer)
        var difference = 0
        for (i in 0 until PacketLayout.MAC_LENGTH) {
            difference = difference or (digest[i].toInt() xor buffer[PacketLayout.OFFSET_MAC + i].toInt())
        }
        return difference == 0
    }

    private fun compute(buffer: ByteArray) {
        mac.update(buffer, 0, PacketLayout.AUTHENTICATED_LENGTH)
        mac.doFinal(digest, 0)
    }
}

/**
 * Descarta pacotes velhos ou repetidos. Uma sessão nova (outro sessionId) recomeça a contagem.
 * Só deve ser chamado depois de o pacote passar pela autenticação.
 */
class SequenceGuard {
    private var hasSession = false
    private var session = 0
    private var last = 0

    /** Quantos pacotes faltaram antes do último aceito. */
    var lastGap: Int = 0
        private set

    fun accept(sessionId: Int, sequence: Int): Boolean {
        if (!hasSession || sessionId != session) {
            hasSession = true
            session = sessionId
            last = sequence
            lastGap = 0
            return true
        }
        val delta = sequence - last // aritmética com sinal trata a volta do contador
        if (delta <= 0) return false
        lastGap = delta - 1
        last = sequence
        return true
    }

    fun reset() {
        hasSession = false
        lastGap = 0
    }
}
