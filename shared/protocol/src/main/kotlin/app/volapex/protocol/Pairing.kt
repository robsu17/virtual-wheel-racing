package app.volapex.protocol

import java.net.URLDecoder
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.Base64

/**
 * Conteúdo do código QR de pareamento: nome do PC, endereços, porta e uma chave aleatória
 * de 128 bits. Formato: `volapex:1?n=<nome>&h=<ip1,ip2>&p=<porta>&k=<chave em base64url>`.
 */
class PairingPayload(
    val pcName: String,
    val hosts: List<String>,
    val port: Int,
    val pairingKey: ByteArray,
) {
    init {
        require(pcName.isNotBlank() && pcName.length <= MAX_NAME_LENGTH) { "Nome do PC inválido" }
        require(hosts.isNotEmpty() && hosts.size <= MAX_HOSTS) { "Lista de endereços inválida" }
        require(hosts.all { HOST_PATTERN.matches(it) }) { "Endereço inválido" }
        require(port in 1..65535) { "Porta inválida" }
        require(pairingKey.size == KEY_LENGTH) { "Chave de pareamento inválida" }
    }

    fun encode(): String = buildString {
        append(PREFIX)
        append("?n=").append(URLEncoder.encode(pcName, "UTF-8"))
        append("&h=").append(hosts.joinToString(","))
        append("&p=").append(port)
        append("&k=").append(Base64.getUrlEncoder().withoutPadding().encodeToString(pairingKey))
    }

    companion object {
        const val KEY_LENGTH = 16
        const val MAX_HOSTS = 8
        private const val MAX_NAME_LENGTH = 64
        private const val PREFIX = "volapex:1"
        private val HOST_PATTERN = Regex("^[0-9A-Za-z.:-]{1,64}$")

        fun generateKey(random: SecureRandom = SecureRandom()): ByteArray = ByteArray(KEY_LENGTH).also(random::nextBytes)

        /** Lê o texto do QR. Devolve null se não for um código do Volapex válido. */
        fun parse(text: String): PairingPayload? {
            if (!text.startsWith("$PREFIX?")) return null
            val params = text.substring(PREFIX.length + 1).split("&").mapNotNull { part ->
                val index = part.indexOf('=')
                if (index <= 0) null else part.substring(0, index) to part.substring(index + 1)
            }.toMap()
            return try {
                PairingPayload(
                    pcName = URLDecoder.decode(params["n"] ?: return null, "UTF-8"),
                    hosts = (params["h"] ?: return null).split(",").filter { it.isNotEmpty() },
                    port = (params["p"] ?: return null).toInt(),
                    pairingKey = Base64.getUrlDecoder().decode(params["k"] ?: return null),
                )
            } catch (e: IllegalArgumentException) {
                null // inclui NumberFormatException e base64 inválido
            }
        }
    }
}
