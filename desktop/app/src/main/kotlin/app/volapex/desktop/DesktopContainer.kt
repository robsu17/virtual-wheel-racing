package app.volapex.desktop

import app.volapex.desktop.receiver.ReceiverService
import app.volapex.desktop.vjoy.VJoyAdapter
import app.volapex.domain.DriverStatus
import app.volapex.domain.OpenResult
import app.volapex.protocol.PairingPayload
import app.volapex.protocol.ProtocolDefaults
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.SecureRandom
import java.util.Base64
import java.util.Properties

/** Raiz de composição do programa do Windows: o único lugar que liga portas a adaptadores. */
class DesktopContainer : AutoCloseable {
    private val random = SecureRandom()
    private val pairingStore = PairingStore(defaultStorePath())
    private val vjoy = VJoyAdapter()
    val openResult: OpenResult = vjoy.open()
    val driverStatus: DriverStatus = vjoy.check()
    val receiver = ReceiverService(vjoy, random = random)
    val port: Int = ProtocolDefaults.PORT
    val addresses: List<String> = localIpv4Addresses()
    val pcName: String = localPcName()

    private var pairingKey: ByteArray = pairingStore.load() ?: newKeySaved()

    var startError: String? = null
        private set

    init {
        receiver.setPairingKey(pairingKey)
        try {
            receiver.start()
        } catch (e: IOException) {
            startError = "Não foi possível abrir a porta $port: ${e.message}"
        }
    }

    /** Conteúdo do QR, ou null se o PC não estiver em nenhuma rede local. */
    fun pairingPayload(): PairingPayload? =
        if (addresses.isEmpty()) null
        else PairingPayload(pcName, addresses.take(PairingPayload.MAX_HOSTS), port, pairingKey)

    /** Gera outro código: celulares pareados antes precisam ler o QR de novo. */
    fun newPairingCode() {
        pairingKey = newKeySaved()
        receiver.setPairingKey(pairingKey)
    }

    override fun close() {
        receiver.close()
        vjoy.close()
    }

    private fun newKeySaved(): ByteArray =
        PairingPayload.generateKey(random).also { pairingStore.save(it) }

    private fun localPcName(): String {
        val name = System.getenv("COMPUTERNAME")
            ?: runCatching { InetAddress.getLocalHost().hostName }.getOrNull()
            ?: "PC"
        return name.take(64).ifBlank { "PC" }
    }

    private fun localIpv4Addresses(): List<String> = try {
        NetworkInterface.getNetworkInterfaces().toList()
            .filter { it.isUp && !it.isLoopback && !it.isVirtual }
            .flatMap { it.inetAddresses.toList() }
            .filterIsInstance<Inet4Address>()
            .filter { it.isSiteLocalAddress }
            .map { it.hostAddress }
            .distinct()
    } catch (e: Exception) {
        emptyList()
    }

    private fun defaultStorePath(): Path =
        Paths.get(System.getenv("APPDATA") ?: System.getProperty("user.home"), "Volapex", "pareamento.properties")
}

/**
 * Guarda a chave de pareamento entre aberturas do programa, para o celular reconectar sem ler o QR de novo.
 * Provisório: no MVP a chave vai para o armazenamento protegido do Windows (DPAPI).
 */
class PairingStore(private val path: Path) {
    fun load(): ByteArray? = try {
        if (!Files.exists(path)) {
            null
        } else {
            val properties = Properties().apply { Files.newInputStream(path).use { load(it) } }
            properties.getProperty(KEY)?.let { Base64.getDecoder().decode(it) }
                ?.takeIf { it.size == PairingPayload.KEY_LENGTH }
        }
    } catch (e: Exception) {
        null
    }

    fun save(key: ByteArray) {
        try {
            Files.createDirectories(path.parent)
            val properties = Properties().apply { setProperty(KEY, Base64.getEncoder().encodeToString(key)) }
            Files.newOutputStream(path).use { properties.store(it, "Volapex: chave de pareamento") }
        } catch (e: IOException) {
            // Sem salvar, o celular só precisa ler o QR de novo na próxima abertura.
        }
    }

    private companion object {
        const val KEY = "chave"
    }
}
