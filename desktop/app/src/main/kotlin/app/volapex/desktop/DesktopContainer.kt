package app.volapex.desktop

import app.volapex.desktop.receiver.ReceiverService
import app.volapex.desktop.vjoy.VJoyAdapter
import app.volapex.domain.DriverStatus
import app.volapex.domain.OpenResult
import app.volapex.protocol.PinPairing
import app.volapex.protocol.ProtocolDefaults
import java.io.IOException
import java.net.Inet4Address
import java.net.NetworkInterface

/** Raiz de composição do programa do Windows: o único lugar que liga portas a adaptadores. */
class DesktopContainer : AutoCloseable {
    private val vjoy = VJoyAdapter()
    val openResult: OpenResult = vjoy.open()
    val driverStatus: DriverStatus = vjoy.check()
    val receiver = ReceiverService(vjoy)
    val port: Int = ProtocolDefaults.PORT
    val addresses: List<String> = localIpv4Addresses()

    var pin: String = PinPairing.generatePin()
        private set

    var startError: String? = null
        private set

    init {
        receiver.setPairingKey(PinPairing.keyFromPin(pin))
        try {
            receiver.start()
        } catch (e: IOException) {
            startError = "Não foi possível abrir a porta $port: ${e.message}"
        }
    }

    fun newPin(): String {
        pin = PinPairing.generatePin()
        receiver.setPairingKey(PinPairing.keyFromPin(pin))
        return pin
    }

    override fun close() {
        receiver.close()
        vjoy.close()
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
}
