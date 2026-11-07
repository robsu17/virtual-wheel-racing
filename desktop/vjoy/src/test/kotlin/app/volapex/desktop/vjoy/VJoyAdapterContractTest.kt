package app.volapex.desktop.vjoy

import app.volapex.domain.VirtualWheel
import app.volapex.domain.VirtualWheelContract
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach

/**
 * Roda o contrato contra o vJoy real. Só executa quando pedido, porque mexe no volante virtual:
 * gradlew :desktop:vjoy:test -Pvolapex.vjoy=true
 */
class VJoyAdapterContractTest : VirtualWheelContract() {
    @BeforeEach
    fun onlyWhenRequested() {
        assumeTrue(System.getProperty("volapex.vjoy") == "true", "Teste com vJoy real desligado")
    }

    override fun createWheel(): VirtualWheel = VJoyAdapter()
}
